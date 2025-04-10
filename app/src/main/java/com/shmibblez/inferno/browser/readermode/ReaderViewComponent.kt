package com.shmibblez.inferno.browser.readermode

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.compose.base.InfernoText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.action.ReaderAction
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.concept.engine.webextension.Port
import mozilla.components.feature.readerview.ReaderViewFeature.ColorScheme
import mozilla.components.feature.readerview.ReaderViewFeature.FontType
import mozilla.components.feature.readerview.UUIDCreator
import mozilla.components.feature.readerview.onReaderViewStatusChange
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.kotlinx.coroutines.flow.filterChanged
import mozilla.components.support.webextensions.WebExtensionController
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.util.Locale
import java.util.UUID

private val logger = Logger("ReaderView")
private val ICON_SIZE = 20.dp

const val MAX_TEXT_SIZE = 9
const val MIN_TEXT_SIZE = 1

internal const val READER_VIEW_EXTENSION_ID = "readerview@mozac.org"

// Name of the port connected to all pages for checking whether or not
// a page is readerable (see readerview_content.js).
internal const val READER_VIEW_CONTENT_PORT = "mozacReaderview"

// Name of the port connected to active reader pages for updating
// appearance configuration (see readerview.js).
internal const val READER_VIEW_ACTIVE_CONTENT_PORT = "mozacReaderviewActive"
internal const val READER_VIEW_EXTENSION_URL = "resource://android/assets/extensions/readerview/"

// Constants for building messages sent to the web extension:
// Change the font type: {"action": "setFontType", "value": "sans-serif"}
// Show reader view: {"action": "show", "value": {"fontSize": 3, "fontType": "serif", "colorScheme": "dark"}}
internal const val ACTION_MESSAGE_KEY = "action"
internal const val ACTION_CACHE_PAGE = "cachePage"
internal const val ACTION_SHOW = "show"
internal const val ACTION_HIDE = "hide"
internal const val ACTION_CHECK_READER_STATE = "checkReaderState"
internal const val ACTION_SET_COLOR_SCHEME = "setColorScheme"
internal const val ACTION_CHANGE_FONT_SIZE = "changeFontSize"
internal const val ACTION_SET_FONT_TYPE = "setFontType"
internal const val ACTION_VALUE = "value"
internal const val ACTION_VALUE_SHOW_FONT_SIZE = "fontSize"
internal const val ACTION_VALUE_SHOW_FONT_TYPE = "fontType"
internal const val ACTION_VALUE_SHOW_COLOR_SCHEME = "colorScheme"
internal const val ACTION_VALUE_SCROLLY = "scrollY"
internal const val ACTION_VALUE_ID = "id"
internal const val READERABLE_RESPONSE_MESSAGE_KEY = "readerable"
internal const val BASE_URL_RESPONSE_MESSAGE_KEY = "baseUrl"
internal const val ACTIVE_URL_RESPONSE_MESSAGE_KEY = "activeUrl"

// Constants for storing the reader mode config in shared preferences
internal const val SHARED_PREF_NAME = "mozac_feature_reader_view"
internal const val COLOR_SCHEME_KEY = "mozac-readerview-colorscheme"
internal const val FONT_TYPE_KEY = "mozac-readerview-fonttype"
internal const val FONT_SIZE_KEY = "mozac-readerview-fontsize"
internal const val FONT_SIZE_DEFAULT = 3

internal fun createCheckReaderStateMessage(): JSONObject {
    return JSONObject().put(ACTION_MESSAGE_KEY, ACTION_CHECK_READER_STATE)
}

internal fun createCachePageMessage(id: String): JSONObject {
    return JSONObject().put(ACTION_MESSAGE_KEY, ACTION_CACHE_PAGE).put(ACTION_VALUE_ID, id)
}

internal fun createShowReaderMessage(
    config: InfernoReaderViewConfig?, scrollY: Int? = null
): JSONObject {
    if (config == null) {
        logger.warn("No config provided. Falling back to default values.")
    }

    val fontSize = config?.fontSize ?: FONT_SIZE_DEFAULT
    val fontType = config?.fontType ?: FontType.SERIF
    val colorScheme = config?.colorScheme ?: ColorScheme.LIGHT
    val configJson = JSONObject().put(ACTION_VALUE_SHOW_FONT_SIZE, fontSize)
        .put(ACTION_VALUE_SHOW_FONT_TYPE, fontType.value.lowercase(Locale.ROOT))
        .put(ACTION_VALUE_SHOW_COLOR_SCHEME, colorScheme.name.lowercase(Locale.ROOT))
    if (scrollY != null) {
        configJson.put(ACTION_VALUE_SCROLLY, scrollY)
    }
    return JSONObject().put(ACTION_MESSAGE_KEY, ACTION_SHOW).put(ACTION_VALUE, configJson)
}

internal fun createHideReaderMessage(): JSONObject {
    return JSONObject().put(ACTION_MESSAGE_KEY, ACTION_HIDE)
}

@Composable
fun ReaderViewComponent(
    tabSessionState: TabSessionState?,
    engine: Engine,
    store: BrowserStore,
    createUUID: UUIDCreator = { UUID.randomUUID().toString() },
    onReaderViewStatusChange: onReaderViewStatusChange = { _, _ -> Unit },
    onDismiss: () -> Unit,
) {
    var scope by remember { mutableStateOf<CoroutineScope?>(null) }
    val context = LocalContext.current
    val extensionController = remember {
        WebExtensionController(
            READER_VIEW_EXTENSION_ID,
            READER_VIEW_EXTENSION_URL,
            READER_VIEW_CONTENT_PORT,
        )
    }
    val config = remember {
        InfernoReaderViewConfig(context) { message ->
            val engineSession = store.state.selectedTab?.engineState?.engineSession
            extensionController.sendContentMessage(
                message, engineSession,
                READER_VIEW_ACTIVE_CONTENT_PORT,
            )
        }
    }
    var readerBaseUrl by remember { mutableStateOf<String?>(null) }

    fun WebExtensionController.createReaderUrl(
        url: String, id: String, config: InfernoReaderViewConfig, readerBaseUrl: String?
    ): String? {
        val colorScheme = config.colorScheme.name.lowercase(Locale.ROOT)
        // Encode the original page url, otherwise when the readerview page will try to
        // parse the url and retrieve the readerview url params (ir and colorScheme)
        // the parser may get confused because the original webpage url being interpolated
        // may also include its own search params non-escaped (See Bug 1860490).
        val encodedUrl = URLEncoder.encode(url, "UTF-8")
        return readerBaseUrl?.let { it + "readerview.html?url=$encodedUrl&id=$id&colorScheme=$colorScheme" }
    }

    /**
     * Shows the reader view UI.
     */
    fun showReaderView(session: TabSessionState? = store.state.selectedTab) {
        session?.let {
            if (!it.readerState.active) {
                val id = createUUID()
                extensionController.sendContentMessage(
                    createCachePageMessage(id),
                    it.engineState.engineSession,
                    READER_VIEW_CONTENT_PORT,
                )

                val readerUrl =
                    extensionController.createReaderUrl(it.content.url, id, config, readerBaseUrl)
                        ?: run {
                            Logger.error("FeatureReaderView unable to create ReaderUrl.")
                            return@let
                        }

                store.dispatch(EngineAction.LoadUrlAction(it.id, readerUrl))
                store.dispatch(ReaderAction.UpdateReaderActiveAction(it.id, true))
            }
        }
    }

    /**
     * Hides the reader view UI.
     */
    fun dismissReaderView(session: TabSessionState? = store.state.selectedTab) {
        session?.let {
            if (it.readerState.active) {
                store.dispatch(ReaderAction.UpdateReaderActiveAction(it.id, false))
                store.dispatch(ReaderAction.UpdateReaderableAction(it.id, false))
                store.dispatch(ReaderAction.ClearReaderActiveUrlAction(it.id))
                if (it.content.canGoBack) {
                    it.engineState.engineSession?.goBack(false)
                } else {
                    extensionController.sendContentMessage(
                        createHideReaderMessage(),
                        it.engineState.engineSession,
                        READER_VIEW_ACTIVE_CONTENT_PORT,
                    )
                }
            }
        }
        onDismiss.invoke()
    }

    LaunchedEffect(tabSessionState?.id, tabSessionState?.readerState?.active) {
        showReaderView()
    }

    @VisibleForTesting
    fun connectReaderViewContentScript(session: TabSessionState? = store.state.selectedTab) {
        session?.engineState?.engineSession?.let { engineSession ->
            extensionController.registerContentMessageHandler(
                engineSession,
                ActiveReaderViewContentMessageHandler(store, session.id, WeakReference(config)),
                READER_VIEW_ACTIVE_CONTENT_PORT,
            )
            extensionController.registerContentMessageHandler(
                engineSession,
                ReaderViewContentMessageHandler(store, session.id),
                READER_VIEW_CONTENT_PORT,
            )
            store.dispatch(ReaderAction.UpdateReaderConnectRequiredAction(session.id, false))
        }
    }

    @VisibleForTesting
    fun checkReaderState(session: TabSessionState? = store.state.selectedTab) {
        session?.engineState?.engineSession?.let { engineSession ->
            val message = createCheckReaderStateMessage()
            if (extensionController.portConnected(engineSession, READER_VIEW_CONTENT_PORT)) {
                extensionController.sendContentMessage(
                    message, engineSession, READER_VIEW_CONTENT_PORT
                )
            }
            if (extensionController.portConnected(
                    engineSession, READER_VIEW_ACTIVE_CONTENT_PORT
                )
            ) {
                extensionController.sendContentMessage(
                    message, engineSession, READER_VIEW_ACTIVE_CONTENT_PORT
                )
            }
            store.dispatch(ReaderAction.UpdateReaderableCheckRequiredAction(session.id, false))
        }
    }

    var lastNotified: Pair<Boolean, Boolean>? = null

    @VisibleForTesting
    fun maybeNotifyReaderStatusChange(readerable: Boolean = false, active: Boolean = false) {
        // Make sure we only notify the UI if needed (an actual change happened) to prevent
        // it from unnecessarily invalidating toolbar/menu items.
        if (lastNotified == null || lastNotified != Pair(readerable, active)) {
            onReaderViewStatusChange(readerable, active)
            lastNotified = Pair(readerable, active)
        }
    }

    fun ensureExtensionInstalled(
        extensionController: WebExtensionController,
        engine: Engine,
        updateReaderBaseUrl: (String?) -> Unit,
    ) {
        extensionController.install(
            engine,
            onSuccess = {
                it.getMetadata()?.run {
                    updateReaderBaseUrl(baseUrl)
                } ?: run {
                    Logger.error("ReaderView extension missing Metadata")
                }

                connectReaderViewContentScript()
            },
        )
    }



    DisposableEffect(tabSessionState?.id) {
        ensureExtensionInstalled(
            extensionController = extensionController,
            engine = engine,
            updateReaderBaseUrl = { readerBaseUrl = it },
        )

        scope = store.flowScoped { flow ->
            flow.mapNotNull { state -> state.tabs }.filterChanged {
                it.readerState
            }.collect { tab ->
                if (tab.readerState.connectRequired) {
                    connectReaderViewContentScript(tab)
                }
                if (tab.readerState.checkRequired) {
                    checkReaderState(tab)
                }
                if (tab.id == store.state.selectedTabId) {
                    maybeNotifyReaderStatusChange(
                        tab.readerState.readerable, tab.readerState.active
                    )
                }
            }
        }

        onDispose {
            scope?.cancel()
        }
    }

    if (tabSessionState?.readerState?.readerable == true)  {
        ReaderViewControls(
            config = config,
            onSetFont = {
                config.fontType = it
            },
            onSetColorScheme = {
                config.colorScheme = it
            },
            onIncreaseFontSize = {
                if (config.fontSize < MAX_TEXT_SIZE) {
                    config.fontSize -= 1
                }
            },
            onDecreaseFontSize = {
                if (config.fontSize > MIN_TEXT_SIZE) {
                    config.fontSize += 1
                }
            },
            increaseEnabled = config.fontSize < MAX_TEXT_SIZE,
            decreaseEnabled = config.fontSize > MIN_TEXT_SIZE,
            dismiss = ::dismissReaderView,
        )
    }
}

@Composable
private fun ReaderViewControls(
    config: InfernoReaderViewConfig,
    onSetFont: (FontType) -> Unit,
    onSetColorScheme: (ColorScheme) -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    increaseEnabled: Boolean,
    decreaseEnabled: Boolean,
    dismiss: () -> Unit,
) {
    var fontExpanded by remember { mutableStateOf(false) }
    var colorSchemeExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ComponentDimens.READER_VIEW_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        // exit reader view
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable(onClick = { dismiss.invoke() }),
            tint = Color.White,
            painter = painterResource(id = R.drawable.mozac_ic_cross_20),
            contentDescription = "exit"
        )
        // set font dropdown
        TextButton(onClick = { fontExpanded = true }) {
            DropdownMenu(
                expanded = fontExpanded,
                onDismissRequest = { fontExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_serif_font)) },
                    onClick = { onSetFont.invoke(FontType.SERIF) },
                )
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_sans_serif_font)) },
                    onClick = { onSetFont.invoke(FontType.SANSSERIF) },
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            ) {
                Icon(
                    modifier = Modifier.size(ICON_SIZE),
                    tint = Color.White,
                    painter = painterResource(id = R.drawable.ic_font_24),
                    contentDescription = when (config.fontType) {
                        FontType.SANSSERIF -> stringResource(R.string.mozac_feature_readerview_sans_serif_font_desc)
                        FontType.SERIF -> stringResource(R.string.mozac_feature_readerview_serif_font_desc)
                    },
                )
                InfernoText(text = when (config.fontType) {
                    FontType.SANSSERIF -> stringResource(R.string.mozac_feature_readerview_sans_serif_font)
                    FontType.SERIF -> stringResource(R.string.mozac_feature_readerview_serif_font)
                })
            }
        }
        // set color scheme dropdown
        TextButton(onClick = { colorSchemeExpanded = true }) {
            DropdownMenu(
                expanded = colorSchemeExpanded,
                onDismissRequest = { colorSchemeExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_light)) },
                    onClick = { onSetColorScheme.invoke(ColorScheme.LIGHT) },
                )
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_sephia)) },
                    onClick = { onSetColorScheme.invoke(ColorScheme.SEPIA) },
                )
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_dark)) },
                    onClick = { onSetColorScheme.invoke(ColorScheme.DARK) },
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            ) {
                Icon(
                    modifier = Modifier.size(ICON_SIZE),
                    tint = Color.White,
                    painter = painterResource(id = R.drawable.ic_color_palette_24),
                    contentDescription = when (config.colorScheme) {
                        ColorScheme.LIGHT -> stringResource(R.string.mozac_feature_readerview_light_color_scheme_desc)
                        ColorScheme.SEPIA -> stringResource(R.string.mozac_feature_readerview_sepia_color_scheme_desc)
                        ColorScheme.DARK -> stringResource(R.string.mozac_feature_readerview_dark_color_scheme_desc)
                    },
                )
                InfernoText(text = when(config.colorScheme) {
                    ColorScheme.LIGHT -> stringResource(R.string.mozac_feature_readerview_light)
                    ColorScheme.SEPIA -> stringResource(R.string.mozac_feature_readerview_sephia)
                    ColorScheme.DARK -> stringResource(R.string.mozac_feature_readerview_dark)
                })
            }
        }

        Spacer(modifier = Modifier.weight(1F))

        // decrease font size
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable(
                    enabled = decreaseEnabled,
                    onClick = { onIncreaseFontSize.invoke() },
                ),
            tint = Color.White,
            painter = painterResource(id = R.drawable.text_decrease_24),
            contentDescription = stringResource(R.string.mozac_feature_readerview_font_size_decrease_desc),
        )
        // increase font size
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable(
                    enabled = increaseEnabled,
                    onClick = { onDecreaseFontSize.invoke() },
                ),
            tint = Color.White,
            painter = painterResource(id = R.drawable.text_increase_24),
            contentDescription = stringResource(R.string.mozac_feature_readerview_font_size_increase_desc),
        )
    }
}

/**
 * Handles content messages from regular pages.
 */
private open class ReaderViewContentMessageHandler(
    protected val store: BrowserStore,
    protected val sessionId: String,
) : MessageHandler {
    override fun onPortConnected(port: Port) {
        port.postMessage(createCheckReaderStateMessage())
    }

    override fun onPortMessage(message: Any, port: Port) {
        if (message is JSONObject) {
            val readerable = message.optBoolean(READERABLE_RESPONSE_MESSAGE_KEY, false)
            store.dispatch(ReaderAction.UpdateReaderableAction(sessionId, readerable))
        }
    }
}

/**
 * Handles content messages from active reader pages.
 */
private class ActiveReaderViewContentMessageHandler(
    store: BrowserStore,
    sessionId: String,
    // This needs to be a weak reference because the engine session this message handler will be
    // attached to has a longer lifespan than the feature instance i.e. a tab can remain open,
    // but we don't want to prevent the feature (and therefore its context/fragment) from
    // being garbage collected. The config has references to both the context and feature.
    private val config: WeakReference<InfernoReaderViewConfig>,
) : ReaderViewContentMessageHandler(store, sessionId) {

    override fun onPortMessage(message: Any, port: Port) {
        super.onPortMessage(message, port)

        if (message is JSONObject) {
            val baseUrl = message.getString(BASE_URL_RESPONSE_MESSAGE_KEY)
            store.dispatch(ReaderAction.UpdateReaderBaseUrlAction(sessionId, baseUrl))

            port.postMessage(
                createShowReaderMessage(
                    config.get(), store.state.selectedTab?.readerState?.scrollY
                )
            )

            val activeUrl = message.getString(ACTIVE_URL_RESPONSE_MESSAGE_KEY)
            store.dispatch(ReaderAction.UpdateReaderActiveUrlAction(sessionId, activeUrl))
        }
    }
}
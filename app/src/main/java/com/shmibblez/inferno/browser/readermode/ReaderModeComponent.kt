package com.shmibblez.inferno.browser.readermode

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.readerview.ReaderViewFeature.ColorScheme
import mozilla.components.feature.readerview.ReaderViewFeature.FontType
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.kotlinx.coroutines.flow.filterChanged
import mozilla.components.support.webextensions.WebExtensionController
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.Locale

private val logger = Logger("ReaderView")

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
    return JSONObject()
        .put(ACTION_MESSAGE_KEY, ACTION_CACHE_PAGE)
        .put(ACTION_VALUE_ID, id)
}

internal fun createShowReaderMessage(config: ReaderViewConfig?, scrollY: Int? = null): JSONObject {
    if (config == null) {
        logger.warn("No config provided. Falling back to default values.")
    }

    val fontSize = config?.fontSize ?: FONT_SIZE_DEFAULT
    val fontType = config?.fontType ?: FontType.SERIF
    val colorScheme = config?.colorScheme ?: ColorScheme.LIGHT
    val configJson = JSONObject()
        .put(ACTION_VALUE_SHOW_FONT_SIZE, fontSize)
        .put(ACTION_VALUE_SHOW_FONT_TYPE, fontType.value.lowercase(Locale.ROOT))
        .put(ACTION_VALUE_SHOW_COLOR_SCHEME, colorScheme.name.lowercase(Locale.ROOT))
    if (scrollY != null) {
        configJson.put(ACTION_VALUE_SCROLLY, scrollY)
    }
    return JSONObject()
        .put(ACTION_MESSAGE_KEY, ACTION_SHOW)
        .put(ACTION_VALUE, configJson)
}

internal fun createHideReaderMessage(): JSONObject {
    return JSONObject().put(ACTION_MESSAGE_KEY, ACTION_HIDE)
}

@Composable
fun ReaderModeComponent(
    tabSessionState: TabSessionState?,
) {
    var scope by remember { mutableStateOf<CoroutineScope?>(null) }
    val context = LocalContext.current
    val store = context.components.core.store
    val engine = context.components.core.engine
    val extensionController = remember { WebExtensionController(
        READER_VIEW_EXTENSION_ID,
        READER_VIEW_EXTENSION_URL,
        READER_VIEW_CONTENT_PORT,
    ) }

    DisposableEffect(null) {
        ensureExtensionInstalled(
            context = context,
            extensionController = extensionController,
            engine = engine,
        )

        scope = store.flowScoped { flow ->
            flow.mapNotNull { state -> state.tabs }
                .filterChanged {
                    it.readerState
                }
                .collect { tab ->
                    if (tab.readerState.connectRequired) {
                        connectReaderViewContentScript(tab)
                    }
                    if (tab.readerState.checkRequired) {
                        checkReaderState(tab)
                    }
                    if (tab.id == store.state.selectedTabId) {
                        maybeNotifyReaderStatusChange(tab.readerState.readerable, tab.readerState.active)
                    }
                }
        }

        // todo controlsInteractor.start()

        onDispose {
            scope?.cancel()
        }
    }
}

private fun ensureExtensionInstalled(context: Context, extensionController: WebExtensionController, engine: Engine) {
    val feature = WeakReference(context)
    extensionController.install(
        engine,
        onSuccess = {
            it.getMetadata()?.run {
                readerBaseUrl = baseUrl
            } ?: run {
                Logger.error("ReaderView extension missing Metadata")
            }

            feature.get()?.connectReaderViewContentScript()
        },
    )
}
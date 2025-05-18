package com.shmibblez.inferno.browser.readermode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.browser.state.action.ReaderAction
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.concept.engine.webextension.Port
import mozilla.components.support.base.log.logger.Logger
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.Locale
import com.shmibblez.inferno.browser.readermode.InfernoReaderViewFeatureState.FontType
import com.shmibblez.inferno.browser.readermode.InfernoReaderViewFeatureState.ColorScheme
import com.shmibblez.inferno.compose.base.InfernoIcon

private val logger = Logger("ReaderView")
private val ICON_SIZE = 18.dp

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
fun InfernoReaderViewControls(
    state: InfernoReaderViewFeatureState,
) {
    if (!state.active) return

    var fontExpanded by remember { mutableStateOf(false) }
    var colorSchemeExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxWidth()
            .height(ComponentDimens.READER_VIEW_HEIGHT)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        // exit reader view
        InfernoIcon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable(onClick = {
                    state.hideReaderView()
                }),
            painter = painterResource(id = R.drawable.ic_cross_24),
            contentDescription = "exit"
        )

        // start spacer for centering controls
        Spacer(modifier = Modifier.weight(1F))

        // set font dropdown
        TextButton(onClick = { fontExpanded = true }) {
            DropdownMenu(
                expanded = fontExpanded,
                onDismissRequest = { fontExpanded = false },
                containerColor = Color.DarkGray,
            ) {
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_serif_font)) },
                    onClick = {
                        state.config.fontType = FontType.SERIF
                        fontExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_sans_serif_font)) },
                    onClick = {
                        state.config.fontType = FontType.SANSSERIF
                        fontExpanded = false
                    },
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            ) {
                InfernoIcon(
                    modifier = Modifier.size(ICON_SIZE),
                    painter = painterResource(id = R.drawable.ic_font_24),
                    contentDescription = when (state.config.fontType) {
                        FontType.SANSSERIF -> stringResource(R.string.mozac_feature_readerview_sans_serif_font_desc)
                        FontType.SERIF -> stringResource(R.string.mozac_feature_readerview_serif_font_desc)
                    },
                )
                InfernoText(
                    text = when (state.config.fontType) {
                        FontType.SANSSERIF -> stringResource(R.string.mozac_feature_readerview_sans_serif_font)
                        FontType.SERIF -> stringResource(R.string.mozac_feature_readerview_serif_font)
                    }
                )
            }
        }

        // set color scheme dropdown
        TextButton(onClick = { colorSchemeExpanded = true }) {
            DropdownMenu(
                expanded = colorSchemeExpanded,
                onDismissRequest = { colorSchemeExpanded = false },
                containerColor = Color.DarkGray,
            ) {
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_light)) },
                    onClick = {
                        state.config.colorScheme = ColorScheme.LIGHT
                        colorSchemeExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_sephia)) },
                    onClick = {
                        state.config.colorScheme = ColorScheme.SEPIA
                        colorSchemeExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { InfernoText(text = stringResource(R.string.mozac_feature_readerview_dark)) },
                    onClick = {
                        state.config.colorScheme = ColorScheme.DARK
                        colorSchemeExpanded = false
                    },
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            ) {
                InfernoIcon(
                    modifier = Modifier.size(ICON_SIZE),
                    painter = painterResource(id = R.drawable.ic_color_palette_24),
                    contentDescription = when (state.config.colorScheme) {
                        ColorScheme.LIGHT -> stringResource(R.string.mozac_feature_readerview_light_color_scheme_desc)
                        ColorScheme.SEPIA -> stringResource(R.string.mozac_feature_readerview_sepia_color_scheme_desc)
                        ColorScheme.DARK -> stringResource(R.string.mozac_feature_readerview_dark_color_scheme_desc)
                    },
                )
                InfernoText(
                    text = when (state.config.colorScheme) {
                        ColorScheme.LIGHT -> stringResource(R.string.mozac_feature_readerview_light)
                        ColorScheme.SEPIA -> stringResource(R.string.mozac_feature_readerview_sephia)
                        ColorScheme.DARK -> stringResource(R.string.mozac_feature_readerview_dark)
                    }
                )
            }
        }

        // decrease font size
        InfernoIcon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable(
                    // enabled if font size greater than min
                    enabled = state.config.fontSize > MIN_TEXT_SIZE,
                    onClick = {
                        state.config.fontSize -= 1
                    },
                ),
            enabled = state.config.fontSize > MIN_TEXT_SIZE,
            painter = painterResource(id = R.drawable.text_decrease_24),
            contentDescription = stringResource(R.string.mozac_feature_readerview_font_size_decrease_desc),
        )

        // increase font size
        InfernoIcon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable(
                    // enabled if less than max
                    enabled = state.config.fontSize < MAX_TEXT_SIZE,
                    onClick = {
                        state.config.fontSize += 1
                    },
                ),
            enabled = state.config.fontSize < MAX_TEXT_SIZE,
            painter = painterResource(id = R.drawable.text_increase_24),
            contentDescription = stringResource(R.string.mozac_feature_readerview_font_size_increase_desc),
        )

        // end spacer for centering controls
        Spacer(modifier = Modifier.weight(1F))
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
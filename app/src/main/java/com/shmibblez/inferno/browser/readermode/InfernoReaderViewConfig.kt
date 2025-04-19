package com.shmibblez.inferno.browser.readermode


import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import mozilla.components.feature.readerview.ReaderViewFeature
import org.json.JSONObject

/**
 * Stores the user configuration for reader view in shared prefs.
 * All values are initialized lazily and cached.
 * @param context Used to lazily obtain shared preferences and to check dark mode status.
 * @param sendConfigMessage If the config changes, this method will be invoked
 * with a JSON object which should be sent to the content script so the new
 * config can be applied.
 */
internal class InfernoReaderViewConfig(
    context: Context,
    private val sendConfigMessage: (JSONObject) -> Unit,
) {

    private val prefs by lazy { context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE) }
    private val resources = context.resources
    private var colorSchemeCache: InfernoReaderViewFeatureState.ColorScheme? = null
    private var fontTypeCache: InfernoReaderViewFeatureState.FontType? = null
    private var fontSizeCache: Int? = null

    var colorScheme by run {
        val state = mutableStateOf(InfernoReaderViewFeatureState.ColorScheme.DARK)
        object: MutableState<InfernoReaderViewFeatureState.ColorScheme> by state {
            override var value: InfernoReaderViewFeatureState.ColorScheme
                get() = run {
                    if (colorSchemeCache == null) {
                        // Default to a dark theme if either the system or local dark theme is active
                        val defaultColor = if (isNightMode()) {
                            InfernoReaderViewFeatureState.ColorScheme.DARK
                        } else {
                            InfernoReaderViewFeatureState.ColorScheme.LIGHT
                        }
                        colorSchemeCache = getEnumFromPrefs(COLOR_SCHEME_KEY, defaultColor)
                    }
                    state.value = colorSchemeCache!!
                    return state.value
                }
                set(value) {
                    if (colorSchemeCache != value) {
                        colorSchemeCache = value
                        // todo: check if recursive
                        state.value = colorSchemeCache!!
                        prefs.edit().putString(COLOR_SCHEME_KEY, value.name).apply()
                        sendMessage(ACTION_SET_COLOR_SCHEME) { put(ACTION_VALUE, value.name) }
                    }
                }
        }
    }

    var fontType by run {
        val state = mutableStateOf(InfernoReaderViewFeatureState.FontType.SERIF)
        object: MutableState<InfernoReaderViewFeatureState.FontType> by state {
            override var value: InfernoReaderViewFeatureState.FontType
                get() = run {
                    if (fontTypeCache == null) {
                        fontTypeCache = getEnumFromPrefs(FONT_TYPE_KEY, InfernoReaderViewFeatureState.FontType.SERIF)
                    }
                    state.value = fontTypeCache!!
                    return state.value
                }
                set(value) {
                    if (fontTypeCache != value) {
                        fontTypeCache = value
                        state.value = fontTypeCache!!
                        prefs.edit().putString(FONT_TYPE_KEY, value.name).apply()
                        sendMessage(ACTION_SET_FONT_TYPE) { put(ACTION_VALUE, value.value) }
                    }
                }
        }
    }

    var fontSize by run {
        val state = mutableIntStateOf(FONT_SIZE_DEFAULT)
        object: MutableState<Int> by state {
            override var value: Int
                get() = run {
                    if (fontSizeCache == null) {
                        fontSizeCache = prefs.getInt(FONT_SIZE_KEY, FONT_SIZE_DEFAULT)
                    }
                    state.intValue = fontSizeCache!!
                    return state.intValue
                }
                set(value) {
                    if (fontSizeCache != value) {
                        val diff = value - state.intValue
                        fontSizeCache = value
                        state.intValue = fontSizeCache!!
                        prefs.edit().putInt(FONT_SIZE_KEY, value).apply()
                        sendMessage(ACTION_CHANGE_FONT_SIZE) { put(ACTION_VALUE, diff) }
                    }
                }
        }
    }

    private inline fun <reified T : Enum<T>> getEnumFromPrefs(key: String, default: T): T {
        val enumName = prefs.getString(key, default.name) ?: default.name
        return enumValueOf(enumName)
    }

    private fun isNightMode(): Boolean {
        val darkFlag = resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        return darkFlag == Configuration.UI_MODE_NIGHT_YES
    }

    private inline fun sendMessage(action: String, crossinline builder: JSONObject.() -> Unit) {
        val message = JSONObject().put(ACTION_MESSAGE_KEY, action)
        builder(message)
        sendConfigMessage(message)
    }
}

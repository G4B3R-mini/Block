package com.shmibblez.inferno.ext

import android.content.Context
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.settings.theme.InfernoTheme

fun InfernoSettings.getSelectedTheme(context: Context): InfernoTheme {
    fun getDefaultTheme(): InfernoTheme {
        return when (this.selectedDefaultTheme) {
            InfernoSettings.DefaultTheme.INFERNO_LIGHT -> InfernoTheme.light(context)
            InfernoSettings.DefaultTheme.MOZILLA_INCOGNITO_DARK -> InfernoTheme.incognitoDark(context)
            InfernoSettings.DefaultTheme.MOZILLA_INCOGNITO_LIGHT -> InfernoTheme.incognitoLight(context)
            InfernoSettings.DefaultTheme.INFERNO_DARK,
            null,
                -> InfernoTheme.dark(context)
        }
    }

    val customName = this.selectedCustomTheme
    // if custom not set, set default theme and return
    if (customName.isBlank()) return getDefaultTheme()
    val customObj = this.customThemesMap.getOrElse(customName) { null }
    // if custom not found, set default theme and return
    if (customObj == null) return getDefaultTheme()
    return InfernoTheme.fromSettingsObj(customObj)
}
package com.shmibblez.inferno.settings.theme

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.getSelectedTheme
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import mozilla.components.ui.colors.PhotonColors

// todo: if doesnt work might have to make each value of InfernoSettings mutable
class InfernoThemeProvider(context: Context) : MutableState<InfernoTheme> {

    override var value: InfernoTheme = InfernoTheme.dark(context)
//        runBlocking {
//            Log.d("InfernoThemeProvider", "theme accessed, getting latest theme")
//            val theme = context.infernoSettingsDataStore.data.last().getSelectedTheme(context)
//            Log.d("InfernoThemeProvider", "latest theme retrieved")
//            theme
//        }

    init {
        Log.d("InfernoThemeProvider", "init")
        // launch collector that updates settings
        MainScope().launch {
            context.infernoSettingsDataStore.data.distinctUntilChanged(areEquivalent = ::didThemeChange)
                .collect {
                    Log.d("InfernoThemeProvider", "theme changed, updating")
                    // if custom set and exists, set custom theme and end fun
                    value = it.getSelectedTheme(context)
                }
        }
    }


    private fun didThemeChange(old: InfernoSettings, new: InfernoSettings): Boolean {
        when {
            // if default theme changed
            old.selectedCustomTheme.isBlank() && new.selectedCustomTheme.isBlank() -> {
                return old.selectedDefaultTheme == new.selectedDefaultTheme
            }
            // if from default to custom or vice versa
            (old.selectedCustomTheme.isBlank() && new.selectedCustomTheme.isNotBlank()) || (old.selectedCustomTheme.isNotBlank() && new.selectedCustomTheme.isBlank()) -> {
                return true
            }
            // if custom theme changed
            else -> {
                val oldTheme = old.customThemesMap.getOrElse(old.selectedCustomTheme) { null }
                val newTheme = new.customThemesMap.getOrElse(new.selectedCustomTheme) { null }
                if (oldTheme == null || newTheme == null) return false
                return oldTheme.timeSet == newTheme.timeSet
            }
        }
    }

    override fun component1(): InfernoTheme {
        return value
    }

    override fun component2(): (InfernoTheme) -> Unit {
        return { value = it }
    }
}

open class InfernoTheme(
    var name: String,
    val defaultType: InfernoSettings.DefaultTheme? = null,
    // text (primary: enabled, secondary: disabled / hint / subtitle)
    var primaryTextColor: Color,
    var secondaryTextColor: Color,
    // icon (primary: enabled, secondary: disabled)
    var primaryIconColor: Color,
    var secondaryIconColor: Color,
    // outline - buttons, text fields, checkbox (primary: enabled, secondary: disabled)
    var primaryOutlineColor: Color,
    var secondaryOutlineColor: Color,
    // action - switches, buttons, check boxes (primary: enabled, secondary: disabled)
    var primaryActionColor: Color,
    var secondaryActionColor: Color,
    // error - text, outline, icon
    var errorColor: Color,
    // background - toolbar (primary: base background, secondary: address bar)
    var primaryBackgroundColor: Color,
    var secondaryBackgroundColor: Color,
) {
    val isDefault: Boolean
        get() = defaultType != null
    val isCustom: Boolean
        get() = defaultType == null

    fun copy(
        name: String? = null,
        primaryTextColor: Color? = null,
        secondaryTextColor: Color? = null,
        primaryIconColor: Color? = null,
        secondaryIconColor: Color? = null,
        primaryOutlineColor: Color? = null,
        secondaryOutlineColor: Color? = null,
        primaryActionColor: Color? = null,
        secondaryActionColor: Color? = null,
        errorColor: Color? = null,
        primaryBackgroundColor: Color? = null,
        secondaryBackgroundColor: Color? = null,
    ) = InfernoTheme(
        name = name ?: this.name,
        defaultType = null,
        primaryTextColor = primaryTextColor ?: this.primaryTextColor,
        secondaryTextColor = secondaryTextColor ?: this.secondaryTextColor,
        primaryIconColor = primaryIconColor ?: this.primaryIconColor,
        secondaryIconColor = secondaryIconColor ?: this.secondaryIconColor,
        primaryOutlineColor = primaryOutlineColor ?: this.primaryOutlineColor,
        secondaryOutlineColor = secondaryOutlineColor ?: this.secondaryOutlineColor,
        primaryActionColor = primaryActionColor ?: this.primaryActionColor,
        secondaryActionColor = secondaryActionColor ?: this.secondaryActionColor,
        errorColor = errorColor ?: this.errorColor,
        primaryBackgroundColor = primaryBackgroundColor ?: this.primaryBackgroundColor,
        secondaryBackgroundColor = secondaryBackgroundColor ?: this.secondaryBackgroundColor,
    )


    fun toSettingsObj(): InfernoSettings.InfernoTheme {
        // todo: use builder to return InfernoSettings.InfernoTheme
        return InfernoSettings.InfernoTheme.newBuilder().setName(this.name)
            .setPrimaryTextColor(this.primaryTextColor.toArgb())
            .setSecondaryTextColor(this.secondaryTextColor.toArgb())
            .setPrimaryIconColor(this.primaryIconColor.toArgb())
            .setSecondaryIconColor(this.secondaryIconColor.toArgb())
            .setPrimaryOutlineColor(this.primaryOutlineColor.toArgb())
            .setSecondaryOutlineColor(this.secondaryOutlineColor.toArgb())
            .setPrimaryActionColor(this.primaryActionColor.toArgb())
            .setSecondaryActionColor(this.secondaryActionColor.toArgb())
            .setErrorColor(this.errorColor.toArgb())
            .setPrimaryBackgroundColor(this.primaryBackgroundColor.toArgb())
            .setSecondaryBackgroundColor(this.secondaryBackgroundColor.toArgb()).build()
    }

    companion object {

        fun dark(context: Context) = InfernoTheme(
            defaultType = InfernoSettings.DefaultTheme.INFERNO_DARK,
            name = context.getString(R.string.preference_dark_theme),
            primaryTextColor = Color.White,
            secondaryTextColor = Color.White.copy(alpha = 0.75F),
            primaryIconColor = Color.White,
            secondaryIconColor = Color.White.copy(alpha = 0.75F),
            primaryOutlineColor = Color.White,
            secondaryOutlineColor = Color.Red,
            primaryActionColor = Color.Red,
            secondaryActionColor = Color.Red.copy(alpha = 0.75F),
            errorColor = Color.Red,
            primaryBackgroundColor = Color.Black,
            secondaryBackgroundColor = Color.DarkGray,
        )

        fun light(context: Context) = InfernoTheme(
            defaultType = InfernoSettings.DefaultTheme.INFERNO_LIGHT,
            name = context.getString(R.string.preference_light_theme),
            primaryTextColor = Color.Black,
            secondaryTextColor = Color.Black.copy(alpha = 0.75F),
            primaryIconColor = Color.Black,
            secondaryIconColor = Color.Black.copy(alpha = 0.75F),
            primaryOutlineColor = Color.Black,
            secondaryOutlineColor = Color.Black.copy(alpha = 0.75F),
            primaryActionColor = Color.Red,
            secondaryActionColor = Color.Red.copy(alpha = 0.75F),
            errorColor = Color.Red,
            primaryBackgroundColor = Color.White,
            secondaryBackgroundColor = Color.LightGray,
        )

        // todo: refine incog based on moz private theme
//        val privateColorPalette = darkColorPalette.copy(
//            layer1 = PhotonColors.Violet90,
//            layer2 = PhotonColors.Violet90,
//            layer3 = PhotonColors.Ink90,
//            layerSearch = PhotonColors.Ink90,
//            borderPrimary = PhotonColors.Ink05,
//            borderSecondary = PhotonColors.Ink10,
//            borderToolbarDivider = PhotonColors.Violet80,
//            tabActive = PhotonColors.Purple60,
//            tabInactive = PhotonColors.Ink90,
//        )

        // todo: refine
        // use color: Color(143, 0, 255) (very nice purple color)
        fun incognitoDark(context: Context) = InfernoTheme(
            defaultType = InfernoSettings.DefaultTheme.MOZILLA_INCOGNITO_DARK,
            name = "Incognito Dark", // todo: string res
            primaryTextColor = Color.White,
            secondaryTextColor = Color.White.copy(alpha = 0.75F),
            primaryIconColor = Color.White,
            secondaryIconColor = Color.White.copy(alpha = 0.75F),
            primaryOutlineColor = Color.White,
            secondaryOutlineColor = PhotonColors.Purple60,
            primaryActionColor = PhotonColors.Purple60,
            secondaryActionColor = PhotonColors.Purple60.copy(alpha = 0.75F),
            errorColor = Color.Red,
            primaryBackgroundColor = Color.Black,
            secondaryBackgroundColor = PhotonColors.Ink90,
        )

        // todo: refine
        fun incognitoLight(context: Context) = InfernoTheme(
            defaultType = InfernoSettings.DefaultTheme.MOZILLA_INCOGNITO_LIGHT,
            name = "Incognito Light", // todo: string res
            primaryTextColor = Color.White,
            secondaryTextColor = Color.White.copy(alpha = 0.75F),
            primaryIconColor = Color.White,
            secondaryIconColor = Color.White.copy(alpha = 0.75F),
            primaryOutlineColor = Color.White,
            secondaryOutlineColor = PhotonColors.Purple60,
            primaryActionColor = PhotonColors.Purple60,
            secondaryActionColor = PhotonColors.Purple60.copy(alpha = 0.75F),
            errorColor = Color.Red,
            primaryBackgroundColor = PhotonColors.Violet90,
            secondaryBackgroundColor = PhotonColors.Violet80,
        )

        fun fromSettingsObj(theme: InfernoSettings.InfernoTheme): InfernoTheme = InfernoTheme(
            name = theme.name,
            primaryTextColor = Color(theme.primaryTextColor),
            secondaryTextColor = Color(theme.secondaryTextColor),
            primaryIconColor = Color(theme.primaryIconColor),
            secondaryIconColor = Color(theme.secondaryIconColor),
            primaryOutlineColor = Color(theme.primaryOutlineColor),
            secondaryOutlineColor = Color(theme.secondaryOutlineColor),
            primaryActionColor = Color(theme.primaryActionColor),
            secondaryActionColor = Color(theme.secondaryActionColor),
            errorColor = Color(theme.errorColor),
            primaryBackgroundColor = Color(theme.primaryBackgroundColor),
            secondaryBackgroundColor = Color(theme.secondaryBackgroundColor),
        )
    }
}


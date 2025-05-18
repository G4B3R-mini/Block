package com.shmibblez.inferno.settings.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.R

class InfernoTheme(
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
            secondaryOutlineColor = Color.White.copy(alpha = 0.75F),
            primaryActionColor = Color.Red,
            secondaryActionColor = Color.Red.copy(alpha = 0.75F),
            errorColor = Color.Red,
            primaryBackgroundColor = Color.Black,
            secondaryBackgroundColor = Color.DarkGray,
        )

        fun light(context: Context) = InfernoTheme(
            defaultType = InfernoSettings.DefaultTheme.INFERNO_LIGHT,
            name = context.getString(R.string.preference_light_theme),
            primaryTextColor = Color.White,
            secondaryTextColor = Color.White.copy(alpha = 0.75F),
            primaryIconColor = Color.White,
            secondaryIconColor = Color.White.copy(alpha = 0.75F),
            primaryOutlineColor = Color.White,
            secondaryOutlineColor = Color.White.copy(alpha = 0.75F),
            primaryActionColor = Color.Red,
            secondaryActionColor = Color.Red.copy(alpha = 0.75F),
            errorColor = Color.Red,
            primaryBackgroundColor = Color.Black,
            secondaryBackgroundColor = Color.DarkGray,
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


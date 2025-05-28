package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.infernoTheme

@Composable
fun InfernoSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: @Composable() (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchColors(
        checkedThumbColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        checkedTrackColor = Color.Transparent,
        checkedBorderColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        checkedIconColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        uncheckedThumbColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        uncheckedTrackColor = Color.Transparent,
        uncheckedBorderColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        uncheckedIconColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        disabledCheckedThumbColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,    // .secondaryBackgroundColor,
        disabledCheckedTrackColor = Color.Transparent,    // .secondaryBackgroundColor,
        disabledCheckedBorderColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,   // .secondaryBackgroundColor,
        disabledCheckedIconColor = LocalContext.current.infernoTheme().value.secondaryIconColor,     // .secondaryIconColor,
        disabledUncheckedThumbColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,  // .secondaryBackgroundColor,
        disabledUncheckedTrackColor = Color.Transparent,  // .secondaryBackgroundColor,
        disabledUncheckedBorderColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor, // .secondaryBackgroundColor,
        disabledUncheckedIconColor = LocalContext.current.infernoTheme().value.secondaryIconColor,   // .secondaryIconColor,
    ),
    interactionSource: MutableInteractionSource? = null,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        thumbContent = thumbContent,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}
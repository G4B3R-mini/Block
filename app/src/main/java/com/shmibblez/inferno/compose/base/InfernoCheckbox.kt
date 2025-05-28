package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.shmibblez.inferno.ext.infernoTheme

@Composable
fun InfernoCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxColors(
        checkedCheckmarkColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        uncheckedCheckmarkColor = Color.Transparent,
        checkedBoxColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        uncheckedBoxColor = Color.Transparent,
        disabledCheckedBoxColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        disabledUncheckedBoxColor = Color.Transparent,
        disabledIndeterminateBoxColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        checkedBorderColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        uncheckedBorderColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        disabledBorderColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        disabledUncheckedBorderColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        disabledIndeterminateBorderColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
    ),
    interactionSource: MutableInteractionSource? = null,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
        )
    }
}
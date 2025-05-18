package com.shmibblez.inferno.compose.base

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.shmibblez.inferno.ext.infernoTheme

@Composable
fun InfernoCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(
        checkedColor = LocalContext.current.infernoTheme().value.primaryActionColor, // Color(143, 0, 255),
        checkmarkColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        uncheckedColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        disabledCheckedColor = LocalContext.current.infernoTheme().value.secondaryActionColor,
        disabledUncheckedColor = LocalContext.current.infernoTheme().value.secondaryIconColor,
        disabledIndeterminateColor = LocalContext.current.infernoTheme().value.secondaryIconColor,
    ),
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
        )
    }
}
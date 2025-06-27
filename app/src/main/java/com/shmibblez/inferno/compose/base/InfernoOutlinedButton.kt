package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.ext.infernoTheme

@Composable
fun InfernoOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors().copy(
        containerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        contentColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        disabledContentColor = LocalContext.current.infernoTheme().value.secondaryOutlineColor,
    ),
    border: BorderStroke? = BorderStroke(
        width = 1.dp,
        color = when (enabled) {
            true -> colors.contentColor
            false ->colors.disabledContentColor
        },
    ),
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier, // .clip(shape),
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
    ) {
        InfernoText(
            text = text,
            maxLines = 1,
            fontColor = when (enabled) {
                true -> colors.contentColor
                false ->colors.disabledContentColor
            },
        )
    }
}

// todo: use enabled to determine colors
@Composable
fun InfernoOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors().copy(
        containerColor = Color.Transparent,
        contentColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        disabledContainerColor = Color.Transparent,
    ),
    border: BorderStroke? = BorderStroke(
        width = 1.dp,
        color = colors.contentColor,
    ),
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        content = content,
    )
}
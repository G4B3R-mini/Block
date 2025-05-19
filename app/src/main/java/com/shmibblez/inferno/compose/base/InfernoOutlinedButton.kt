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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors().copy(
        containerColor = Color.Transparent,
        contentColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        disabledContainerColor = Color.Transparent,
    ),
    border: BorderStroke? = BorderStroke(
        width = 1.dp,
        color = LocalContext.current.infernoTheme().value.primaryOutlineColor,
    ),
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
//        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        content = content,
    )
}
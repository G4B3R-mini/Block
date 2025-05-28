package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.infernoTheme

@Composable
fun InfernoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonColors(
        containerColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        contentColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        disabledContentColor = LocalContext.current.infernoTheme().value.secondaryTextColor
    ),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        InfernoText(
            text = text,
            fontColor = LocalContext.current.infernoTheme().value.let {
                when (enabled) {
                    true -> colors.contentColor
                    false -> colors.disabledContentColor
                }
            },
        )
    }
}
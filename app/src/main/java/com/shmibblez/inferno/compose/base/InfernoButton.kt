package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.shmibblez.inferno.ext.infernoTheme

/**
 * @param sensitive whether action is sensitive
 */
@Composable
fun InfernoButton(
    text: String,
    sensitive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonColors(
        containerColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        contentColor = when (sensitive) {
            true -> LocalContext.current.infernoTheme().value.errorColor
            false -> LocalContext.current.infernoTheme().value.primaryTextColor
        },
        disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryActionColor,
        disabledContentColor = when (sensitive) {
            true -> LocalContext.current.infernoTheme().value.errorColor
            false -> LocalContext.current.infernoTheme().value.secondaryTextColor
        }
    ),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier, // .clip(shape),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        leadingIcon?.invoke()
        InfernoText(
            text = text,
            maxLines = 1,
            modifier = Modifier.weight(1F),
            textAlign = TextAlign.Center,
            fontColor = LocalContext.current.infernoTheme().value.let {
                when (enabled) {
                    true -> colors.contentColor
                    false -> colors.disabledContentColor
                }
            },
        )
    }
}
/**
 * @param sensitive whether action is sensitive
 */
@Composable
fun InfernoButton(
    sensitive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonColors(
        containerColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        contentColor = when (sensitive) {
            true -> LocalContext.current.infernoTheme().value.errorColor
            false -> LocalContext.current.infernoTheme().value.primaryTextColor
        },
        disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryActionColor,
        disabledContentColor = when (sensitive) {
            true -> LocalContext.current.infernoTheme().value.errorColor
            false -> LocalContext.current.infernoTheme().value.secondaryTextColor
        }
    ),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier, // .clip(shape),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}
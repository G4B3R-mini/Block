/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.compose.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
//import com.shmibblez.inferno.mozillaAndroidComponents.base.compose.annotation.LightDarkPreview
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.annotation.LightDarkPreview

const val DEFAULT_MAX_LINES = 2

/**
 * Base component for buttons.
 *
 * @param text The button text to be displayed.
 * @param textColor [Color] to apply to the button text.
 * @param backgroundColor The background [Color] of the button.
 * @param modifier [Modifier] to be applied to the layout.
 * @param enabled Controls the enabled state of the button.
 * When false, this button will not be clickable.
 * @param icon Optional [Painter] used to display a [InfernoIcon] before the button text.
 * @param iconModifier [Modifier] to be applied to the icon.
 * @param tint Tint [Color] to be applied to the icon.
 * @param onClick Invoked when the user clicks on the button.
 */
@Composable
private fun Button(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: Painter? = null,
    iconModifier: Modifier = Modifier,
    tint: Color,
    onClick: () -> Unit,
) {
    // Required to detect if font increased due to accessibility.
    val fontScale: Float = LocalConfiguration.current.fontScale

    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 0.dp, pressedElevation = 0.dp
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
        ),
    ) {
        icon?.let { painter ->
            InfernoIcon(
                painter = painter,
                contentDescription = null,
                modifier = iconModifier,
                tint = tint,
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        InfernoText(
            text = text,
            textAlign = TextAlign.Center,
            fontColor = textColor,
//            style = FirefoxTheme.typography.button,
            maxLines = if (fontScale > 1.0f) Int.MAX_VALUE else DEFAULT_MAX_LINES,
        )
    }
}

/**
 * Primary button.
 *
 * @param text The button text to be displayed.
 * @param modifier [Modifier] to be applied to the layout.
 * @param enabled Controls the enabled state of the button.
 * When false, this button will not be clickable.
 * Whenever [textColor] and [backgroundColor] are not defaults, and [enabled] is false,
 * then the default color state for a disabled button will be presented.
 * @param textColor [Color] to apply to the button text.
 * @param backgroundColor The background [Color] of the button.
 * @param icon Optional [Painter] used to display an [InfernoIcon] before the button text.
 * @param iconModifier [Modifier] to be applied to the icon.
 * @param onClick Invoked when the user clicks on the button.
 */
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    textColor: Color = LocalContext.current.infernoTheme().value.primaryTextColor,
    backgroundColor: Color = LocalContext.current.infernoTheme().value.primaryActionColor,
    icon: Painter? = null,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var buttonTextColor = textColor
    var buttonBackgroundColor = backgroundColor

    // If not enabled and using default colors, then use the disabled button color defaults.
    if (!enabled && textColor == LocalContext.current.infernoTheme().value.primaryTextColor && backgroundColor == LocalContext.current.infernoTheme().value.primaryActionColor) {
        buttonTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor
        buttonBackgroundColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor
    }

    Button(
        text = text,
        textColor = buttonTextColor,
        backgroundColor = buttonBackgroundColor,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        iconModifier = iconModifier,
        tint = LocalContext.current.infernoTheme().value.primaryIconColor,
        onClick = onClick,
    )
}

/**
 * Secondary button.
 *
 * @param text The button text to be displayed.
 * @param modifier [Modifier] to be applied to the layout.
 * @param enabled Controls the enabled state of the button.
 * When false, this button will not be clickable
 * @param textColor [Color] to apply to the button text.
 * @param backgroundColor The background [Color] of the button.
 * @param icon Optional [Painter] used to display an [InfernoIcon] before the button text.
 * @param iconModifier [Modifier] to be applied to the icon.
 * @param onClick Invoked when the user clicks on the button.
 */
@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    textColor: Color = LocalContext.current.infernoTheme().value.primaryTextColor,// FirefoxTheme.colors.textActionSecondary,
    backgroundColor: Color = LocalContext.current.infernoTheme().value.primaryActionColor, // FirefoxTheme.colors.actionSecondary,
    icon: Painter? = null,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        text = text,
        textColor = textColor,
        backgroundColor = backgroundColor,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        iconModifier = iconModifier,
        tint = LocalContext.current.infernoTheme().value.primaryIconColor,// FirefoxTheme.colors.iconActionSecondary,
        onClick = onClick,
    )
}

/**
 * Tertiary button.
 *
 * @param text The button text to be displayed.
 * @param modifier [Modifier] to be applied to the layout.
 * @param enabled Controls the enabled state of the button.
 * When false, this button will not be clickable
 * @param textColor [Color] to apply to the button text.
 * @param backgroundColor The background [Color] of the button.
 * @param icon Optional [Painter] used to display an [InfernoIcon] before the button text.
 * @param iconModifier [Modifier] to be applied to the icon.
 * @param onClick Invoked when the user clicks on the button.
 */
@Composable
fun TertiaryButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    textColor: Color = LocalContext.current.infernoTheme().value.primaryTextColor,
    backgroundColor: Color = LocalContext.current.infernoTheme().value.primaryActionColor,
    icon: Painter? = null,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        text = text,
        textColor = textColor,
        backgroundColor = backgroundColor,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        iconModifier = iconModifier,
        tint = LocalContext.current.infernoTheme().value.secondaryBackgroundColor, // FirefoxTheme.colors.iconActionTertiary,
        onClick = onClick,
    )
}

/**
 * Destructive button.
 *
 * @param text The button text to be displayed.
 * @param modifier [Modifier] to be applied to the layout.
 * @param enabled Controls the enabled state of the button.
 * When false, this button will not be clickable
 * @param textColor [Color] to apply to the button text.
 * @param backgroundColor The background [Color] of the button.
 * @param icon Optional [Painter] used to display an [InfernoIcon] before the button text.
 * @param iconModifier [Modifier] to be applied to the icon.
 * @param onClick Invoked when the user clicks on the button.
 */
@Composable
fun DestructiveButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    textColor: Color = LocalContext.current.infernoTheme().value.primaryTextColor,
    backgroundColor: Color = LocalContext.current.infernoTheme().value.primaryActionColor,
    icon: Painter? = null,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        text = text,
        textColor = textColor,
        backgroundColor = backgroundColor,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        iconModifier = iconModifier,
        tint = LocalContext.current.infernoTheme().value.errorColor,
        onClick = onClick,
    )
}

@Composable
@LightDarkPreview
private fun ButtonPreview() {
    Column(
        modifier = Modifier
            .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PrimaryButton(
            text = "Label",
            icon = painterResource(R.drawable.ic_tab_collection),
            onClick = {},
        )

        SecondaryButton(
            text = "Label",
            icon = painterResource(R.drawable.ic_tab_collection),
            onClick = {},
        )

        TertiaryButton(
            text = "Label",
            icon = painterResource(R.drawable.ic_tab_collection),
            onClick = {},
        )

        DestructiveButton(
            text = "Label",
            icon = painterResource(R.drawable.ic_tab_collection),
            onClick = {},
        )
    }
}

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.compose.menu

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.compose.menu.MenuItem.FixedItem.Level
import com.shmibblez.inferno.compose.text.Text
import com.shmibblez.inferno.compose.text.value
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.Divider
import androidx.compose.material3.DropdownMenu as MaterialDropdownMenu
import androidx.compose.material3.DropdownMenuItem as MaterialDropdownMenuItem

private val ItemHorizontalSpaceBetween = 16.dp
private val defaultMenuShape = RoundedCornerShape(8.dp)

/**
 * A dropdown menu that displays a list of [MenuItem]s. The menu can be expanded or collapsed and
 * is displayed as a popup anchored to the menu button that triggers it.
 *
 * @param menuItems the list of [MenuItem]s to display in the menu.
 * @param expanded whether or not the menu is expanded.
 * @param modifier [Modifier] to be applied to the menu.
 * @param offset [DpOffset] from the original anchor position of the menu.
 * @param scrollState [ScrollState] used by the menu's content for vertical scrolling.
 * @param onDismissRequest Invoked when the user requests to dismiss the menu, such as by tapping
 * outside the menu's bounds.
 */
@Composable
fun DropdownMenu(
    menuItems: List<MenuItem>,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    onDismissRequest: () -> Unit,
) {
    MaterialTheme(shapes = MaterialTheme.shapes.copy(medium = defaultMenuShape)) {
        MaterialDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            offset = offset,
            scrollState = scrollState,
            containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
            modifier = modifier, // .background(LocalContext.current.infernoTheme().value.secondaryBackgroundColor),
        ) {
            DropdownMenuContent(
                menuItems = menuItems,
                onDismissRequest = onDismissRequest,
            )
        }
    }
}

@Composable
private fun DropdownMenuContent(
    menuItems: List<MenuItem>,
    onDismissRequest: () -> Unit,
) {
    menuItems.forEach {
        when (it) {
            is MenuItem.FixedItem -> {
                CompositionLocalProvider(LocalLevelColor provides it.level) {
                    when (it) {
                        is MenuItem.TextItem -> FlexibleDropdownMenuItem(
                            onClick = {
                                onDismissRequest()
                                it.onClick()
                            },
                            modifier = Modifier.testTag(it.testTag),
                            content = {
                                TextMenuItemContent(item = it)
                            },
                        )

                        is MenuItem.IconItem -> FlexibleDropdownMenuItem(
                            onClick = {
                                onDismissRequest()
                                it.onClick()
                            },
                            modifier = Modifier.testTag(it.testTag),
                            content = {
                                IconMenuItemContent(item = it)
                            },
                        )

                        is MenuItem.CheckableItem -> FlexibleDropdownMenuItem(
                            modifier = Modifier
                                .selectable(
                                    selected = it.isChecked,
                                    role = Role.Button,
                                    onClick = {
                                        onDismissRequest()
                                        it.onClick()
                                    },
                                )
                                .testTag(it.testTag),
                            onClick = {
                                onDismissRequest()
                                it.onClick()
                            },
                            content = {
                                CheckableMenuItemContent(item = it)
                            },
                        )
                    }
                }
            }

            is MenuItem.CustomMenuItem -> FlexibleDropdownMenuItem(
                onClick = {},
                content = {
                    it.content()
                },
            )

            is MenuItem.Divider -> Divider()
        }
    }
}

@Composable
private fun TextMenuItemContent(
    item: MenuItem.TextItem,
) {
    MenuItemText(item.text)
}

@Composable
private fun CheckableMenuItemContent(
    item: MenuItem.CheckableItem,
) {
    if (item.isChecked) {
        InfernoIcon(
            painter = painterResource(R.drawable.mozac_ic_checkmark_24),
            contentDescription = null,
        )
    } else {
        Spacer(modifier = Modifier.size(24.dp))
    }

    MenuItemText(item.text)
}

@Composable
private fun IconMenuItemContent(
    item: MenuItem.IconItem,
) {
    InfernoIcon(
        painter = painterResource(item.drawableRes),
        contentDescription = null,
    )

    MenuItemText(item.text)
}

@Composable
private fun FlexibleDropdownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    MaterialDropdownMenuItem(
        onClick = onClick,
        modifier = modifier.semantics(mergeDescendants = true) {},
        enabled = enabled,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        colors = MenuItemColors(
            textColor = LocalContext.current.infernoTheme().value.primaryTextColor,
            leadingIconColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            trailingIconColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            disabledTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
            disabledLeadingIconColor = LocalContext.current.infernoTheme().value.secondaryIconColor,
            disabledTrailingIconColor = LocalContext.current.infernoTheme().value.secondaryIconColor,
        ),
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ItemHorizontalSpaceBetween),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
            }
        },
    )
}

@Composable
private fun MenuItemText(text: Text) {
    InfernoText(
        text = text.value,
        fontColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        infernoStyle = InfernoTextStyle.Subtitle,
    )
}

/**
 * CompositionLocal that provides the current level of importance.
 */
private val LocalLevelColor = compositionLocalOf { Level.Default }
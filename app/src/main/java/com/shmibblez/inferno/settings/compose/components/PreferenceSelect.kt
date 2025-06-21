package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PreferenceSelect(
    text: String,
    description: String? = null,
    enabled: Boolean = true,
    selectedMenuItem: T,
    menuItems: List<T>,
    mapToTitle: (T) -> String,
    preferenceLeadingIcon: (@Composable () -> Unit)? = null,
    selectedLeadingIcon: (@Composable (T) -> Unit)? = null,
    menuItemLeadingIcon: (@Composable (T) -> Unit)? = null,
    menuItemTrailingIcon: (@Composable (T) -> Unit)? = null,
    onSelectMenuItem: (menuItem: T) -> Unit,
    additionalMenuItems: List<@Composable () -> Unit>? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
            ),
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // leading icon (if set)
        preferenceLeadingIcon?.invoke()

        Column(
            modifier = Modifier.weight(2F),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // title
            InfernoText(
                text = text,
                modifier = Modifier.alpha(
                    when (enabled) {
                        true -> 1F
                        false -> 0.75F
                    }
                ),
            )
            // description
            if (description != null) {
                InfernoText(
                    text = description,
                    infernoStyle = InfernoTextStyle.SmallSecondary,
                    modifier = Modifier.alpha(
                        when (enabled) {
                            true -> 0.75F
                            false -> 0.5F
                        }
                    ),
                    fontSize = 12.sp,
                )
            }
        }

        // dropdown menu
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1F),
        ) {
            InfernoOutlinedTextField(
                value = mapToTitle.invoke(selectedMenuItem),
                onValueChange = { },
                modifier = Modifier.menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable, enabled = enabled
                ),
                readOnly = true,
                label = null,
                leadingIcon = when (selectedLeadingIcon != null) {
                    true -> {
                        { selectedLeadingIcon.invoke(selectedMenuItem) }
                    }

                    false -> null
                },
                trailingIcon = {
                    InfernoIcon(
                        painter = when (expanded) {
                            true -> painterResource(R.drawable.ic_arrow_drop_up_24)
                            false -> painterResource(R.drawable.ic_arrow_drop_down_24)
                        },
                        contentDescription = "",
                    )
                },
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
            ) {
                // show menu items
                menuItems.map { it to mapToTitle.invoke(it) }.forEach { (item, name) ->
                        DropdownMenuItem(
                            text = { InfernoText(name) },
                            onClick = {
                                onSelectMenuItem.invoke(item)
                                expanded = false
                            },
                            leadingIcon = when (menuItemLeadingIcon != null) {
                                true -> {
                                    { menuItemLeadingIcon.invoke(item) }
                                }

                                false -> null
                            },
                            trailingIcon = when (menuItemTrailingIcon != null) {
                                true -> {
                                    { menuItemTrailingIcon.invoke(item) }
                                }

                                false -> null
                            },
                            colors = MenuItemColors(
                                textColor = LocalContext.current.infernoTheme().value.primaryTextColor,
                                leadingIconColor = LocalContext.current.infernoTheme().value.primaryIconColor,
                                trailingIconColor = LocalContext.current.infernoTheme().value.primaryIconColor,
                                disabledTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
                                disabledLeadingIconColor = LocalContext.current.infernoTheme().value.secondaryIconColor,
                                disabledTrailingIconColor = LocalContext.current.infernoTheme().value.secondaryIconColor,
                            ),
                        )
                    }
                // show additional menu items
                additionalMenuItems?.forEach { it.invoke() }
            }
        }
    }
}
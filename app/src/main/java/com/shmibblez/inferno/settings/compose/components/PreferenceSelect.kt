package com.shmibblez.inferno.settings.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PreferenceSelect(
    text: String,
    description: String? = null,
    enabled: Boolean = true,
    initiallySelectedMenuItem: String,
    menuItems: List<T>,
    mapToTitle: (T) -> String,
    onSelectMenuItem: (menuItem: T) -> Unit,
    additionalMenuItems: List<@Composable () -> Unit>? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(initiallySelectedMenuItem) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PreferenceConstants.PREFERENCE_VERTICAL_PADDING,
            ),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // title
            InfernoText(
                text = text,
                modifier = Modifier
                    .alpha(
                        when (enabled) {
                            true -> 1F
                            false -> 0.75F
                        }
                    ),
                fontColor = Color.White, // todo: theme
            )
            // description
            if (description != null) {
                InfernoText(
                    text = description,
                    modifier = Modifier
                        .alpha(
                            when (enabled) {
                                true -> 0.75F
                                false -> 0.5F
                            }
                        ),
                    fontSize = 12.sp,
                    fontColor = Color.DarkGray, // todo: theme
                )
            }
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = initiallySelectedMenuItem,
                onValueChange = { selectedItem = it },
                modifier = Modifier.menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                ),
                readOnly = true,
                label = null,
                trailingIcon = {
                    Icon(
                        painter = when (expanded) {
                            true -> painterResource(R.drawable.ic_arrow_drop_up_24)
                            false -> painterResource(R.drawable.ic_arrow_drop_down_24)
                        },
                        contentDescription = "",
                        tint = Color.White, // todo: theme
                    )
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // show menu items
                menuItems.map { it to mapToTitle.invoke(it) }.forEach { (item, name) ->
                    DropdownMenuItem(
                        text = { InfernoText(name) },
                        onClick = { onSelectMenuItem.invoke(item) }
                    )
                }
                // show additional menu items
                additionalMenuItems?.forEach { it.invoke() }
            }
        }
    }
}
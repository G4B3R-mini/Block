package com.shmibblez.inferno.settings.toolbar

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.tabs.tabstray.TabList
import com.shmibblez.inferno.tabstray.browser.compose.DragItemContainer
import com.shmibblez.inferno.tabstray.browser.compose.createListReorderState
import com.shmibblez.inferno.toolbar.ToToolbarIcon


private const val MAX_TOOLBAR_ITEMS = 7;

/** todo: for debugging, reference implementation in [TabList] */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceToolbarItems(
    modifier:Modifier = Modifier,
    topPreferences: List<@Composable () -> Unit>,
    selectedItems: List<InfernoSettings.ToolbarItem>,
    remainingItems: List<InfernoSettings.ToolbarItem>,
    onSelectedItemsChanged: (List<InfernoSettings.ToolbarItem>) -> Unit,
) {
    val context = LocalContext.current

    val state = rememberLazyListState()
    val reorderState = createListReorderState(
        listState = state,
        onMove = { from, to ->
            val steps = to.index - from.index
            val key = from.key as InfernoSettings.ToolbarItem
            val fromIndex = from.index - topPreferences.size
            val toIndex = fromIndex + steps
            when {
                steps > 0 -> {
                    val newList = selectedItems.toMutableList()
                    newList.removeAt(fromIndex)
                    newList.add(toIndex, key)
                    onSelectedItemsChanged(newList.toList())
                }

                steps < 0 -> {
                    val newList = selectedItems.toMutableList()
                    newList.removeAt(fromIndex)
                    newList.add(toIndex, key)
                    onSelectedItemsChanged(newList.toList())
                }

                else -> {/* steps is 0, nothing */
                }
            }
        },
        onLongPress = { },
        onExitLongPress = {},
        ignoredItems = remainingItems + (0..topPreferences.lastIndex).toList(),
    )

    fun onSelected(item: InfernoSettings.ToolbarItem) {
        if (selectedItems.size <= MAX_TOOLBAR_ITEMS) {
            onSelectedItemsChanged.invoke(selectedItems + item)
        } else {
            Toast.makeText(
                context, "Max $MAX_TOOLBAR_ITEMS items fit in the toolbar.", Toast.LENGTH_LONG
            ).show() // todo: string res for toast
        }
    }

    fun onUnselected(item: InfernoSettings.ToolbarItem) {
        // do not remove items that are required
        if (item.isRequired()) return
        onSelectedItemsChanged.invoke(selectedItems - item)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        itemsIndexed(topPreferences, key = { index, _ -> index }) { _, item ->
            item.invoke()
        }

        itemsIndexed(selectedItems, key = { _, item -> item }) { index, item ->
            DragItemContainer(
                state = reorderState,
                key = item,
                position = index + topPreferences.size,
            ) {
                ToolbarItem(
                    item = item,
                    selected = true,
                    onSelected = {},
                    onUnselected = ::onUnselected,
                )
            }
        }
        items(remainingItems, key = { it }) {
            ToolbarItem(
                item = it,
                selected = false,
                onSelected = ::onSelected,
                onUnselected = {},
            )
        }
    }
}


@Composable
private fun ToolbarItem(
    item: InfernoSettings.ToolbarItem,
    selected: Boolean,
    onSelected: (InfernoSettings.ToolbarItem) -> Unit,
    onUnselected: (InfernoSettings.ToolbarItem) -> Unit,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
    ) {
        // drag handle, only show if selected
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_drag_handle_24),
                contentDescription = "",
                modifier = Modifier.size(12.dp),
                tint = Color.White, // todo: theme
            )
        }

        // item icon
        item.ToToolbarIcon()

        // toolbar item name
        InfernoText(
            text = item.toPrefString(context),
            modifier = Modifier.weight(1F),
            fontColor = Color.White, // todo: theme
        )

        // selected checkbox
        InfernoCheckbox(
            checked = selected,
            enabled = item.isRequired(),
            onCheckedChange = {
                when (selected) {
                    true -> onUnselected.invoke(item)
                    false -> onSelected.invoke(item)
                }
            },
        )
    }
}

private fun InfernoSettings.ToolbarItem.isRequired(): Boolean {
    return when (this) {
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI -> true
        else -> false
    }
}

private fun InfernoSettings.ToolbarItem.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SETTINGS -> context.getString(R.string.settings)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN -> "Address bar" // todo: string res - context.getString(R.string.)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI -> "Mini address bar" // todo: string res - context.getString(R.string.)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK -> context.getString(R.string.browser_menu_back)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD -> context.getString(R.string.browser_menu_forward)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD -> context.getString(R.string.browser_menu_refresh)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_HISTORY -> context.getString(R.string.preferences_sync_history)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP -> context.getString(R.string.browser_menu_desktop_site)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FIND_IN_PAGE -> context.getString(R.string.browser_menu_find_in_page)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW -> context.getString(R.string.browser_menu_turn_on_reader_view)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PRIVATE_MODE -> context.getString(R.string.content_description_private_browsing_button)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY -> "Show tab tray" // todo: string res - context.getString(R.string.)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE -> context.getString(R.string.share_header_2)
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU -> context.getString(R.string.content_description_menu)
        InfernoSettings.ToolbarItem.UNRECOGNIZED -> ""
    }
}
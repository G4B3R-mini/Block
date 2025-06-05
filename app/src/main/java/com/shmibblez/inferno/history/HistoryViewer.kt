package com.shmibblez.inferno.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.Favicon
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.library.history.History
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryViewer(
    state: HistoryViewerState,
    modifier: Modifier,
    onOpenHistoryItem: (History) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = state.isRefreshing,
        onRefresh = { state.refreshList() },
        modifier = modifier,
        contentAlignment = Alignment.Center,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
//                modifier = ,
                containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                color = LocalContext.current.infernoTheme().value.primaryActionColor,
//                threshold = ,
            )
        },
    ) {
        LazyColumn(
//        modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.visibleItems ?: emptyList()) { item ->
                when (item) {
                    is History.Regular -> {
                        HistoryRegularItem(item = item,
                            onOpen = onOpenHistoryItem,
                            selected = state.selectedItems?.contains(item),
                            onSelect = { state.selectRegularItem(it) },
                            onUnselect = { state.unselectRegularItem(it) },
                            onDelete = { state.deleteItem(it) })
                    }

                    is History.Metadata -> {
                        HistoryMetadataItem(item = item,
                            onOpen = onOpenHistoryItem,
                            selected = state.selectedItems?.contains(item),
                            onSelect = { state.selectMetadataItem(it) },
                            onUnselect = { state.unselectMetadataItem(it) },
                            onDelete = { state.deleteItem(it) })
                    }

                    is History.Group -> {
                        HistoryGroupItem(item = item,
                            onOpenSubItem = onOpenHistoryItem,
                            selectedItems = state.selectedItems,
                            onSelectGroup = { state.selectGroupItem(it) },
                            onUnselectGroup = { state.unselectGroupItem(it) },
                            onSelectSubItem = { group, subItem ->
                                state.selectGroupSubItem(group, subItem)
                            },
                            onUnselectSubItem = { group, subItem ->
                                state.unselectGroupSubItem(group, subItem)
                            },
                            onDeleteGroup = { state.deleteItem(it) },
                            onDeleteSubItem = { state.deleteItem(it) })
                    }
                }
            }
        }
    }
}

/**
 * @param selected if not in selection mode, null; if in selection mode, true or false
 * @param expanded if not expandable, null; if expandable, true or false
 */
@Composable
private fun <T : History> ItemTemplate(
    item: T,
    onOpen: (History) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    leadingIcon: @Composable () -> Unit,
    onDelete: (T) -> Unit,
    selected: Boolean? = null,
    onToggleSelected: (() -> Unit)?,
    expanded: Boolean? = null,
    onToggleExpanded: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.combinedClickable(
            onClick = {
                when (selected != null) {
                    // if in selection mode, toggle selected
                    true -> onToggleSelected?.invoke()
                    // if not in selection mode, open
                    false -> onOpen.invoke(item)
                }
            },
            onLongClick = onToggleSelected ?: {},
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // icon
        // if selected show circle with checkbox
        // if not selected, show favicon or group icon
        if (selected == true) {
            // selected checkbox
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        LocalContext.current.infernoTheme().value.primaryActionColor, CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                InfernoIcon(
                    painter = painterResource(R.drawable.ic_checkmark_24),
                    contentDescription = "",
                    modifier = Modifier.size(12.dp),
                )
            }
        } else {
            // icon
            leadingIcon.invoke()
        }

        // title and description
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start,
        ) {
            // title
            InfernoText(text = title, infernoStyle = InfernoTextStyle.Normal)
            // subtitle
            InfernoText(text = subtitle, infernoStyle = InfernoTextStyle.Subtitle)
        }

        // expand icon
        if (expanded != null) {
            InfernoIcon(
                painter = painterResource(
                    when (expanded) {
                        true -> R.drawable.ic_arrow_drop_up_24
                        false -> R.drawable.ic_arrow_drop_down_24
                    }
                ),
                contentDescription = stringResource(R.string.a11y_action_label_expand),
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onToggleExpanded!!),
            )
        }

        // delete icon
        if (selected == null) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_close_24),
                contentDescription = stringResource(R.string.history_delete_item),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onDelete.invoke(item) },
            )
        }
    }
}

@Composable
private fun HistoryGroupItem(
    item: History.Group,
    onOpenSubItem: (History.Metadata) -> Unit,
    selectedItems: Set<History>?,
    onSelectGroup: (History.Group) -> Unit,
    onUnselectGroup: (History.Group) -> Unit,
    onSelectSubItem: (group: History.Group, subItem: History.Metadata) -> Unit,
    onUnselectSubItem: (group: History.Group, subItem: History.Metadata) -> Unit,
    onDeleteGroup: (History.Group) -> Unit,
    onDeleteSubItem: (History.Metadata) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val groupSelected = selectedItems?.contains(item) ?: false

    Column(modifier = Modifier.fillMaxWidth()) {
        // group item
        ItemTemplate(
            item = item,
            onOpen = {},
            title = item.title,
            subtitle = stringResource(R.string.history_search_group_sites_1, "${item.items.size}"),
            leadingIcon = {
                InfernoIcon(
                    painterResource(R.drawable.ic_multiple_tabs_24),
                    contentDescription = "",
                    modifier = Modifier.size(18.dp),
                )
            },
            onDelete = { onDeleteGroup.invoke(it) },
            selected = groupSelected,
            onToggleSelected = {
                when (groupSelected) {
                    true -> onUnselectGroup.invoke(item)
                    false -> onSelectGroup.invoke(item)
                }
            },
            expanded = expanded,
            onToggleExpanded = { expanded = !expanded },
        )

        // group sub items
        if (expanded) {
            for (subItem in item.items) {
                val subItemSelected = groupSelected || selectedItems?.contains(subItem) == true
                ItemTemplate(
                    item = subItem,
                    onOpen = { onOpenSubItem.invoke(subItem) },
                    modifier = Modifier.padding(start = 16.dp),
                    title = subItem.title,
                    subtitle = subItem.url,
                    leadingIcon = { Favicon(subItem.url, size = 18.dp) },
                    onDelete = { onDeleteSubItem.invoke(subItem) },
                    selected = subItemSelected,
                    onToggleSelected = {
                        when (subItemSelected) {
                            true -> onUnselectSubItem.invoke(item, subItem)
                            false -> onSelectSubItem.invoke(item, subItem)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryRegularItem(
    item: History.Regular,
    onOpen: (History) -> Unit,
    selected: Boolean?,
    onSelect: (History.Regular) -> Unit,
    onUnselect: (History.Regular) -> Unit,
    onDelete: (History.Regular) -> Unit,
) {
    ItemTemplate(
        item = item,
        onOpen = onOpen,
        title = item.title,
        subtitle = item.url,
        leadingIcon = { Favicon(item.url, size = 18.dp) },
        onDelete = onDelete,
        selected = selected,
        onToggleSelected = {
            when (selected) {
                true -> onUnselect.invoke(item)
                false -> onSelect.invoke(item)
                null -> {}
            }
        },
    )
}

@Composable
private fun HistoryMetadataItem(
    item: History.Metadata,
    onOpen: (History) -> Unit,
    selected: Boolean?,
    onSelect: (History.Metadata) -> Unit,
    onUnselect: (History.Metadata) -> Unit,
    onDelete: (History.Metadata) -> Unit,
) {
    ItemTemplate(
        item = item,
        onOpen = onOpen,
        title = item.title,
        subtitle = item.url,
        leadingIcon = { Favicon(item.url, size = 18.dp) },
        onDelete = onDelete,
        selected = selected,
        onToggleSelected = {
            when (selected) {
                true -> onUnselect.invoke(item)
                false -> onSelect.invoke(item)
                null -> {}
            }
        },
    )
}
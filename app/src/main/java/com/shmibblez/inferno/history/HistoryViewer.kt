package com.shmibblez.inferno.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

@Composable
internal fun HistoryViewer(
    state: HistoryViewerState,
    modifier: Modifier,
    onOpenHistoryItem: (History) -> Unit,
    onDeleteItem: (History) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.historyGroups ?: emptyList()) {
            var expanded by remember { mutableStateOf(false) }
            HistoryItem(
                item = it,
                modifier = Modifier.padding(horizontal = 8.dp),
                selectionMode = state.mode is HistoryViewerState.Mode.Selection,
                selectedItems = (state.mode as? HistoryViewerState.Mode.Selection)?.selectedItems
                    ?: emptySet(),
                expanded = if (it is History.Group) expanded else false,
                onToggleExpanded = if (it is History.Group) {
                    { expanded = !expanded }
                } else {
                    {}
                },
                pendingDeletionCount = state.pendingDeletion?.size ?: 0,
                onClick = { clicked ->
                    when (state.mode) {
                        // if normal, expand group or open in browser
                        HistoryViewerState.Mode.Normal -> {
                            if (clicked is History.Group) {
                                expanded = !expanded
                            } else {
                                onOpenHistoryItem.invoke(clicked)
                            }
                        }
                        // if selection, add individual items
                        is HistoryViewerState.Mode.Selection -> {
                            // add individual items
                            (state.mode as HistoryViewerState.Mode.Selection).let {
                                if (clicked is History.Group) {
                                    (state.mode as HistoryViewerState.Mode.Selection).selectedItems += clicked.items
                                } else {
                                    (state.mode as HistoryViewerState.Mode.Selection).selectedItems
                                }
                            }
                        }
                        // if syncing do nothing
                        HistoryViewerState.Mode.Syncing -> {} // no-op
                    }
                },
                onLongClick = { clicked ->
                    when (state.mode) {
                        // if normal, select item (or child items in case of group)
                        HistoryViewerState.Mode.Normal -> {
                            // add individual items
                            (state.mode as HistoryViewerState.Mode.Selection).let {
                                if (clicked is History.Group) {
                                    (state.mode as HistoryViewerState.Mode.Selection).selectedItems += clicked.items
                                } else {
                                    (state.mode as HistoryViewerState.Mode.Selection).selectedItems
                                }
                            }
                        }
                        // if selection, add individual items
                        is HistoryViewerState.Mode.Selection -> {
                            // add individual items
                            (state.mode as HistoryViewerState.Mode.Selection).let {
                                if (clicked is History.Group) {
                                    (state.mode as HistoryViewerState.Mode.Selection).selectedItems += clicked.items
                                } else {
                                    (state.mode as HistoryViewerState.Mode.Selection).selectedItems
                                }
                            }
                        }
                        // if syncing, do nothing
                        HistoryViewerState.Mode.Syncing -> {} // no-op
                    }
                },
                onDeletePressed = { deleted ->
                    onDeleteItem.invoke(deleted)
                },
            )
        }
    }
}

@Composable
private fun HistoryItem(
    item: History,
    isChild: Boolean = false,
    modifier: Modifier = Modifier,
    selectionMode: Boolean,
    selectedItems: Set<History>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    pendingDeletionCount: Int,
    onClick: (History) -> Unit,
    onLongClick: (History) -> Unit,
    onDeletePressed: (History) -> Unit,
) {
    Row(
        modifier = modifier.combinedClickable(
            onClick = { onClick.invoke(item) },
            onLongClick = { onLongClick.invoke(item) },
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // icon
        // if selected show circle with checkbox
        // if not selected, show favicon or group icon
        if (selectedItems.contains(item)) {
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
            when (item) {
                is History.Metadata -> Favicon(item.url, size = 18.dp)
                is History.Regular -> Favicon(item.url, size = 18.dp)
                is History.Group -> InfernoIcon(
                    painterResource(R.drawable.ic_multiple_tabs_24),
                    contentDescription = "",
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // title and description
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start,
        ) {
            InfernoText(text = item.title, infernoStyle = InfernoTextStyle.Normal)
            InfernoText(
                text = when (item) {
                    is History.Group -> "${item.items.size - pendingDeletionCount}"
                    is History.Metadata -> item.url
                    is History.Regular -> item.url
                },
                infernoStyle = InfernoTextStyle.Subtitle,
            )
        }

        // expand icon
        if (!isChild && item is History.Group) {
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
                    .clickable(onClick = onToggleExpanded),
            )
        }

        // delete button
        if (!selectionMode) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_close_24),
                contentDescription = stringResource(R.string.history_delete_item),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onDeletePressed.invoke(item) },
            )
        }

        // if expanded show content
        if (!isChild && item is History.Group && expanded) {
            Column {
                for (subItem in item.items) {
                    HistoryItem(
                        item = subItem,
                        isChild = true,
                        modifier = Modifier.padding(start = 16.dp),
                        selectionMode = selectionMode,
                        selectedItems = selectedItems,
                        expanded = false,
                        onToggleExpanded = {},
                        pendingDeletionCount = 0,
                        onClick = { onClick.invoke(subItem) },
                        onLongClick = { onLongClick.invoke(subItem) },
                        onDeletePressed = { onDeletePressed.invoke(subItem) },
                    )
                }
            }
        }
    }
}
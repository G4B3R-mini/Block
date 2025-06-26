package com.shmibblez.inferno.bookmarks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.library.bookmarks.ui.BookmarkItem
import com.shmibblez.inferno.toolbar.InfernoLoadingScreen
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.library.bookmarks.ui.isDesktopFolder

private val ICON_SIZE = 18.dp

/**
 * todo: edit dialogs, move bookmarks dialog, and delete / delete all
 *  could also include add bookmark option and remove close icon
 */
@Composable
fun BookmarksManager(
    state: BookmarksManagerState,
    modifier: Modifier = Modifier,
    loadUrl: (String) -> Unit,
    copy: (url: String) -> Unit,
    share: (url: String) -> Unit,
    openInNewTab: (url: String, private: Boolean) -> Unit,
    openAllInNewTab: (urls: List<String>, private: Boolean) -> Unit,
    onRequestEditBookmark: (Pair<BookmarkItem.Bookmark?, Boolean>) -> Unit,
    onRequestEditFolder: (Pair<BookmarkItem.Folder?, Boolean>) -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (state.loading) {
            InfernoLoadingScreen(modifier = Modifier.fillMaxSize())
        }

        // bookmarks page
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {
            // if empty show placeholder
            if (state.bookmarkItems.isEmpty()) {
                item {
                    NoBookmarksItem()
                }
            }

            // bookmarked items (folders & bookmarks)
            items(state.bookmarkItems) {
                when (it) {
                    is BookmarkItem.Bookmark -> {
                        var menuExpanded by remember { mutableStateOf(false) }
                        BookmarksItem(
                            item = it,
                            selected = state.isSelected(it),
                            onClick = {
                                when (state.mode) {
                                    BookmarksManagerState.Mode.Normal -> loadUrl.invoke(it.url)
                                    BookmarksManagerState.Mode.Select -> state.selectBookmark(it)
                                }
                            },
                            onLongClick = { state.selectBookmark(it) },
                            menuIcon = {
                                Box(modifier = Modifier.size(ICON_SIZE)) {
                                    InfernoIcon(
                                        painter = painterResource(R.drawable.ic_menu_24),
                                        contentDescription = "",
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                                    ) {
                                        // edit bookmark
                                        DropdownMenuItem(
                                            text = { InfernoText(stringResource(R.string.bookmark_menu_edit_button)) },
                                            onClick = { onRequestEditBookmark.invoke(it to false) },
                                        )
                                        // copy url
                                        DropdownMenuItem(
                                            text = { InfernoText(stringResource(R.string.bookmark_menu_copy_button)) },
                                            onClick = { copy.invoke(it.url) },
                                        )
                                        // share url
                                        DropdownMenuItem(
                                            text = { InfernoText(stringResource(R.string.bookmark_menu_share_button)) },
                                            onClick = { share.invoke(it.url) },
                                        )
                                        // open in new tab
                                        DropdownMenuItem(
                                            text = { InfernoText(stringResource(R.string.bookmark_menu_open_in_new_tab_button)) },
                                            onClick = { openInNewTab(it.url, false) },
                                        )
                                        // open in new private tab
                                        DropdownMenuItem(
                                            text = { InfernoText(stringResource(R.string.bookmark_menu_open_in_private_tab_button)) },
                                            onClick = { openInNewTab(it.url, true) },
                                        )
                                        // delete
                                        DropdownMenuItem(
                                            text = {
                                                InfernoText(
                                                    stringResource(R.string.bookmark_menu_delete_button),
                                                    fontColor = LocalContext.current.infernoTheme().value.errorColor,
                                                )
                                            },
                                            onClick = { state.deleteBookmark(it) },
                                        )
                                    }
                                }
                            },
                        )
                    }

                    is BookmarkItem.Folder -> {
                        var menuExpanded by remember { mutableStateOf(false) }
                        BookmarksFolderItem(
                            item = it,
                            selected = when (it.isDesktopFolder) {
                                true -> false
                                false -> state.isSelected(it)
                            },
                            onClick = {
                                when (state.mode) {
                                    BookmarksManagerState.Mode.Normal -> state.setRoot(it.guid)
                                    BookmarksManagerState.Mode.Select -> state.selectFolder(it)
                                }
                            },
                            onLongClick = {
                                // if item is desktop folder, ignored
                                state.selectFolder(it)
                            },
                            menuIcon = {
                                // only show menu if not desktop folder
                                if (!it.isDesktopFolder) {
                                    Box(modifier = Modifier.size(ICON_SIZE)) {
                                        InfernoIcon(
                                            painter = painterResource(R.drawable.ic_menu_24),
                                            contentDescription = "",
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false },
                                            containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                                        ) {
                                            // edit bookmark
                                            DropdownMenuItem(
                                                text = { InfernoText(stringResource(R.string.bookmark_menu_edit_button)) },
                                                onClick = {
                                                    onRequestEditFolder.invoke(it to false)
                                                },
                                            )
                                            // open all in normal tabs
                                            DropdownMenuItem(
                                                text = { InfernoText(stringResource(R.string.bookmark_menu_open_all_in_tabs_button)) },
                                                onClick = {
                                                    state.loadBookmarkUrls(
                                                        item = it,
                                                        onLoad = { urls ->
                                                            openAllInNewTab(urls, false)
                                                        },
                                                    )
                                                },
                                            )
                                            // open all in private tabs
                                            DropdownMenuItem(
                                                text = { InfernoText(stringResource(R.string.bookmark_menu_open_all_in_private_tabs_button)) },
                                                onClick = {
                                                    state.loadBookmarkUrls(
                                                        item = it,
                                                        onLoad = { urls ->
                                                            openAllInNewTab(urls, true)
                                                        },
                                                    )
                                                },
                                            )
                                            // delete bookmark
                                            DropdownMenuItem(
                                                text = { InfernoText(stringResource(R.string.bookmark_menu_delete_button)) },
                                                onClick = { state.deleteFolder(it) },
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoBookmarksItem() {
    // todo: use res depending on auth state when implemented
    InfernoText(stringResource(R.string.bookmark_empty_list_title))
}

@Composable
private fun BookmarksFolderItem(
    item: BookmarkItem.Folder,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    menuIcon: @Composable () -> Unit,
) {
    ListItem(
        title = item.title,
        selected = selected,
        leadingIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_folder_24),
                contentDescription = "",
                modifier = Modifier.size(ICON_SIZE),
            )
        },
        trailingIcon = menuIcon,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

@Composable
private fun BookmarksItem(
    item: BookmarkItem.Bookmark,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    menuIcon: @Composable () -> Unit,
) {
    ListItem(
        title = item.title,
        description = item.url,
        selected = selected,
        leadingIcon = {
            Favicon(
                item.url,
                size = ICON_SIZE,
            )
        },
        trailingIcon = menuIcon,
        onClick = onClick,
        onLongClick = onLongClick,
    )
}

@Composable
private fun ListItem(
    leadingIcon: @Composable () -> Unit,
    title: String,
    description: String? = null,
    selected: Boolean,
    trailingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // icon, if selected show checkmark
        when (selected) {
            true -> InfernoIcon(
                painter = painterResource(R.drawable.ic_checkmark_24),
                contentDescription = "",
                modifier = Modifier
                    .size(ICON_SIZE)
                    .background(
                        color = LocalContext.current.infernoTheme().value.primaryActionColor,
                        shape = CircleShape,
                    ),
            )

            false -> leadingIcon.invoke()
        }

        // title & description
        Column(
            modifier = Modifier.weight(1F),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfernoText(text = title, maxLines = 1)
            description?.let {
                InfernoText(text = it, maxLines = 1, infernoStyle = InfernoTextStyle.SmallSecondary)
            }
        }

        trailingIcon.invoke()
    }
}
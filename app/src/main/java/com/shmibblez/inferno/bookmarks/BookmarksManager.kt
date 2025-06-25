package com.shmibblez.inferno.bookmarks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private const val WARN_OPEN_ALL_SIZE = 15

/**
 * todo: dialogs, top bar (copy history page), and delete / delete all
 */
@Composable
fun BookmarksManager(
    state: BookmarksManagerState,
    goBack: () -> Unit,
    loadUrl: (String) -> Unit,
    copy: (url: String) -> Unit,
    share: (url: String) -> Unit,
    openInNewTab: (url: String, private: Boolean) -> Unit,
    openAllInNewTab: (urls: List<String>, private: Boolean) -> Unit,
) {
    // if cannot go back more, go to prev page
    BackHandler { if (!state.fuckGoBack()) goBack.invoke() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (state.loading) {
            InfernoLoadingScreen(modifier = Modifier.fillMaxSize())
        }

        // bookmarks page
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(state.bookmarkItems) {
                when (it) {
                    is BookmarkItem.Bookmark -> {
                        var menuExpanded by remember { mutableStateOf(false) }
                        BookmarksItem(
                            item = it,
                            onClick = {
                                when (state.type) {
                                    BookmarksManagerState.Type.Normal -> loadUrl.invoke(it.url)
                                    BookmarksManagerState.Type.Select -> state.select(it.guid)
                                }
                            },
                            onLongClick = { state.select(it.guid) },
                            menuIcon = {
                                Box(modifier = Modifier.size(18.dp)) {
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
                                                // todo: edit bookmark dialog
                                            },
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
                                            text = { InfernoText(stringResource(R.string.bookmark_menu_delete_button)) },
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
                            onClick = {
                                when (state.type) {
                                    BookmarksManagerState.Type.Normal -> state.setRoot(it.guid)
                                    BookmarksManagerState.Type.Select -> state.select(it.guid)
                                }
                            },
                            onLongClick = { state.select(it.guid) },
                            menuIcon = {
                                Box(modifier = Modifier.size(18.dp)) {
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
                                                // todo: edit bookmark dialog
                                            },
                                        )
                                        // open all in normal tabs
                                        DropdownMenuItem(
                                            text = { InfernoText(stringResource(R.string.bookmark_menu_open_all_in_tabs_button)) },
                                            onClick = {
                                                state.loadBookmarkUrls(
                                                    item = it,
                                                    onLoad = { urls ->
                                                        when (urls.size > WARN_OPEN_ALL_SIZE) {
                                                            true -> {
                                                                // if should warn, show warn dialog
                                                                // todo: show warn dialog
                                                            }

                                                            false -> {
                                                                // else open
                                                                openAllInNewTab(urls, false)
                                                            }
                                                        }
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
                                                        when (urls.size > WARN_OPEN_ALL_SIZE) {
                                                            true -> {
                                                                // if should warn, show warn dialog
                                                                // todo: show warn dialog
                                                            }

                                                            false -> {
                                                                // else open
                                                                openAllInNewTab(urls, true)
                                                            }
                                                        }
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
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarksFolderItem(
    item: BookmarkItem.Folder,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    menuIcon: @Composable () -> Unit,
) {
    // todo: folder icon, folder name, menu at end
    ListItem(
        title = item.title,
        leadingIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_folder_24),
                contentDescription = "",
                modifier = Modifier.size(18.dp),
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    menuIcon: @Composable () -> Unit,
) {
    // todo: bookmark icon, name & url, menu at end
    ListItem(
        title = item.title,
        description = item.url,
        leadingIcon = {
            Favicon(
                item.url,
                size = 18.dp,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon.invoke()

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
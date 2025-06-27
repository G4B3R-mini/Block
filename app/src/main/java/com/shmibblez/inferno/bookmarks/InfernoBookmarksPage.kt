package com.shmibblez.inferno.bookmarks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.UiConst
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.ext.shareTextList
import com.shmibblez.inferno.library.bookmarks.ui.BookmarkItem
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkInfo
import mozilla.components.support.ktx.android.content.share

private val ICON_SIZE = 18.dp
private const val WARN_OPEN_ALL_SIZE = 15

// todo: add delete warn dialog

@Composable
fun InfernoBookmarksPage(
    goBack: () -> Unit,
    onNavToBrowser: () -> Unit,
    initialGuid: String = BookmarkRoot.Root.id,
) {
    val context = LocalContext.current
    val components = context.components
    val managerState by rememberBookmarksManagerState(initialGuid = initialGuid)
    var showEditBookmarkDialogFor by remember {
        mutableStateOf<Pair<BookmarkItem.Bookmark?, Boolean>?>(null)
    }
    var showEditFolderDialogFor by remember {
        mutableStateOf<Pair<BookmarkItem.Folder?, Boolean>?>(null)
    }
    var showWarnDialogFor by remember { mutableStateOf<Pair<() -> Unit, Int>?>(null) }
    var showSelectFolderFor by remember {
        mutableStateOf<Pair<BookmarkItem.Folder, (BookmarkItem.Folder) -> Unit>?>(
            null
        )
    }

    fun backPressed() {
        if (!managerState.fuckGoBack()) goBack.invoke()
    }

    // if cannot go back more, go to prev page
    BackHandler { backPressed() }

    fun copy(url: String) {
        val urlClipData = ClipData.newPlainText(url, url)
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager?.let {
            it.setPrimaryClip(urlClipData)
            Toast.makeText(context, R.string.url_copied, Toast.LENGTH_SHORT).show()
        }
    }

    fun share(url: String) {
        managerState.exitSelect()
        context.share(url)
    }

    fun shareAll(urls: List<String>) {
        managerState.exitSelect()
        context.shareTextList(ArrayList(urls))
    }

    // if no items selected, exit selection mode
    LaunchedEffect(managerState.mode.asSelect()?.totalItemCount == 0) {
//        managerState.mode.asSelect()?.totalItemCount?.let {
//            if (it == 0) managerState.exitSelect()
//        }
    }

    fun openInNewTab(url: String, private: Boolean) {
        managerState.exitSelect()
        onNavToBrowser.invoke()
        components.newTab(url = url, private = private)
    }

    fun openAllInNewTab(urls: List<String>, private: Boolean, override: Boolean = false) {
        if (urls.size > WARN_OPEN_ALL_SIZE && !override) {
            showWarnDialogFor = { openAllInNewTab(urls, private, override = true) } to urls.size
            return
        }
        managerState.exitSelect()
        onNavToBrowser.invoke()
        for (url in urls) {
            components.newTab(url = url, private = private)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when (managerState.mode) {
                is BookmarksManagerState.Mode.Select -> {
                    val selectedCount =
                        (managerState.mode as BookmarksManagerState.Mode.Select).totalItemCount.toInt()
                    EditingTopBar(
                        onStopEditing = { managerState.exitSelect() },
                        title = stringResource(
                            R.string.bookmarks_multi_select_title,
                            selectedCount,
                        ),
                        actions = {
                            val selected =
                                managerState.mode.asSelect()?.selectedRoots ?: return@EditingTopBar
                            when {
                                selected.isEmpty() -> {} // no-op, automatically goes back to normal
                                selected.any { it is BookmarkItem.Folder } -> {
                                    // move action
                                    InfernoIcon(
                                        painter = painterResource(R.drawable.ic_folder_arrow_right_24),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                                            .size(ICON_SIZE)
                                            .clickable(
                                                onClick = {
                                                    showSelectFolderFor = managerState.folder to {
                                                        managerState.moveSelected(it.guid)
                                                    }
                                                },
                                            ),
                                    )
                                    // delete selected action
                                    InfernoIcon(
                                        painter = painterResource(R.drawable.ic_delete_24),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                                            .size(ICON_SIZE)
                                            .clickable(
                                                onClick = {
                                                    managerState.deleteSelected()
                                                },
                                            ),
                                    )
                                }

                                else -> {
                                    var menuExpanded by remember { mutableStateOf(false) }

                                    // edit action
                                    if (selected.size == 1) {
                                        InfernoIcon(
                                            painter = painterResource(R.drawable.ic_edit_24),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                                                .size(ICON_SIZE)
                                                .clickable(onClick = {
                                                    val bookmark =
                                                        selected.first() as BookmarkItem.Bookmark
                                                    showEditBookmarkDialogFor = bookmark to false
                                                }),
                                        )
                                    }
                                    // move action
                                    InfernoIcon(
                                        painter = painterResource(R.drawable.ic_folder_arrow_right_24),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                                            .size(ICON_SIZE)
                                            .clickable(
                                                onClick = {
                                                    showSelectFolderFor = managerState.folder to {
                                                        managerState.moveSelected(it.guid)
                                                    }
                                                },
                                            ),
                                    )
                                    // menu
                                    Box(
                                        modifier = Modifier
                                            .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                                            .size(ICON_SIZE)
                                            .clickable(onClick = { menuExpanded = true })
                                    ) {
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
                                            // select all
                                            DropdownMenuItem(
                                                text = { InfernoText(stringResource(R.string.create_collection_select_all)) },
                                                onClick = {
                                                    managerState.selectAll()
                                                    menuExpanded = false
                                                },
                                            )
                                            // copy url
                                            if (selected.size == 1) {
                                                val item = selected.first() as BookmarkItem.Bookmark
                                                DropdownMenuItem(
                                                    text = { InfernoText(stringResource(R.string.bookmark_menu_copy_button)) },
                                                    onClick = {
                                                        copy(item.url)
                                                        menuExpanded = false
                                                    },
                                                )
                                            }
                                            // share url
                                            DropdownMenuItem(
                                                text = { InfernoText(stringResource(R.string.bookmark_menu_share_button)) },
                                                onClick = {
                                                    shareAll(selected.map { (it as BookmarkItem.Bookmark).url })
                                                    menuExpanded = false
                                                },
                                            )
                                            // open in new tab
                                            DropdownMenuItem(
                                                text = { InfernoText(stringResource(R.string.bookmark_menu_open_in_new_tab_button)) },
                                                onClick = {
                                                    openAllInNewTab(
                                                        selected.map { (it as BookmarkItem.Bookmark).url },
                                                        false,
                                                    )
                                                    menuExpanded = false
                                                },
                                            )
                                            // open in new private tab
                                            DropdownMenuItem(
                                                text = { InfernoText(stringResource(R.string.bookmark_menu_open_in_private_tab_button)) },
                                                onClick = {
                                                    openAllInNewTab(
                                                        selected.map { (it as BookmarkItem.Bookmark).url },
                                                        true,
                                                    )
                                                    menuExpanded = false
                                                },
                                            )
                                            // delete
                                            DropdownMenuItem(
                                                text = {
                                                    InfernoText(
                                                        stringResource(R.string.bookmark_menu_delete_button),
                                                        fontColor = LocalContext.current.infernoTheme().value.errorColor,
                                                    )
                                                },
                                                onClick = {
                                                    managerState.deleteSelected()
                                                    menuExpanded = false
                                                },
                                            )
                                        }
                                    }
                                }
                            }

                            // only show add if not in root
                            if (managerState.root?.guid != BookmarkRoot.Root.id) {
                                InfernoIcon(
                                    painter = painterResource(R.drawable.ic_folder_new_24),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                                        .size(ICON_SIZE)
                                        .clickable(onClick = onNavToBrowser),
                                )
                            }
                        },
                    )
                }

                BookmarksManagerState.Mode.Normal -> NormalTopBar(goBack = ::backPressed,
                    title = managerState.folder.title,
                    actions = {
                        // todo: order by
//                        // filter
//                        InfernoIcon(
//                            painter = painterResource(R.drawable.ic_close_24),
//                            contentDescription = "",
//                            modifier = Modifier
//                                .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
//                                .size(ICON_SIZE)
//                                .clickable(onClick = onNavToBrowser),
//                        )

                        // new folder
                        // only show add if not in root
                        if (managerState.root?.guid != BookmarkRoot.Root.id) {
                            InfernoIcon(
                                painter = painterResource(R.drawable.ic_folder_new_24),
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                                    .size(ICON_SIZE)
                                    .clickable(onClick = {
                                        showEditFolderDialogFor = null to true
                                    }),
                            )
                        }

                        // exit (nav to browser)
                        InfernoIcon(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(end = UiConst.TOP_BAR_INTERNAL_PADDING)
                                .size(ICON_SIZE)
                                .clickable(onClick = onNavToBrowser),
                        )
                    })
            }
        },
        containerColor = context.infernoTheme().value.primaryBackgroundColor,
    ) { edgeInsets ->
        BookmarksManager(
            state = managerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(edgeInsets),
            loadUrl = {
                onNavToBrowser.invoke()
                components.newTab(url = it, private = false)
            },
            copy = ::copy,
            share = ::share,
            openInNewTab = ::openInNewTab,
            openAllInNewTab = ::openAllInNewTab,
            onRequestEditBookmark = { showEditBookmarkDialogFor = it },
            onRequestEditFolder = { showEditFolderDialogFor = it },
        )
    }

    // warn open all dialog
    if (showWarnDialogFor != null) {
        // show dialog if trying to open too many tabs, just to confirm
        OpenAllWarnDialog(
            onDismiss = { showWarnDialogFor = null },
            onConfirm = showWarnDialogFor!!.first,
            numberOfTabs = showWarnDialogFor!!.second,
        )
    }

    // modify bookmark dialog
    if (showEditBookmarkDialogFor != null) {
        val bookmark = showEditBookmarkDialogFor!!.first
        val create = showEditBookmarkDialogFor!!.second
        when (create) {
            true -> {
                ModifyBookmarkDialog(bookmarkGuid = null,
                    initialTitle = "",
                    initialUrl = "",
                    previewImageUrl = "",
                    initialParentFolder = managerState.folder,
                    onDismiss = { showEditBookmarkDialogFor = null },
                    onConfirm = { parentGuid, _, title, url, position ->
                        managerState.createBookmark(
                            parentGuid = parentGuid, position = position, title = title, url = url
                        )
                    })
            }

            false -> {
                ModifyBookmarkDialog(bookmarkGuid = bookmark!!.guid,
                    initialTitle = bookmark.title,
                    initialUrl = bookmark.url,
                    previewImageUrl = bookmark.previewImageUrl,
                    initialParentFolder = managerState.folder,
                    onDismiss = { showEditBookmarkDialogFor = null },
                    onConfirm = { parentGuid, guid, title, url, position ->
                        managerState.update(
                            guid, BookmarkInfo(
                                parentGuid = parentGuid,
                                position = position,
                                title = title,
                                url = url,
                            )
                        )
                    })
            }
        }
    }

    // modify folder dialog
    if (showEditFolderDialogFor != null) {
        val folder = showEditFolderDialogFor!!.first
        val create = showEditFolderDialogFor!!.second
        when (create) {
            true -> {
                ModifyFolderDialog(folderGuid = null,
                    initialTitle = "",
                    initialParentFolder = managerState.folder,
                    onDismiss = { showEditFolderDialogFor = null },
                    onConfirm = { parentGuid, _, title, position ->
                        managerState.createFolder(
                            parentGuid = parentGuid,
                            position = position,
                            title = title,
                        )
                    })
            }

            false -> {
                ModifyFolderDialog(folderGuid = folder!!.guid,
                    initialTitle = folder.title,
                    initialParentFolder = managerState.folder,
                    onDismiss = { showEditFolderDialogFor = null },
                    onConfirm = { parentGuid, guid, title, position ->
                        // todo: test
                        managerState.update(
                            guid, BookmarkInfo(
                                parentGuid = parentGuid,
                                position = position,
                                title = title,
                                url = null,
                            )
                        )
                    })
            }
        }
    }

    // select folder dialog
    if (showSelectFolderFor != null) {
        SelectParentFolderDialog(
            onDismiss = { showSelectFolderFor = null },
            initialSelection = showSelectFolderFor!!.first,
            onConfirmSelection = showSelectFolderFor!!.second,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopBar(
    goBack: () -> Unit,
    title: String,
    actions: @Composable RowScope.() -> Unit,
) {
    TopAppBar(
        title = {
            InfernoText(
                text = title,
                infernoStyle = InfernoTextStyle.Title,
                modifier = Modifier.padding(horizontal = UiConst.TOP_BAR_INTERNAL_PADDING)
            )
        },
        colors = TopAppBarColors(
            containerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
            scrolledContainerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
            navigationIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            titleContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            actionIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        ),
        navigationIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_back_button_24),
                contentDescription = "",
                modifier = Modifier
                    .padding(start = UiConst.TOP_BAR_INTERNAL_PADDING)
                    .size(ICON_SIZE)
                    .clickable(onClick = goBack),
            )
        },
        actions = actions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditingTopBar(
    onStopEditing: () -> Unit,
    title: String,
    actions: @Composable RowScope.() -> Unit,
) {
    TopAppBar(
        title = {
            InfernoText(
                text = title,
                infernoStyle = InfernoTextStyle.Title,
                modifier = Modifier.padding(horizontal = UiConst.TOP_BAR_INTERNAL_PADDING),
            )
        },
        colors = TopAppBarColors(
            containerColor = LocalContext.current.infernoTheme().value.primaryActionColor,
            scrolledContainerColor = LocalContext.current.infernoTheme().value.primaryActionColor,
            navigationIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            titleContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
            actionIconContentColor = LocalContext.current.infernoTheme().value.primaryIconColor,
        ),
        navigationIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_close_24),
                contentDescription = "",
                modifier = Modifier
                    .padding(start = UiConst.TOP_BAR_INTERNAL_PADDING)
                    .size(ICON_SIZE)
                    .clickable(onClick = onStopEditing),
            )
        },
        actions = actions,
    )
}
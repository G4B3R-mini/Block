package com.shmibblez.inferno.bookmarks

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.library.bookmarks.ui.BookmarkItem
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.library.bookmarks.ui.SelectFolderItem
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarksStorage

@Composable
internal fun SelectParentFolderDialog(
    onDismiss: () -> Unit,
    store: BookmarksStorage = LocalContext.current.components.core.bookmarksStorage,
    initialSelection: BookmarkItem.Folder,
    onConfirmSelection: (BookmarkItem.Folder) -> Unit,
) {
    val context = LocalContext.current
    var selectedFolder by remember { mutableStateOf(initialSelection) }
    var folders by remember { mutableStateOf<List<SelectFolderItem>?>(null) }

    LaunchedEffect(null) {
        folders = store.loadFolders(context)
        Log.d("SelectParentFolderDialo", "all folders: $folders")
    }

    InfernoDialog(
        onDismiss = onDismiss,
        onConfirm = {
            onConfirmSelection.invoke(selectedFolder)
            onDismiss.invoke()
        },
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = true,
        )
    ) {
        // dialog title
        item {
            InfernoText(
                text = stringResource(R.string.bookmark_select_folder_fragment_label),
                infernoStyle = InfernoTextStyle.Title,
            )
        }

        folders?.let {
            items(it) { folder ->
                SmolFolder(
                    title = folder.title,
                    selected = folder.guid == selectedFolder.guid,
                    indentation = folder.indentation,
                    onSelect = {
                        // only unselectable folder is root
                        if (folder.guid != BookmarkRoot.Root.id) {
                            selectedFolder = BookmarkItem.Folder(
                                title = folder.title,
                                guid = folder.guid,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SmolFolder(
    title: String,
    selected: Boolean,
    indentation: Int,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (16 * indentation).dp)
            .clickable(onClick = onSelect),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // folder icon, checkmark if selected
        when (selected) {
            true -> {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            color = LocalContext.current.infernoTheme().value.primaryActionColor,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_checkmark_24),
                        contentDescription = "",
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            false -> {
                InfernoIcon(
                    painter = painterResource(R.drawable.ic_folder_24),
                    contentDescription = "",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        // folder title
        InfernoText(text = title, maxLines = 1)
    }
}
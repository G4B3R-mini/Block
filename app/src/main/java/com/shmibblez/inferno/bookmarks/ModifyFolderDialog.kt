package com.shmibblez.inferno.bookmarks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.library.bookmarks.ui.BookmarkItem
import com.shmibblez.inferno.toolbar.InfernoLoadingScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkInfo
import mozilla.components.concept.storage.BookmarksStorage

/**
 * @param [folderGuid] if null, create new, if [String], edit
 */
@Composable
fun <G : String?> ModifyFolderDialog(
    folderGuid: G,
    onDismiss: () -> Unit,
    initialTitle: String,
    initialParentFolder: BookmarkItem.Folder? = null,
    create: Boolean,
    scope: CoroutineScope = rememberCoroutineScope(),
    store: BookmarksStorage = LocalContext.current.components.core.bookmarksStorage,
) {
    val context = LocalContext.current
    var parentFolder by remember { mutableStateOf<BookmarkItem.Folder?>(null) }

    LaunchedEffect(initialParentFolder) {
        parentFolder = initialParentFolder ?: store.getTree(BookmarkRoot.Mobile.id)?.let {
            BookmarkItem.Folder(
                title = it.resolveFolderTitle(context),
                guid = it.guid
            )
        }
    }

    when (parentFolder) {
        null -> {
            InfernoLoadingScreen(Modifier.fillMaxSize())
        }

        else -> {
            ModifyFolderDialog(
                folderGuid = folderGuid,
                initialTitle = initialTitle,
                initialParentFolder = parentFolder!!,
                onDismiss = onDismiss,
                onConfirm = { parentGuid, guid, title, position ->
                    scope.launch {
                        when (create) {
                            true -> {
                                store.addFolder(
                                    parentGuid = parentGuid ?: BookmarkRoot.Mobile.id,
                                    title = title,
                                    position = position,
                                )
                            }

                            false -> {
                                if (guid == null) throw IllegalArgumentException("when updating a folder, its guid cannot be null")
                                store.updateNode(
                                    guid = guid,
                                    info = BookmarkInfo(
                                        parentGuid = parentGuid ?: BookmarkRoot.Mobile.id,
                                        position = position,
                                        title = title,
                                        url = null,
                                    ),
                                )
                            }
                        }
                    }
                    onDismiss.invoke()
                },
            )
        }
    }
}

/**
 * @param [folderGuid] if null, create new, if [String], edit
 */
@Composable
internal fun <G : String?> ModifyFolderDialog(
    folderGuid: G,
    initialTitle: String,
    initialParentFolder: BookmarkItem.Folder,
    onDismiss: () -> Unit,
    onConfirm: (parentGuid: String?, guid: G, title: String, position: UInt?) -> Unit,
) {
    var title by remember { mutableStateOf(initialTitle) }
    var parentFolder by remember { mutableStateOf(initialParentFolder) }
    var showSelectFolderDialogFor by remember { mutableStateOf<BookmarkItem.Folder?>(null) }

    var titleError by remember { mutableStateOf(false) }

    /**
     * @return false if no errors, true if at least one error
     */
    fun anyErrors(
        titleCheck: String = title,
    ): Boolean {
        titleError = titleCheck.isEmpty()

        return titleError
    }

    InfernoDialog(
        onDismiss = onDismiss,
        onConfirm = {
            val t = title
            // if no errors, confirm & dismiss
            if (!anyErrors(t)) {
                onConfirm.invoke(parentFolder.guid, folderGuid, t, null)
                onDismiss.invoke()
            }
        },
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        confirmEnabled = !titleError,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = true,
        ),
    ) {
        // page title
        item {
            InfernoText(
                // if guid is null, create
                text = when (folderGuid == null) {
                    true -> stringResource(R.string.bookmark_add_folder)
                    false -> stringResource(R.string.edit_bookmark_folder_fragment_title)
                },
                infernoStyle = InfernoTextStyle.Title
            )
            // todo: add delete icon after title (in Row)
        }

        // title editor
        item {
            InfernoOutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    anyErrors()
                },
                label = {
                    InfernoText(text = stringResource(R.string.bookmark_name_label_normal_case))
                },
                isError = titleError,
                supportingText = {
                    // error text
                    if (titleError) {
                        InfernoText(
                            text = stringResource(R.string.credit_cards_name_on_card_validation_error_message_2),
                            infernoStyle = InfernoTextStyle.Error,
                        )
                    }
                }
            )
        }

        // save in label
        item {
            InfernoText(text = stringResource(R.string.bookmark_save_in_label))
        }

        // folder selector
        // when clicked select folder dialog is shown
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showSelectFolderDialogFor = parentFolder
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // folder icon
                InfernoIcon(
                    painter = painterResource(R.drawable.ic_folder_24),
                    contentDescription = "",
                    modifier = Modifier.size(18.dp),
                )

                // folder title
                InfernoText(text = parentFolder.title, maxLines = 1)
            }
        }
    }

    if (showSelectFolderDialogFor != null) {
        SelectParentFolderDialog(
            onDismiss = { showSelectFolderDialogFor = null },
            initialSelection = showSelectFolderDialogFor!!,
            onConfirmSelection = { parentFolder = it },
        )
    }
}
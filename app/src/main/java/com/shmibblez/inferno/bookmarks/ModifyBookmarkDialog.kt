package com.shmibblez.inferno.bookmarks

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.library.bookmarks.ui.BookmarkItem
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.Favicon
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.pxToDp
import com.shmibblez.inferno.toolbar.InfernoLoadingScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkInfo
import mozilla.components.concept.storage.BookmarksStorage
import mozilla.components.support.utils.WebURLFinder.Companion.isValidWebURL

/**
 * @param [bookmarkGuid] if null, create new, if [String], edit
 */
@Composable
fun <G : String?> ModifyBookmarkDialog(
    bookmarkGuid: G,
    onDismiss: () -> Unit,
    initialTitle: String,
    initialUrl: String,
    initialParentFolder: BookmarkItem.Folder? = null,
    create: Boolean,
    scope: CoroutineScope,
    store: BookmarksStorage = LocalContext.current.components.core.bookmarksStorage,
) {
    val context = LocalContext.current
    var parentFolder by remember { mutableStateOf<BookmarkItem.Folder?>(null) }

    LaunchedEffect(initialParentFolder) {
        parentFolder = initialParentFolder ?: store.getTree(BookmarkRoot.Root.id)?.let {
            BookmarkItem.Folder(
                title = it.resolveFolderTitle(context), guid = it.guid
            )
        }
    }

    when (parentFolder) {
        null -> {
            InfernoDialog(
                onDismiss = onDismiss,
                onConfirm = null,
            ) {
                item {
                    InfernoLoadingScreen(Modifier.fillMaxSize())
                }
            }
        }

        else -> {
            ModifyBookmarkDialog(
                bookmarkGuid = bookmarkGuid,
                initialTitle = initialTitle,
                initialUrl = initialUrl,
                previewImageUrl = initialUrl,
                initialParentFolder = parentFolder!!,
                onDismiss = onDismiss,
                onConfirm = { parentGuid, guid, title, url, position ->
                    Log.d(
                        "ModifyBookmarkDialog",
                        "onConfirm, begin save"
                    )
                    scope.launch {
                        when (create) {
                            true -> {
                                Log.d(
                                    "ModifyBookmarkDialog",
                                    "onConfirm, create item"
                                )
                                val res = store.addItem(
                                    parentGuid = parentGuid ?: BookmarkRoot.Mobile.id,
                                    url = url,
                                    title = title,
                                    position = position,
                                )
                                Log.d(
                                    "ModifyBookmarkDialog",
                                    "onConfirm, save success, new guid: $res"
                                )
                            }

                            false -> {
                                if (guid == null) throw IllegalArgumentException("when updating a bookmark, its guid cannot be null")
                                store.updateNode(
                                    guid = guid,
                                    info = BookmarkInfo(
                                        parentGuid = parentGuid ?: BookmarkRoot.Mobile.id,
                                        position = position,
                                        title = title,
                                        url = url,
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
 * @param [bookmarkGuid] if null, create new, if [String], edit
 */
@Composable
internal fun <G : String?> ModifyBookmarkDialog(
    bookmarkGuid: G,
    initialTitle: String,
    initialUrl: String,
    previewImageUrl: String,
    initialParentFolder: BookmarkItem.Folder,
    onDismiss: () -> Unit,
    onConfirm: (parentGuid: String?, guid: G, title: String, url: String, position: UInt?) -> Unit,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTitle) }
    var url by remember { mutableStateOf(initialUrl) }
    var parentFolder by remember { mutableStateOf(initialParentFolder) }
    var showSelectFolderDialogFor by remember { mutableStateOf<BookmarkItem.Folder?>(null) }

    var titleError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }
    var faviconSize by remember { mutableStateOf(0.dp) }


    /**
     * @return false if no errors, true if at least one error
     */
    fun anyErrors(
        titleCheck: String = title,
        urlCheck: String = url,
    ): Boolean {
        titleError = titleCheck.isEmpty()
        urlError = !urlCheck.isValidWebURL()

        return titleError || urlError
    }

    InfernoDialog(
        modifier = Modifier.onSizeChanged {
            faviconSize = it.width.pxToDp(context)
        },
        onDismiss = onDismiss,
        onConfirm = {
            val t = title
            val u = url
            // if no errors, confirm & dismiss
            if (!anyErrors(t, u)) {
                onConfirm.invoke(parentFolder.guid, bookmarkGuid, t, u, null)
                onDismiss.invoke()
            }
        },
        confirmEnabled = !titleError && !urlError,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = true,
        ),
        backgroundContent = {
            // bookmark image
            Box(
                modifier = Modifier
                    .size(faviconSize)
                    .alpha(0.5F)
            ) {
                Favicon(
                    url = previewImageUrl,
                    size = faviconSize,
                )
            }
        }
    ) {
        // page title
        item {
            InfernoText(
                // if guid is null, create
                text = when (bookmarkGuid == null) {
                    true -> stringResource(R.string.bookmark_save_in_label)
                    false -> stringResource(R.string.edit_bookmark_fragment_title)
                }, infernoStyle = InfernoTextStyle.Title
            )
            // todo: add delete icon after title (in Row)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.Top,
            ) {
//                // bookmark image
//                Favicon(
//                    url = previewImageUrl,
//                    size = 64.dp,
//                )
                // title & url editors
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    // title editor
                    InfernoOutlinedTextField(value = title, onValueChange = {
                        title = it
                        anyErrors()
                    }, label = {
                        InfernoText(text = stringResource(R.string.bookmark_name_label_normal_case))
                    }, isError = titleError, supportingText = {
                        // error text
                        if (titleError) {
                            InfernoText(
                                text = stringResource(R.string.credit_cards_name_on_card_validation_error_message_2),
                                infernoStyle = InfernoTextStyle.Error,
                            )
                        }
                    })
                    // url editor
                    InfernoOutlinedTextField(
                        value = url,
                        onValueChange = {
                            url = it
                            anyErrors()
                        },
                        label = {
                            InfernoText(text = stringResource(R.string.bookmark_url_label))
                        },
                        isError = urlError,
                        supportingText = {
                            // error text
                            if (urlError) {
                                InfernoText(
                                    text = stringResource(R.string.top_sites_edit_dialog_url_error),
                                    infernoStyle = InfernoTextStyle.Error,
                                )
                            }
                        },
                    )
                }
            }
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
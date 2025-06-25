package com.shmibblez.inferno.bookmarks

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.biometric.BiometricPromptCallbackManager
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.history.ConsecutiveUniqueJobHandler
import com.shmibblez.inferno.library.bookmarks.composeRootTitles
import com.shmibblez.inferno.library.bookmarks.friendlyRootTitle
import com.shmibblez.inferno.library.bookmarks.ui.BookmarkItem
import com.shmibblez.inferno.library.bookmarks.ui.OpenTabsConfirmationDialogAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.concept.storage.BookmarksStorage
import mozilla.components.service.fxa.store.SyncStore
import mozilla.components.support.base.feature.LifecycleAwareFeature

@Composable
fun rememberBookmarksManagerState(
    initialGuid: String = BookmarkRoot.Root.id,
    context: Context = LocalContext.current,
    storage: BookmarksStorage = LocalContext.current.components.core.bookmarksStorage,
    scope: CoroutineScope = rememberCoroutineScope(),
    resolveFolderTitle: (BookmarkNode) -> String = {
        friendlyRootTitle(
            context = context,
            node = it,
            rootTitles = composeRootTitles(context),
        ) ?: ""
    },
): MutableState<BookmarksManagerState> {
    val state = remember {
        mutableStateOf(
            BookmarksManagerState(
                initialGuid = initialGuid,
                storage = storage,
                scope = scope,
                resolveFolderTitle = resolveFolderTitle,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

/**
 * todo: also create ActivityResultManager, similar to [BiometricPromptCallbackManager],
 *  - allow adding listeners
 *  - store in activity, update listeners with manager.onActivityResult() with result code etc.
 *  - check way to create launchers, might be error if created after activity onCreate(), if not,
 *    then add fun manager.createLauncher() with necessary params
 *  - check compatibility with moz FileManager and AndroidPhotoPicker, those are the main reason why
 *    this is needed
 */

/**
 * manages bookmarks
 * starts at initialGuid
 * - can go to parent with [BookmarksManagerState.fuckGoBack], if at root (can't nav up) returns false
 * - can go to child with guid provided to [BookmarksManagerState.setRoot]
 */
class BookmarksManagerState(
    private val initialGuid: String,
    private val storage: BookmarksStorage,
    private val scope: CoroutineScope,
    private val resolveFolderTitle: (BookmarkNode) -> String,
) : LifecycleAwareFeature {
    // todo: add states for selection and normal
    sealed interface Type {
        data object Normal : Type
        data object Select : Type {
            var selectedRoots by mutableStateOf(setOf<String>())
            var totalItemCount by mutableStateOf(0U)
        }

        fun asSelect(): Select? {
            return this as? Select
        }
    }

    var type by mutableStateOf<Type>(Type.Normal)
        private set

    val isSelection
        get() = type is Type.Select

    val isNormal
        get() = type is Type.Normal

    private enum class JobType {
        LOAD, SELECT, LOAD_URLS, DELETE_BOOKMARK, DELETE_FOLDER, DELETE_SELECTED,
    }

//    var guid by mutableStateOf(initialGuid)
//        private set

    var loading by mutableStateOf(false)
        private set

    var root by mutableStateOf<BookmarkNode?>(null)
        private set

    var folder by mutableStateOf(
        BookmarkItem.Folder(
            guid = initialGuid,
            title = "Loading...", //  todo: string res
        )
    )
        private set

    var bookmarkItems by mutableStateOf(listOf<BookmarkItem>())
        private set

    private val taskManager = ConsecutiveUniqueJobHandler<JobType>(scope)

    private suspend fun setRootSus(newGuid: String) {
        storage.getTree(newGuid)?.let { rootNode ->
            folder = BookmarkItem.Folder(
                guid = newGuid,
                title = resolveFolderTitle.invoke(rootNode),
            )
            bookmarkItems = when (newGuid) {
                BookmarkRoot.Root.id -> {
                    rootNode.copy(
                        children = rootNode.children?.filterNot { it.guid == BookmarkRoot.Mobile.id }
                            ?.map { it.copy(title = resolveFolderTitle(it)) },
                    ).childItems()
                }

                BookmarkRoot.Mobile.id -> {
                    if (storage.hasDesktopBookmarks()) {
                        val desktopNode = storage.getTree(BookmarkRoot.Root.id)?.let {
                            it.copy(title = resolveFolderTitle(it))
                        }

                        val mobileRoot = rootNode.copy(
                            children = listOfNotNull(desktopNode) + rootNode.children.orEmpty(),
                        )
                        mobileRoot.childItems()
                    } else {
                        rootNode.childItems()
                    }
                }

                else -> rootNode.childItems()
            }
        }
    }

    fun setRoot(newGuid: String) {
        // reset to normal (removes selected)
        if (isSelection) {
            type = Type.Normal
        }
        taskManager.processTask(
            type = JobType.LOAD,
            task = { setRootSus(newGuid) },
            onBegin = {
                loading = true
            },
            onComplete = {
                loading = !it
                onFailure {
                    // todo: error handling, show retry button
                    folder = BookmarkItem.Folder(
                        guid = newGuid,
                        title = "Error...", //  todo: string res
                    )
                }
            },
        )
    }

    /**
     * @return If in [Type.Select] resets to [Type.Normal], else true if went back, false if cannot
     * go back
     */
    fun fuckGoBack(): Boolean {
        // reset to normal (removes selected)
        if (isSelection) {
            type = Type.Normal
            return true
        }
        val parent = root?.parentGuid ?: return false
        setRoot(parent)
        return true
    }

    private suspend fun selectSus(guid: String): Pair<String, UInt>? {
        if (isNormal) {
            type = Type.Select
        }
        val selected = (type as? Type.Select)?.selectedRoots ?: return null
        val count = storage.countBookmarksInTrees((selected + guid).toList())
        return guid to count
    }

    fun exitSelect() {
        type = Type.Normal
    }

    fun select(guid: String) {
        taskManager.processTask(type = JobType.SELECT, task = { selectSus(guid) }, onComplete = {
            onFailure {
                // todo
            }
            onSuccess { result ->
                result?.let { res ->
                    val (guidAdded, count) = res
                    (type as? Type.Select)?.let {
                        it.selectedRoots += guidAdded
                        it.totalItemCount = count
                    }
                }
            }
        })
    }

    fun loadBookmarkUrls(item: BookmarkItem.Folder, onLoad: (urls: List<String>) -> Unit) {
        taskManager.processTask(
            type = JobType.LOAD_URLS,
            task = {
                storage.getTree(item.guid)?.also {
                    it.children?.mapNotNull { child -> child.url }?.let(onLoad)
                }
            },
        )
    }

    fun deleteBookmark(item: BookmarkItem.Bookmark) {
        taskManager.processTask(
            type = JobType.DELETE_BOOKMARK,
            task = {
                // todo: delete bookmark
            },
        )
    }

    fun deleteFolder(item: BookmarkItem.Folder) {
        taskManager.processTask(
            type = JobType.DELETE_FOLDER,
            task = {
                // todo: delete folder
            },
        )
    }

    fun deleteSelected() {
        val selected = type.asSelect()?.selectedRoots
        if (selected.isNullOrEmpty()) {
            type = Type.Normal
            return
        }

        taskManager.processTask(
            type = JobType.DELETE_SELECTED,
            task = {
                // todo: get whole tree recursive, store all jobs in list, await all and set normal
            },
        )

    }

    override fun start() {
        setRoot(initialGuid)
    }

    override fun stop() {
        scope.cancel()
    }
}

private suspend fun BookmarksStorage.hasDesktopBookmarks(): Boolean {
    return countBookmarksInTrees(
        listOf(BookmarkRoot.Menu.id, BookmarkRoot.Toolbar.id, BookmarkRoot.Unfiled.id),
    ) > 0u
}


private fun BookmarkNode.childItems(): List<BookmarkItem> =
    this.children?.sortedByDescending { it.lastModified }?.mapNotNull { node ->
        Result.runCatching {
            when (node.type) {
                BookmarkNodeType.ITEM -> BookmarkItem.Bookmark(
                    url = node.url!!,
                    title = node.title ?: node.url ?: "",
                    previewImageUrl = node.url!!,
                    guid = node.guid,
                )

                BookmarkNodeType.FOLDER -> BookmarkItem.Folder(
                    title = node.title!!,
                    guid = node.guid,
                )

                BookmarkNodeType.SEPARATOR -> null
            }
        }.getOrNull()
    } ?: listOf()

private class BookmarksSyncManager(
    private val syncStore: SyncStore,
    private val scope: CoroutineScope,
) {

}
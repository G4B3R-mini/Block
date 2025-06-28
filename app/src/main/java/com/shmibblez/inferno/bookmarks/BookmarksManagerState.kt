package com.shmibblez.inferno.bookmarks

import android.content.Context
import android.util.Log
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
import com.shmibblez.inferno.library.bookmarks.ui.SelectFolderItem
import com.shmibblez.inferno.library.bookmarks.ui.isDesktopFolder
import com.shmibblez.inferno.library.bookmarks.ui.title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarkInfo
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
 * todo: important, also create ActivityResultManager, similar to [BiometricPromptCallbackManager],
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
    sealed interface Mode {
        data object Normal : Mode
        data object Select : Mode {
            var selectedRoots by mutableStateOf(setOf<BookmarkItem>())
            var totalItemCount by mutableStateOf(0)
        }

        fun asSelect(): Select? {
            return this as? Select
        }
    }

    var mode by mutableStateOf<Mode>(Mode.Normal)
        private set

    val isSelection
        get() = mode is Mode.Select

    val isNormal
        get() = mode is Mode.Normal

    private enum class JobType {
        LOAD, SELECT, SELECT_ALL, LOAD_URLS, DELETE_BOOKMARK, DELETE_FOLDER, DELETE_SELECTED, UPDATE, CREATE_BOOKMARK, CREATE_FOLDER, MOVE_ITEMS,
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

    private suspend fun refreshItemsSus() {
        root?.guid?.let { setRootSus(it) }
    }

    private suspend fun setRootSus(newGuid: String) {
        storage.getTree(newGuid)?.let { rootNode ->
            root = rootNode
            folder = BookmarkItem.Folder(
                guid = newGuid,
                title = resolveFolderTitle.invoke(rootNode),
            )
            bookmarkItems = when (newGuid) {
//                BookmarkRoot.Root.id -> {
//                    rootNode.copy(
//                        children = rootNode.children?.filterNot { it.guid == BookmarkRoot.Mobile.id }
//                            ?.map { it.copy(title = resolveFolderTitle(it)) },
//                    ).childItems()
//                }

//                BookmarkRoot.Mobile.id -> {
//                    if (storage.hasDesktopBookmarks()) {
//                        val desktopNode = storage.getTree(BookmarkRoot.Root.id)?.let {
//                            it.copy(title = resolveFolderTitle(it))
//                        }
//
//                        val mobileRoot = rootNode.copy(
//                            children = listOfNotNull(desktopNode) + rootNode.children.orEmpty(),
//                        )
//                        mobileRoot.childItems()
//                    } else {
//                        rootNode.childItems()
//                    }
//                }

                else -> rootNode.childItems()
            }
        }
        Log.d(
            "BookmarksManagerState",
            "setRootSus(), items loaded: $bookmarkItems"
        )
    }

    fun setRoot(newGuid: String) {
        // reset to normal (removes selected)
        if (isSelection) {
            mode = Mode.Normal
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
                    // todo: handle error, show retry button
                    folder = BookmarkItem.Folder(
                        guid = newGuid,
                        title = "Error...", //  todo: string res
                    )
                }
            },
        )
    }

    /**
     * @return If in [Mode.Select] resets to [Mode.Normal], else true if went back, false if cannot
     * go back
     */
    fun fuckGoBack(): Boolean {
        // reset to normal (removes selected)
        if (isSelection) {
            mode = Mode.Normal
            return true
        }
        // if at root return false
        val parent = root?.parentGuid ?: return false
        setRoot(parent)
        return true
    }

    /**
     * if in selection mode, go back to normal
     */
    fun exitSelect() {
        if (isSelection) {
            mode = Mode.Normal
        }
    }

    fun isSelected(item: BookmarkItem): Boolean {
        val selected = mode.asSelect()?.selectedRoots ?: return false
        return selected.contains(item)
    }

    /**
     * @param item item to select, if [item] is desktop folder, do nothing
     */
    fun selectItem(item: BookmarkItem) {
        // cannot select desktop folders
        if ((item as? BookmarkItem.Folder)?.isDesktopFolder == true) return
        // if not in selection mode init
        if (!isSelection) mode = Mode.Select
        val selectedItems = (mode as Mode.Select).selectedRoots
        val isSelected = isSelected(item)
        taskManager.processTask(
            type = JobType.SELECT,
            task = {
                // if not in selection mode by the time
                // this task is processed, return
                if (isNormal) return@processTask null
                val itemsToCount = when (isSelected) {
                    true -> (selectedItems - item).map { it.guid }
                    false -> (selectedItems + item).map { it.guid }
                }
                val count =
                    storage.countBookmarksInTrees(itemsToCount).toInt()
                return@processTask item to count
            },
            onComplete = {
                onFailure {
                    // todo: handle error
                }
                onSuccess { result ->
                    result?.let { res ->
                        Log.d(
                            "BookmarksManagerState",
                            "item selected, before select, roots: ${(mode as Mode.Select).selectedRoots}, count: ${(mode as Mode.Select).totalItemCount} "
                        )
                        val (guidAdded, nestedItemCount) = res
                        if (!isSelection) return@onSuccess
                        when (isSelected) {
                            true -> (mode as Mode.Select).selectedRoots -= guidAdded
                            false -> (mode as Mode.Select).selectedRoots += guidAdded
                        }
                        (mode as Mode.Select).totalItemCount = (mode as Mode.Select).selectedRoots.size + nestedItemCount
                        Log.d(
                            "BookmarksManagerState",
                            "item selected, after select, roots: ${(mode as Mode.Select).selectedRoots}, count: ${(mode as Mode.Select).totalItemCount} "
                        )
                    }
                }
            },
        )
    }

    fun selectAll() {
        if (isSelection) {
            mode.asSelect()!!.selectedRoots = bookmarkItems.toSet()
        }

        // if not in select mode return
        if (!isSelection) return
        // guid when task requested
        val initialRootGuid = root?.guid
        // filter out desktop folders
        val selectableItems = bookmarkItems.mapNotNull { when ((it as? BookmarkItem.Folder)?.isDesktopFolder) {
            true -> null
            else -> it
        } } ?: return
        taskManager.processTask(
            type = JobType.SELECT_ALL,
            task = {
                // if not in selection mode or root guid changed by the time
                // this task is processed, return
                if (isNormal || root?.guid != initialRootGuid) return@processTask null
                val itemsToCount = selectableItems.map { it.guid }
                val count = storage.countBookmarksInTrees(itemsToCount).toInt()
                return@processTask selectableItems to count
            },
            onComplete = {
                onFailure {
                    // todo: handle error
                }
                onSuccess { result ->
                    result?.let { res ->
                        if (!isSelection) return@onSuccess

                        val (selectableItems, nestedItemCount) = res
                        (mode as Mode.Select).selectedRoots = selectableItems.toSet()
                        (mode as Mode.Select).totalItemCount = (mode as Mode.Select).selectedRoots.size + nestedItemCount
                        Log.d(
                            "BookmarksManagerState",
                            "item selected, after select, roots: ${(mode as Mode.Select).selectedRoots}, count: ${(mode as Mode.Select).totalItemCount} "
                        )
                    }
                }
            },
        )
    }

    fun loadBookmarkUrls(item: BookmarkItem.Folder, onLoad: (urls: List<String>) -> Unit) {
        taskManager.processTask(
            type = JobType.LOAD_URLS,
            task = {
                storage.getTree(item.guid)?.also {
                    it.children?.mapNotNull { child -> child.url }?.let(onLoad)
                }
            },
            onBegin = { loading = true },
            onComplete = { loading = !it },
        )
    }

    fun deleteBookmark(item: BookmarkItem.Bookmark) {
        taskManager.processTask(
            type = JobType.DELETE_BOOKMARK,
            task = {
                storage.deleteNode(item.guid)
                refreshItemsSus()
            },
        )
    }

    fun deleteFolder(item: BookmarkItem.Folder) {
        taskManager.processTask(
            type = JobType.DELETE_FOLDER,
            task = {
                storage.deleteNode(item.guid)
                refreshItemsSus()
            },
        )
    }

    fun deleteSelected() {
        val selected = mode.asSelect()?.selectedRoots
        if (selected.isNullOrEmpty()) {
            mode = Mode.Normal
            return
        }

        taskManager.processTask(
            type = JobType.DELETE_SELECTED,
            task = {
                val jobs = mutableListOf<Deferred<Boolean>>()
                for (item in selected) {
                    val job = scope.async { storage.deleteNode(item.guid) }
                    jobs.add(job)
                }
                jobs.awaitAll()
                refreshItemsSus()
                mode = Mode.Normal
            },
        )
    }

    fun update(guid: String, info: BookmarkInfo) {
        taskManager.processTask(
            type = JobType.UPDATE,
            task = {
                storage.updateNode(guid, info)
            },
        )
    }

    fun createBookmark(
        parentGuid: String?,
        url: String,
        title: String,
        position: UInt? = null,
    ) {
        taskManager.processTask(
            type = JobType.CREATE_BOOKMARK,
            task = {
                storage.addItem(
                    parentGuid = parentGuid ?: BookmarkRoot.Mobile.id,
                    url = url,
                    title = title,
                    position = position,
                )
                refreshItemsSus()
            },
        )
    }

    fun createFolder(
        parentGuid: String?,
        title: String,
        position: UInt? = null,
    ) {
        taskManager.processTask(
            type = JobType.CREATE_FOLDER,
            task = {
                storage.addFolder(
                    parentGuid = parentGuid ?: BookmarkRoot.Mobile.id,
                    title = title,
                    position = position,
                )
                refreshItemsSus()
            },
        )
    }

    fun moveSelected(destinationGuid: String) {
        mode.asSelect()?.selectedRoots?.let {
            taskManager.processTask(type = JobType.MOVE_ITEMS, task = {
                val jobs = mutableListOf<Deferred<Any?>>()
                for (item in it) {
                    val info = BookmarkInfo(
                        parentGuid = destinationGuid,
                        position = null,
                        title = item.title,
                        url = when (item) {
                            is BookmarkItem.Bookmark -> item.url
                            is BookmarkItem.Folder -> null
                        },
                    )
                    val job = scope.async { storage.updateNode(item.guid, info) }
                    jobs.add(job)
                }
                jobs.awaitAll()
                refreshItemsSus()
            })
        }
    }

    override fun start() {
        setRoot(initialGuid)
    }

    override fun stop() {
        scope.cancel()
    }
}

suspend fun BookmarksStorage.hasDesktopBookmarks(): Boolean {
    return countBookmarksInTrees(
        listOf(BookmarkRoot.Menu.id, BookmarkRoot.Toolbar.id, BookmarkRoot.Unfiled.id),
    ) > 0u
}


fun BookmarkNode.childItems(): List<BookmarkItem> =
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

suspend fun BookmarksStorage.loadFolders(context: Context): List<SelectFolderItem>? {
//    val folders = if (this.hasDesktopBookmarks()) {
       val folders = this.getTree(BookmarkRoot.Root.id, recursive = true)?.let { rootNode ->
            val excludingMobile = rootNode.children?.filterNot { it.guid == BookmarkRoot.Mobile.id }
            val desktopRoot = rootNode.copy(children = excludingMobile)
            rootNode.children?.find { it.guid == BookmarkRoot.Mobile.id }?.let {
                val newChildren = listOf(desktopRoot) + it.children.orEmpty()
                it.copy(children = newChildren)
            }?.let { collectFolders(context, it) }
        }
//    } else {
//        this.getTree(BookmarkRoot.Mobile.id, recursive = true)?.let { rootNode ->
//            collectFolders(context, rootNode)
//        }
//    }

    return folders
}

private fun collectFolders(
    context: Context,
    node: BookmarkNode,
    indentation: Int = 0,
    folders: MutableList<SelectFolderItem> = mutableListOf(),
): List<SelectFolderItem> {
    if (node.type == BookmarkNodeType.FOLDER) {
        folders.add(
            SelectFolderItem(
                indentation = indentation,
                folder = BookmarkItem.Folder(
                    guid = node.guid,
                    title = resolveFolderTitle(context, node),
                ),
            ),
        )

        node.children?.forEach { child ->
            folders.addAll(collectFolders(context, child, indentation + 1))
        }
    }

    return folders
}

fun BookmarkNode.resolveFolderTitle(context: Context): String {
    return friendlyRootTitle(
        context = context,
        node = this,
        rootTitles = composeRootTitles(context),
    ) ?: ""
}

private fun resolveFolderTitle(context: Context, node: BookmarkNode) = friendlyRootTitle(
    context = context,
    node = node,
    rootTitles = composeRootTitles(context),
) ?: ""


private class BookmarksSyncManager(
    private val syncStore: SyncStore,
    private val scope: CoroutineScope,
) {

}
package com.shmibblez.inferno.history

import android.annotation.SuppressLint
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
import com.shmibblez.inferno.components.history.HistoryDB
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.ext.shareTextList
import com.shmibblez.inferno.library.history.History
import com.shmibblez.inferno.library.history.HistoryView
import com.shmibblez.inferno.utils.Settings.Companion.SEARCH_GROUP_MINIMUM_SITES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import mozilla.components.browser.state.action.HistoryMetadataAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.concept.storage.HistoryMetadata
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.concept.storage.VisitType
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlin.tryGetHostFromUrl

/**
 * todo: this is wack
 *  look over [HistoryView] and use existing pager implementation
 *  (check if compatible with refresh, requires custom page size each time)
 */

/** handles jobs consecutively, ensuring only unique jobs are in queue */
class ConsecutiveUniqueJobHandler<T : Enum<T>>(
    val scope: CoroutineScope,
) {
    class ResultScope<out E : Throwable?, out R : Any?>(
        val isSuccess: Boolean,
        val isFailure: Boolean,
        val e: E?,
        val r: R?,
    ) {
        fun onSuccess(callback: (r: R) -> Unit) {
            if (isSuccess) callback.invoke(r!!)
        }

        fun onFailure(callback: (e: E) -> Unit) {
            if (isFailure) callback.invoke(e!!)
        }
    }

    /**
     * operation wrapper
     *
     * @param type type of operation
     * @param task task to complete
     */
    data class Op<T : Enum<T>, R : Any?>(
        val type: T,
        val task: suspend () -> R?,
        val onBegin: (() -> Unit)? = null,
        val onComplete: (ResultScope<Throwable, R>.(currentlyIdle: Boolean) -> Unit)? = null,
    ) {
        override fun equals(other: Any?): Boolean {
            return other != null && other is Op<*, *> && other.type == this.type
        }

        override fun hashCode(): Int {
//            var result = type.hashCode()
//            result = 31 * result + task.hashCode()
//            result = 31 * result + (onComplete?.hashCode() ?: 0)
//            return result
            return type.ordinal
        }

        fun invoke(
            scope: CoroutineScope,
            onFinish: () -> Unit,
            isIdle: () -> Boolean,
        ): Deferred<R?> {
            Log.d("CUJobHandler.Op", "invoke called, starting job")
            onBegin?.invoke()
            return scope.async {
                val r = task.runCatching { this.invoke() }
                onFinish.invoke()
                Log.d("CUJobHandler.Op", "invoke onFinish invoked, ending job")
                onComplete?.invoke(
                    ResultScope(
                        isSuccess = r.isSuccess,
                        isFailure = r.isFailure,
                        e = r.exceptionOrNull(),
                        r = r.getOrNull(),
                    ),
                    isIdle.invoke(),
                )
                r.getOrNull()
            }
        }
    }

    private var queue = emptyList<Any>()
    private var currentTask: Deferred<Any?>? = null

    /** true if no pending tasks & not currently busy */
    private val isIdle: Boolean
        get() = queue.isEmpty()

    private fun invokeNext() {
        Log.d("CUJobHandler", "invokeNext called")
        if (currentTask != null) return
        val op = queue.firstOrNull()?.let { it as Op<*, *> } ?: return
        Log.d("CUJobHandler", "invokeNext, isIdle: $isIdle")
        currentTask = op.invoke(
            scope = scope,
            onFinish = {
                Log.d(
                    "CUJobHandler.Op",
                    "onFinish start, queue: ${queue.map { (it as Op<*, *>).type }}, isIdle: $isIdle"
                )
                queue -= op; currentTask = null
                invokeNext()
                Log.d(
                    "CUJobHandler.Op",
                    "onFinish end, queue: ${queue.map { (it as Op<*, *>).type }}, isIdle: $isIdle"
                )
            },
            isIdle = { queue.isEmpty() },
        )
    }

    /**
     * @param onComplete called when finished loading, scope provides onSuccess{} and onFailure{}
     */
    fun <R : Any?> processTask(
//        op: Op<T, R>,
        type: T,
        task: suspend () -> R,
        onBegin: (() -> Unit)? = null,
        onComplete: (ResultScope<Throwable, R>.(currentlyIdle: Boolean) -> Unit)? = null,
    ) {
        Log.d("CUJobHandler", "processTask called")
        val op = Op(type, task, onBegin, onComplete)
        if (queue.contains(op)) return
        queue += op
        invokeNext()
    }
}


@Composable
internal fun rememberHistoryViewerState(
    browserStore: BrowserStore = LocalContext.current.components.core.store,
    historyStorage: PlacesHistoryStorage = LocalContext.current.components.core.historyStorage,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MutableState<HistoryManagerState> {
    val context = LocalContext.current
    val state = remember {
        mutableStateOf(
            HistoryManagerState(
                context = context,
                browserStore = browserStore,
                historyStorage = historyStorage,
                coroutineScope = coroutineScope,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

private const val NUMBER_OF_HISTORY_ITEMS = 25
private const val BUFFER_TIME = 15000 // 15 seconds in ms


internal class HistoryManagerState(
    private val context: Context,
    private val browserStore: BrowserStore,
    private val historyStorage: PlacesHistoryStorage,
    private val coroutineScope: CoroutineScope,
    private val numberOfItemsToLoad: Int = NUMBER_OF_HISTORY_ITEMS,
) : LifecycleAwareFeature {

    internal sealed interface Mode {
        fun isSelection() = this is Selection
        fun isNormal() = this is Normal
        fun isSyncing() = this is Syncing

        data object Normal : Mode
        data object Syncing : Mode
        class Selection(selectedItems: Set<History>) : Mode {
            // selected items only includes single items (Metadata or Regular)
            var selectedItems by mutableStateOf(selectedItems)
        }
    }

    /**
     * database vars and funs
     */

    internal enum class TaskType { LOAD_MORE, REFRESH, DELETE, }

    private val taskHandler = ConsecutiveUniqueJobHandler<TaskType>(coroutineScope)

    var isBusy by mutableStateOf(false)
        private set

    var noMoreItems by mutableStateOf(false)
        private set

    /**
     * Types of visits we currently do not display in the History UI.
     */
    private val excludedVisitTypes = listOf(
        VisitType.DOWNLOAD,
        VisitType.REDIRECT_PERMANENT,
        VisitType.REDIRECT_TEMPORARY,
        VisitType.RELOAD,
        VisitType.EMBED,
        VisitType.FRAMED_LINK,
    )

    /**
     * All types of visits that aren't redirects. This is used for fetching only redirecting visits
     * from the store so that we can filter them out.
     */
    private val notRedirectTypes = VisitType.entries.filterNot {
        it == VisitType.REDIRECT_PERMANENT || it == VisitType.REDIRECT_TEMPORARY
    }

    private var dbOffset = 0

    @Volatile
    private var historyGroups: List<HistoryDB.Group>? = null

    @SuppressLint("VisibleForTests")
    suspend fun getHistory(
        offset: Int,
        numberOfItems: Int,
    ): List<HistoryDB> {
        // We need to re-fetch all the history metadata if the offset resets back at 0
        // in the case of a pull to refresh.
        if (historyGroups == null || offset == 0) {
            historyGroups = historyStorage.getHistoryMetadataSince(Long.MIN_VALUE).asSequence()
                .sortedByDescending { it.createdAt }.filter { it.key.searchTerm != null }
                .groupBy { it.key.searchTerm!! }.map { (searchTerm, items) ->
                    HistoryDB.Group(
                        title = searchTerm,
                        visitedAt = items.first().createdAt,
                        items = items.map { it.toHistoryDBMetadata() },
                    )
                }.filter {
                    it.items.size >= SEARCH_GROUP_MINIMUM_SITES
                }.toList()
        }

        return getHistoryAndSearchGroups(offset, numberOfItems)
    }

    @Suppress("MagicNumber")
    private suspend fun getHistoryAndSearchGroups(
        offset: Int,
        numberOfItems: Int,
    ): List<HistoryDB> {
        val result = mutableListOf<HistoryDB>()
        var history: List<HistoryDB.Regular> = historyStorage.getVisitsPaginated(
            offset.toLong(),
            numberOfItems.toLong(),
            excludeTypes = excludedVisitTypes,
        ).also {
            // update offset
            dbOffset += it.size + 1 // +1 is to start at item after last next time
            // check if no more items to display
            if (it.isEmpty() && numberOfItems != 0) noMoreItems = true
        }.map {
            transformVisitInfoToHistoryItem(it)
        }

        // We'll use this list to filter out redirects from metadata groups below.
        val redirectsInThePage = if (history.isNotEmpty()) {
            historyStorage.getDetailedVisits(
                start = history.last().visitedAt,
                end = history.first().visitedAt,
                excludeTypes = notRedirectTypes,
            ).map { it.url }
        } else {
            // Edge-case this doesn't cover: if we only had redirects in the current page,
            // we'd end up with an empty 'history' list since the redirects would have been
            // filtered out above. One possible solution would be to look at redirects in all of
            // history, but that's potentially quite expensive on large profiles, and introduces
            // other problems (e.g. pages that were redirects a month ago may not be redirects today).
            emptyList()
        }

        // History metadata items are recorded after their associated visited info, we add an
        // additional buffer time to the most recent visit to account for a history group
        // appearing as the most recent item.
        val visitedAtBuffer = if (offset == 0) BUFFER_TIME else 0

        // Get the history groups that fit within the range of visited times in the current history
        // items.
        val historyGroupsInOffset = if (history.isNotEmpty()) {
            historyGroups?.filter {
                it.items.any { item ->
                    (history.last().visitedAt - visitedAtBuffer) <= item.visitedAt && item.visitedAt <= (history.first().visitedAt + visitedAtBuffer)
                }
            } ?: emptyList()
        } else {
            emptyList()
        }
        val historyMetadata = historyGroupsInOffset.flatMap { it.items }
        history = history.distinctBy { Pair(it.historyTimeGroup, it.url) }

        // Add all history items that are not in a group filtering out any matches with a history
        // metadata item.
        result.addAll(history.filter { item -> historyMetadata.find { it.url == item.url } == null })

        // Filter history metadata items with no view time and dedupe by url.
        // Note that distinctBy is sufficient here as it keeps the order of the source
        // collection, and we're only sorting by visitedAt (=updatedAt) currently.
        // If we needed the view time we'd have to aggregate it for entries with the same
        // url, but we don't have a use case for this currently in the history view.
        result.addAll(
            historyGroupsInOffset.map { group ->
                group.copy(items = group.items.distinctBy { it.url }
                    .filterNot { redirectsInThePage.contains(it.url) })
            },
        )

        return result.sortedByDescending { it.visitedAt }
    }

    private fun transformVisitInfoToHistoryItem(visit: VisitInfo): HistoryDB.Regular {
        val title = visit.title?.takeIf(String::isNotEmpty) ?: visit.url.tryGetHostFromUrl()

        return HistoryDB.Regular(
            title = title,
            url = visit.url,
            visitedAt = visit.visitTime,
            isRemote = visit.isRemote,
        )
    }


    /**
     * Removes [group] and any corresponding history visits.
     */
    private suspend fun deleteMetadataSearchGroup(group: History.Group) {
        // The intention is to delete items from history for good.
        // Corresponding metadata items would also be removed,
        // because of ON DELETE CASCADE relation in DB schema.
        for (historyMetadata in group.items) {
            historyStorage.deleteVisitsFor(historyMetadata.url)
        }

        // Force a re-fetch of the groups next time we go through #getHistory.
        historyGroups = null
    }

    /** loads up to where offset was before */
    private suspend fun resetListSus() {
        Log.d("HistoryManagerState", "resetListSus invoked")
        dbOffset = 0
        historyGroups = emptyList()

        loadMoreSus()
    }

    /** completely reloads list up to current offset */
    private suspend fun refreshListSus() {
        Log.d("HistoryManagerState", "refreshListSus invoked")
        val n = dbOffset
        dbOffset = 0
        historyGroups = emptyList()

        allItems = getHistory(dbOffset, n).run {
            positionWithOffset(dbOffset)
        }
        visibleItems = allItems - pendingDeletion.toSet()
    }

    private suspend fun loadMoreSus(numberOfItems: Int = numberOfItemsToLoad) {
        Log.d("HistoryManagerState", "loadMoreSus invoked")
        allItems += getHistory(dbOffset, numberOfItems).run {
            positionWithOffset(dbOffset)
        }
        visibleItems = allItems - pendingDeletion.toSet()
    }

    /** delete [item] from history, does not refresh, updates [visibleItems] list */
    private suspend fun deleteItemSus(item: History) {
        Log.d("HistoryManagerState", "deleteItemSus invoked")
        pendingDeletion += item
        when (item) {
            is History.Regular -> {
                try {
                    historyStorage.deleteVisitsFor(item.url)
                    allItems -= item
                } catch (e: Exception) {
                    Log.e(
                        "HistoryManagerState", "deleteItem: failed to delete Regular item, e: $e"
                    )
                }
            }

            is History.Group -> {
                // NB: If we have non-search groups, this logic needs to be updated.
                try {
                    deleteMetadataSearchGroup(item)
                    browserStore.dispatch(
                        HistoryMetadataAction.DisbandSearchGroupAction(searchTerm = item.title),
                    )
                    allItems -= item
                } catch (e: Exception) {
                    Log.e(
                        "HistoryManagerState", "deleteItem: failed to delete Group item, e: $e"
                    )
                }
            }
            // We won't encounter individual metadata entries outside of groups.
            is History.Metadata -> {
                try {
                    context.components.core.historyStorage.deleteVisitsFor(item.url)
                    // there is a case when all metadata items are selected which
                    // would lead to group item with no children existing
                    // this is handled by HistoryGroupItem in HistoryViewer though
                    //
                    // required code to disband group is commented below
                    // context.components.core.store.dispatch(
                    //     HistoryMetadataAction.DisbandSearchGroupAction(searchTerm = group.title),
                    // )
//                        allItems -= item
                    refreshListSus()
                } catch (e: Exception) {
                    Log.e(
                        "HistoryManagerState", "deleteItem: failed to delete Metadata item, e: $e"
                    )
                }
            }
        }
        // since doesnt refresh list, maintain pendingDeletion
//        pendingDeletion -= item
    }

    /**
     * selected items helpers
     */

    /** delete selected items and exit selection mode */
    private suspend fun deleteItemsSus(items: Set<History>) {
        Log.d("HistoryManagerState", "deleteItemsSus invoked")
        // add all to pending deletion so not shown
        pendingDeletion += items
        val ops = mutableListOf<Deferred<Any>>()

        // delete
        for (item in items) {
            val deleteJob = coroutineScope.async { deleteItemSus(item) }
            ops.add(deleteJob)
        }
        ops.awaitAll()
        refreshListSus()
        // reset pending deletion
        pendingDeletion = emptyList()
        mode = Mode.Normal
    }


    /**
     * public state management funs
     */


    fun loadMore() {
        Log.d("HistoryManagerState", "loadMore invoked")
        taskHandler.processTask(
            type = TaskType.LOAD_MORE,
            task = { loadMoreSus() },
            onComplete = { isBusy = !it },
        )
    }

    fun refreshList() {
        Log.d("HistoryManagerState", "refreshList invoked")
        taskHandler.processTask(
            type = TaskType.REFRESH,
            task = { refreshListSus() },
            onComplete = { isBusy = !it },
        )
    }

    fun deleteItem(item: History) {
        Log.d("HistoryManagerState", "deleteItem invoked")
        taskHandler.processTask(
            type = TaskType.DELETE,
            task = { deleteItemSus(item) },
            onComplete = { isBusy = !it },
        )
    }

    fun deleteSelected() {
        Log.d("HistoryManagerState", "deleteSelected invoked")
        val items = (mode as? Mode.Selection)?.selectedItems ?: return
        taskHandler.processTask(
            type = TaskType.DELETE,
            task = { deleteItemsSus(items = items) },
            onComplete = { isBusy = !it },
        )
    }

    fun deleteTimeframe() {
        // todo
    }


    override fun start() {
        taskHandler.processTask(type = TaskType.LOAD_MORE,
            task = { resetListSus() },
            onComplete = { isBusy = !it })
    }

    override fun stop() {
        coroutineScope.cancel()
    }

    /**
     * state vars
     */

    /** history viewer state mode, one of [Mode.Normal], [Mode.Selection], or [Mode.Syncing] */
    var mode: Mode by mutableStateOf(Mode.Normal)
        private set

    /** all items currently loaded */
    private var allItems: List<History> by mutableStateOf(emptyList())

    /**
     * items pending deletion
     * updates visibleItems when edited
     * */
    var pendingDeletion by run {
        val state = mutableStateOf<List<History>>(emptyList())
        object : MutableState<List<History>> by state {
            override var value: List<History>
                get() = state.value
                set(value) {
                    Log.d(
                        "HistoryManagerState",
                        "pendingDeletion set, old size: ${state.value.size}, new size: ${value.size}"
                    )
                    state.value = value
                    visibleItems = allItems - state.value.toSet()
                }
        }
    }
        private set

    /** items that should be displayed to user, internally [allItems] - [pendingDeletion] */
    var visibleItems: List<History>? by mutableStateOf(null)
        private set

    /** selected items helper, if not in selection mode then null */
    val selectedItems: Set<History>?
        get() = (mode as? Mode.Selection)?.selectedItems

    fun selectRegularItem(item: History.Regular) {
        // if busy, return
        if (isBusy) return

        when (mode.isSelection()) {
            // if in selection mode
            true -> (mode as Mode.Selection).let {
                // if already selected, return
                if (it.selectedItems.contains(item)) return
                // if not selected, add
                it.selectedItems += item
            }
            // if not in selection mode, init
            false -> mode = Mode.Selection(setOf<History>(item))
        }
    }

    fun unselectRegularItem(item: History.Regular) {
        // if busy, return
        if (isBusy) return

        // if in selection mode, remove, no need to check if present
        (mode as? Mode.Selection)?.let { it.selectedItems -= item }
    }


    fun selectMetadataItem(item: History.Metadata) {
        // if busy, return
        if (isBusy) return

        when (mode.isSelection()) {
            // if in selection mode
            true -> (mode as Mode.Selection).let {
                // if already selected, return
                if (it.selectedItems.contains(item)) return
                // if not selected, add
                it.selectedItems += item
            }
            // if not in selection mode, init
            false -> mode = Mode.Selection(setOf<History>(item))
        }
    }

    fun unselectMetadataItem(item: History.Metadata) {
        // if busy, return
        if (isBusy) return

        // if in selection mode, remove, no need to check if present
        (mode as? Mode.Selection)?.let { it.selectedItems -= item }
    }


    fun selectGroupItem(item: History.Group) {
        // if busy, return
        if (isBusy) return

        if (mode.isSelection()) {
            // if in selection mode
            (mode as Mode.Selection).let {
                // if already selected, return
                if (it.selectedItems.contains(item)) return
                // remove sub items if selected
                it.selectedItems -= item.items
                // add whole group item
                it.selectedItems += item
            }
        } else {
            // if not in selection mode, init
            mode = Mode.Selection(setOf<History>(item))
        }
    }

    fun unselectGroupItem(item: History.Group) {
        // if busy, return
        if (isBusy) return

        // if not in selection mode, return
        if (!mode.isSelection()) return
        // if in selection mode
        (mode as Mode.Selection).let {
            // if not selected, return
            if (!it.selectedItems.contains(item)) return
            // remove sub items
            it.selectedItems -= item.items
            // remove group item
            it.selectedItems -= item
        }
    }

    /**
     * @param group history group [item] belongs to (must be parent of [item])
     * @param item item to select
     */
    fun selectGroupSubItem(group: History.Group, item: History.Metadata) {
        // if busy, return
        if (isBusy) return

        if (mode.isSelection()) {
            // if in selection mode
            (mode as Mode.Selection).let {
                // if already selected, return
                if (it.selectedItems.contains(item)) return
                // if whole group already selected, return
                if (it.selectedItems.contains(group)) return
                // if not selected, add
                it.selectedItems += item
            }
        } else {
            // if not in selection mode, init
            mode = Mode.Selection(setOf<History>(item))
        }
    }

    /**
     * @param group history group [item] belongs to (must be parent of [item])
     * @param item item to select
     */
    fun unselectGroupSubItem(group: History.Group, item: History.Metadata) {
        // if busy, return
        if (isBusy) return

        // if not in selection mode, or item not in group, return
        if (!mode.isSelection() || !group.items.contains(item)) return
        // if in selection mode
        (mode as Mode.Selection).let {
            // if group selected, remove group and add all sub-items
            if (it.selectedItems.contains(group)) {
                // remove group
                it.selectedItems -= group
                // add all group sub-items
                it.selectedItems += group.items
            }
            // remove sub-item, no need to check if in list
            it.selectedItems -= item
        }
    }

    /** share selected item urls */
    fun shareSelected() {
        (mode as? Mode.Selection)?.selectedItems?.let {
            val urls = mutableListOf<String>()
            it.forEach { item ->
                when (item) {
                    is History.Group -> urls.addAll(item.items.map { meta -> meta.url })
                    is History.Metadata -> item.url
                    is History.Regular -> item.url
                }
            }
            context.shareTextList(ArrayList(urls))
        }
    }

    /**
     * open selected items in browser
     *
     * @param then what to do after urls loaded
     */
    fun openSelectedInBrowser(private: Boolean = false, then: () -> Unit) {
        fun loadUrl(url: String, i: Int) {
            // select first tab
            context.components.newTab(url = url, private = private, selectTab = i == 0)
        }

        // load urls
        (mode as? Mode.Selection)?.selectedItems?.forEachIndexed { i, it ->
            when (it) {
                is History.Group -> it.items.forEachIndexed { j, meta ->
                    loadUrl(
                        meta.url, i + j
                    )
                }

                is History.Metadata -> loadUrl(it.url, i)
                is History.Regular -> loadUrl(it.url, i)
            }
        }
        // after tabs opened, do then
        then.invoke()
    }

    fun switchToNormalMode() {
        mode = Mode.Normal
    }
}

private fun HistoryMetadata.toHistoryDBMetadata(): HistoryDB.Metadata {
    return HistoryDB.Metadata(
        title = title?.takeIf(String::isNotEmpty) ?: key.url.tryGetHostFromUrl(),
        url = key.url,
        visitedAt = createdAt,
        totalViewTime = totalViewTime,
        historyMetadataKey = key,
    )
}

//@VisibleForTesting
private fun List<HistoryDB>.positionWithOffset(offset: Int): List<History> {
    return this.foldIndexed(listOf()) { index, prev, item ->
        // Only offset once while folding, so that we don't accumulate the offset for each element.
        val itemOffset = if (index == 0) {
            offset
        } else {
            0
        }
        val previousPosition = prev.lastOrNull()?.position ?: 0
        when (item) {
            is HistoryDB.Group -> {
                // XXX considering an empty group to have a non-zero offset is the obvious
                // limitation of the current approach, and indicates that we're conflating
                // two concepts here - position of an element for the sake of a RecyclerView,
                // and an offset for the sake of our history pagination API.
                val groupOffset = if (item.items.isEmpty()) {
                    1
                } else {
                    item.items.size
                }
                prev + item.positioned(position = previousPosition + itemOffset + groupOffset)
            }

            is HistoryDB.Metadata -> {
                prev + item.positioned(previousPosition + itemOffset + 1)
            }

            is HistoryDB.Regular -> {
                prev + item.positioned(previousPosition + itemOffset + 1)
            }
        }
    }
}


private fun HistoryDB.Group.positioned(position: Int): History.Group {
    return History.Group(
        position = position,
        items = this.items.mapIndexed { index, item -> item.positioned(index) },
        title = this.title,
        visitedAt = this.visitedAt,
        historyTimeGroup = this.historyTimeGroup,
    )
}

private fun HistoryDB.Metadata.positioned(position: Int): History.Metadata {
    return History.Metadata(
        position = position,
        historyMetadataKey = this.historyMetadataKey,
        title = this.title,
        totalViewTime = this.totalViewTime,
        url = this.url,
        visitedAt = this.visitedAt,
        historyTimeGroup = this.historyTimeGroup,
    )
}

private fun HistoryDB.Regular.positioned(position: Int): History.Regular {
    return History.Regular(
        position = position,
        title = this.title,
        url = this.url,
        visitedAt = this.visitedAt,
        historyTimeGroup = this.historyTimeGroup,
        isRemote = this.isRemote,
    )
}

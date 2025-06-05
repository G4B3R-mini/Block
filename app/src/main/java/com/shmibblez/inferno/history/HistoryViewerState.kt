package com.shmibblez.inferno.history

import android.annotation.SuppressLint
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
import com.shmibblez.inferno.library.history.History
import com.shmibblez.inferno.utils.Settings.Companion.SEARCH_GROUP_MINIMUM_SITES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.HistoryMetadataAction
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.concept.storage.HistoryMetadata
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.concept.storage.VisitType
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlin.tryGetHostFromUrl

@Composable
internal fun rememberHistoryViewerState(
    browserStore: BrowserStore = LocalContext.current.components.core.store,
    historyStorage: PlacesHistoryStorage = LocalContext.current.components.core.historyStorage,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MutableState<HistoryViewerState> {
    val state = remember {
        mutableStateOf(
            HistoryViewerState(
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


internal class HistoryViewerState(
    private val browserStore: BrowserStore,
    private val historyStorage: PlacesHistoryStorage,
    private val coroutineScope: CoroutineScope,
    val numberOfItems: Int = NUMBER_OF_HISTORY_ITEMS,
) : LifecycleAwareFeature {

    internal sealed interface Mode {
        fun isSelection() = this is Mode.Selection
        fun isNormal() = this is Mode.Normal
        fun isSyncing() = this is Mode.Syncing

        data object Normal : Mode
        data object Syncing : Mode
        class Selection(selectedItems: Set<History>) : Mode {
            // selected items only includes single items (Metadata or Regular)
            var selectedItems by mutableStateOf(selectedItems)

            fun shareSelected() {
                // todo: share urls, check how its done by HistoryFragmentStore
            }

            fun deleteSelected() {
                // todo: delete selected items
            }

            fun openAllInNewTab() {
                // todo: load urls in browser, check how its done by HistoryFragmentStore
            }

            fun openAllInPrivateTab() {
                // todo: load urls in browser, check how its done by HistoryFragmentStore
            }
        }
    }

    /**
     * database vars and funs
     */

    private val jobs: MutableList<Job> = mutableListOf()

    val isLoading: Boolean
        get() = jobs.isNotEmpty()

    var isRefreshing: Boolean = false
        private set

    // removes all jobs that are not active
    private fun clearFinishedJobs() {
        jobs.removeAll { !it.isActive }
    }

    // launches suspend function and tracks its state
    private fun launchSuspend(block: suspend CoroutineScope.() -> Unit): Job {
        val job = coroutineScope.launch(block = block).apply {
            this.invokeOnCompletion { clearFinishedJobs() }
        }
        jobs.add(job)
        return job
    }

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

    private var offset = 0

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
            ).map { transformVisitInfoToHistoryItem(it) }

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

    private suspend fun resetList(numberOfItems: Int = NUMBER_OF_HISTORY_ITEMS) {
        offset = 0
        allItems = emptyList()
        historyGroups = emptyList()

        loadMore(numberOfItems)
    }

    /** completely reloads list up to current offset */
    fun refreshList() {
        // if already refreshing or in selection mode, return
        if (isRefreshing || mode.isSelection()) return
        // if not refreshing, refresh
        isRefreshing = true
        launchSuspend {
            resetList(offset)
            isRefreshing = false
        }
    }


    private suspend fun loadMore(numberOfItems: Int = NUMBER_OF_HISTORY_ITEMS) {
        allItems += getHistory(offset, numberOfItems).run {
            positionWithOffset(offset)
        }
        visibleItems = allItems - pendingDeletion.toSet()
    }


    override fun start() {
        launchSuspend { resetList() }
    }

    override fun stop() {
        jobs.forEach { it.cancel() }
    }

    /**
     * state vars
     */

    /** history viewer state mode, one of [Mode.Normal], [Mode.Selection], or [Mode.Syncing] */
    var mode: Mode by mutableStateOf(Mode.Normal)
        private set

    /** all items currently loaded */
    private var allItems: List<History> by mutableStateOf(emptyList())

    /** items pending deletion */
    private var pendingDeletion: List<History> by mutableStateOf(emptyList())

    // todo: every time update occurs, visibleItems = allItems - pendingDeletion
    /** items that should be displayed to user, internally [allItems] - [pendingDeletion] */
    var visibleItems: List<History>? by mutableStateOf(null)
        private set

    /** selected items helper, if not in selection mode then null */
    val selectedItems: Set<History>?
        get() = (mode as? Mode.Selection)?.selectedItems

    fun selectRegularItem(item: History.Regular) {
        // if refreshing, return
        if (isRefreshing) return

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
        // if in selection mode, remove, no need to check if present
        (mode as? Mode.Selection)?.let { it.selectedItems -= item }
    }


    fun selectMetadataItem(item: History.Metadata) {
        // if refreshing, return
        if (isRefreshing) return

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
        // if in selection mode, remove, no need to check if present
        (mode as? Mode.Selection)?.let { it.selectedItems -= item }
    }


    fun selectGroupItem(item: History.Group) {
        // if refreshing, return
        if (isRefreshing) return

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
        // if refreshing, return
        if (isRefreshing) return

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
        // if not in selection mode, return
        if (!mode.isSelection()) return
        // if in selection mode
        (mode as Mode.Selection).let {
            // if not selected, return
            if (!it.selectedItems.contains(item)) return
            // if group selected
            if (it.selectedItems.contains(group)) {
                // remove group
                it.selectedItems -= group
                // add all group sub-items
                it.selectedItems += group.items
            }
            // remove sub-item
            it.selectedItems -= item
        }
    }


    fun deleteItem(item: History) {
        // if refreshing, return
        if (isRefreshing) return

        // todo: copy for deleteAll in selection
        launchSuspend {
            when (item) {
                is History.Regular -> historyStorage.deleteVisitsFor(item.url)
                is History.Group -> {
                    // NB: If we have non-search groups, this logic needs to be updated.
                    deleteMetadataSearchGroup(item)
                    browserStore.dispatch(
                        HistoryMetadataAction.DisbandSearchGroupAction(searchTerm = item.title),
                    )
                }
                // We won't encounter individual metadata entries outside of groups.
                is History.Metadata -> {}
            }
        }
    }

    // todo:
    //  update visibleItems (add items to pendingDeletion)
    //  make delete tasks async, figure out how to do something on async task complete
    //  add onSuccess and onFailure callbacks to launchSuspend
    //    - if failure, then remove from pendingDeletion
    //    - if success, remove from pendingDeletion
    //    - on all task complete, refresh list
    fun deleteAll(items: List<History>) {
        // if refreshing, return
        if (isRefreshing) return

        // todo: copy for deleteAll in selection
        for (item in items) {
            when (item) {
                is History.Regular -> launchSuspend { historyStorage.deleteVisitsFor(item.url) }
                is History.Group -> {
                    // NB: If we have non-search groups, this logic needs to be updated.
                    launchSuspend {
                        deleteMetadataSearchGroup(item)
                        browserStore.dispatch(
                            HistoryMetadataAction.DisbandSearchGroupAction(searchTerm = item.title),
                        )
                    }
                }
                // We won't encounter individual metadata entries outside of groups.
                is History.Metadata -> {
                    // todo: check sub-page for history groups, refresh
                }
            }
        }
    }


    fun stopEditing() {
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

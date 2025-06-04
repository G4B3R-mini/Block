package com.shmibblez.inferno.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.library.history.History
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.concept.storage.VisitType
import mozilla.components.support.base.feature.LifecycleAwareFeature

@Composable
internal fun rememberHistoryViewerState(): MutableState<HistoryViewerState> {
    val context = LocalContext.current

    val state = remember {
        mutableStateOf(
            HistoryViewerState(
                historyStorage = context.components.core.historyStorage,
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

internal class HistoryViewerState(
    private val historyStorage: PlacesHistoryStorage,
    val numberOfItems: Int = NUMBER_OF_HISTORY_ITEMS,
) : LifecycleAwareFeature {

    internal sealed interface Mode {
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

    override fun start() {
        // todo: sync and use pager
        //  also implement reload
    }

    override fun stop() {
        // todo: sync and use pager
        //  also implement reload
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

    var mode: Mode by mutableStateOf(Mode.Normal)
        private set

    private var offset: Int = 0

    var historyGroups: List<History>? by mutableStateOf(null)
        private set
    var pendingDeletion: List<History>? by mutableStateOf(null)
        private set

    fun deleteItem(item: History) {
        // todo: delete history item (how tf)
        //  also add to pending deletion i guess
    }

    /**
     * switch to editing mode and add item
     */
    fun startEditing(item: History) {
        mode = Mode.Selection(emptySet())
        (mode as? Mode.Selection)?.let { mode ->
            when (item) {
                is History.Group -> mode.selectedItems += item.items
                is History.Metadata -> mode.selectedItems += item
                is History.Regular -> mode.selectedItems += item
            }
        }
    }

    fun stopEditing() {
        mode = Mode.Normal
    }
}
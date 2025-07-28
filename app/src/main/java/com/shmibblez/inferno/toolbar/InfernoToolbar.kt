package com.shmibblez.inferno.toolbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import com.shmibblez.inferno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature


@Composable
fun rememberInfernoToolbarState(
    // item params
    store: BrowserStore = LocalContext.current.components.core.store,
): MutableState<InfernoToolbarState> {
    val state = remember {
        mutableStateOf(
            InfernoToolbarState(
                store = store,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

class InfernoToolbarState(
    // item params
    private val store: BrowserStore,
) : LifecycleAwareFeature {
    private var scope: CoroutineScope? = null

    var tabSessionState by mutableStateOf<TabSessionState?>(null)
        private set

    var tabCount by mutableIntStateOf(0)
        private set

    var awesomeSearchText by mutableStateOf("")

    var onAutocomplete by mutableStateOf<(TextFieldValue) -> Unit>({})

    var searchEngine by mutableStateOf<SearchEngine?>(null)

    override fun start() {
        scope = store.flowScoped { flow ->
            flow.map { it }.collect {
                tabSessionState = it.selectedTab
                // update selected tab tray tab based on if normal or private
                it.selectedTab?.content?.private?.let { private ->
                    tabCount = when (private) {
                        true -> it.privateTabs.size
                        false -> it.normalTabs.size
                    }
                }
                searchEngine = it.search.selectedOrDefaultSearchEngine!!
            }
        }
    }

    override fun stop() {
        scope?.cancel()
    }
}

// todo: wrap params in viewmodel, emit from BrowserComponent, find out how to update only
//  necessary components
@Composable
fun InfernoToolbar(
    state: InfernoToolbarState,
    onShowMenuBottomSheet: () -> Unit,
    onDismissMenuBottomSheet: () -> Unit,
    onRequestSearchBar: () -> Unit,
    onActivateFindInPage: () -> Unit,
    onActivateReaderView: () -> Unit,
    onNavToSettings: () -> Unit,
    onNavToHistory: () -> Unit,
    onNavToBookmarks: () -> Unit,
    onNavToAddBookmarkDialog: () -> Unit,
    onNavToExtensions: () -> Unit,
    onNavToPasswords: () -> Unit,
    onNavToTabsTray: () -> Unit,
    // origin params
    editMode: Boolean,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
) {
    if (state.tabSessionState == null || state.searchEngine == null) {
        PlaceholderBrowserToolbar()
        return
    }
    // todo:
    //  1. get list of toolbar items
    //  2. if contains origin, show origin toolbar, if contains mini origin, show mini origin toolbar
    //  3. when clicked, show toolbar or searchbar depending on setting
    //  4. cannot contain both origin and mini origin, selected in radio button setting in toolbar settings

    val isOrigin = true
    val isMiniOrigin = true
    // for now just show OriginBrowserToolbar
    if (isOrigin) {
        InfernoOriginToolbar(
            state = state,
            tabSessionState = state.tabSessionState!!,
            tabCount = state.tabCount,
            onShowMenuBottomSheet = onShowMenuBottomSheet,
            onDismissMenuBottomSheet = onDismissMenuBottomSheet,
            onRequestSearchBar = onRequestSearchBar,
            onActivateFindInPage = onActivateFindInPage,
            onActivateReaderView = onActivateReaderView,
            onNavToSettings = onNavToSettings,
            onNavToHistory = onNavToHistory,
            onNavToBookmarks = onNavToBookmarks,
            onNavToAddBookmarkDialog = onNavToAddBookmarkDialog,
            onNavToExtensions = onNavToExtensions,
            onNavToPasswords = onNavToPasswords,
            onNavToTabsTray = onNavToTabsTray,
            searchEngine = state.searchEngine,
            editMode = editMode,
            onStartSearch = onStartSearch,
            onStopSearch = onStopSearch,
        )
    } else if (isMiniOrigin) {
        InfernoMiniOriginToolbar(
            tabSessionState = state.tabSessionState!!,
            tabCount = state.tabCount,
            onShowMenuBottomSheet = onShowMenuBottomSheet,
            onDismissMenuBottomSheet = onDismissMenuBottomSheet,
            onRequestSearchBar = onRequestSearchBar,
            onActivateFindInPage = onActivateFindInPage,
            onActivateReaderView = onActivateReaderView,
            onNavToSettings = onNavToSettings,
            onNavToHistory = onNavToHistory,
            onNavToBookmarks = onNavToBookmarks,
            onNavToAddBookmarkDialog = onNavToAddBookmarkDialog,
            onNavToExtensions = onNavToExtensions,
            onNavToPasswords = onNavToPasswords,
            onNavToTabsTray = onNavToTabsTray,
            searchEngine = state.searchEngine,
            editMode = editMode,
            onStartSearch = onStartSearch,
            onStopSearch = onStopSearch,
        )
        // todo: not implemented yet (urgent)
    }
}
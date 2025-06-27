package com.shmibblez.inferno.toolbar

import androidx.compose.runtime.Composable
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.state.TabSessionState

// todo: wrap params in viewmodel, emit from BrowserComponent, find out how to update only
//  necessary components
@Composable
fun InfernoToolbar(
    // item params
    tabSessionState: TabSessionState?,
    tabCount: Int,
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
    searchEngine: SearchEngine?,
    editMode: Boolean,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
) {
    if (tabSessionState == null || searchEngine == null) {
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
            tabSessionState = tabSessionState,
            tabCount = tabCount,
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
            searchEngine = searchEngine,
            editMode = editMode,
            onStartSearch = onStartSearch,
            onStopSearch = onStopSearch,
        )
    } else if (isMiniOrigin) {
        InfernoMiniOriginToolbar(
            tabSessionState = tabSessionState,
            tabCount = tabCount,
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
            searchEngine = searchEngine,
            editMode = editMode,
            onStartSearch = onStartSearch,
            onStopSearch = onStopSearch,
        )
        TODO()
    }
}
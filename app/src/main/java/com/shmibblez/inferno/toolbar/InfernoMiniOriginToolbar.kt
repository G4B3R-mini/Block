package com.shmibblez.inferno.toolbar

import androidx.compose.runtime.Composable
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.state.TabSessionState

@Composable
internal fun InfernoMiniOriginToolbar(
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
    onNavToTabsTray: () -> Unit,
    // origin params
    searchEngine: SearchEngine?,
    editMode: Boolean,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
) {
    // todo:
    //  - only icons, mini origin item obligatory
    //  - when mini origin clicked, animate all icons slide out, and toolbar origin slide in
    //  - browser SearchBar is optional, if enabled do not focus edittext and do not slide out,
    //    just show bar in browser as planned
}
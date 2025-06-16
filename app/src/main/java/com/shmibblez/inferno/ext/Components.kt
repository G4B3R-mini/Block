package com.shmibblez.inferno.ext

import com.shmibblez.inferno.components.Components
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.state.SessionState
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.storage.HistoryMetadataKey

/**
 * selects last used private tab if possible, if not, select last or create new one
 */
fun Components.selectLastPrivateTab() {
    val tabsUseCases = useCases.tabsUseCases
    val privateTabs = core.store.state.privateTabs

    val lastPrivateTabId = core.store.state.lastOpenedPrivateTab?.id
    if (privateTabs.isEmpty()) {
        newTab(private = true)
    } else if (privateTabs.any { tab -> tab.id == lastPrivateTabId }) {
        tabsUseCases.selectTab(
            lastPrivateTabId!!
        )
    } else {
        tabsUseCases.selectTab(privateTabs.last().id)
    }
}

/**
 * selects last used normal tab if possible, if not, select last or create new one
 */
fun Components.selectLastNormalTab() {
    val tabsUseCases = useCases.tabsUseCases
    val normalTabs = core.store.state.normalTabs

    val lastNormalTabId = core.store.state.lastOpenedNormalTab?.id
    if (normalTabs.isEmpty()) {
        newTab(private = false)
    } else if (normalTabs.any { tab -> tab.id == lastNormalTabId }) {
        tabsUseCases.selectTab(
            lastNormalTabId!!
        )
    } else {
        tabsUseCases.selectTab(normalTabs.last().id)
    }
}

/**
 * Add new tab convenience fun
 */
// todo: next to current, go to tab action add new tab and copy
fun Components.newTab(
    nextTo: String? = null,
    private: Boolean = false,
    url: String = if (!private) "inferno:home" else "inferno:privatebrowsing",
    selectTab: Boolean = true,
    startLoading: Boolean = true,
    parentId: String? = null,
    flags: EngineSession.LoadUrlFlags = EngineSession.LoadUrlFlags.none(),
    contextId: String? = null,
    engineSession: EngineSession? = null,
    source: SessionState.Source = SessionState.Source.Internal.NewTab,
    searchTerms: String = "",
    historyMetadata: HistoryMetadataKey? = null,
    isSearch: Boolean = false,
    searchEngineName: String? = null,
    additionalHeaders: Map<String, String>? = null,
): String {
    val tabId = this.useCases.tabsUseCases.addTab(
        url = url,
        selectTab = selectTab,
        startLoading = startLoading,
        parentId = parentId,
        flags = flags,
        contextId = contextId,
        engineSession = engineSession,
        source = source,
        searchTerms = searchTerms,
        private = private,
        historyMetadata = historyMetadata,
        isSearch = isSearch,
        searchEngineName = searchEngineName,
        additionalHeaders = additionalHeaders,
    )

    if (nextTo != null)
    // move new tab next to current
        this.useCases.tabsUseCases.moveTabs(listOf(tabId), nextTo, true)

    return tabId
}
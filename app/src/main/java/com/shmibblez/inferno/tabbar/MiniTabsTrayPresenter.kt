@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.shmibblez.inferno.tabbar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabPartition
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.feature.tabs.ext.toTabList
import mozilla.components.feature.tabs.ext.toTabs
import mozilla.components.lib.state.ext.flowScoped

class MiniTabsTrayPresenter (
    private val tabsTray: TabsTray,
    private val store: BrowserStore,
    internal var tabsFilter: (TabSessionState) -> Boolean,
    internal var tabPartitionsFilter: (Map<String, TabPartition>) -> TabPartition?,
) {
    private var scope: CoroutineScope? = null
    private var initialOpen: Boolean = true

    fun start() {
        scope = store.flowScoped { flow -> collect(flow) }
    }

    fun stop() {
        scope?.cancel()
    }

    private suspend fun collect(flow: Flow<BrowserState>) {
        flow.distinctUntilChangedBy { Pair(it.toTabs(tabsFilter), tabPartitionsFilter(it.tabPartitions)) }
            .collect { state ->
                val (tabs, selectedTabId) = state.toTabList(tabsFilter)
                // Do not invoke the callback on start if this is the initial state.
                if (tabs.isEmpty() && !initialOpen) {
                    // TODO: add new tab
                }

                tabsTray.updateTabs(tabs, tabPartitionsFilter(state.tabPartitions), selectedTabId)

                initialOpen = false
            }
    }
}
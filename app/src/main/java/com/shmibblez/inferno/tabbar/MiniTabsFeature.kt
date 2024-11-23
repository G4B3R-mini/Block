@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.shmibblez.inferno.tabbar

import androidx.annotation.VisibleForTesting
import mozilla.components.browser.state.state.TabPartition
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.feature.tabs.ext.toTabList // internal to mozilla, accessing anyway in case of updates
import mozilla.components.support.base.feature.LifecycleAwareFeature

/**
 * Feature implementation for connecting a tabs tray implementation with the session module.
 *
 * @param defaultTabsFilter A tab filter that is used for the initial presenting of tabs.
 * @param defaultTabPartitionsFilter A tab partition filter that is used for the initial presenting of
 * tabs.
 * @param onCloseTray a callback invoked when the last tab is closed.
 */
class MiniTabsFeature(
    private val tabsTray: TabsTray,
    private val store: BrowserStore,
    private val defaultTabPartitionsFilter: (Map<String, TabPartition>) -> TabPartition? = { null },
    private val defaultTabsFilter: (TabSessionState) -> Boolean = { true },
) : LifecycleAwareFeature {
    @VisibleForTesting
    internal var miniPresenter = MiniTabsTrayPresenter(
        tabsTray,
        store,
        defaultTabsFilter,
        defaultTabPartitionsFilter,
    )

    override fun start() {
        miniPresenter.start()
    }

    override fun stop() {
        miniPresenter.stop()
    }


    /**
     * Filter the list of tabs using [tabsFilter].
     *
     * @param tabsFilter A filter function returning `true` for all tabs that should be displayed in
     * the tabs tray. Uses the [defaultTabsFilter] if none is provided.
     */
    fun filterTabs(tabsFilter: (TabSessionState) -> Boolean = defaultTabsFilter) {
        miniPresenter.tabsFilter = tabsFilter

        val state = store.state
        val (tabs, selectedTabId) = state.toTabList(tabsFilter)

        tabsTray.updateTabs(tabs, null, selectedTabId)
    }
}
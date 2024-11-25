package com.shmibblez.inferno.tabbar

import mozilla.components.browser.state.state.TabPartition
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.TabsTray

interface MiniTabsTray : TabsTray {

    /**
     * Interface to be implemented by classes that want to observe or react to the interactions on the tabs list.
     */
    interface Delegate {

        /**
         * A new tab has been selected.
         */
        fun onTabSelected(tab: TabSessionState, source: String? = null)

        /**
         * A tab has been closed.
         */
        fun onTabClosed(tab: TabSessionState, source: String? = null)
    }

    /**
     * Called when the list of tabs are updated.
     */
    override fun updateTabs(
        tabs: List<TabSessionState>,
        tabPartition: TabPartition?,
        selectedTabId: String?,
    )
}
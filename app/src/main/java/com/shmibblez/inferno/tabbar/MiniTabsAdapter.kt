package com.shmibblez.inferno.tabbar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.shmibblez.inferno.R
import mozilla.components.browser.state.state.TabPartition
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.TabsTrayStyling

typealias ViewHolderProvider = (ViewGroup) -> MiniTabViewHolder

open class MiniTabsAdapter(
    private val viewHolderProvider: ViewHolderProvider = { parent ->
        DefaultMiniTabViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.mozac_browser_tabstray_item, parent, false),
        )
    },
    private val styling: TabsTrayStyling = TabsTrayStyling(),
    private val delegate: MiniTabsTray.Delegate,
) : ListAdapter<TabSessionState, MiniTabViewHolder>(DiffCallback), MiniTabsTray  {
    private var selectedTabId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniTabViewHolder {
        return viewHolderProvider.invoke(parent)
    }

    override fun onBindViewHolder(holder: MiniTabViewHolder, position: Int) {
        val tab = getItem(position)

        holder.bind(tab, tab.id == selectedTabId, styling, delegate)
    }

    override fun onBindViewHolder(
        holder: MiniTabViewHolder,
        position: Int,
        payloads: List<Any>,
    ) {
        val tabs = currentList
        if (tabs.isEmpty()) return

        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        val tab = getItem(position)

        if (payloads.contains(PAYLOAD_HIGHLIGHT_SELECTED_ITEM) && tab.id == selectedTabId) {
            holder.updateSelectedTabIndicator(true)
        } else if (payloads.contains(PAYLOAD_DONT_HIGHLIGHT_SELECTED_ITEM) && tab.id == selectedTabId) {
            holder.updateSelectedTabIndicator(false)
        }
    }

    override fun updateTabs(tabs: List<TabSessionState>, tabPartition: TabPartition?, selectedTabId: String?) {
        this.selectedTabId = selectedTabId

        submitList(tabs)
    }

    companion object {
        /**
         * Payload used in onBindViewHolder for a partial update of the current view.
         *
         * Signals that the currently selected tab should be highlighted. This is the default behavior.
         */
        val PAYLOAD_HIGHLIGHT_SELECTED_ITEM: Int = R.id.payload_highlight_selected_item

        /**
         * Payload used in onBindViewHolder for a partial update of the current view.
         *
         * Signals that the currently selected tab should NOT be highlighted. No tabs would appear as highlighted.
         */
        val PAYLOAD_DONT_HIGHLIGHT_SELECTED_ITEM: Int = R.id.payload_dont_highlight_selected_item
    }

    private object DiffCallback : DiffUtil.ItemCallback<TabSessionState>() {
        override fun areItemsTheSame(oldItem: TabSessionState, newItem: TabSessionState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TabSessionState, newItem: TabSessionState): Boolean {
            return oldItem == newItem
        }
    }
}
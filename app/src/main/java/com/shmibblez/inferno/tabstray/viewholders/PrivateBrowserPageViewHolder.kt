/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.tabstray.viewholders

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.store.BrowserStore
import com.shmibblez.inferno.R
import com.shmibblez.inferno.tabstray.TabsTrayInteractor
import com.shmibblez.inferno.tabstray.TabsTrayStore
import com.shmibblez.inferno.tabstray.ext.defaultBrowserLayoutColumns
import com.shmibblez.inferno.tabstray.ext.observeFirstInsert
import com.shmibblez.inferno.tabstray.ext.selectedPrivateTab

/**
 * View holder for the private tabs tray list.
 */
class PrivateBrowserPageViewHolder(
    containerView: View,
    tabsTrayStore: TabsTrayStore,
    private val browserStore: BrowserStore,
    interactor: TabsTrayInteractor,
) : AbstractBrowserPageViewHolder(
    containerView,
    tabsTrayStore,
    interactor,
) {

    override val emptyStringText: String
        get() = itemView.resources.getString(R.string.no_private_tabs_description)

    override fun scrollToTab(
        adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
        layoutManager: RecyclerView.LayoutManager,
    ) {
        adapter.observeFirstInsert {
            val selectedTab = browserStore.state.selectedPrivateTab ?: return@observeFirstInsert
            val scrollIndex = browserStore.state.privateTabs.indexOf(selectedTab)

            layoutManager.scrollToPosition(scrollIndex)
        }
    }

    override fun bind(
        adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>,
    ) {
        val context = containerView.context
        val columns = context.defaultBrowserLayoutColumns
        val manager = GridLayoutManager(context, columns)

        super.bind(adapter, manager)
    }

    companion object {
        const val LAYOUT_ID = R.layout.private_browser_tray_list
    }
}

package com.shmibblez.inferno.tabbar

import android.view.LayoutInflater
import android.view.ViewGroup
import com.shmibblez.inferno.R
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.browser.tabstray.TabsTrayStyling

typealias ViewHolderProvider = (ViewGroup) -> MiniTabViewHolder

class MiniTabsAdapter(
    private val viewHolderProvider: ViewHolderProvider = { parent ->
        DefaultMiniTabViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.mozac_browser_tabstray_item, parent, false),
        )
    },
    private val styling: TabsTrayStyling = TabsTrayStyling(),
    private val delegate: TabsTray.Delegate,
) {
}
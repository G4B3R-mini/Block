package com.shmibblez.inferno.tabbar

import android.view.LayoutInflater
import mozilla.components.browser.tabstray.DefaultTabViewHolder
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.browser.tabstray.TabsTrayStyling

class MiniTabsAdapter(
    private val viewHolderProvider: ViewHolderProvider = { parent ->
        DefaultTabViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.mozac_browser_tabstray_item, parent, false),
        )
    },
    private val styling: TabsTrayStyling = TabsTrayStyling(),
    private val delegate: TabsTray.Delegate,
) {
}
package com.shmibblez.inferno.tabbar

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.tabs.TabsTouchHelper
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.DefaultTabViewHolder
import mozilla.components.browser.tabstray.TabsAdapter
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.browser.tabstray.TabsTrayStyling
import mozilla.components.browser.tabstray.ViewHolderProvider
import mozilla.components.browser.thumbnails.loader.ThumbnailLoader

@Composable
fun BrowserTabBar() {
    val (miniTabsFeature, setMiniTabsFeature) = remember { mutableStateOf<MiniTabsFeature?>(null) }
    val context = LocalContext.current
    LaunchedEffect(key1 = miniTabsFeature) {
        val trayAdapter = createAndSetupMiniTabsTray(context)
        setMiniTabsFeature(
            MiniTabsFeature(
                trayAdapter,
                context.components.core.store,
            ) { !it.content.private }
        )
    }
}

private fun createAndSetupMiniTabsTray(context: Context): TabsTray {
    val layoutManager = LinearLayoutManager(context)
    val thumbnailLoader = ThumbnailLoader(context.components.core.thumbnailStorage)
    val trayStyling =
        TabsTrayStyling(itemBackgroundColor = Color.TRANSPARENT, itemTextColor = Color.WHITE)
    val viewHolderProvider: ViewHolderProvider = { viewGroup ->
        val view = LayoutInflater.from(context)
            .inflate(R.layout.browser_tabstray_item, viewGroup, false)

        DefaultTabViewHolder(view, thumbnailLoader)
    }
    val tabsAdapter = TabsAdapter(
        thumbnailLoader = thumbnailLoader,
        viewHolderProvider = viewHolderProvider,
        styling = trayStyling,
        delegate = object : TabsTray.Delegate {
            override fun onTabSelected(tab: TabSessionState, source: String?) {
                context.components.useCases.tabsUseCases.selectTab(tab.id)
                closeTabsTray()
            }

            override fun onTabClosed(tab: TabSessionState, source: String?) {
                requireComponents.useCases.tabsUseCases.removeTab(tab.id)
            }
        },
    )

    val tabsTray = requireView().findViewById<RecyclerView>(R.id.tabsTray)
    tabsTray.layoutManager = layoutManager
    tabsTray.adapter = tabsAdapter

    TabsTouchHelper {
        requireComponents.useCases.tabsUseCases.removeTab(it.id)
    }.attachToRecyclerView(tabsTray)

    return tabsAdapter
}
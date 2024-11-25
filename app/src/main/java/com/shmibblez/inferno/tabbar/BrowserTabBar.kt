package com.shmibblez.inferno.tabbar

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.TabsTrayStyling
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.tabs.tabstray.TabsFeature

// TODO: update MiniTabViewHolder layout for individual tab layout
fun BrowserState.toTabList(
    tabsFilter: (TabSessionState) -> Boolean = { true },
): Pair<List<TabSessionState>, String?> {
    val tabStates = tabs.filter(tabsFilter)
    val selectedTabId = tabStates
        .filter(tabsFilter)
        .firstOrNull { it.id == selectedTabId }
        ?.id

    return Pair(tabStates, selectedTabId)
}

@Preview
@Composable
fun BrowserTabBar() {
    val context = LocalContext.current
    val (tabList, selectedTab) = context.components.core.store.state.toTabList()

    return LazyRow(
        Modifier
            .fillMaxSize()
            .height(Dp(44F))
    ) {
        items(tabList.size) {
            MiniTab(context, tabList[it])
        }
    }
}

@Composable
private fun MiniTab(context: Context, tabSessionState: TabSessionState) {
    return Row(
        Modifier
            .height(Dp(40F))
            .width(Dp(50F))
            .background(Color(0xFFFFFFFF))
    ) {
        Text(
            text = tabSessionState.content.title.ifEmpty { tabSessionState.content.url },
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(onClick = { context.components.useCases.tabsUseCases.removeTab(tabSessionState.id) }) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.close),
                contentDescription = "close tab"
            )
        }
    }
}

private fun onLastTabClosed(context: Context) {
    context.components.useCases.tabsUseCases.addTab.invoke("about:blank", selectTab = true)
}

// TODO: add listener for tab private changed, if yes then show private tabs, update here too
//private fun updateTabsToolbar(isPrivate: Boolean) {
//    val tabsToolbar = requireView().findViewById<TabsToolbar>(R.id.tabsToolbar)
//    tabsToolbar.updateToolbar(isPrivate)
//}
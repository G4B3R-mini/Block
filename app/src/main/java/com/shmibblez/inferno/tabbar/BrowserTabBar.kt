package com.shmibblez.inferno.tabbar

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.browser.toPx
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState

// todo:
//   - add gesture detection for switching tabs (swipe left/right to go to tab on left/right)
//   - update MiniTabViewHolder layout for individual tab layout
fun BrowserState.toTabList(
    tabsFilter: (TabSessionState) -> Boolean = { true },
): Pair<List<TabSessionState>, String?> {
    val tabStates = tabs.filter(tabsFilter)
    val selectedTabId = tabStates.filter(tabsFilter).firstOrNull { it.id == selectedTabId }?.id

    return Pair(tabStates, selectedTabId)
}

inline fun <T> Iterable<T>.findIndex(predicate: (T) -> Boolean): Int? {
    forEachIndexed { i, e -> if (predicate(e)) return i }
    return null
}

@Composable
fun BrowserTabBar(tabList: List<TabSessionState>) {
    val context = LocalContext.current
    val localConfig = LocalConfiguration.current
    val listState = rememberLazyListState()
    val selectedTabId = context.components.core.store.state.toTabList().second
//    val
    val isPrivateSession: Boolean = (if (tabList.isEmpty()) {
        false
    } else if (selectedTabId == null) {
        newTab(context, false)
        false
    } else tabList.find { it.id == selectedTabId }!!.content.private)
    val displayedTabs =
        with(context.components.core.store.state) { if (isPrivateSession) this.privateTabs else this.normalTabs }
    // scroll to active tab
    val i = displayedTabs.findIndex { it.id == selectedTabId }
    LaunchedEffect(i) {
        val sw = localConfig.screenWidthDp.dp
        if (i != null) listState.animateScrollToItem(
            i,
            -(sw - ComponentDimens.TAB_WIDTH).toPx() / 2
        )
    }
    // if tab list empty add new tab
    LaunchedEffect(displayedTabs) {
        // TODO: replace with moz last tab callback
        if (displayedTabs.isEmpty()) newTab(context, isPrivateSession)
    }

    if (displayedTabs.isEmpty()) return Row(
        Modifier
            .fillMaxWidth()
            .height(ComponentDimens.TAB_BAR_HEIGHT)
            .background(Color.Black)
    ) { }

    return Row(
        Modifier
            .fillMaxWidth()
            .height(ComponentDimens.TAB_BAR_HEIGHT)
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .height(ComponentDimens.TAB_BAR_HEIGHT)
                .background(Color.Black)
                .weight(1F),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(displayedTabs.size) {
                MiniTab(
                    context, displayedTabs[it], displayedTabs[it].id == selectedTabId
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(2F / 3F)
                    .align(Alignment.Center)
                    .clickable { newTab(context, isPrivateSession) },
                imageVector = ImageVector.vectorResource(R.drawable.baseline_add_24),
                contentDescription = "new tab"
            )
        }
    }
}

@Composable
private fun MiniTab(
    context: Context, tabSessionState: TabSessionState, selected: Boolean
) {
    return Row(modifier = Modifier
        .fillMaxSize()
        .width(ComponentDimens.TAB_WIDTH)
        .background(if (selected) Color.Black else Color.DarkGray)
        .drawBehind {
            val w = size.width
            val h = size.height
            val cap = StrokeCap.Square
            val sw = 1.dp.toPx()
            val hsw = sw / 2
            val color = if (selected) Color.DarkGray else Color.Gray
            // left
            drawLine(
                cap = cap,
                color = color,
                strokeWidth = sw,
                start = Offset(hsw, hsw),
                end = Offset(hsw, h - hsw)
            )
            // right
            drawLine(
                cap = cap,
                color = color,
                strokeWidth = sw,
                start = Offset(w - hsw, hsw),
                end = Offset(w - hsw, h - hsw)
            )
            // bottom
            drawLine(
                cap = cap,
                color = color,
                strokeWidth = sw,
                start = Offset(hsw, hsw),
                end = Offset(w - hsw, hsw)
            )
        }
        .clickable(enabled = !selected) {
            context.components.useCases.tabsUseCases.selectTab(
                tabSessionState.id
            )
        }, verticalAlignment = Alignment.CenterVertically

    ) {
        Text(
            text = tabSessionState.content.title.ifEmpty { tabSessionState.content.url },
            modifier = Modifier
                .wrapContentHeight()
                .padding(4.dp, 0.dp, 0.dp, 0.dp)
                .weight(1F)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent, if (selected) Color.Black else Color.DarkGray
                        ),
                        startX = (90.dp - 10.dp)
                            .toPx()
                            .toFloat()
                    )
                ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = Color.White,
            textAlign = TextAlign.Start
        )
        Image(
            modifier = Modifier
                .fillMaxHeight()
                .padding(0.dp, 0.dp, 4.dp, 0.dp)
                .aspectRatio(0.5F)
                .clickable {
                    context.components.useCases.tabsUseCases.removeTab(tabSessionState.id)
                },
            imageVector = ImageVector.vectorResource(R.drawable.close),
            contentDescription = "close tab"
        )
    }
}

private fun newTab(context: Context, isPrivateSession: Boolean) {
    context.components.useCases.tabsUseCases.addTab(
        url = if (isPrivateSession) "inferno:privatebrowsing" else "inferno:home",
        selectTab = true,
        private = isPrivateSession
    )
}

// TODO: add listener for tab private changed, if yes then show private tabs, update here too
//private fun updateTabsToolbar(isPrivate: Boolean) {
//    val tabsToolbar = requireView().findViewById<TabsToolbar>(R.id.tabsToolbar)
//    tabsToolbar.updateToolbar(isPrivate)
//}
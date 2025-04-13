package com.shmibblez.inferno.tabbar

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.browser.toPx
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.newTab
import mozilla.components.browser.state.ext.getUrl
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState

// todo:
//   - add gesture detection for switching tabs (swipe left/right to go to tab on left/right)
//   - update MiniTabViewHolder layout for individual tab layout
fun BrowserState.toTabList(
    tabsFilter: (TabSessionState) -> Boolean = { true },
): Pair<List<TabSessionState>, TabSessionState?> {
    val tabStates = tabs.filter(tabsFilter)
    return Pair(tabStates, selectedTab)
}

inline fun <T> Iterable<T>.findIndex(predicate: (T) -> Boolean): Int? {
    forEachIndexed { i, e -> if (predicate(e)) return i }
    return null
}

@Composable
fun BrowserTabBar(tabList: List<TabSessionState>, selectedTab: TabSessionState?) {
    val context = LocalContext.current
    val localConfig = LocalConfiguration.current
    val listState = rememberLazyListState()
    // if no tab selected return
    val isPrivateSession: Boolean = (if (tabList.isEmpty()) {
        Log.d("BrowserTabBar", "tab list empty")
        false
    } else if (selectedTab == null) {
        false
    } else tabList.find { it.id == selectedTab.id }!!.content.private)

    // scroll to active tab
    val i = tabList.findIndex { it.id == selectedTab?.id }
    LaunchedEffect(i, LocalConfiguration.current.orientation) {
        val sw = localConfig.screenWidthDp.dp
        if (i != null) listState.animateScrollToItem(
            i, -(sw - ComponentDimens.TAB_WIDTH).toPx() / 2
        )
    }

    // if tab list empty or no tab selected return empty tab list
    if (tabList.isEmpty() || selectedTab == null) return Row(
        Modifier
            .fillMaxWidth()
            .height(ComponentDimens.TAB_BAR_HEIGHT)
            .background(Color.Black)
    ) { }

    return Row(
        Modifier
            .fillMaxWidth()
            .height(ComponentDimens.TAB_BAR_HEIGHT)
            .background(Color.Black),
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .height(ComponentDimens.TAB_BAR_HEIGHT)
                .background(Color.Black)
                .weight(1F),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedIndex = tabList.findIndex { it.id == selectedTab.id }
            items(tabList.size) {
                MiniTab(
                    context,
                    tabList[it],
                    tabList[it].id == selectedTab.id,
                    it,
                    selectedIndex!!,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            // add tab
            Icon(
                modifier = Modifier
                    .size(12.dp)
                    .clickable {
                        context.components.newTab(
                            isPrivateSession = isPrivateSession,
                            nextTo = selectedTab.id, // todo: next to current based on config, default is true
                        )
                    },
                painter = painterResource(R.drawable.ic_new),
                tint = Color.White,
                contentDescription = "new tab"
            )
        }
    }
}

@Composable
private fun MiniTab(
    context: Context,
    tabSessionState: TabSessionState,
    selected: Boolean,
    index: Int,
    selectedIndex: Int,
) {
    return Row(modifier = Modifier
        .alpha(if (selected) 1F else 0.33F)
        .fillMaxSize()
        .width(ComponentDimens.TAB_WIDTH)
        .background(Color.Black)
        .drawBehind {
            // draw borders
            val w = size.width
            val h = size.height
            val cap = StrokeCap.Square
            val sw = 1.dp.toPx()
            val hsw = sw / 2
            val color = if (selected) Color.Black else Color.Gray
            // left, only draw if first or to right of selected tab
            if (index == 0 || index - 1 == selectedIndex) drawLine(
                cap = cap,
                color = color,
                strokeWidth = sw,
                start = Offset(hsw, hsw),
                end = Offset(hsw, h - hsw)
            )
            // right
            drawLine(
                cap = cap, color = color,
                // if last or to left of selected tab sw else hsw
                strokeWidth = sw, start = Offset(w - hsw, hsw), end = Offset(w - hsw, h - hsw)
            )
//            // top selected indicator
//            if (selected) drawLine(
//                cap = cap,
//                color = Color.Red,
//                strokeWidth = sw,
//                start = Offset(hsw, hsw),
//                end = Offset(w - hsw, hsw)
//            )
            // bottom selected indicator
            if (selected) drawLine(
                cap = cap,
                color = Color.Red,
                strokeWidth = sw,
                start = Offset(hsw, h - hsw),
                end = Offset(w - hsw, h - hsw)
            )
        }
        .clickable(enabled = !selected) {
            context.components.useCases.tabsUseCases.selectTab(
                tabSessionState.id
            )
        }, verticalAlignment = Alignment.CenterVertically

    ) {
        val url = tabSessionState.getUrl()
        val isHomePage = url == "inferno:home" || url == "about:blank"
        val isPrivateHomePage = url == "inferno:privatebrowsing" || url == "about:privatebrowsing"
        val icon = tabSessionState.content.icon
        val favicon = if (isHomePage) {
            painterResource(R.drawable.inferno)
        } else if (isPrivateHomePage) {
            painterResource(R.drawable.ic_private_browsing)
        } else {
            if (icon != null) BitmapPainter(icon.asImageBitmap()) else painterResource(R.drawable.mozac_ic_globe_24)
        }
        // favicon
        Image(
            painter = favicon,
            contentDescription = "favicon",
            modifier = Modifier
                .aspectRatio(1F)
                .padding(6.dp)
                .padding(start = 1.dp),
        )
        // site name with gradient
        Box(
            modifier = Modifier
                .weight(1F)
                .wrapContentHeight()
                .padding(0.dp)
                .weight(1F),
        ) {
            InfernoText(
                text = tabSessionState.content.title.ifEmpty { tabSessionState.content.url },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontSize = 14.sp,
                fontColor = Color.White,
            )
            // gradient
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .aspectRatio(0.5F)
                    .padding(vertical = 1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent, Color.Black
                            )
                        )
                    )
            )
        }
        // close
        Icon(
            modifier = Modifier
                .padding(8.dp, 0.dp, 8.dp, 0.dp)
                .size(10.dp)
                .clickable {
                    context.components.useCases.tabsUseCases.removeTab(tabSessionState.id)
                },
            painter = painterResource(R.drawable.ic_close),
            tint = Color.White,
            contentDescription = stringResource(R.string.close_tab),
        )
    }
}

// TODO: add listener for tab private changed, if yes then show private tabs, update here too
//private fun updateTabsToolbar(isPrivate: Boolean) {
//    val tabsToolbar = requireView().findViewById<TabsToolbar>(R.id.tabsToolbar)
//    tabsToolbar.updateToolbar(isPrivate)
//}
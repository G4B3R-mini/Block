package com.shmibblez.inferno.tabbar

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.UiConst
import com.shmibblez.inferno.compose.Favicon
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.toolbar.ToolbarOnlyComponents.Companion.ProgressBar
import mozilla.components.browser.state.ext.getUrl
import mozilla.components.browser.state.state.TabSessionState


// todo:
//   - add gesture detection for switching tabs (swipe left/right to go to tab on left/right)
//   - update MiniTabViewHolder layout for individual tab layout

inline fun <T> Iterable<T>.findIndex(predicate: (T) -> Boolean): Int? {
    forEachIndexed { i, e -> if (predicate(e)) return i }
    return null
}

private val verticalDividerPadding = 6.dp
private val TAB_END_PADDING = 4.dp

@Composable
fun InfernoTabBar(tabList: List<TabSessionState>, selectedTab: TabSessionState?) {
    val configuration = LocalConfiguration.current
    fun calculateTabWidth(): Dp {
        val screenWidth = configuration.screenWidthDp.dp
        // available space for tabs to occupy
        // screen width - side padding - add square width
        val availableTabSpace = screenWidth - (1F * TAB_END_PADDING) - 1.dp - UiConst.TAB_BAR_HEIGHT
        val tabWidth = availableTabSpace / tabList.size.let { if (it <= 0) 1 else it }
        return when (tabWidth > UiConst.TAB_WIDTH) {
            true -> tabWidth
            false -> UiConst.TAB_WIDTH
        }
    }

    val context = LocalContext.current
    val settings by context.infernoSettingsDataStore.data.collectAsState(
        initial = InfernoSettings.getDefaultInstance(),
    )

    context.components.core.store
    val listState = rememberLazyListState()
    var tabAutoWidth by remember { mutableStateOf(calculateTabWidth()) }
    // if no tab selected return
    val isPrivateSession: Boolean = (if (tabList.isEmpty()) {
        Log.d("BrowserTabBar", "tab list empty")
        false
    } else if (selectedTab == null) {
        false
    } else tabList.find { it.id == selectedTab.id }!!.content.private)

    // scroll to active tab
    LaunchedEffect(selectedTab?.id, LocalConfiguration.current.orientation) {
        // scroll to selected tab, auto centers
        val i = tabList.findIndex { it.id == selectedTab?.id }
        if (i != null) listState.animateScrollToItem(
            i, 0,
        )
    }

    // calculate tab width
    LaunchedEffect(tabList.size, LocalConfiguration.current.orientation) {
        tabAutoWidth = calculateTabWidth()
        Log.d("BrowserTabBar", "tabAutoWidth changed")
    }

    // if tab list empty or no tab selected return empty tab list, return nothing
    if (tabList.isEmpty() || selectedTab == null) return

    return Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(UiConst.TAB_BAR_HEIGHT)
            .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor.copy(alpha = UiConst.BAR_BG_ALPHA)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
        ) {
            // tabs
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1F),
            ) {
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        // 8dp is material small
//                .clip(RoundedCornerShape(0.dp, 8.dp, 8.dp, 0.dp))
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    val selectedIndex = tabList.findIndex { it.id == selectedTab.id }
                    item { Spacer(Modifier.width(TAB_END_PADDING)) }
                    items(tabList.size) {
                        MiniTab(
                            context = context,
                            tabSessionState = tabList[it],
                            autoWidth = tabAutoWidth,
                            selected = tabList[it].id == selectedTab.id,
                            index = it,
                            selectedIndex = selectedIndex!!,
                            lastIndex = tabList.size - 1
                        )
                    }
                    item { Spacer(Modifier.width(TAB_END_PADDING)) }
                }
            }

            // divider
            VerticalDivider(
                modifier = Modifier.padding(vertical = verticalDividerPadding),
                thickness = 1.dp,
                color = LocalContext.current.infernoTheme().value.primaryIconColor,
            )

            // add tab icon
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1F),
                contentAlignment = Alignment.Center,
            ) {
                InfernoIcon(
                    modifier = Modifier
                        .size(12.dp)
                        .clickable {
                            context.components.newTab(
                                customHomeUrl = if (settings.shouldUseInfernoHome) null else {
                                    settings.customHomeUrl.let {
                                        when (it.isNullOrEmpty()) {
                                            true -> null
                                            false -> it
                                        }
                                    }
                                },
                                private = isPrivateSession,
                                nextTo = selectedTab.id, // todo: next to current based on config, default is true
                            )
                        },
                    painter = painterResource(R.drawable.ic_new_24),
                    contentDescription = "new tab",
                )
            }
        }

        // loading bar
        if (selectedTab.content.loading) {
            ProgressBar(
                progress = (selectedTab.content.progress.toFloat()) / 100F,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .height(2.dp),
            )
        }
    }
}

@Composable
private fun MiniTab(
    context: Context,
    tabSessionState: TabSessionState,
    autoWidth: Dp,
    selected: Boolean,
    index: Int,
    selectedIndex: Int,
    lastIndex: Int,
) {
    // only draw border if not to left of selected, not selected, and not last
    val drawBorder = index + 1 != selectedIndex && !selected && index != lastIndex
    val width = remember { Animatable(autoWidth.value) }

    LaunchedEffect(autoWidth) {
        width.animateTo(targetValue = autoWidth.value, animationSpec = tween(250))
    }

    return Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width.value.dp)
    ) {
        Row(
            modifier = Modifier
//        .alpha(if (selected) 1F else 0.33F)
                .fillMaxSize()
                .background(
                    color = when (selected) {
                        true -> LocalContext.current.infernoTheme().value.secondaryBackgroundColor.copy(
                            alpha = UiConst.BAR_BG_ALPHA
                        )

                        false -> Color.Transparent
                    }, shape = when (selected) {
                        true -> MaterialTheme.shapes.small
                        false -> RectangleShape
                    }
                )
                .clickable(enabled = !selected) {
                    context.components.useCases.tabsUseCases.selectTab(
                        tabSessionState.id
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val url = tabSessionState.getUrl()
            val isHomePage = url == "inferno:home" || url == "about:blank"
            val isPrivateHomePage =
                url == "inferno:privatebrowsing" || url == "about:privatebrowsing"

            // favicon
            when {
                isHomePage -> {
                    Image(
                        painter = painterResource(R.drawable.inferno),
                        contentDescription = "favicon",
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp),
                    )
                }

                isPrivateHomePage -> {
                    Image(
                        painter = painterResource(R.drawable.ic_private_browsing_24),
                        contentDescription = "favicon",
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp),
                    )
                }

                else -> {
                    when (tabSessionState.content.icon) {
                        null -> {
                            Favicon(
                                url = url ?: "",
                                size = 18.dp,
                                modifier = Modifier
                                    .aspectRatio(1F)
                                    .padding(6.dp),
                            )
                        }

                        else -> {
                            Image(
                                bitmap = tabSessionState.content.icon!!.asImageBitmap(),
                                contentDescription = "favicon",
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp),
                            )
                        }
                    }
                }
            }
            // site title
            InfernoText(
                text = tabSessionState.content.title.ifEmpty { tabSessionState.content.url },
                modifier = Modifier.weight(1F),
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
            )
            // close
            InfernoIcon(
                modifier = Modifier
                    .padding(8.dp, 0.dp, 8.dp, 0.dp)
                    .size(10.dp)
                    .clickable {
                        context.components.useCases.tabsUseCases.removeTab(tabSessionState.id)
                    },
                painter = painterResource(R.drawable.ic_close_24),
                contentDescription = stringResource(R.string.close_tab),
            )
            // separator
            if (drawBorder) {
                VerticalDivider(
                    modifier = Modifier.padding(vertical = verticalDividerPadding),
                    thickness = 1.dp,
                    color = LocalContext.current.infernoTheme().value.primaryIconColor,
                )
            }
        }
    }
}
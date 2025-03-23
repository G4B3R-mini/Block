package com.shmibblez.inferno.toolbar

//import com.shmibblez.inferno.ext.share
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.BrowserComponentMode
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.sessionUseCases
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.DividerToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.FindInPageToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.NavOptionsToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.PrivateModeToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.RequestDesktopSiteToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.SettingsToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.ShareToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarSeparator
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarEmptyIndicator
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarSecurityIndicator
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarTrackingProtectionIndicator
import mozilla.components.browser.state.ext.getUrl
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.ktx.android.content.share

// TODO: test implementations
// todo:
//   - icons left to add (add settings screen for ones shown on tab, max 5):
//     - settings icon
//     - private mode toggle
//     - view desktop site toggle
//   - add double up and down chevron icon for options tray
//     - options tray is a sheet that pops up or down and shows all options available as icons
//   - make options in menu a grid

object IconConstants {
    const val ICON_ASPECT_RATIO = 1F
    val ICON_START_PADDING = 6.dp
    val ICON_TOP_PADDING = 10.dp
    val ICON_END_PADDING = 6.dp
    val ICON_BOTTOM_PADDING = 10.dp
    val INDICATOR_ICON_START_PADDING = 8.dp
    val INDICATOR_ICON_TOP_PADDING = 8.dp
    val INDICATOR_ICON_END_PADDING = 8.dp
    val INDICATOR_ICON_BOTTOM_PADDING = 8.dp
}

/**
 * @param progress 0.0 is 0%, 1.0 is 100%
 */
@Composable
internal fun ProgressBar(progress: Float) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .height(ComponentDimens.PROGRESS_BAR_HEIGHT)
            .fillMaxWidth(),
        color = Color.Red,
        trackColor = Color.Black
    )
}

interface ToolbarOptionsScope {
    // TODO: add options icons for
    //  - find in page (search icon)
    //  - switch to desktop site (desktop icon)
    //  - share (share icon)
    //  - settings (settings icon)
    //  - private mode (incog symbol)

    @Composable
    fun ToolbarSeparator()

    @Composable
    fun ToolbarBack(enabled: Boolean)

    @Composable
    fun ToolbarForward(enabled: Boolean)

    @Composable
    fun ToolbarReload(enabled: Boolean, loading: Boolean)

    @Composable
    fun ToolbarStopLoading(enabled: Boolean)

    @Composable
    fun ToolbarShowTabsTray(tabCount: Int, onNavToTabsTray: () -> Unit)
}

object ToolbarOptionsScopeInstance : ToolbarOptionsScope {
    @Composable
    override fun ToolbarSeparator() {
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 4.dp),
            color = Color.White,
            thickness = 1.dp,
        )
    }

    @Composable
    override fun ToolbarBack(enabled: Boolean) {
        val useCases = sessionUseCases()
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .alpha(if (enabled) 1F else 0.5F)
                .padding(
                    start = IconConstants.ICON_START_PADDING,
                    top = IconConstants.ICON_TOP_PADDING,
                    end = IconConstants.ICON_END_PADDING,
                    bottom = IconConstants.ICON_BOTTOM_PADDING
                )
                .aspectRatio(IconConstants.ICON_ASPECT_RATIO)
                .clickable(enabled = enabled) { useCases.goBack.invoke() },
            painter = painterResource(id = R.drawable.baseline_chevron_left_24),
            contentDescription = "back",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarForward(enabled: Boolean) {
        val useCases = sessionUseCases()
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .alpha(if (enabled) 1F else 0.5F)
                .padding(
                    start = IconConstants.ICON_START_PADDING,
                    top = IconConstants.ICON_TOP_PADDING,
                    end = IconConstants.ICON_END_PADDING,
                    bottom = IconConstants.ICON_BOTTOM_PADDING
                )
                .aspectRatio(IconConstants.ICON_ASPECT_RATIO)
                .clickable(enabled = enabled) { useCases.goForward.invoke() },
            painter = painterResource(id = R.drawable.baseline_chevron_right_24),
            contentDescription = "forward",
            tint = Color.White,
        )
    }

    @Composable
    override fun ToolbarReload(enabled: Boolean, loading: Boolean) {
        val useCases = sessionUseCases()
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .alpha(if (enabled) 1F else 0.5F)
                .padding(
                    start = IconConstants.ICON_START_PADDING,
                    top = IconConstants.ICON_TOP_PADDING,
                    end = IconConstants.ICON_END_PADDING,
                    bottom = IconConstants.ICON_BOTTOM_PADDING
                )
                .aspectRatio(IconConstants.ICON_ASPECT_RATIO)
                .clickable(enabled = enabled) {
                    if (loading) useCases.stopLoading.invoke() else useCases.reload.invoke()
                },
            painter = if (loading) painterResource(id = R.drawable.ic_cross_24) else painterResource(
                id = R.drawable.ic_arrow_clockwise_24
            ),
            contentDescription = "reload page",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarStopLoading(enabled: Boolean) {
        val useCases = sessionUseCases()
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .alpha(if (enabled) 1F else 0.5F)
                .padding(
                    start = IconConstants.ICON_START_PADDING,
                    top = IconConstants.ICON_TOP_PADDING,
                    end = IconConstants.ICON_END_PADDING,
                    bottom = IconConstants.ICON_BOTTOM_PADDING
                )
                .aspectRatio(IconConstants.ICON_ASPECT_RATIO)
                .clickable(enabled = enabled) { useCases.stopLoading.invoke() },
            painter = painterResource(id = R.drawable.ic_cross_24),
            contentDescription = "stop loading",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarShowTabsTray(tabCount: Int, onNavToTabsTray: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
//                .alpha(0.5F)
                .padding(
                    start = IconConstants.ICON_START_PADDING,
                    top = IconConstants.ICON_TOP_PADDING,
                    end = IconConstants.ICON_END_PADDING,
                    bottom = IconConstants.ICON_BOTTOM_PADDING
                )
                .aspectRatio(IconConstants.ICON_ASPECT_RATIO)
                .clickable { onNavToTabsTray.invoke() }
                .wrapContentHeight(unbounded = true),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.ic_tabcounter_box_24),
                contentDescription = "show tabs tray",
                tint = Color.White,
            )
            InfernoText(
                text = tabCount.toString(),
                fontColor = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 10.sp
            )
        }
    }

    @Composable
    fun ToolbarMenuIcon(setShowMenu: (Boolean) -> Unit) {
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    start = IconConstants.ICON_START_PADDING,
                    top = IconConstants.ICON_TOP_PADDING,
                    end = IconConstants.ICON_END_PADDING,
                    bottom = IconConstants.ICON_BOTTOM_PADDING
                )
                .aspectRatio(IconConstants.ICON_ASPECT_RATIO)
                .clickable { setShowMenu(true) },
            painter = painterResource(id = R.drawable.ic_app_menu_24),
            contentDescription = "menu",
            tint = Color.White
        )
    }
}

data class ToolbarOriginData(
    val siteSecure: SiteSecurity,
    val siteTrackingProtection: SiteTrackingProtection,
    val url: String?,
    val searchTerms: String,
    val setEditMode: (Boolean) -> Unit
)

data class OriginBounds(
    val left: Dp,
    val right: Dp,
)

@Composable
fun RowScope.ToolbarOrigin(
    modifier: Modifier,
    toolbarOriginData: ToolbarOriginData,
    setOriginBounds: (OriginBounds) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp)
            .weight(1F)
//            .padding(all = 4.dp)
            .align(Alignment.CenterVertically)
            .background(Color.DarkGray, shape = MaterialTheme.shapes.small)
            .onGloballyPositioned { layoutCoordinates ->
                val left = layoutCoordinates.boundsInWindow().left.dp
                val right = layoutCoordinates.boundsInWindow().right.dp
                setOriginBounds(OriginBounds(left, right))
            },
    ) {
        // toolbar indicators
        with(toolbarOriginData) {
            ToolbarTrackingProtectionIndicator(trackingProtection = siteTrackingProtection)
            if (siteTrackingProtection != SiteTrackingProtection.OFF_GLOBALLY) ToolbarSeparator()
            ToolbarSecurityIndicator(siteSecure)
            if (url == null) ToolbarSeparator()
            ToolbarEmptyIndicator(enabled = url == null)
            // url
            Text(text = url ?: "",
                minLines = 1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                color = Color.White,
                modifier = Modifier
                    .padding(
                        end = IconConstants.INDICATOR_ICON_END_PADDING
                    )
                    .weight(1F)
                    .wrapContentHeight(Alignment.CenterVertically)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        setEditMode(true)
                    })
        }
    }
}

interface ToolbarOriginScope {
    @Composable
    fun ToolbarEmptyIndicator(enabled: Boolean)

    @Composable
    fun ToolbarTrackingProtectionIndicator(trackingProtection: SiteTrackingProtection?)

    @Composable
    fun ToolbarSecurityIndicator(siteSecurity: SiteSecurity)

    @Composable
    fun ToolbarSearchEngineSelector(
        currentSearchEngine: SearchEngine, showPopupMenu: (Boolean) -> Unit
    )
}

/**
 * Indicates which tracking protection status a site has.
 */
enum class SiteTrackingProtection {
    /**
     * The site has tracking protection enabled, but none trackers have been blocked or detected.
     */
    ON_NO_TRACKERS_BLOCKED,

    /**
     * The site has tracking protection enabled, and trackers have been blocked or detected.
     */
    ON_TRACKERS_BLOCKED,

    /**
     * Tracking protection has been disabled for a specific site.
     */
    OFF_FOR_A_SITE,

    /**
     * Tracking protection has been disabled for all sites.
     */
    OFF_GLOBALLY,
}

enum class SiteSecurity {
    INSECURE, SECURE,
}

object ToolbarOriginScopeInstance : ToolbarOriginScope {
    @Composable
    override fun ToolbarEmptyIndicator(enabled: Boolean) {
        if (enabled) Icon(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    start = IconConstants.INDICATOR_ICON_START_PADDING,
                    top = IconConstants.INDICATOR_ICON_TOP_PADDING,
                    end = IconConstants.INDICATOR_ICON_END_PADDING,
                    bottom = IconConstants.INDICATOR_ICON_BOTTOM_PADDING
                )
                .aspectRatio(IconConstants.ICON_ASPECT_RATIO),
            painter = painterResource(id = R.drawable.ic_search_24),
            contentDescription = "empty indicator",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarTrackingProtectionIndicator(trackingProtection: SiteTrackingProtection?) {
        when (trackingProtection) {
            SiteTrackingProtection.ON_TRACKERS_BLOCKED, SiteTrackingProtection.ON_NO_TRACKERS_BLOCKED -> {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(
                            start = IconConstants.INDICATOR_ICON_START_PADDING,
                            top = IconConstants.INDICATOR_ICON_TOP_PADDING,
                            end = IconConstants.INDICATOR_ICON_END_PADDING,
                            bottom = IconConstants.INDICATOR_ICON_BOTTOM_PADDING
                        )
                        .aspectRatio(IconConstants.ICON_ASPECT_RATIO),
                    painter = painterResource(id = R.drawable.ic_tracking_protection_on_trackers_blocked),
                    contentDescription = "tracking protection indicator",
                    tint = Color.White
                )
            }

            SiteTrackingProtection.OFF_FOR_A_SITE -> {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(
                            start = IconConstants.INDICATOR_ICON_START_PADDING,
                            top = IconConstants.INDICATOR_ICON_TOP_PADDING,
                            end = IconConstants.INDICATOR_ICON_END_PADDING,
                            bottom = IconConstants.INDICATOR_ICON_BOTTOM_PADDING
                        )
                        .aspectRatio(IconConstants.ICON_ASPECT_RATIO),
                    painter = painterResource(id = R.drawable.ic_tracking_protection_on_trackers_blocked),
                    contentDescription = "tracking protection indicator",
                    tint = Color.White
                )
            }

            SiteTrackingProtection.OFF_GLOBALLY -> {}

            else -> {}
        }
    }

    @Composable
    override fun ToolbarSecurityIndicator(siteSecurity: SiteSecurity) {
        if (siteSecurity == SiteSecurity.SECURE) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(
                        start = IconConstants.INDICATOR_ICON_START_PADDING,
                        top = IconConstants.INDICATOR_ICON_TOP_PADDING,
                        end = IconConstants.INDICATOR_ICON_END_PADDING,
                        bottom = IconConstants.INDICATOR_ICON_BOTTOM_PADDING
                    )
                    .aspectRatio(IconConstants.ICON_ASPECT_RATIO),
                painter = painterResource(id = R.drawable.ic_lock_20),
                contentDescription = "security indicator",
                tint = Color.White
            )
        } else if (siteSecurity == SiteSecurity.INSECURE) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(
                        start = IconConstants.INDICATOR_ICON_START_PADDING,
                        top = IconConstants.INDICATOR_ICON_TOP_PADDING,
                        end = IconConstants.INDICATOR_ICON_END_PADDING,
                        bottom = IconConstants.INDICATOR_ICON_BOTTOM_PADDING
                    )
                    .aspectRatio(IconConstants.ICON_ASPECT_RATIO),
                painter = painterResource(id = R.drawable.ic_broken_lock),
                contentDescription = "security indicator",
                tint = Color.White
            )
        }
    }

    @Composable
    override fun ToolbarSearchEngineSelector(
        currentSearchEngine: SearchEngine, showPopupMenu: (Boolean) -> Unit
    ) {
        Box(modifier = Modifier
            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            .fillMaxHeight()
            .focusable(false)
            .clickable { showPopupMenu(true) }) {
            Row(
                modifier = Modifier
                    .background(
                        color = Color.Black, shape = MaterialTheme.shapes.extraSmall
                    )
                    .fillMaxHeight(),
            ) {
                Image(
                    bitmap = currentSearchEngine.icon.asImageBitmap(),
                    contentDescription = "search engine icon",
//                    tint = Color.Red,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.extraSmall)
                        .padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                        .aspectRatio(1F),
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_down_24),
                    contentDescription = "open menu", modifier = Modifier.padding(horizontal = 4.dp).size(4.dp),
                    tint = Color.White,
                )
            }
        }
    }
}

interface ToolbarMenuItemsScope {
    // TODO: add add to home screen, add-ons, synced tabs, and report issue
    @Composable
    fun DividerToolbarMenuItem(modifier: Modifier = Modifier)

    @Composable
    fun ShareToolbarMenuItem()

    @Composable
    fun RequestDesktopSiteToolbarMenuItem(desktopMode: Boolean)

    @Composable
    fun FindInPageToolbarMenuItem(
        setBrowserComponentMode: (BrowserComponentMode) -> Unit, dismissMenuSheet: () -> Unit
    )

    @Composable
    fun SettingsToolbarMenuItem(onNavToSettings: () -> Unit)

    @Composable
    fun PrivateModeToolbarMenuItem(isPrivateMode: Boolean, dismissMenuSheet: () -> Unit)

    // stop, refresh, forward, back
    @Composable
    fun NavOptionsToolbarMenuItem(loading: Boolean)
}

object ToolbarMenuItemConstants {
    val OPTION_HEIGHT = 40.dp
    val OPTION_PADDING_START = 4.dp
    val OPTION_PADDING_TOP = 4.dp
    val OPTION_PADDING_END = 4.dp
    val OPTION_PADDING_BOTTOM = 4.dp
    val SHEET_HANDLE_HEIGHT = 2.5.dp
}

object ToolbarMenuItemsScopeInstance : ToolbarMenuItemsScope {
    @Composable
    override fun DividerToolbarMenuItem(modifier: Modifier) {
        HorizontalDivider(modifier = modifier, thickness = 0.25F.dp, color = Color.White)
    }

    @Composable
    override fun ShareToolbarMenuItem() {
        val context = LocalContext.current
        Box(contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(ToolbarMenuItemConstants.OPTION_HEIGHT)
                .padding(
                    start = ToolbarMenuItemConstants.OPTION_PADDING_START,
                    top = ToolbarMenuItemConstants.OPTION_PADDING_TOP,
                    end = ToolbarMenuItemConstants.OPTION_PADDING_END,
                    bottom = ToolbarMenuItemConstants.OPTION_PADDING_BOTTOM,
                )
                .clickable {
                    val url = context.components.core.store.state.selectedTab
                        ?.getUrl()
                        .orEmpty()
                    context.share(url)
                }) {
            Text(
                text = "Share", color = Color.White, modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun RequestDesktopSiteToolbarMenuItem(desktopMode: Boolean) {
        val useCases = sessionUseCases()
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(ToolbarMenuItemConstants.OPTION_HEIGHT)
                .fillMaxWidth()
                .padding(
                    start = ToolbarMenuItemConstants.OPTION_PADDING_START,
                    top = ToolbarMenuItemConstants.OPTION_PADDING_TOP,
//                    end = ToolbarMenuItemConstants.OPTION_PADDING_END,
                    bottom = ToolbarMenuItemConstants.OPTION_PADDING_BOTTOM,
                )
                .clickable { useCases.requestDesktopSite.invoke(!desktopMode) }) {
            Text(
                text = "Request Desktop Site",
                color = Color.White,
                modifier = Modifier.wrapContentHeight(),
            )
            InfernoCheckbox(
                checked = desktopMode,
                onCheckedChange = {},
                modifier = Modifier.padding(all = 0.dp),
            )
        }
    }

    @Composable
    override fun FindInPageToolbarMenuItem(
        setBrowserComponentMode: (BrowserComponentMode) -> Unit, dismissMenuSheet: () -> Unit
    ) {
        Box(contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(ToolbarMenuItemConstants.OPTION_HEIGHT)
                .padding(
                    start = ToolbarMenuItemConstants.OPTION_PADDING_START,
                    top = ToolbarMenuItemConstants.OPTION_PADDING_TOP,
                    end = ToolbarMenuItemConstants.OPTION_PADDING_END,
                    bottom = ToolbarMenuItemConstants.OPTION_PADDING_BOTTOM,
                )
                .clickable {
                    setBrowserComponentMode(BrowserComponentMode.FIND_IN_PAGE)
                    dismissMenuSheet()
                }) {
            Text(
                text = "Find In Page", color = Color.White, modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun SettingsToolbarMenuItem(onNavToSettings: () -> Unit) {


        val context = LocalContext.current
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(ToolbarMenuItemConstants.OPTION_HEIGHT)
                .padding(
                    start = ToolbarMenuItemConstants.OPTION_PADDING_START,
                    top = ToolbarMenuItemConstants.OPTION_PADDING_TOP,
                    end = ToolbarMenuItemConstants.OPTION_PADDING_END,
                    bottom = ToolbarMenuItemConstants.OPTION_PADDING_BOTTOM,
                )
                .fillMaxWidth()
                .clickable { onNavToSettings.invoke() },
        ) {
            Text(
                text = "Settings", color = Color.White, modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun PrivateModeToolbarMenuItem(isPrivateMode: Boolean, dismissMenuSheet: () -> Unit) {
        fun newTab(tabsUseCases: TabsUseCases, isPrivateSession: Boolean) {
            Log.d("BrowserTabBar", "newTab: isPrivateSession: $isPrivateSession")
            tabsUseCases.addTab(
                url = if (isPrivateSession) "inferno:privatebrowsing" else "inferno:home",
                selectTab = true,
                private = isPrivateSession
            )
        }

        val state = LocalContext.current.components.core.store.state
        val tabsUseCases = LocalContext.current.components.useCases.tabsUseCases
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(ToolbarMenuItemConstants.OPTION_HEIGHT)
                .fillMaxWidth()
                .padding(
                    start = ToolbarMenuItemConstants.OPTION_PADDING_START,
                    top = ToolbarMenuItemConstants.OPTION_PADDING_TOP,
//                    end = ToolbarMenuItemConstants.OPTION_PADDING_END,
                    bottom = ToolbarMenuItemConstants.OPTION_PADDING_BOTTOM,
                )
                .clickable {
                    if (isPrivateMode) {
                        // if private switch to normal tabs
                        val lastNormalTab = try {
                            state.normalTabs.last()
                        } catch (e: NoSuchElementException) {
                            null
                        }
                        if (lastNormalTab != null) {
                            // if previous tabs exist switch to last one
                            tabsUseCases.selectTab(lastNormalTab.id)
                        } else {
                            // if no normal tabs create new one
                            newTab(tabsUseCases, false)
                        }
                    } else {
                        // if normal mode switch to private tabs
                        val lastPrivateTab = try {
                            state.privateTabs.last()
                        } catch (e: NoSuchElementException) {
                            null
                        }
                        if (lastPrivateTab != null) {
                            // if private tabs exist switch to last one
                            tabsUseCases.selectTab(lastPrivateTab.id)
                        } else {
                            // if no private tabs exist create new one
                            newTab(tabsUseCases, true)
                        }
                    }

                    dismissMenuSheet()
                }) {
            Text(
                text = "Private Browsing Activated",
                color = Color.White,
                modifier = Modifier.wrapContentHeight(),
            )
            InfernoCheckbox(
                checked = isPrivateMode,
                onCheckedChange = {},
                modifier = Modifier.padding(all = 0.dp),
            )
        }
    }

    @Composable
    override fun NavOptionsToolbarMenuItem(loading: Boolean) {
        val tabSessionState = LocalContext.current.components.core.store.state.selectedTab
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(ToolbarMenuItemConstants.OPTION_HEIGHT)
                .fillMaxWidth()
        ) {
            ToolbarOptionsScopeInstance.ToolbarBack(
                enabled = tabSessionState?.content?.canGoBack ?: false
            )
            ToolbarOptionsScopeInstance.ToolbarForward(
                enabled = tabSessionState?.content?.canGoForward ?: false
            )
            ToolbarOptionsScopeInstance.ToolbarReload(enabled = tabSessionState != null, loading = loading)
            ToolbarOptionsScopeInstance.ToolbarStopLoading(enabled = tabSessionState != null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarMenuBottomSheet(
    tabSessionState: TabSessionState?,
    loading: Boolean,
    setShowBottomMenuSheet: (Boolean) -> Unit,
    setBrowserComponentMode: (BrowserComponentMode) -> Unit,
    onNavToSettings: () -> Unit,
) {
    if (tabSessionState == null) return
    ModalBottomSheet(
        onDismissRequest = { setShowBottomMenuSheet(false) },
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Black,
        scrimColor = Color.Black.copy(alpha = 0.1F),
        shape = RectangleShape,
        dragHandle = { /* no drag handle */
            // in case want to add one, make custom component centered in middle
//            BottomSheetDefaults.DragHandle(
//                color = Color.White,
//                height = ToolbarMenuItemConstants.SHEET_HANDLE_HEIGHT,
////            shape = RectangleShape,
//            )
        },
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // todo: move to bottom when switch to grid view
            // stop, refresh, forward, back
            NavOptionsToolbarMenuItem(loading)

            DividerToolbarMenuItem()

            ShareToolbarMenuItem()
            DividerToolbarMenuItem()

            PrivateModeToolbarMenuItem(isPrivateMode = tabSessionState.content.private,
                dismissMenuSheet = { setShowBottomMenuSheet(false) })
            DividerToolbarMenuItem()

            RequestDesktopSiteToolbarMenuItem(desktopMode = tabSessionState.content.desktopMode)
            DividerToolbarMenuItem()

            FindInPageToolbarMenuItem(setBrowserComponentMode = setBrowserComponentMode,
                dismissMenuSheet = { setShowBottomMenuSheet(false) })
            DividerToolbarMenuItem()

            SettingsToolbarMenuItem(onNavToSettings = onNavToSettings)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarSearchEngineSelectorPopupMenu(
    searchEngines: List<SearchEngine>, showPopupMenu: Boolean, setShowPopupMenu: (Boolean) -> Unit
) {
    fun setCurrentSearchEngine(context: Context, searchEngine: SearchEngine) {
        context.components.useCases.searchUseCases.selectSearchEngine.invoke(searchEngine)
    }

    val context = LocalContext.current
    DropdownMenu(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp),
        expanded = showPopupMenu,
        containerColor = Color.Black,
        onDismissRequest = { setShowPopupMenu(false) },
    ) {
        for (engine in searchEngines) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ToolbarMenuItemConstants.OPTION_HEIGHT)
                    .padding(
                        start = ToolbarMenuItemConstants.OPTION_PADDING_START,
                        top = ToolbarMenuItemConstants.OPTION_PADDING_TOP,
                        end = ToolbarMenuItemConstants.OPTION_PADDING_END,
                        bottom = ToolbarMenuItemConstants.OPTION_PADDING_BOTTOM,
                    )
                    .clickable {
                        setCurrentSearchEngine(context, engine)
                        setShowPopupMenu(false)
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    bitmap = engine.icon.asImageBitmap(),
                    contentDescription = "search engine icon",
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1F)
                        .padding(
                            horizontal = 4.dp,
                        )
                        .clip(MaterialTheme.shapes.extraSmall)

                )
                Text(
                    engine.name,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 4.dp)
                )
            }
//            DividerToolbarMenuItem()
        }

        DividerToolbarMenuItem()
        // TODO: search engine settings
    }
}
package com.shmibblez.inferno.toolbar

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.sessionUseCases
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.ext.getUrl
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.ktx.android.content.share


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
        onActivateFindInPage: () -> Unit, dismissMenuSheet: () -> Unit
    )

    @Composable
    fun SettingsToolbarMenuItem(onNavToSettings: () -> Unit)

    @Composable
    fun PrivateModeToolbarMenuItem(isPrivateMode: Boolean, dismissMenuSheet: () -> Unit)

    @Composable
    fun ReaderViewToolbarMenuItem(
        enabled: Boolean, onActivateReaderView: () -> Unit, dismissMenuSheet: () -> Unit
    )

    // stop, refresh, forward, back
    @Composable
    fun NavOptionsToolbarMenuItem(loading: Boolean)
}

object ToolbarMenuItemsScopeInstance : ToolbarMenuItemsScope {
    @Composable
    override fun DividerToolbarMenuItem(modifier: Modifier) {
        HorizontalDivider(modifier = modifier, thickness = 0.25.dp, color = Color.White)
    }

    @Composable
    override fun ShareToolbarMenuItem() {
        val context = LocalContext.current
        Box(contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
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
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable { useCases.requestDesktopSite.invoke(!desktopMode) }) {
            Text(
                text = "Request Desktop Site",
                color = Color.White,
                modifier = Modifier.wrapContentHeight(),
            )
            InfernoCheckbox(
                checked = desktopMode,
                onCheckedChange = {},
            )
        }
    }

    @Composable
    override fun FindInPageToolbarMenuItem(
        onActivateFindInPage: () -> Unit, dismissMenuSheet: () -> Unit
    ) {
        Box(contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable {
                    onActivateFindInPage()
                    dismissMenuSheet()
                }) {
            Text(
                text = "Find In Page", color = Color.White, modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun SettingsToolbarMenuItem(onNavToSettings: () -> Unit) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(OPTION_HEIGHT)
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
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
                },
        ) {
            Text(
                text = "Private Browsing Activated",
                color = Color.White,
                modifier = Modifier.wrapContentHeight(),
            )
            InfernoCheckbox(
                checked = isPrivateMode,
                onCheckedChange = {},
            )
        }
    }

    @Composable
    override fun ReaderViewToolbarMenuItem(
        enabled: Boolean, onActivateReaderView: () -> Unit, dismissMenuSheet: () -> Unit
    ) {
        Log.d("ReaderViewToolbarMenuIt", "enabled/readerable: $enabled")
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .alpha(if (enabled) 1F else 0.5F)
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable(enabled) {
                    onActivateReaderView.invoke()
                    dismissMenuSheet.invoke()
                },
        ) {
            Text(
                text = stringResource(R.string.browser_menu_turn_on_reader_view),
                color = Color.White,
                modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun NavOptionsToolbarMenuItem(loading: Boolean) {
        val tabSessionState = LocalContext.current.components.core.store.state.selectedTab
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolbarOptionsScopeInstance.ToolbarBack(
                enabled = tabSessionState?.content?.canGoBack ?: false,
            )
            ToolbarOptionsScopeInstance.ToolbarReload(
                enabled = tabSessionState != null, loading = loading,
            )
            ToolbarOptionsScopeInstance.ToolbarShare(
                url = tabSessionState?.content?.url,
            )
            ToolbarOptionsScopeInstance.ToolbarForward(
                enabled = tabSessionState?.content?.canGoForward ?: false,
            )
        }
    }
}
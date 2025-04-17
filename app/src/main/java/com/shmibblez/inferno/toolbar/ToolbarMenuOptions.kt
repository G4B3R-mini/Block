package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.sessionUseCases
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarBack
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarForward
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarReload
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarShare
import mozilla.components.browser.state.ext.getUrl
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.ktx.android.content.share

internal val TOOLBAR_MENU_ICON_SIZE = 24.dp

internal class ToolbarMenuOptions {
    companion object {
        @Composable
        private fun ToolbarMenuOptionTemplate(
            iconPainter: Painter,
            description: String,
            onClick: () -> Unit,
            enabled: Boolean = true,
        ) {
            Column(
                modifier = Modifier
                    .clickable(enabled = enabled, onClick = onClick)
                    .alpha(if (enabled) 1F else 0.5F),
//                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = iconPainter,
                    tint = Color.White,
                    contentDescription = description,
                    modifier = Modifier.size(TOOLBAR_MENU_ICON_SIZE)
                )
                InfernoText(
                    text = description,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3,
                    fontSize = 10.sp,
                    fontColor = Color.White,
                )
            }
        }

        @Composable
        internal fun LazyGridItemScope.ShareToolbarMenuItem() {
            val context = LocalContext.current
            val description = stringResource(R.string.share_header_2)
            ToolbarMenuOptionTemplate(
                iconPainter = painterResource(R.drawable.ic_share),
                description = description,
                onClick = {
                    val url = context.components.core.store.state.selectedTab?.getUrl().orEmpty()
                    context.share(url)
                },
            )
        }

        @Composable
        internal fun LazyGridItemScope.RequestDesktopSiteToolbarMenuItem(desktopMode: Boolean) {
            val useCases = sessionUseCases()
            val context = LocalContext.current
            val isDesktopSite =
                context.components.core.store.state.selectedTab?.content?.desktopMode
                    ?: context.components.core.store.state.desktopMode
            ToolbarMenuOptionTemplate(
                iconPainter = painterResource(
                    when (isDesktopSite) {
                        true -> R.drawable.mozac_ic_device_mobile_24
                        false -> R.drawable.mozac_ic_device_desktop_24
                    }
                ),
                description = stringResource(
                    when (isDesktopSite) {
                        true -> R.string.browser_menu_switch_to_mobile_site
                        false -> R.string.browser_menu_switch_to_desktop_site
                    }
                ),
                onClick = { useCases.requestDesktopSite.invoke(!desktopMode) },
            )
        }

        @Composable
        internal fun LazyGridItemScope.FindInPageToolbarMenuItem(
            onActivateFindInPage: () -> Unit, dismissMenuSheet: () -> Unit
        ) {
            ToolbarMenuOptionTemplate(
                iconPainter = painterResource(R.drawable.ic_search_24),
                description = stringResource(R.string.browser_menu_find_in_page),
                onClick = {
                    onActivateFindInPage.invoke()
                    dismissMenuSheet.invoke()
                },
            )
        }

        @Composable
        internal fun LazyGridItemScope.SettingsToolbarMenuItem(onNavToSettings: () -> Unit) {
            ToolbarMenuOptionTemplate(
                iconPainter = painterResource(R.drawable.mozac_ic_settings_24),
                description = stringResource(R.string.browser_menu_settings),
                onClick = onNavToSettings,
            )
        }

        @Composable
        internal fun LazyGridItemScope.PrivateModeToolbarMenuItem(
            isPrivateMode: Boolean, dismissMenuSheet: () -> Unit
        ) {
            fun newTab(tabsUseCases: TabsUseCases, isPrivateSession: Boolean) {
                tabsUseCases.addTab(
                    url = if (isPrivateSession) "inferno:privatebrowsing" else "inferno:home",
                    selectTab = true,
                    private = isPrivateSession
                )
            }

            val state = LocalContext.current.components.core.store.state
            val tabsUseCases = LocalContext.current.components.useCases.tabsUseCases

            ToolbarMenuOptionTemplate(
                iconPainter = painterResource(
                    when (isPrivateMode) {
                        true -> R.drawable.mozac_ic_globe_24
                        false -> R.drawable.ic_private_browsing
                    }
                ),
                description = stringResource(
                    when (isPrivateMode) {
                        true -> R.string.content_description_disable_private_browsing_button
                        false -> R.string.content_description_private_browsing_button
                    }
                ),
                onClick = {
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
            )
        }

        @Composable
        internal fun LazyGridItemScope.ReaderViewToolbarMenuItem(
            enabled: Boolean, onActivateReaderView: () -> Unit, dismissMenuSheet: () -> Unit
        ) {
            ToolbarMenuOptionTemplate(
                iconPainter = painterResource(R.drawable.mozac_ic_reader_view_24),
                description = stringResource(R.string.browser_menu_turn_on_reader_view),
                onClick = {
                    onActivateReaderView.invoke()
                    dismissMenuSheet.invoke()
                },
                enabled = enabled,
            )
        }

        @Composable
        internal fun LazyGridItemScope.NavOptionsToolbarMenuItem(loading: Boolean) {
            val tabSessionState = LocalContext.current.components.core.store.state.selectedTab
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .height(TOOLBAR_MENU_OPTION_HEIGHT)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolbarBack(
                    enabled = tabSessionState?.content?.canGoBack ?: false,
                )
                ToolbarReload(
                    enabled = tabSessionState != null, loading = loading,
                )
                ToolbarShare(
                    url = tabSessionState?.content?.url,
                )
                ToolbarForward(
                    enabled = tabSessionState?.content?.canGoForward ?: false,
                )
            }
        }
    }
}
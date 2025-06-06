package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.UiConst
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.sessionUseCases
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarBack
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarForward
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarReload
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarShare
import mozilla.components.browser.state.ext.getUrl
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.ktx.android.content.share

internal val TOOLBAR_ICON_SIZE = 18.dp
internal val TOOLBAR_SWITCH_ICON_SIZE = 8.dp
internal val TOOLBAR_SWITCH_ICON_EXTRA = 4.dp
internal val TOOLBAR_ICON_PADDING = 12.dp
internal val TOOLBAR_INDICATOR_ICON_SIZE = 16.dp
internal val TOOLBAR_INDICATOR_ICON_PADDING = 4.dp
internal val TOOLBAR_MENU_OPTION_HEIGHT = 40.dp

internal val TOOLBAR_MENU_ICON_SIZE = 24.dp
internal val TOOLBAR_MENU_SWITCH_ICON_SIZE =
    (TOOLBAR_SWITCH_ICON_SIZE / TOOLBAR_ICON_SIZE) * TOOLBAR_MENU_ICON_SIZE

private const val DISABLED_ALPHA = 0.5F

/**
 * @property ICON used to display icon inside toolbar, small and no description
 * @property EXPANDED used to display icon inside menu, larger and with description
 */
enum class ToolbarOptionType {
    ICON, EXPANDED
}

// todo: selected tab has rounded borders and bg color is lighter, unselected tabs will not be darkened by alpha,
//  just have vertical separator between them with top + bottom padding (4dp for now) and darker background (no border, just plain)
// todo: add search bar in BrowserComponent, shows up close to keyboard, awesomebar above
//  - add SearchBar in BrowserComponent, shown if user presses mini origin, keyboard pops up
//    - shown by default just above keyboard, awesome bar pops up above, settings to invert (bar above, awesomebar below)
//    - SearchBar has padding all around
//  - toolbar settings
//    - origin section: radio button with options origin (full size) and mini origin, one must be selected to be able to use search function
//    - toolbar actions section:
//      - for v1 make checkbox selection, with max n items (if more selected show toast saying max n can be selected), menu cannot be unselected
//        - user can reorder selected actions, preview is shown at top
//      - for v2 can make drag and drop, selected origin type is shown and cannot be removed, menu can also not be removed
//
// todo: user setting for toolbar top or bottom when vertical
// todo: user setting for toolbar left, right, top, or bottom when horizontal, for now just vertical
//  will need to add vertical tabs for horizontal mode

// current items:
// - ToolbarSettings
// - ToolbarOriginMini
// - ToolbarBack
// - ToolbarForward
// - ToolbarReload
// - ToolbarHistory
// - ToolbarRequestDesktopSite
// - ToolbarFindInPage
// - ToolbarRequestReaderView
// - ToolbarPrivateModeToggle
// - ToolbarShowTabsTray
// - ToolbarShare
// -
// - toolbar only:
//   - ToolbarMenuIcon
//   - ToolbarOrigin
// - menu only:
//   - NavOptions
// todo: left to add:
//   - ToolbarBookmarks (bookmark icon)
//   - ToolbarPrintPage (printer icon)
//   - ToolbarScrollingScreenshot (scan icon)
//   - ToolbarExtensions (extensions icon) - go to extensions page, installed
//   -

// todo: store toolbar items in settings, add item that converts string key to composable, when() for all keys possible

internal class ToolbarOnlyOptions {
    companion object {
        @Composable
        fun ToolbarMenuIcon(onShowMenuBottomSheet: () -> Unit) {
            InfernoIcon(
                modifier = Modifier
                    .size(TOOLBAR_ICON_SIZE)
                    .clickable(onClick = onShowMenuBottomSheet),
                painter = painterResource(id = R.drawable.ic_app_menu_24),
                contentDescription = stringResource(R.string.content_description_menu),
            )
        }

        @Composable
        fun ToolbarOrigin(
            tabSessionState: TabSessionState,
            searchEngine: SearchEngine?,
            siteSecure: SiteSecurity,
            siteTrackingProtection: SiteTrackingProtection,
            setAwesomeSearchText: (String) -> Unit,
            setOnAutocomplete: ((TextFieldValue) -> Unit) -> Unit,
            originModifier: Modifier = Modifier,
            editMode: Boolean,
            onStartSearch: () -> Unit,
            onStopSearch: () -> Unit,
            animationValue: Float,
        ) {
            BaseToolbarOrigin(
                originModifier = originModifier,
                tabSessionState = tabSessionState,
                searchEngine = searchEngine,
                setAwesomeSearchText = setAwesomeSearchText,
                setOnAutocomplete = setOnAutocomplete,
                siteSecure = siteSecure,
                siteTrackingProtection = siteTrackingProtection,
                editMode = editMode,
                onStartSearch = onStartSearch,
                onStopSearch = onStopSearch,
                animationValue = animationValue,
            )
        }
    }
}

internal class ToolbarOnlyComponents {
    companion object {
        /**
         * @param progress 0.0 is 0%, 1.0 is 100%
         */
        @Composable
        internal fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = modifier
                    .height(UiConst.PROGRESS_BAR_HEIGHT)
                    .fillMaxWidth(),
                color = LocalContext.current.infernoTheme().value.primaryActionColor,
                trackColor = Color.Transparent,
                drawStopIndicator = {}, // dont draw
            )
        }

        @Composable
        fun ToolbarSeparator() {
            VerticalDivider(
                modifier = Modifier.height(TOOLBAR_ICON_SIZE),
                color = LocalContext.current.infernoTheme().value.primaryIconColor,
                thickness = 1.dp,
            )
        }
    }
}

internal class MenuOnlyComponents {
    companion object {
        @Composable
        internal fun NavOptions(loading: Boolean) {
            val tabSessionState = LocalContext.current.components.core.store.state.selectedTab
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
//                    .height(TOOLBAR_MENU_OPTION_HEIGHT)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolbarBack(
                    type = ToolbarOptionType.ICON,
                    enabled = tabSessionState?.content?.canGoBack ?: false,
                )
                ToolbarReload(
                    type = ToolbarOptionType.ICON,
                    enabled = tabSessionState != null, loading = loading,
                )
                ToolbarShare(
                    type = ToolbarOptionType.ICON,
                )
                ToolbarForward(
                    type = ToolbarOptionType.ICON,
                    enabled = tabSessionState?.content?.canGoForward ?: false,
                )
            }
        }
    }
}

class ToolbarOptions {
    companion object {

        /**
         * @param icon icon composable, must use params provided to be displayed properly
         */
        @Composable
        private fun ToolbarOptionTemplate(
            icon: @Composable (modifier: Modifier, extraPadding: Dp, contentDescription: String) -> Unit,
            description: String,
            contentDescription: String = description,
            onClick: () -> Unit,
            enabled: Boolean = true,
            type: ToolbarOptionType,
        ) {
            when (type) {
                ToolbarOptionType.ICON -> {
                    icon.invoke(
                        Modifier
                            .size(TOOLBAR_ICON_SIZE + TOOLBAR_SWITCH_ICON_EXTRA)
                            .alpha(if (enabled) 1F else DISABLED_ALPHA)
                            .clickable(enabled = enabled, onClick = onClick),
                        TOOLBAR_SWITCH_ICON_EXTRA / 2,
                        contentDescription,
                    )
                }

                ToolbarOptionType.EXPANDED -> {
                    Column(
                        modifier = Modifier
                            .clickable(enabled = enabled, onClick = onClick)
                            .alpha(if (enabled) 1F else DISABLED_ALPHA),
//                    .padding(horizontal = 8.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp - TOOLBAR_SWITCH_ICON_EXTRA, Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        icon.invoke(
                            Modifier.size(TOOLBAR_MENU_ICON_SIZE + TOOLBAR_SWITCH_ICON_EXTRA),
                            TOOLBAR_SWITCH_ICON_EXTRA,
                            contentDescription,
                        )
                        InfernoText(
                            text = description,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 3,
                            fontSize = 10.sp,
                            fontColor = LocalContext.current.infernoTheme().value.primaryIconColor,
                        )
                    }
                }
            }
        }

        @Composable
        private fun ToolbarOptionTemplate(
            iconPainter: Painter,
            description: String,
            contentDescription: String = description,
            onClick: () -> Unit,
            enabled: Boolean = true,
            type: ToolbarOptionType,
        ) {
            when (type) {
                ToolbarOptionType.ICON -> {
                    InfernoIcon(
                        modifier = Modifier
                            .size(TOOLBAR_ICON_SIZE)
                            .alpha(if (enabled) 1F else DISABLED_ALPHA)
                            .clickable(enabled = enabled, onClick = onClick),
                        painter = iconPainter,
                        contentDescription = contentDescription,
                    )
                }

                ToolbarOptionType.EXPANDED -> {
                    Column(
                        modifier = Modifier
                            .clickable(enabled = enabled, onClick = onClick)
                            .alpha(if (enabled) 1F else DISABLED_ALPHA),
//                    .padding(horizontal = 8.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp, Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        InfernoIcon(
                            painter = iconPainter,
                            contentDescription = contentDescription,
                            modifier = Modifier.size(TOOLBAR_MENU_ICON_SIZE),
                        )
                        InfernoText(
                            text = description,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 3,
                            fontSize = 10.sp,
                            fontColor = LocalContext.current.infernoTheme().value.primaryIconColor,
                        )
                    }
                }
            }
        }

        @Composable
        fun ToolbarSettings(
            type: ToolbarOptionType,
            onNavToSettings: () -> Unit,
        ) {
            ToolbarOptionTemplate(
                iconPainter = painterResource(R.drawable.ic_settings_24),
                description = stringResource(R.string.browser_menu_settings),
                onClick = onNavToSettings,
                type = type,
            )
        }

        @Composable
        fun ToolbarOriginMini(
            type: ToolbarOptionType,
            onRequestSearchBar: () -> Unit,
        ) {
            ToolbarOptionTemplate(
                icon = { modifier, extraPadding, contentDescription ->
                    Box(
                        modifier = modifier,
                    ) {
                        // current mode, big icon
                        InfernoIcon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                                .padding(extraPadding),
                            painter = painterResource(id = R.drawable.ic_globe_24),
                            contentDescription = contentDescription,
                        )
                        // switch to, smol icon
                        InfernoIcon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
                                .padding(2.dp)
                                .size(
                                    when (type) {
                                        ToolbarOptionType.ICON -> TOOLBAR_SWITCH_ICON_SIZE
                                        ToolbarOptionType.EXPANDED -> TOOLBAR_MENU_SWITCH_ICON_SIZE
                                    }
                                )
                                .align(Alignment.BottomEnd),
                            painter = painterResource(id = R.drawable.ic_search_24),
                            contentDescription = contentDescription,
                        )
                    }
                },
                description = stringResource(R.string.search_hint),
                onClick = onRequestSearchBar,
                type = type,
            )
        }

        @Composable
        fun ToolbarBack(type: ToolbarOptionType, enabled: Boolean, onClick: (() -> Unit)? = null) {
            val useCases = sessionUseCases()
            ToolbarOptionTemplate(
                iconPainter = painterResource(id = R.drawable.ic_chevron_left_24),
                description = stringResource(R.string.browser_menu_back),
                onClick = {
                    useCases.goBack.invoke()
                    onClick?.invoke()
                },
                enabled = enabled,
                type = type,
            )
        }

        @Composable
        fun ToolbarForward(
            type: ToolbarOptionType,
            enabled: Boolean,
            onClick: (() -> Unit)? = null,
        ) {
            val useCases = sessionUseCases()
            ToolbarOptionTemplate(
                iconPainter = painterResource(id = R.drawable.ic_chevron_right_24),
                description = stringResource(R.string.browser_menu_forward),
                onClick = {
                    useCases.goForward.invoke()
                    onClick?.invoke()
                },
                enabled = enabled,
                type = type,
            )
        }

        @Composable
        fun ToolbarReload(
            type: ToolbarOptionType,
            enabled: Boolean,
            loading: Boolean,
            dismissMenuSheet: (() -> Unit)? = null,
            onClick: (() -> Unit)? = null,
        ) {
            val useCases = sessionUseCases()
            ToolbarOptionTemplate(
                iconPainter = when (loading) {
                    true -> painterResource(id = R.drawable.ic_cross_24)
                    false -> painterResource(id = R.drawable.ic_arrow_clockwise_24)
                },
                description = when (loading) {
                    true -> stringResource(android.R.string.cancel)
                    false -> stringResource(R.string.browser_menu_refresh)
                },
                onClick = {
                    if (loading) useCases.stopLoading.invoke() else useCases.reload.invoke()
                    dismissMenuSheet?.invoke()
                    onClick?.invoke()
                },
                enabled = enabled,
                type = type,
            )
        }

        @Composable
        fun ToolbarHistory(
            type: ToolbarOptionType,
            onNavToHistory: () -> Unit,
            dismissMenuSheet: (() -> Unit)? = null,
        ) {
            ToolbarOptionTemplate(
                iconPainter = painterResource(R.drawable.ic_history_24),
                description = stringResource(R.string.library_history),
                onClick = {
                    onNavToHistory.invoke()
                    dismissMenuSheet?.invoke()
                },
                enabled = true,
                type = type,
            )
        }

        @Composable
        fun ToolbarRequestDesktopSite(
            type: ToolbarOptionType,
            desktopMode: Boolean,
            dismissMenuSheet: (() -> Unit)? = null,
        ) {
            val useCases = sessionUseCases()
            val context = LocalContext.current
            val isDesktopSite =
                context.components.core.store.state.selectedTab?.content?.desktopMode
                    ?: context.components.core.store.state.desktopMode
            ToolbarOptionTemplate(
                icon = { modifier, extraPadding, contentDescription ->
                    Box(
                        modifier = modifier,
                    ) {
                        // current mode, big icon
                        InfernoIcon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                                .padding(extraPadding),
                            painter = when (isDesktopSite) {
                                true -> painterResource(id = R.drawable.ic_device_desktop_24)
                                false -> painterResource(id = R.drawable.ic_device_mobile_24)
                            },
                            contentDescription = contentDescription,
                        )
                        // switch to, smol icon
                        InfernoIcon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
                                .padding(2.dp)
                                .size(
                                    when (type) {
                                        ToolbarOptionType.ICON -> TOOLBAR_SWITCH_ICON_SIZE
                                        ToolbarOptionType.EXPANDED -> TOOLBAR_MENU_SWITCH_ICON_SIZE
                                    }
                                )
                                .align(Alignment.BottomEnd),
                            painter = when (isDesktopSite) {
                                true -> painterResource(id = R.drawable.ic_device_mobile_24)
                                false -> painterResource(id = R.drawable.ic_device_desktop_24)
                            },
                            contentDescription = contentDescription,
                        )
                    }
                },
                description = stringResource(
                    when (isDesktopSite) {
                        true -> R.string.browser_menu_switch_to_mobile_site
                        false -> R.string.browser_menu_switch_to_desktop_site
                    }
                ),
                onClick = {
                    useCases.requestDesktopSite.invoke(!desktopMode)
                    dismissMenuSheet?.invoke()
                },
                type = type,
            )
        }

        @Composable
        fun ToolbarFindInPage(
            type: ToolbarOptionType,
            onActivateFindInPage: () -> Unit,
            dismissMenuSheet: () -> Unit,
        ) {
            ToolbarOptionTemplate(
                iconPainter = painterResource(R.drawable.ic_search_24),
                description = stringResource(R.string.browser_menu_find_in_page),
                onClick = {
                    onActivateFindInPage.invoke()
                    dismissMenuSheet.invoke()
                },
                type = type,
            )
        }

        @Composable
        fun ToolbarRequestReaderView(
            type: ToolbarOptionType,
            enabled: Boolean,
            dismissMenuSheet: (() -> Unit)? = null,
            onActivateReaderView: () -> Unit,
        ) {
            ToolbarOptionTemplate(
                iconPainter = painterResource(R.drawable.ic_reader_view_24),
                description = stringResource(R.string.browser_menu_turn_on_reader_view),
                onClick = {
                    onActivateReaderView.invoke()
                    dismissMenuSheet?.invoke()
                },
                enabled = enabled,
                type = type,
            )
        }

        @Composable
        fun ToolbarPrivateModeToggle(
            type: ToolbarOptionType,
            isPrivateMode: Boolean,
            dismissMenuSheet: () -> Unit = {},
        ) {
            fun newTab(tabsUseCases: TabsUseCases, isPrivateSession: Boolean) {
                tabsUseCases.addTab(
                    url = if (isPrivateSession) "inferno:privatebrowsing" else "inferno:home",
                    selectTab = true,
                    private = isPrivateSession,
                )
            }

            val state = LocalContext.current.components.core.store.state
            val tabsUseCases = LocalContext.current.components.useCases.tabsUseCases

            fun disablePrivateMode() {
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
            }

            fun enablePrivateMode() {
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

            ToolbarOptionTemplate(
                icon = { modifier, extraPadding, contentDescription ->
                    Box(
                        modifier = modifier,
                    ) {
                        // current mode, big icon
                        InfernoIcon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                                .padding(extraPadding),
                            painter = when (isPrivateMode) {
                                true -> painterResource(id = R.drawable.ic_private_browsing)
                                false -> painterResource(id = R.drawable.ic_globe_24)
                            },
                            contentDescription = contentDescription,
                        )
                        // switch to, smol icon
                        InfernoIcon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
                                .padding(2.dp)
                                .size(
                                    when (type) {
                                        ToolbarOptionType.ICON -> TOOLBAR_SWITCH_ICON_SIZE
                                        ToolbarOptionType.EXPANDED -> TOOLBAR_MENU_SWITCH_ICON_SIZE
                                    }
                                )
                                .align(Alignment.BottomEnd),
                            painter = when (isPrivateMode) {
                                true -> painterResource(id = R.drawable.ic_globe_24)
                                false -> painterResource(id = R.drawable.ic_private_browsing)
                            },
                            contentDescription = contentDescription,
                        )
                    }
                },
                description = stringResource(
                    when (isPrivateMode) {
                        true -> R.string.content_description_disable_private_browsing_button
                        false -> R.string.content_description_private_browsing_button
                    }
                ),
                onClick = {
                    if (isPrivateMode) {
                        disablePrivateMode()
                    } else {
                        enablePrivateMode()
                    }

                    dismissMenuSheet()
                },
                type = type,
            )
        }

        @Composable
        fun ToolbarShowTabsTray(
            type: ToolbarOptionType,
            tabCount: Int,
            dismissMenuSheet: (() -> Unit)? = null,
            onNavToTabsTray: () -> Unit,
        ) {
            ToolbarOptionTemplate(
                icon = { modifier, extraPadding, contentDescription ->
                    Box(
                        modifier = modifier.wrapContentHeight(unbounded = true),
                        contentAlignment = Alignment.Center,
                    ) {
                        InfernoIcon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                                .padding(extraPadding),
                            painter = painterResource(id = R.drawable.ic_tabcounter_box_24),
                            contentDescription = contentDescription,
                        )
                        InfernoText(
                            modifier = Modifier.fillMaxSize(),
                            text = tabCount.toString(),
                            fontColor = LocalContext.current.infernoTheme().value.primaryIconColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                description = when (tabCount) {
                    1 -> stringResource(R.string.mozac_tab_counter_open_tab_tray_single)
                    else -> stringResource(
                        R.string.mozac_tab_counter_open_tab_tray_plural, tabCount
                    )
                },
                onClick = {
                    onNavToTabsTray.invoke()
                    dismissMenuSheet?.invoke()
                },
                type = type,
            )
        }

        @Composable
        fun ToolbarShare(type: ToolbarOptionType, onClick: (() -> Unit)? = null) {
            val context = LocalContext.current
            ToolbarOptionTemplate(
                iconPainter = painterResource(R.drawable.ic_share_24),
                description = stringResource(R.string.share_header_2),
                onClick = {
                    val url = context.components.core.store.state.selectedTab?.getUrl().orEmpty()
                    context.share(url)
                    onClick?.invoke()
                },
                type = type,
            )
        }
    }
}

class ToolbarOptionsIcons {
    companion object {
        /**
         * @param icon icon composable, must use params provided to be displayed properly
         */
        @Composable
        private fun ToolbarIconTemplate(
            icon: @Composable (modifier: Modifier, extraPadding: Dp, contentDescription: String, tint: Color) -> Unit,
            contentDescription: String,
            tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor,
        ) {
            icon.invoke(
                Modifier.size(TOOLBAR_ICON_SIZE + TOOLBAR_SWITCH_ICON_EXTRA),
                TOOLBAR_SWITCH_ICON_EXTRA / 2,
                contentDescription,
                tint,
            )
        }

        @Composable
        private fun ToolbarIconTemplate(
            iconPainter: Painter,
            contentDescription: String,
            tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor,
        ) {
            InfernoIcon(
                modifier = Modifier.size(TOOLBAR_ICON_SIZE),
                painter = iconPainter,
                contentDescription = contentDescription,
                tint = tint,
            )
        }

        @Composable
        fun ToolbarSettings(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(R.drawable.ic_settings_24),
                contentDescription = stringResource(R.string.browser_menu_settings),
                tint = tint,
            )
        }

        @Composable
        fun ToolbarOriginMini(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                tint = tint,
                icon = { modifier, extraPadding, contentDescription, iconTint ->
                    Box(modifier = modifier) {
                        // current mode, big icon
                        InfernoIcon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                                .padding(extraPadding),
                            painter = painterResource(id = R.drawable.ic_globe_24),
                            contentDescription = contentDescription,
                            tint = iconTint,
                        )
                        // switch to, smol icon
                        InfernoIcon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
                                .padding(2.dp)
                                .size(TOOLBAR_SWITCH_ICON_SIZE)
                                .align(Alignment.BottomEnd),
                            painter = painterResource(id = R.drawable.ic_search_24),
                            contentDescription = contentDescription,
                            tint = iconTint,
                        )
                    }
                },
                contentDescription = stringResource(R.string.search_hint),
            )
        }

        @Composable
        fun ToolbarBack(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(id = R.drawable.ic_chevron_left_24),
                contentDescription = stringResource(R.string.browser_menu_back),
                tint = tint,
            )
        }

        @Composable
        fun ToolbarForward(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(id = R.drawable.ic_chevron_right_24),
                contentDescription = stringResource(R.string.browser_menu_forward),
                tint = tint,
            )
        }

        @Composable
        fun ToolbarReload(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(id = R.drawable.ic_arrow_clockwise_24),
                contentDescription = stringResource(R.string.browser_menu_refresh),
                tint = tint,
            )
        }

        @Composable
        fun ToolbarHistory(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(R.drawable.ic_history_24),
                contentDescription = stringResource(R.string.library_history),
                tint = tint,
            )
        }

        @Composable
        fun ToolbarRequestDesktopSite(
            tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor,
            variant: Boolean = false,
        ) {
            ToolbarIconTemplate(
                tint = tint,
                icon = { modifier, extraPadding, contentDescription, iconTint ->
                    Box(modifier = modifier) {
                        // current mode, big icon
                        InfernoIcon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                                .padding(extraPadding),
                            painter = painterResource(
                                id = when (variant) {
                                    false -> R.drawable.ic_device_mobile_24
                                    true -> R.drawable.ic_device_desktop_24
                                }
                            ),
                            contentDescription = contentDescription,
                            tint = iconTint,
                        )
                        // switch to, smol icon
                        InfernoIcon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
                                .padding(2.dp)
                                .size(TOOLBAR_SWITCH_ICON_SIZE)
                                .align(Alignment.BottomEnd),
                            painter = painterResource(
                                id = when (variant) {
                                    false -> R.drawable.ic_device_desktop_24
                                    true -> R.drawable.ic_device_mobile_24
                                }
                            ),
                            contentDescription = contentDescription,
                            tint = iconTint,
                        )
                    }
                },
                contentDescription = stringResource(R.string.browser_menu_switch_to_desktop_site),
            )
        }

        @Composable
        fun ToolbarFindInPage(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(R.drawable.ic_search_24),
                contentDescription = stringResource(R.string.browser_menu_find_in_page),
                tint = tint,
            )
        }

        @Composable
        fun ToolbarRequestReaderView(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(R.drawable.ic_reader_view_24),
                contentDescription = stringResource(R.string.browser_menu_turn_on_reader_view),
                tint = tint,
            )
        }

        @Composable
        fun ToolbarPrivateModeToggle(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                tint = tint,
                icon = { modifier, extraPadding, contentDescription, iconTint ->
                    Box(
                        modifier = modifier,
                    ) {
                        // current mode, big icon
                        InfernoIcon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                                .padding(extraPadding),
                            painter = painterResource(id = R.drawable.ic_globe_24),
                            contentDescription = contentDescription,
                            tint = iconTint,
                        )
                        // switch to, smol icon
                        InfernoIcon(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
                                .padding(2.dp)
                                .size(TOOLBAR_SWITCH_ICON_SIZE)
                                .align(Alignment.BottomEnd),
                            painter = painterResource(id = R.drawable.ic_private_browsing),
                            contentDescription = contentDescription,
                            tint = iconTint,
                        )
                    }
                },
                contentDescription = stringResource(R.string.content_description_private_browsing_button),
            )
        }

        @Composable
        fun ToolbarShowTabsTray(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                tint = tint,
                icon = { modifier, extraPadding, contentDescription, iconTint ->
                    Box(
                        modifier = modifier.wrapContentHeight(unbounded = true),
                        contentAlignment = Alignment.Center,
                    ) {
                        InfernoIcon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxSize()
                                .padding(extraPadding),
                            painter = painterResource(id = R.drawable.ic_tabcounter_box_24),
                            contentDescription = contentDescription,
                            tint = iconTint,
                        )
                        InfernoText(
                            modifier = Modifier.fillMaxSize(),
                            text = "4",
                            fontColor = iconTint,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                contentDescription = stringResource(R.string.mozac_tab_counter_open_tab_tray_single),
            )
        }

        @Composable
        fun ToolbarShare(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(R.drawable.ic_share_24),
                contentDescription = stringResource(R.string.share_header_2),
                tint = tint,
            )
        }

        @Composable
        fun ToolbarMenuIcon(tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor) {
            ToolbarIconTemplate(
                iconPainter = painterResource(R.drawable.ic_app_menu_24),
                contentDescription = stringResource(R.string.content_description_menu),
                tint = tint,
            )
        }
    }
}
package com.shmibblez.inferno.toolbar

import androidx.compose.runtime.Composable
import com.shmibblez.inferno.toolbar.ToolbarOnlyOptions.Companion.ToolbarMenuIcon
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarBack
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarFindInPage
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarForward
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarHistory
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarOriginMini
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarPrivateModeToggle
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarReload
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarRequestDesktopSite
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarRequestReaderView
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarSettings
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarShare
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarShowTabsTray
import mozilla.components.browser.state.state.TabSessionState

internal enum class ToolbarItemKey {
    toolbar_item_settings,
    toolbar_item_origin,
    toolbar_item_origin_mini,
    toolbar_item_back,
    toolbar_item_forward,
    toolbar_item_reload,
    toolbar_item_history,
    toolbar_item_request_desktop,
    toolbar_item_find_in_page,
    toolbar_item_request_reader_view,
    toolbar_item_private_mode,
    toolbar_item_show_tabs_tray,
    toolbar_item_share,
    toolbar_item_menu;

    companion object
}

//internal object ToolbarItemKey {
//    const val TOOLBAR_ITEM_SETTINGS = "toolbar_item_settings"
//    const val TOOLBAR_ITEM_BACK = "toolbar_item_back"
//    const val TOOLBAR_ITEM_FORWARD = "toolbar_item_forward"
//    const val TOOLBAR_ITEM_RELOAD = "toolbar_item_reload"
//    const val TOOLBAR_ITEM_REQUEST_DESKTOP = "toolbar_item_request_desktop"
//    const val TOOLBAR_ITEM_FIND_IN_PAGE = "toolbar_item_find_in_page"
//    const val TOOLBAR_ITEM_REQUEST_READER_VIEW = "toolbar_item_request_reader_view"
//    const val TOOLBAR_ITEM_PRIVATE_MODE = "toolbar_item_private_mode"
//    const val TOOLBAR_ITEM_SHOW_TABS_TRAY = "toolbar_item_show_tabs_tray"
//    const val TOOLBAR_ITEM_SHARE = "toolbar_item_share"
//    const val TOOLBAR_ITEM_MENU = "toolbar_item_menu"
//}

internal fun ToolbarItemKey.Companion.fromString(key: String): ToolbarItemKey {
    return when (key) {
        "toolbar_item_settings" -> ToolbarItemKey.toolbar_item_settings
        "toolbar_item_origin" -> ToolbarItemKey.toolbar_item_origin
        "toolbar_item_origin_mini" -> ToolbarItemKey.toolbar_item_origin_mini
        "toolbar_item_back" -> ToolbarItemKey.toolbar_item_back
        "toolbar_item_forward" -> ToolbarItemKey.toolbar_item_forward
        "toolbar_item_reload" -> ToolbarItemKey.toolbar_item_reload
        "toolbar_item_history" -> ToolbarItemKey.toolbar_item_history
        "toolbar_item_request_desktop" -> ToolbarItemKey.toolbar_item_request_desktop
        "toolbar_item_find_in_page" -> ToolbarItemKey.toolbar_item_find_in_page
        "toolbar_item_request_reader_view" -> ToolbarItemKey.toolbar_item_request_reader_view
        "toolbar_item_private_mode" -> ToolbarItemKey.toolbar_item_private_mode
        "toolbar_item_show_tabs_tray" -> ToolbarItemKey.toolbar_item_show_tabs_tray
        "toolbar_item_share" -> ToolbarItemKey.toolbar_item_share
        "toolbar_item_menu" -> ToolbarItemKey.toolbar_item_menu
        else -> {
            throw IllegalArgumentException("unknown key $key provided, make sure member of ${ToolbarItemKey::class.simpleName}")
        }
    }
}

internal fun ToolbarItemKey.Companion.fromStrings(keys: List<String>): List<ToolbarItemKey> {
    return List(size = keys.size, init = { ToolbarItemKey.fromString(keys[it]) })
}

internal class ToolbarItems {
    companion object {
        val defaultToolbarItemKeysStr = listOf(
            ToolbarItemKey.toolbar_item_back,
            ToolbarItemKey.toolbar_item_forward,
            ToolbarItemKey.toolbar_item_origin,
            ToolbarItemKey.toolbar_item_reload,
            ToolbarItemKey.toolbar_item_show_tabs_tray,
            ToolbarItemKey.toolbar_item_menu,
        ).map { it.name }

        val defaultToolbarItemKeys = ToolbarItemKey.fromStrings(defaultToolbarItemKeysStr)

        fun fromKeys(
            keys: List<ToolbarItemKey>,
            // item params
            type: ToolbarOptionType,
            tabSessionState: TabSessionState,
            loading: Boolean,
            tabCount: Int,
            onShowMenuBottomSheet: () -> Unit,
            onDismissMenuBottomSheet: () -> Unit,
            onRequestSearchBar: () -> Unit,
            onActivateFindInPage: () -> Unit,
            onActivateReaderView: () -> Unit,
            onNavToSettings: () -> Unit,
            onNavToHistory: () -> Unit,
            onNavToTabsTray: () -> Unit,
//            // origin params
//            searchEngine: SearchEngine,
//            siteSecure: SiteSecurity,
//            siteTrackingProtection: SiteTrackingProtection,
//            setAwesomeSearchText: (String) -> Unit,
//            setOnAutocomplete: ((TextFieldValue) -> Unit) -> Unit,
//            originModifier: Modifier = Modifier,
//            editMode: Boolean,
//            onStartSearch: () -> Unit,
//            onStopSearch: () -> Unit,
//            animationValue: Float,
        ): List<@Composable () -> Unit> {
            return List(
                size = keys.size,
                init = {
                    {
                        ToolbarItem(
                            key = keys[it],
                            type = type,
                            tabSessionState = tabSessionState,
                            loading = loading,
                            tabCount = tabCount,
                            onShowMenuBottomSheet = onShowMenuBottomSheet,
                            onDismissMenuBottomSheet = onDismissMenuBottomSheet,
                            onRequestSearchBar = onRequestSearchBar,
                            onActivateFindInPage = onActivateFindInPage,
                            onActivateReaderView = onActivateReaderView,
                            onNavToSettings = onNavToSettings,
                            onNavToHistory = onNavToHistory,
                            onNavToTabsTray = onNavToTabsTray,
//                            searchEngine = searchEngine,
//                            siteSecure = siteSecure,
//                            siteTrackingProtection = siteTrackingProtection,
//                            setAwesomeSearchText = setAwesomeSearchText,
//                            setOnAutocomplete = setOnAutocomplete,
//                            originModifier = originModifier,
//                            editMode = editMode,
//                            onStartSearch = onStartSearch,
//                            onStopSearch = onStopSearch,
//                            animationValue = animationValue,
                        )
                    }
                },
            )
        }
    }
}

@Composable
internal fun ToolbarItem(
    key: ToolbarItemKey,
    type: ToolbarOptionType,
    tabSessionState: TabSessionState,
    loading: Boolean,
    tabCount: Int,
    onShowMenuBottomSheet: () -> Unit,
    onDismissMenuBottomSheet: () -> Unit,
    onRequestSearchBar: () -> Unit,
    onActivateFindInPage: () -> Unit,
    onActivateReaderView: () -> Unit,
    onNavToSettings: () -> Unit,
    onNavToHistory: () -> Unit,
    onNavToTabsTray: () -> Unit,
//    // origin params
//    searchEngine: SearchEngine,
//    siteSecure: SiteSecurity,
//    siteTrackingProtection: SiteTrackingProtection,
//    setAwesomeSearchText: (String) -> Unit,
//    setOnAutocomplete: ((TextFieldValue) -> Unit) -> Unit,
//    originModifier: Modifier = Modifier,
//    editMode: Boolean,
//    onStartSearch: () -> Unit,
//    onStopSearch: () -> Unit,
//    animationValue: Float,
) {
    when (key) {
        ToolbarItemKey.toolbar_item_settings -> {
            ToolbarSettings(
                type = type,
                onNavToSettings = onNavToSettings,
            )
        }

        ToolbarItemKey.toolbar_item_origin -> {
            when (type) {
                ToolbarOptionType.ICON -> {
//                    ToolbarOrigin(
//                        tabSessionState = tabSessionState,
//                        searchEngine = searchEngine,
//                        siteSecure = siteSecure,
//                        siteTrackingProtection = siteTrackingProtection,
//                        setAwesomeSearchText = setAwesomeSearchText,
//                        setOnAutocomplete = setOnAutocomplete,
//                        originModifier = originModifier,
//                        editMode = editMode,
//                        onStartSearch = onStartSearch,
//                        onStopSearch = onStopSearch,
//                        animationValue = animationValue,
//                    )
                }

                ToolbarOptionType.EXPANDED -> {} // no-op, origin can only be shown in toolbar
            }
        }

        ToolbarItemKey.toolbar_item_origin_mini -> {
            ToolbarOriginMini(
                type = type,
                onRequestSearchBar = onRequestSearchBar,
            )
        }

        ToolbarItemKey.toolbar_item_back -> {
            ToolbarBack(
                type = type,
                enabled = tabSessionState.content.canGoBack,
            )
        }

        ToolbarItemKey.toolbar_item_forward -> {
            ToolbarForward(
                type = type,
                enabled = tabSessionState.content.canGoForward,
            )
        }

        ToolbarItemKey.toolbar_item_reload -> {
            ToolbarReload(
                type = type,
                enabled = true,
                loading = loading,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        ToolbarItemKey.toolbar_item_history -> {
            ToolbarHistory(
                type = type,
                onNavToHistory = onNavToHistory,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        ToolbarItemKey.toolbar_item_request_desktop -> {
            ToolbarRequestDesktopSite(
                type = type,
                desktopMode = tabSessionState.content.desktopMode,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        ToolbarItemKey.toolbar_item_find_in_page -> {
            ToolbarFindInPage(
                type = type,
                onActivateFindInPage = onActivateFindInPage,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        ToolbarItemKey.toolbar_item_request_reader_view -> {
            ToolbarRequestReaderView(
                type = type,
                enabled = tabSessionState.readerState.readerable,
                dismissMenuSheet = onDismissMenuBottomSheet,
                onActivateReaderView = onActivateReaderView,
            )
        }

        ToolbarItemKey.toolbar_item_private_mode -> {
            ToolbarPrivateModeToggle(
                type = type,
                isPrivateMode = tabSessionState.content.private,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        ToolbarItemKey.toolbar_item_show_tabs_tray -> {
            ToolbarShowTabsTray(
                type = type,
                tabCount = tabCount,
                dismissMenuSheet = onDismissMenuBottomSheet,
                onNavToTabsTray = onNavToTabsTray,
            )
        }

        ToolbarItemKey.toolbar_item_share -> {
            ToolbarShare(type = ToolbarOptionType.EXPANDED)
        }

        ToolbarItemKey.toolbar_item_menu -> {
            when (type) {
                ToolbarOptionType.ICON -> {
                    ToolbarMenuIcon(
                        onShowMenuBottomSheet = onShowMenuBottomSheet,
                    )
                }

                ToolbarOptionType.EXPANDED -> {} // no-op, menu can only be shown in toolbar
            }
        }
    }
}

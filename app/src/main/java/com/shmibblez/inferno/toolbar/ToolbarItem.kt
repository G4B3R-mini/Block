package com.shmibblez.inferno.toolbar

import androidx.compose.runtime.Composable
import com.shmibblez.inferno.R
import com.shmibblez.inferno.proto.InfernoSettings
import mozilla.components.browser.state.state.TabSessionState

val InfernoSettings.ToolbarItem?.defaultToolbarItems
    get() = listOf(
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU,
    )

val InfernoSettings.ToolbarItem?.allToolbarItemsNoMiniOrigin
    get() = listOf(
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SETTINGS,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN,
//            InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_HISTORY,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FIND_IN_PAGE,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PRIVATE_MODE,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU,
    )

val InfernoSettings.ToolbarItem?.allToolbarItemsNoOrigin
    get() = listOf(
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SETTINGS,
//            InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_HISTORY,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FIND_IN_PAGE,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PRIVATE_MODE,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU,
    )

@Composable
fun InfernoSettings.ToolbarItem.ToToolbarOption(
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
) {
    when (this) {
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SETTINGS -> {
            ToolbarOptions.ToolbarSettings(
                type = type,
                onNavToSettings = onNavToSettings,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN -> {
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

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI -> {
            ToolbarOptions.ToolbarOriginMini(
                type = type,
                onRequestSearchBar = onRequestSearchBar,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK -> {
            ToolbarOptions.ToolbarBack(
                type = type,
                enabled = tabSessionState.content.canGoBack,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD -> {
            ToolbarOptions.ToolbarForward(
                type = type,
                enabled = tabSessionState.content.canGoForward,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD -> {
            ToolbarOptions.ToolbarReload(
                type = type,
                enabled = true,
                loading = loading,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_HISTORY -> {
            ToolbarOptions.ToolbarHistory(
                type = type,
                onNavToHistory = onNavToHistory,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP -> {
            ToolbarOptions.ToolbarRequestDesktopSite(
                type = type,
                desktopMode = tabSessionState.content.desktopMode,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FIND_IN_PAGE -> {
            ToolbarOptions.ToolbarFindInPage(
                type = type,
                onActivateFindInPage = onActivateFindInPage,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW -> {
            ToolbarOptions.ToolbarRequestReaderView(
                type = type,
                enabled = tabSessionState.readerState.readerable,
                dismissMenuSheet = onDismissMenuBottomSheet,
                onActivateReaderView = onActivateReaderView,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PRIVATE_MODE -> {
            ToolbarOptions.ToolbarPrivateModeToggle(
                type = type,
                isPrivateMode = tabSessionState.content.private,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY -> {
            ToolbarOptions. ToolbarShowTabsTray(
                type = type,
                tabCount = tabCount,
                dismissMenuSheet = onDismissMenuBottomSheet,
                onNavToTabsTray = onNavToTabsTray,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE -> {
            ToolbarOptions.ToolbarShare(type = ToolbarOptionType.EXPANDED)
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU -> {
            when (type) {
                ToolbarOptionType.ICON -> {
                    ToolbarOnlyOptions.ToolbarMenuIcon(
                        onShowMenuBottomSheet = onShowMenuBottomSheet,
                    )
                }

                ToolbarOptionType.EXPANDED -> {} // no-op, menu can only be shown in toolbar
            }
        }

        InfernoSettings.ToolbarItem.UNRECOGNIZED -> {/* no-op */
        }
    }
}

@Composable
fun InfernoSettings.ToolbarItem.ToToolbarIcon() {
    when (this) {
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SETTINGS -> ToolbarOptionsIcons.ToolbarSettings()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN -> {}
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI -> ToolbarOptionsIcons.ToolbarOriginMini()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK -> ToolbarOptionsIcons.ToolbarBack()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD -> ToolbarOptionsIcons.ToolbarForward()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD -> ToolbarOptionsIcons.ToolbarReload()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_HISTORY -> ToolbarOptionsIcons.ToolbarHistory()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP -> ToolbarOptionsIcons.ToolbarRequestDesktopSite()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FIND_IN_PAGE -> ToolbarOptionsIcons.ToolbarFindInPage()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW -> ToolbarOptionsIcons.ToolbarRequestReaderView()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PRIVATE_MODE -> ToolbarOptionsIcons.ToolbarPrivateModeToggle()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY -> ToolbarOptionsIcons.ToolbarShowTabsTray()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE -> ToolbarOptionsIcons.ToolbarShare()
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU -> ToolbarOptionsIcons.ToolbarMenuIcon()
        InfernoSettings.ToolbarItem.UNRECOGNIZED -> {}
    }
}

private fun InfernoSettings.ToolbarItem.toPrefIconRes(): Int? {
    return when (this) {
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SETTINGS -> R.drawable.ic_settings_24
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN -> null
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI -> null // todo: make svg for mini origin icon, also use in toolbar, or return icons here instead
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK -> R.drawable.ic_chevron_left_24
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD -> R.drawable.ic_chevron_right_24
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD -> R.drawable.ic_refresh
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_HISTORY -> R.drawable.ic_history_24
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP -> R.drawable.ic_device_desktop_24
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FIND_IN_PAGE -> R.drawable.ic_search_24
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW -> R.drawable.ic_reader_view_24
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PRIVATE_MODE -> R.drawable.ic_private_browsing
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY -> R.drawable.ic_tabcounter_box_24
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE -> R.drawable.ic_share
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU -> R.drawable.ic_app_menu_24
        InfernoSettings.ToolbarItem.UNRECOGNIZED -> null
    }
}

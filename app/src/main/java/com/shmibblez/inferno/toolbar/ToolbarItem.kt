package com.shmibblez.inferno.toolbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.infernoTheme
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
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_EXTENSIONS,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PASSWORDS,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BOOKMARKS,
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
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_EXTENSIONS,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PASSWORDS,
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BOOKMARKS,
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
    onNavToBookmarks: () -> Unit,
    onNavToExtensions: () -> Unit,
    onNavToPasswords: () -> Unit,
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
            ToolbarOptions.ToolbarShowTabsTray(
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

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_EXTENSIONS -> {
            ToolbarOptions.ToolbarExtensions(
                type = type,
                dismissMenuSheet = onDismissMenuBottomSheet,
                onNavToExtensions = onNavToExtensions,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PASSWORDS -> {
            ToolbarOptions.ToolbarPasswords(
                type = type,
                dismissMenuSheet = onDismissMenuBottomSheet,
                onNavToPasswords = onNavToPasswords,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BOOKMARKS -> {
            ToolbarOptions.ToolbarBookmarks(
                type = type,
                onNavToBookmarks = onNavToBookmarks,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }
    }
}

@Composable
fun InfernoSettings.ToolbarItem.ToToolbarIcon(
    tint: Color = LocalContext.current.infernoTheme().value.primaryIconColor,
    variant: Boolean = false,
) {
    when (this) {
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SETTINGS -> ToolbarOptionsIcons.ToolbarSettings(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN -> {}

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI -> ToolbarOptionsIcons.ToolbarOriginMini(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK -> ToolbarOptionsIcons.ToolbarBack(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD -> ToolbarOptionsIcons.ToolbarForward(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD -> ToolbarOptionsIcons.ToolbarReload(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_HISTORY -> ToolbarOptionsIcons.ToolbarHistory(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP -> ToolbarOptionsIcons.ToolbarRequestDesktopSite(
            tint = tint,
            variant = variant,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FIND_IN_PAGE -> ToolbarOptionsIcons.ToolbarFindInPage(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW -> ToolbarOptionsIcons.ToolbarRequestReaderView(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PRIVATE_MODE -> ToolbarOptionsIcons.ToolbarPrivateModeToggle(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY -> ToolbarOptionsIcons.ToolbarShowTabsTray(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE -> ToolbarOptionsIcons.ToolbarShare(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU -> ToolbarOptionsIcons.ToolbarMenuIcon(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_EXTENSIONS -> ToolbarOptionsIcons.ToolbarExtensionsIcon(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PASSWORDS -> ToolbarOptionsIcons.ToolbarPasswowrdsIcon(
            tint = tint,
        )

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BOOKMARKS -> ToolbarOptionsIcons.ToolbarBookmarksIcon(
            tint = tint,
        )
    }
}
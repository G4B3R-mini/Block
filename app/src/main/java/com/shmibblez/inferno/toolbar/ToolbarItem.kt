package com.shmibblez.inferno.toolbar

import androidx.compose.runtime.Composable
import com.shmibblez.inferno.proto.InfernoSettings
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

internal class ToolbarItems {
    companion object {
        val defaultToolbarItems = listOf(
            InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK,
            InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD,
            InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN,
            InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD,
            InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY,
            InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU,
        )

//        fun fromKeys(
//            keys: List<InfernoSettings.ToolbarItem>,
//            // item params
//            type: ToolbarOptionType,
//            tabSessionState: TabSessionState,
//            loading: Boolean,
//            tabCount: Int,
//            onShowMenuBottomSheet: () -> Unit,
//            onDismissMenuBottomSheet: () -> Unit,
//            onRequestSearchBar: () -> Unit,
//            onActivateFindInPage: () -> Unit,
//            onActivateReaderView: () -> Unit,
//            onNavToSettings: () -> Unit,
//            onNavToHistory: () -> Unit,
//            onNavToTabsTray: () -> Unit,
////            // origin params
////            searchEngine: SearchEngine,
////            siteSecure: SiteSecurity,
////            siteTrackingProtection: SiteTrackingProtection,
////            setAwesomeSearchText: (String) -> Unit,
////            setOnAutocomplete: ((TextFieldValue) -> Unit) -> Unit,
////            originModifier: Modifier = Modifier,
////            editMode: Boolean,
////            onStartSearch: () -> Unit,
////            onStopSearch: () -> Unit,
////            animationValue: Float,
//        ): List<@Composable () -> Unit> {
//            return List(
//                size = keys.size,
//                init = {
//                    {
//                        ToolbarItem(
//                            key = keys[it],
//                            type = type,
//                            tabSessionState = tabSessionState,
//                            loading = loading,
//                            tabCount = tabCount,
//                            onShowMenuBottomSheet = onShowMenuBottomSheet,
//                            onDismissMenuBottomSheet = onDismissMenuBottomSheet,
//                            onRequestSearchBar = onRequestSearchBar,
//                            onActivateFindInPage = onActivateFindInPage,
//                            onActivateReaderView = onActivateReaderView,
//                            onNavToSettings = onNavToSettings,
//                            onNavToHistory = onNavToHistory,
//                            onNavToTabsTray = onNavToTabsTray,
////                            searchEngine = searchEngine,
////                            siteSecure = siteSecure,
////                            siteTrackingProtection = siteTrackingProtection,
////                            setAwesomeSearchText = setAwesomeSearchText,
////                            setOnAutocomplete = setOnAutocomplete,
////                            originModifier = originModifier,
////                            editMode = editMode,
////                            onStartSearch = onStartSearch,
////                            onStopSearch = onStopSearch,
////                            animationValue = animationValue,
//                        )
//                    }
//                },
//            )
//        }
    }
}

@Composable
internal fun ToolbarItem(
    key: InfernoSettings.ToolbarItem,
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
        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SETTINGS -> {
            ToolbarSettings(
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
            ToolbarOriginMini(
                type = type,
                onRequestSearchBar = onRequestSearchBar,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_BACK -> {
            ToolbarBack(
                type = type,
                enabled = tabSessionState.content.canGoBack,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FORWARD -> {
            ToolbarForward(
                type = type,
                enabled = tabSessionState.content.canGoForward,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_RELOAD -> {
            ToolbarReload(
                type = type,
                enabled = true,
                loading = loading,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_HISTORY -> {
            ToolbarHistory(
                type = type,
                onNavToHistory = onNavToHistory,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_DESKTOP -> {
            ToolbarRequestDesktopSite(
                type = type,
                desktopMode = tabSessionState.content.desktopMode,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_FIND_IN_PAGE -> {
            ToolbarFindInPage(
                type = type,
                onActivateFindInPage = onActivateFindInPage,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_REQUEST_READER_VIEW -> {
            ToolbarRequestReaderView(
                type = type,
                enabled = tabSessionState.readerState.readerable,
                dismissMenuSheet = onDismissMenuBottomSheet,
                onActivateReaderView = onActivateReaderView,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_PRIVATE_MODE -> {
            ToolbarPrivateModeToggle(
                type = type,
                isPrivateMode = tabSessionState.content.private,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHOW_TABS_TRAY -> {
            ToolbarShowTabsTray(
                type = type,
                tabCount = tabCount,
                dismissMenuSheet = onDismissMenuBottomSheet,
                onNavToTabsTray = onNavToTabsTray,
            )
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_SHARE -> {
            ToolbarShare(type = ToolbarOptionType.EXPANDED)
        }

        InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU -> {
            when (type) {
                ToolbarOptionType.ICON -> {
                    ToolbarMenuIcon(
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

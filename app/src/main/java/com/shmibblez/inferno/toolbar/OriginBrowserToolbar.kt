/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.toolbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.browser.InfernoAwesomeBar
import com.shmibblez.inferno.ext.dpToPx
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.toolbar.ToolbarOnlyOptions.Companion.ToolbarOrigin
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.compose.browser.awesomebar.AwesomeBarDefaults
import mozilla.components.compose.browser.awesomebar.AwesomeBarOrientation
import kotlin.math.roundToInt

// TODO:
//  -[x] progress with tabSessionState.content.progress
//  -[x] when app started, new home tab added, instead go to last used tab
//  -[x] when last private tab closed doesn't switch to normal tabs
//  -[ ] swipe gesture to switch tabs, left and right
//  -[x] implement moz AwesomeBarFeature
//  -[ ] implement moz TabsToolbarFeature
//  -[x] implement moz ReaderViewIntegration
//  -[ ] implement moz WebExtensionToolbarFeature
//  -[ ] implement moz engineView!!.setDynamicToolbarMaxHeight
//  -[ ] implement moz ToolbarIntegration

const val DISPLAY_VALUE = 1F
const val EDIT_VALUE = 0F

private fun iconsWidth(nOptions: Int): Dp {
    return TOOLBAR_ICON_PADDING + (TOOLBAR_ICON_SIZE + TOOLBAR_ICON_PADDING) * nOptions
}

//private fun iconsWidthPx(nOptions: Int): Int {
//    return iconsWidth(nOptions).dpToPx()
//}

@Composable
internal fun OriginBrowserToolbar(
    // item params
    tabSessionState: TabSessionState,
    tabCount: Int,
    onShowMenuBottomSheet: () -> Unit,
    onDismissMenuBottomSheet: () -> Unit,
    onRequestSearchBar: () -> Unit,
    onActivateFindInPage: () -> Unit,
    onActivateReaderView: () -> Unit,
    onNavToSettings: () -> Unit,
    onNavToHistory: () -> Unit,
    onNavToTabsTray: () -> Unit,
    // origin params
    searchEngine: SearchEngine?,
    editMode: Boolean,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
) {
    val animationValue = remember { Animatable(if (editMode) EDIT_VALUE else DISPLAY_VALUE) }

    var awesomeSearchText by remember { mutableStateOf("") }
    val loading = tabSessionState.content.loading
    val context = LocalContext.current
    val settings by context.infernoSettingsDataStore.data.collectAsState(initial = InfernoSettings.getDefaultInstance())
    var onAutocomplete: (TextFieldValue) -> Unit by remember { mutableStateOf({}) }


    LaunchedEffect(editMode) {
        if (editMode) {
            animationValue.animateTo(targetValue = EDIT_VALUE, animationSpec = tween(250))
        } else {
            animationValue.animateTo(targetValue = DISPLAY_VALUE, animationSpec = tween(250))
        }
    }


    fun getToolbarItems(): List<InfernoSettings.ToolbarItem> {
        settings.toolbarItemsList.let {
            return when (it.size) {
                0 -> (null as InfernoSettings.ToolbarItem?).defaultToolbarItems
                else -> it
            }
        }
    }

    var toolbarItems by remember { mutableStateOf(getToolbarItems()) }
    fun getOriginIndex() = toolbarItems.indexOf(InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN)
    var indexOrigin by remember { mutableIntStateOf(getOriginIndex()) }
    fun getLeftKeys() = toolbarItems.take(indexOrigin)
    fun getRightKeys() = toolbarItems.drop(indexOrigin + 1)
    var leftKeys by remember { mutableStateOf(getLeftKeys()) }
    var rightKeys by remember { mutableStateOf(getRightKeys()) }
    var leftWidth = remember { iconsWidth(leftKeys.size) }
    var leftWidthPx = remember { leftWidth.dpToPx(context) }
    var rightWidth = remember { iconsWidth(rightKeys.size) }
    var rightWidthPx = remember { rightWidth.dpToPx(context) }


    LaunchedEffect(settings.toolbarItemsList) {
        // on settings change, recalc values
        toolbarItems = getToolbarItems()
        indexOrigin = getOriginIndex()
        leftKeys = getLeftKeys()
        rightKeys = getRightKeys()
        leftWidth = iconsWidth(leftKeys.size)
        leftWidthPx = leftWidth.dpToPx(context)
        rightWidth = iconsWidth(rightKeys.size)
        rightWidthPx = rightWidth.dpToPx(context)
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                when (editMode) {
                    true -> ComponentDimens.TOOLBAR_HEIGHT + ComponentDimens.AWESOME_BAR_HEIGHT
                    false -> ComponentDimens.TOOLBAR_HEIGHT
                }
            )
            .background(Color.Transparent),
    ) {
        // awesome bar
        if (editMode) {
            InfernoAwesomeBar(
                text = awesomeSearchText,
                colors = AwesomeBarDefaults.colors(),
//                    providers = emptyList(),
                orientation = AwesomeBarOrientation.BOTTOM,
                onSuggestionClicked = { providerGroup, suggestion ->
                    // todo: change action based on providerGroup
                    val t = suggestion.title
                    if (t != null) {
                        onAutocomplete(TextFieldValue(t, TextRange(t.length)))
                    }
                },
                onAutoComplete = { providerGroup, suggestion ->
                    // todo: filter out based on providerGroup
                    val t = suggestion.title
                    if (t != null) {
                        onAutocomplete(TextFieldValue(t, TextRange(t.length)))
                    }
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ComponentDimens.TOOLBAR_HEIGHT)
                .background(Color.Black),
        ) {
            // origin
            ToolbarOrigin(
                originModifier = Modifier.padding(
                    start = (leftWidth * animationValue.value),
                    end = (rightWidth * animationValue.value),
                ),
                tabSessionState = tabSessionState,
                searchEngine = searchEngine,
                setAwesomeSearchText = { awesomeSearchText = it },
                setOnAutocomplete = { onAutocomplete = it },
                siteSecure = detectSiteSecurity(tabSessionState),
                siteTrackingProtection = detectSiteTrackingProtection(tabSessionState),
                editMode = editMode,
                onStartSearch = onStartSearch,
                onStopSearch = onStopSearch,
                animationValue = animationValue.value,
            )

            // icons on left
            Row(
                modifier = Modifier
                    .padding(start = TOOLBAR_ICON_PADDING, end = 4.dp)
                    .align(Alignment.CenterStart)
                    .offset {
                        IntOffset(
                            x = (-leftWidthPx * (1F - animationValue.value)).roundToInt(), y = 0
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    TOOLBAR_ICON_PADDING, Alignment.CenterHorizontally
                )
            ) {
                for (k in leftKeys) {
                    k.ToToolbarOption(
                        type = ToolbarOptionType.ICON,
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
//                searchEngine = searchEngine,
//                siteSecure = siteSecure,
//                siteTrackingProtection = siteTrackingProtection,
//                setAwesomeSearchText = setAwesomeSearchText,
//                setOnAutocomplete = setOnAutocomplete,
//                originModifier = originModifier,
//                editMode = editMode,
//                onStartSearch = onStartSearch,
//                onStopSearch = onStopSearch,
//                // todo: test if still recomposes if animationValue passed directly and remembered
//                //  in toolbar origin, might also have to have different animatable in origin composable
//                animationValue = animationValue.value,
                    )
                }
            }

            // icons on right
            Row(
                modifier = Modifier
                    .padding(horizontal = TOOLBAR_ICON_PADDING)
                    .align(Alignment.CenterEnd)
                    .offset {
                        IntOffset(
                            x = (rightWidthPx * (1F - animationValue.value)).roundToInt(), y = 0
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    TOOLBAR_ICON_PADDING, Alignment.CenterHorizontally
                )
            ) {
                for (k in rightKeys) {
                    k.ToToolbarOption(
                        type = ToolbarOptionType.ICON,
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
//                searchEngine = searchEngine,
//                siteSecure = siteSecure,
//                siteTrackingProtection = siteTrackingProtection,
//                setAwesomeSearchText = setAwesomeSearchText,
//                setOnAutocomplete = setOnAutocomplete,
//                originModifier = originModifier,
//                editMode = editMode,
//                onStartSearch = onStartSearch,
//                onStopSearch = onStopSearch,
//                // todo: test if still recomposes if animationValue passed directly and remembered
//                //  in toolbar origin, might also have to have different animatable in origin composable
//                animationValue = animationValue.value,
                    )
                }
            }
        }
    }
}

fun detectSiteTrackingProtection(tabSessionState: TabSessionState): SiteTrackingProtection {
    return if (!tabSessionState.trackingProtection.enabled) SiteTrackingProtection.OFF_GLOBALLY
    else {
        if (tabSessionState.trackingProtection.ignoredOnTrackingProtection) SiteTrackingProtection.OFF_FOR_A_SITE
        else if (tabSessionState.trackingProtection.blockedTrackers.isEmpty()) SiteTrackingProtection.ON_NO_TRACKERS_BLOCKED
        else SiteTrackingProtection.ON_TRACKERS_BLOCKED
    }
}

fun detectSiteSecurity(tabSessionState: TabSessionState): SiteSecurity {
    return if (tabSessionState.content.securityInfo.secure) SiteSecurity.SECURE else SiteSecurity.INSECURE
}


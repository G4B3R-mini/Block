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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.browser.InfernoAwesomeBar
import com.shmibblez.inferno.ext.dpToPx
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarBack
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarForward
import com.shmibblez.inferno.toolbar.ToolbarOnlyOptions.Companion.ToolbarMenuIcon
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarReload
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarShowTabsTray
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

private fun iconsWidthPx(nOptions: Int): Int {
    return iconsWidth(nOptions).dpToPx()
}

@Composable
fun BrowserToolbar(
    tabSessionState: TabSessionState?,
    searchEngine: SearchEngine?,
    tabCount: Int,
    onShowMenuBottomSheet: () -> Unit,
    onNavToTabsTray: () -> Unit,
    editMode: Boolean,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
) {
    if (tabSessionState == null || searchEngine == null) {
        PlaceholderBrowserToolbar()
        return
    }

    val animationValue = remember { Animatable(if (editMode) EDIT_VALUE else DISPLAY_VALUE) }
    val leftWidth = remember { iconsWidth(2) }
    val leftWidthPx = remember { iconsWidthPx(2) }
    val rightWidth = remember { iconsWidth(3) }
    val rightWidthPx = remember { iconsWidthPx(3) }

    var awesomeSearchText by remember { mutableStateOf("") }
    val loading = tabSessionState.content.loading

    LaunchedEffect(editMode) {
        if (editMode) {
            animationValue.animateTo(targetValue = EDIT_VALUE, animationSpec = tween(250))
        } else {
            animationValue.animateTo(targetValue = DISPLAY_VALUE, animationSpec = tween(250))
        }
    }

    var onAutocomplete: (TextFieldValue) -> Unit by remember { mutableStateOf({}) }

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
                // todo: move cursor to end on suggestion set
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
                indicatorModifier = Modifier,
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
                ToolbarBack(
                    type = ToolbarOptionType.ICON, enabled = tabSessionState.content.canGoBack
                )
                ToolbarForward(
                    type = ToolbarOptionType.ICON, enabled = tabSessionState.content.canGoForward
                )
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
                ToolbarReload(type = ToolbarOptionType.ICON, enabled = true, loading = loading)
                ToolbarShowTabsTray(
                    type = ToolbarOptionType.ICON,
                    tabCount = tabCount,
                    onNavToTabsTray = onNavToTabsTray
                )
                ToolbarMenuIcon(onShowMenuBottomSheet = onShowMenuBottomSheet)
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


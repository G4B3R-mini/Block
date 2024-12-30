/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.toolbar

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.compose.browserStore
import com.shmibblez.inferno.compose.sessionUseCases
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.state.observeAsState
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarBack
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarMenuIcon
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarReload
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarForward
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarShowTabsTray
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarSearchEngineSelector
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine

// TODO:
//  -[ ] implement moz AwesomeBarFeature
//  -[ ] implement moz TabsToolbarFeature
//  -[ ] implement moz ReaderViewIntegration
//  -[ ] implement moz WebExtensionToolbarFeature
//  -[ ] implement moz engineView!!.setDynamicToolbarMaxHeight
//  -[ ] implement moz ToolbarIntegration

@Composable
fun BrowserToolbar(tabSessionState: TabSessionState?,searchEngine: SearchEngine, setShowMenu: (Boolean) -> Unit) {
    if (tabSessionState == null) {
        // don't show if null, TODO: show loading bar
        return
    }
    val context = LocalContext.current
    val searchTerms by remember { mutableStateOf(tabSessionState.content.searchTerms) }
    val url: String? by browserStore().observeAsState { state -> state.selectedTab?.content?.url }
    val (editMode, setEditMode) = remember { mutableStateOf(false) }
    val useCases = sessionUseCases()
    val (originBounds, setOriginBounds) = remember { mutableStateOf(OriginBounds(0.dp, 0.dp)) }
//    val siteSecure = tabSessionState.content.securityInfo.secure
//    val trackingProtectionEnabled = tabSessionState.trackingProtection.enabled

    if (editMode) {
        BrowserEditToolbar(
            tabSessionState = tabSessionState,
            setEditMode = setEditMode,
            originBounds = originBounds,
            searchEngine = searchEngine,
        )
    } else {
        BrowserDisplayToolbar(
            url = url ?: "<empty>",
            searchTerms = searchTerms,
            setOriginBounds = setOriginBounds,
            tabSessionState = tabSessionState,
            setEditMode = setEditMode,
            setShowMenu = setShowMenu
        )
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

@Composable
fun BrowserDisplayToolbar(
    url: String?,
    searchTerms: String,
    setOriginBounds: (OriginBounds) -> Unit,
    tabSessionState: TabSessionState,
    setEditMode: (Boolean) -> Unit,
    setShowMenu: (Boolean) -> Unit
) {
    var textFullSize by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        textFullSize = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(ComponentDimens.TOOLBAR_HEIGHT)
    ) {
        if (!textFullSize) {
            ToolbarBack(enabled = tabSessionState.content.canGoBack)
            ToolbarForward(enabled = tabSessionState.content.canGoForward)
        }
        ToolbarOrigin(
            modifier = Modifier.animateContentSize() { initialValue, targetValue ->
                // finished callback
            }, toolbarOriginData = ToolbarOriginData(
                url = url,
                searchTerms = searchTerms,
                siteSecure = detectSiteSecurity(tabSessionState),
                siteTrackingProtection = detectSiteTrackingProtection(tabSessionState),
                setEditMode = setEditMode,
            ), setOriginBounds = setOriginBounds
        )
        if (!textFullSize) {
            ToolbarReload(enabled = true)
            ToolbarShowTabsTray()
            ToolbarMenuIcon(setShowMenu = setShowMenu)
        }
    }
}

// TODO: add blend in animation
@Composable
fun BrowserEditToolbar(
    tabSessionState: TabSessionState?,
    setEditMode: (Boolean) -> Unit,
    originBounds: OriginBounds,
    searchEngine: SearchEngine,

    ) {
    fun parseInput(): TextFieldValue {
        return (tabSessionState?.content?.searchTerms?.ifEmpty { tabSessionState.content.url }
            ?: "<empty>").let {
            (if (it != "about:blank" && it != "about:privatebrowsing") it else "").let { searchTerms ->
                TextFieldValue(
                    text = searchTerms,
                    selection = if (searchTerms.isEmpty()) TextRange.Zero else TextRange(searchTerms.length)
                )
            }
        }
    }

    val context = LocalContext.current
    var input by remember { mutableStateOf(parseInput()) }
    var textFullSize by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var alreadyFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val (showPopupMenu, setShowPopupMenu) = remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        // animate to fill width after first compose
        focusRequester.requestFocus()
        textFullSize = true
    }

    LaunchedEffect(tabSessionState?.id) {
        if (tabSessionState != null) input = parseInput()
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Black)
        .height(ComponentDimens.TOOLBAR_HEIGHT)
        .onFocusChanged { focusState ->
            // if focus lost, go back to editing mode
            if (focusState.hasFocus) {
                alreadyFocused = true
            }
            if (alreadyFocused && !focusState.hasFocus) setEditMode(false)
            Log.d(
                "BrowserEditToolbar", "alreadyFocused: $alreadyFocused, focusState: $focusState"
            )
        }
        .focusable(true)
        .focusRequester(focusRequester)) {
        if (!textFullSize) {
            Box(
                modifier = Modifier
                    .width(originBounds.left)
                    .fillMaxHeight()
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 4.dp)
                .weight(1F)
                .background(Color.DarkGray, shape = MaterialTheme.shapes.small)
                .animateContentSize()
        ) {
            val customTextSelectionColors = TextSelectionColors(
                handleColor = Color.White, backgroundColor = Color.White.copy(alpha = 0.4F)
            )
            ToolbarSearchEngineSelectorPopupMenu(
                searchEngines = context.components.core.customSearchEngines,
                showPopupMenu = showPopupMenu,
                setShowPopupMenu = setShowPopupMenu,
            )
            ToolbarSearchEngineSelector(
                currentSearchEngine = searchEngine,
                showPopupMenu = setShowPopupMenu,
            )
            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                BasicTextField(
                    value = input,
                    onValueChange = { v ->
                        // move cursor to end
                        input = v
                    },
                    enabled = true,
                    singleLine = true,
                    interactionSource = interactionSource,
                    textStyle = TextStyle(
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None,
                        )
                    ),
                    cursorBrush = SolidColor(Color.White),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            context.components.useCases.searchUseCases.addSearchEngine
                            context.components.useCases.searchUseCases.defaultSearch.invoke(
                                searchTerms = input.text,
                                searchEngine = context.components.core.store.state.search.selectedOrDefaultSearchEngine!!,
                                parentSessionId = null,
                            )
                            setEditMode(false)
                        },
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go
                    ),
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .weight(1F)
                        .padding(all = 4.dp),
                )
            }
        }
        if (!textFullSize) {
            Box(
                modifier = Modifier
                    .width(originBounds.right)
                    .fillMaxHeight()
            )
        }
    }
}

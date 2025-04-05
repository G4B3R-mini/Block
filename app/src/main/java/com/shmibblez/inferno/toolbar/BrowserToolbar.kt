/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.toolbar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.shmibblez.inferno.browser.InfernoAwesomeBar
import com.shmibblez.inferno.compose.browserStore
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.state.observeAsState
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarBack
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarForward
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarMenuIcon
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarReload
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarShowTabsTray
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarClearText
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarSearchEngineSelector
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarUndoClearText
import kotlinx.coroutines.delay
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.searchEngines
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.compose.browser.awesomebar.AwesomeBarDefaults
import mozilla.components.compose.browser.awesomebar.AwesomeBarOrientation
import mozilla.components.concept.engine.EngineSession
import mozilla.components.support.ktx.kotlin.isUrl
import mozilla.components.support.ktx.kotlin.toNormalizedUrl

// TODO:
//  -[x] progress with tabSessionState.content.progress
//  -[ ] when app started, new home tab added, instead go to last used tab
//  -[ ] when last private tab closed doesn't switch to normal tabs
//  -[ ] swipe gesture to switch tabs, left and right
//  -[ ] implement moz AwesomeBarFeature
//  -[ ] implement moz TabsToolbarFeature
//  -[ ] implement moz ReaderViewIntegration
//  -[ ] implement moz WebExtensionToolbarFeature
//  -[ ] implement moz engineView!!.setDynamicToolbarMaxHeight
//  -[ ] implement moz ToolbarIntegration

@Composable
fun BrowserToolbar(
    tabSessionState: TabSessionState?,
    searchEngine: SearchEngine,
    tabCount: Int,
    onShowMenuBottomSheet: () -> Unit,
    onNavToTabsTray: () -> Unit,
    editMode: Boolean,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
) {
    if (tabSessionState == null) {
        // don't show if null, TODO: show loading bar
        return
    }
    val searchTerms by remember { mutableStateOf(tabSessionState.content.searchTerms) }
    val url: String? by browserStore().observeAsState { state -> state.selectedTab?.content?.url }
//    var editMode by remember { mutableStateOf(false) }
    val (originBounds, setOriginBounds) = remember { mutableStateOf(OriginBounds(0.dp, 0.dp)) }
    val loading by remember { derivedStateOf { tabSessionState.content.loading } }
//    val siteSecure = tabSessionState.content.securityInfo.secure
//    val trackingProtectionEnabled = tabSessionState.trackingProtection.enabled

    if (editMode) {
        BrowserEditToolbar(
            tabSessionState = tabSessionState,
            onDisableEditMode = onStopSearch,
            originBounds = originBounds,
            searchEngine = searchEngine,
        )
    } else {
        BrowserDisplayToolbar(
            url = url ?: "<empty>",
            loading = loading,
            searchTerms = searchTerms,
            setOriginBounds = setOriginBounds,
            tabCount = tabCount,
            tabSessionState = tabSessionState,
            onEnableEditMode = onStartSearch,
            onShowMenuBottomSheet = onShowMenuBottomSheet,
            onNavToTabsTray = onNavToTabsTray,
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
    loading: Boolean,
    searchTerms: String,
    setOriginBounds: (OriginBounds) -> Unit,
    tabCount: Int,
    tabSessionState: TabSessionState,
    onEnableEditMode: () -> Unit,
    onShowMenuBottomSheet: () -> Unit,
    onNavToTabsTray: () -> Unit,
) {
    var textFullSize by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        textFullSize = false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .height(ComponentDimens.TOOLBAR_HEIGHT), contentAlignment = Alignment.TopCenter
    ) {
        // loading bar
        if (loading) {
            ProgressBar(progress = (tabSessionState.content.progress.toFloat() ?: 0F) / 100F)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ComponentDimens.TOOLBAR_HEIGHT)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                ICON_PADDING, Alignment.CenterHorizontally
            ),
        ) {
            if (!textFullSize) {
                ToolbarBack(enabled = tabSessionState.content.canGoBack)
                ToolbarForward(enabled = tabSessionState.content.canGoForward)
            }
            ToolbarOrigin(
                modifier = Modifier.animateContentSize { initialValue, targetValue ->
                    // finished callback
                }, toolbarOriginData = ToolbarOriginData(
                    url = url,
                    searchTerms = searchTerms,
                    siteSecure = detectSiteSecurity(tabSessionState),
                    siteTrackingProtection = detectSiteTrackingProtection(tabSessionState),
                    onEnableEditMode = onEnableEditMode,
                ), setOriginBounds = setOriginBounds
            )
            if (!textFullSize) {
                ToolbarReload(enabled = true, loading = loading)
                ToolbarShowTabsTray(tabCount = tabCount, onNavToTabsTray = onNavToTabsTray)
                ToolbarMenuIcon(onShowMenuBottomSheet = onShowMenuBottomSheet)
            }
        }
    }
}

// TODO: add blend in animation
@Composable
fun BrowserEditToolbar(
    tabSessionState: TabSessionState?,
    onDisableEditMode: () -> Unit,
    originBounds: OriginBounds,
    searchEngine: SearchEngine,
) {
    fun parseInput(): TextFieldValue {
        return (tabSessionState?.content?.searchTerms?.ifEmpty { tabSessionState.content.url }
            ?: "<empty>").let {
            (if (it != "inferno:home" && it != "inferno:privatebrowsing") it else "").let { searchTerms ->
                TextFieldValue(
                    text = searchTerms,
                    selection = if (searchTerms.isEmpty()) TextRange.Zero else TextRange(searchTerms.length)
                )
            }
        }
    }

    val context = LocalContext.current
    var searchText by remember { mutableStateOf(parseInput()) }
    var undoClearText by remember { mutableStateOf<TextFieldValue?>(null) }
    var textFullSize by remember { mutableStateOf(false) }
    // focus requester makes sure toolbar is set to display mode when focus lost
    val focusRequester = remember { FocusRequester() }
    var focusReady = remember { false }
    // origin focus requester makes sure toolbar edittext is focused when start
    val originFocusRequester = remember { FocusRequester() }
    val (showPopupMenu, setShowPopupMenu) = remember { mutableStateOf(false) }
    val loading = tabSessionState?.content?.loading ?: false

    LaunchedEffect(true) {
        // animate to fill width after first compose
        focusRequester.requestFocus()
        originFocusRequester.requestFocus()
        textFullSize = true
    }

    LaunchedEffect(tabSessionState?.id) {
        if (tabSessionState != null) searchText = parseInput()
    }

    Column(
        modifier = Modifier
            .onFocusChanged { focusState ->
                if (focusState.hasFocus) {
                    focusReady = true
                } else if (focusReady && !focusState.hasFocus) {
                    onDisableEditMode.invoke()
                }
            }
            .focusRequester(focusRequester)
            .focusable(true)
            .fillMaxWidth()
            .background(Color.Transparent)
            .height(ComponentDimens.TOOLBAR_HEIGHT + ComponentDimens.AWESOME_BAR_HEIGHT),
    ) {
        InfernoAwesomeBar(
            text = searchText.text,
            modifier = Modifier.focusable(false),
            colors = AwesomeBarDefaults.colors(),
//                    providers = emptyList(),
            orientation = AwesomeBarOrientation.BOTTOM,
            // todo: move cursor to end on suggestion set
            onSuggestionClicked = { providerGroup, suggestion ->
                val t = suggestion.title
                if (t != null) {
                    searchText = TextFieldValue(t, TextRange(t.length))
                }
            },
            onAutoComplete = { providerGroup, suggestion ->
                val t = suggestion.title
                if (t != null) {
                    searchText = TextFieldValue(t, TextRange(t.length))
                }
            },
//                            modifier = Modifier
//                                .onGloballyPositioned {
//                                    val y = -it.size.height
//                                    Log.d("InfernoAwesomeBar", "y offset: $y")
//                                    IntOffset(x = 0, y = 0)
//                                }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .height(ComponentDimens.TOOLBAR_HEIGHT),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!textFullSize) {
                Spacer(
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
                    searchEngines = context.components.core.store.state.search.searchEngines,
                    showPopupMenu = showPopupMenu,
                    setShowPopupMenu = setShowPopupMenu,
                )
                ToolbarSearchEngineSelector(
                    currentSearchEngine = searchEngine,
                    showPopupMenu = setShowPopupMenu,
                )
                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    BasicTextField(
                        value = searchText,
                        onValueChange = { v ->
                            // move cursor to end
                            searchText = v
                            undoClearText = null
                        },
                        enabled = true,
                        singleLine = true,
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
                                with(searchText.text) {
                                    if (this.isUrl()) {
                                        context.components.useCases.sessionUseCases.loadUrl(
                                            url = this.toNormalizedUrl(),
                                            flags = EngineSession.LoadUrlFlags.none()
                                        )
                                    } else {
                                        context.components.useCases.searchUseCases.defaultSearch.invoke(
                                            searchTerms = this,
                                            searchEngine = context.components.core.store.state.search.selectedOrDefaultSearchEngine!!,
                                            parentSessionId = null,
                                        )
                                    }
                                }
                                onDisableEditMode.invoke()
                            },
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go
                        ),
                        modifier = Modifier
                            .focusRequester(originFocusRequester)
                            .focusable(true)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .weight(1F)
                            .padding(horizontal = 8.dp),
                    )
                }
                if (undoClearText != null) {
                    ToolbarUndoClearText(
                        onClick = {
                            searchText = undoClearText!!
                            undoClearText = null
                        },
                    )
                } else if (searchText.text.isNotEmpty()) {
                    ToolbarClearText(
                        onClick = {
                            undoClearText = searchText
                            searchText = TextFieldValue("")
                        },
                    )
                }
            }

            if (!textFullSize) {
                Spacer(
                    modifier = Modifier
                        .width(originBounds.right)
                        .fillMaxHeight()
                )
            }
        }
        // loading bar
        if (loading) {
            ProgressBar((tabSessionState?.content?.progress?.toFloat() ?: 0F) / 100F)
        }
    }
}

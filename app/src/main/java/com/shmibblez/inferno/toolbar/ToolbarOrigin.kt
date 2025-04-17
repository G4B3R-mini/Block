package com.shmibblez.inferno.toolbar

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.browser.toPx
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.searchEngines
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.support.ktx.kotlin.isUrl
import mozilla.components.support.ktx.kotlin.toNormalizedUrl
import kotlin.math.roundToInt
import com.shmibblez.inferno.toolbar.ToolbarOptions.Companion.ToolbarSeparator


// start padding + (width - vertical padding since 1:1 aspect ratio) + expand icon start padding + expand icon size + expand icon end padding
private val TOOLBAR_SEARCH_ENGINE_SELECTOR_WIDTH =
    4.dp + (ComponentDimens.TOOLBAR_HEIGHT - 16.dp - 8.dp - 4.dp) + (4.dp + 6.dp + 4.dp) + 4.dp
private val TOOLBAR_SEARCH_ENGINE_SELECTOR_WIDTH_PX = TOOLBAR_SEARCH_ENGINE_SELECTOR_WIDTH.toPx()

// start padding + indicator icon size + end padding
private val TOOLBAR_ACTION_WIDTH = 4.dp + TOOLBAR_INDICATOR_ICON_SIZE + 8.dp
private val TOOLBAR_ACTION_WIDTH_PX = TOOLBAR_ACTION_WIDTH.toPx()

//
private fun toolbarIndicatorWidth(siteTrackingProtection: SiteTrackingProtection): Dp {
    return 8.dp + TOOLBAR_INDICATOR_ICON_SIZE + TOOLBAR_INDICATOR_ICON_PADDING + (if (siteTrackingProtection != SiteTrackingProtection.OFF_GLOBALLY) TOOLBAR_INDICATOR_ICON_SIZE + TOOLBAR_INDICATOR_ICON_PADDING + 1.dp + TOOLBAR_INDICATOR_ICON_PADDING else 0.dp) + 4.dp
}

private fun toolbarIndicatorWidthPx(siteTrackingProtection: SiteTrackingProtection): Int {
    return toolbarIndicatorWidth(siteTrackingProtection).toPx()
}

@Composable
fun ToolbarOrigin(
    tabSessionState: TabSessionState,
    searchEngine: SearchEngine,
    siteSecure: SiteSecurity,
    siteTrackingProtection: SiteTrackingProtection,
    searchText: TextFieldValue,
    setSearchText: (TextFieldValue) -> Unit,
    originModifier: Modifier = Modifier,
    indicatorModifier: Modifier = Modifier,
    editMode: Boolean,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
    animationValue: Float,
) {
    fun parseInput(): TextFieldValue {
        return (tabSessionState.content.searchTerms.ifEmpty { tabSessionState.content.url }).let {
            TextFieldValue(
                text = (if (it != "inferno:home" && it != "inferno:privatebrowsing") it else ""),
                selection = if (tabSessionState.content.searchTerms.isEmpty()) TextRange.Zero else TextRange(
                    tabSessionState.content.searchTerms.length
                )
            )
        }
    }

    val context = LocalContext.current
    var undoClearText by remember { mutableStateOf<TextFieldValue?>(null) }
    var indicatorWidth by remember { mutableStateOf(toolbarIndicatorWidth(siteTrackingProtection)) }
    var indicatorWidthPx by remember {
        mutableIntStateOf(
            toolbarIndicatorWidthPx(
                siteTrackingProtection
            )
        )
    }
    val originFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(siteTrackingProtection) {
        indicatorWidth = toolbarIndicatorWidth(siteTrackingProtection)
        indicatorWidthPx = toolbarIndicatorWidthPx(siteTrackingProtection)
    }

    LaunchedEffect(editMode, tabSessionState.content.url, tabSessionState.content.searchTerms) {
        if (editMode) {
            setSearchText(parseInput())
        } else {
            focusManager.clearFocus(force = true)
            setSearchText(TextFieldValue(tabSessionState.content.url))
        }
    }

    Box(
        modifier = originModifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color.DarkGray),
    ) {
        // origin editor
        val customTextSelectionColors = TextSelectionColors(
            handleColor = Color.White, backgroundColor = Color.White.copy(alpha = 0.4F)
        )
        // url editor
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = indicatorWidth * (animationValue) + TOOLBAR_SEARCH_ENGINE_SELECTOR_WIDTH * (1F - animationValue),
                )
                .padding(
                    end = (TOOLBAR_ACTION_WIDTH * (1F - animationValue)) + 4.dp,
                ),
        ) {
            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                BasicTextField(
                    value = searchText,
                    onValueChange = { v ->
                        // move cursor to end
                        setSearchText(v)
                        undoClearText = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart)
                        .focusRequester(originFocusRequester)
                        .focusable()
                        .onFocusChanged {
                            if (it.isFocused) {
                                onStartSearch.invoke()
                            } else if (it.hasFocus) {
                                onStartSearch.invoke()
                            } else {
                                onStopSearch.invoke()
                            }
                        },
                    enabled = true,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
//                        when (animationValue < 0.5) {
//                            true -> {
//                                Color.White.copy(
//                                    alpha = 255F - animationValue.toRange(
//                                        from = Pair(
//                                            0F, 0.5F
//                                        ), to = Pair(0F, 255F)
//                                    )
//                                )
//                            }
//
//                            false -> {
//                                Color.White.copy(
//                                    alpha = animationValue.toRange(
//                                        from = Pair(
//                                            0.5F, 1F
//                                        ), to = Pair(0F, 255F)
//                                    )
//                                )
//                            }
//                        }
                        textAlign = TextAlign.Start, lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.None,
                        ), fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            with(searchText.text) {
                                if (this.isUrl()) {
                                    context.components.useCases.sessionUseCases.loadUrl(
                                        url = this.toNormalizedUrl(),
                                        flags = mozilla.components.concept.engine.EngineSession.LoadUrlFlags.none()
                                    )
                                } else {
                                    context.components.useCases.searchUseCases.defaultSearch.invoke(
                                        searchTerms = this,
                                        searchEngine = context.components.core.store.state.search.selectedOrDefaultSearchEngine!!,
                                        parentSessionId = null,
                                    )
                                }
                            }
                            onStopSearch.invoke()
                        },
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go
                    ),
                )
            }
            // end gradient
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .aspectRatio(1F)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent, Color.DarkGray
                            )
                        )
                    )
            )
        }

        // search engine selector
        ToolbarSearchEngineSelector(
            currentSearchEngine = searchEngine,
            modifier = Modifier
                .padding(start = 4.dp)
                .height(ComponentDimens.TOOLBAR_HEIGHT - 16.dp - 8.dp)
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(
                        x = (-TOOLBAR_SEARCH_ENGINE_SELECTOR_WIDTH_PX * animationValue).roundToInt(),
                        y = 0,
                    )
                },
        )

        // indicators
        Row(
            modifier = indicatorModifier
                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                .background(Color.Transparent)
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(x = (-indicatorWidthPx * (1F - animationValue)).roundToInt(), y = 0)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                TOOLBAR_INDICATOR_ICON_PADDING, Alignment.CenterHorizontally
            )
        ) {
            // toolbar indicators
            ToolbarTrackingProtectionIndicator(trackingProtection = siteTrackingProtection)
            if (siteTrackingProtection != SiteTrackingProtection.OFF_GLOBALLY) ToolbarSeparator()
            ToolbarSecurityIndicator(siteSecure)
//            if (tabSessionState.content.url == null) ToolbarSeparator()
//            ToolbarEmptyIndicator(enabled = tabSessionState.content.url == null)
        }

        // undo / clear buttons
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(
                    start = 4.dp, end = 8.dp
                )
                .size(TOOLBAR_INDICATOR_ICON_SIZE)
                .offset {
                    IntOffset(x = (TOOLBAR_ACTION_WIDTH_PX * animationValue).roundToInt(), y = 0)
                },
        ) {
            if (editMode && undoClearText != null) {
                ToolbarUndoClearText(
                    onClick = {
                        setSearchText(undoClearText!!)
                        undoClearText = null
                    }, modifier = Modifier.align(Alignment.CenterEnd)
                )
            } else if (editMode && searchText.text.isNotEmpty()) {
                ToolbarClearText(
                    onClick = {
                        undoClearText = searchText
                        setSearchText(TextFieldValue(""))
                    }, modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

/**
 * Indicates which tracking protection status a site has.
 */
enum class SiteTrackingProtection {
    /**
     * The site has tracking protection enabled, but none trackers have been blocked or detected.
     */
    ON_NO_TRACKERS_BLOCKED,

    /**
     * The site has tracking protection enabled, and trackers have been blocked or detected.
     */
    ON_TRACKERS_BLOCKED,

    /**
     * Tracking protection has been disabled for a specific site.
     */
    OFF_FOR_A_SITE,

    /**
     * Tracking protection has been disabled for all sites.
     */
    OFF_GLOBALLY,
}

enum class SiteSecurity {
    INSECURE, SECURE,
}

@Composable
fun ToolbarEmptyIndicator(enabled: Boolean) {
    if (enabled) Icon(
        modifier = Modifier.size(TOOLBAR_INDICATOR_ICON_SIZE),
        painter = painterResource(id = R.drawable.ic_search_24),
        contentDescription = "empty indicator",
        tint = Color.White
    )
}

@Composable
private fun ToolbarTrackingProtectionIndicator(trackingProtection: SiteTrackingProtection?) {
    when (trackingProtection) {
        SiteTrackingProtection.ON_TRACKERS_BLOCKED, SiteTrackingProtection.ON_NO_TRACKERS_BLOCKED -> {
            Icon(
                modifier = Modifier.size(TOOLBAR_INDICATOR_ICON_SIZE),
                painter = painterResource(id = R.drawable.ic_tracking_protection_on_trackers_blocked),
                contentDescription = "tracking protection indicator",
                tint = Color.White
            )
        }

        SiteTrackingProtection.OFF_FOR_A_SITE -> {
            Icon(
                modifier = Modifier.size(TOOLBAR_INDICATOR_ICON_SIZE),
                painter = painterResource(id = R.drawable.ic_tracking_protection_on_trackers_blocked),
                contentDescription = "tracking protection indicator",
                tint = Color.White
            )
        }

        SiteTrackingProtection.OFF_GLOBALLY -> {}

        else -> {}
    }
}

@Composable
private fun ToolbarSecurityIndicator(siteSecurity: SiteSecurity) {
    if (siteSecurity == SiteSecurity.SECURE) {
        Icon(
            modifier = Modifier.size(TOOLBAR_INDICATOR_ICON_SIZE),
            painter = painterResource(id = R.drawable.ic_lock_20),
            contentDescription = "security indicator",
            tint = Color.White
        )
    } else if (siteSecurity == SiteSecurity.INSECURE) {
        Icon(
            modifier = Modifier.size(TOOLBAR_INDICATOR_ICON_SIZE),
            painter = painterResource(id = R.drawable.ic_broken_lock),
            contentDescription = "security indicator",
            tint = Color.White
        )
    }
}

@Composable
private fun ToolbarSearchEngineSelector(
    currentSearchEngine: SearchEngine,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var showPopupMenu by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .focusable(false)
            .clickable { showPopupMenu = true },
    ) {
        ToolbarSearchEngineSelectorPopupMenu(
            searchEngines = context.components.core.store.state.search.searchEngines,
            showPopupMenu = showPopupMenu,
            setShowPopupMenu = { showPopupMenu = it },
        )
        Row(
            modifier = Modifier
                .background(
                    color = Color.Black, shape = MaterialTheme.shapes.extraSmall
                )
                .fillMaxHeight()
                .focusGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                bitmap = currentSearchEngine.icon.asImageBitmap(),
                contentDescription = "search engine icon",
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                    .aspectRatio(1F),
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_down_24),
                contentDescription = "open menu",
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(6.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun ToolbarClearText(onClick: () -> Unit, modifier: Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_clear_24),
        contentDescription = "",
        modifier = modifier
            .size(TOOLBAR_INDICATOR_ICON_SIZE)
            .clickable(onClick = onClick),
        tint = Color.LightGray,
    )
}

@Composable
private fun ToolbarUndoClearText(onClick: () -> Unit, modifier: Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_undo_24),
        contentDescription = "",
        modifier = modifier
            .size(TOOLBAR_INDICATOR_ICON_SIZE)
            .clickable(onClick = onClick),
        tint = Color.LightGray,
    )
}


@Composable
private fun ToolbarSearchEngineSelectorPopupMenu(
    searchEngines: List<SearchEngine>, showPopupMenu: Boolean, setShowPopupMenu: (Boolean) -> Unit
) {
    fun setCurrentSearchEngine(context: Context, searchEngine: SearchEngine) {
        context.components.useCases.searchUseCases.selectSearchEngine.invoke(searchEngine)
    }

    val context = LocalContext.current
    DropdownMenu(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
        expanded = showPopupMenu,
        containerColor = Color.Black,
        onDismissRequest = { setShowPopupMenu(false) },
    ) {
        for (engine in searchEngines) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TOOLBAR_MENU_OPTION_HEIGHT)
                    .clickable {
                        setCurrentSearchEngine(context, engine)
                        setShowPopupMenu(false)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    bitmap = engine.icon.asImageBitmap(),
                    contentDescription = "search engine icon",
                    modifier = Modifier
                        .size(TOOLBAR_ICON_SIZE)
                        .clip(MaterialTheme.shapes.extraSmall)

                )
                Text(
                    engine.name,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                )
            }
//            DividerToolbarMenuItem()
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            thickness = 0.5.dp,
            color = Color.White,
        )
        // TODO: search engine settings
    }
}
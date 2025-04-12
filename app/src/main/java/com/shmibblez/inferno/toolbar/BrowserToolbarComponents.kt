package com.shmibblez.inferno.toolbar

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.sessionUseCases
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.DividerToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.FindInPageToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.NavOptionsToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.PrivateModeToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.ReaderViewToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.RequestDesktopSiteToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.SettingsToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarMenuItemsScopeInstance.ShareToolbarMenuItem
import com.shmibblez.inferno.toolbar.ToolbarOptionsScopeInstance.ToolbarSeparator
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarClearText
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarSearchEngineSelector
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarSecurityIndicator
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarTrackingProtectionIndicator
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarUndoClearText
import mozilla.components.browser.state.ext.getUrl
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.selector.privateTabs
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.searchEngines
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.ktx.android.content.createChooserExcludingCurrentApp
import mozilla.components.support.ktx.android.content.share
import mozilla.components.support.ktx.kotlin.isUrl
import mozilla.components.support.ktx.kotlin.toNormalizedUrl
import kotlin.math.roundToInt

// TODO: test implementations
// todo: add options icons for
//  - settings (settings icon)
//  - find in page (search icon)
//  - view desktop site toggle (desktop icon)
//  - share (share icon)
//  - private mode toggle (incog symbol) (incog with browser icon bottom right, reversed for switch back)
//  - print page
//  - scrolling screenshot
//  - extensions (go to extensions page, installed)
//  - reader view (if enabled for page)
// todo:
//   - add settings screen for options shown on toolbar, max 7, rearrangeable and selectable from list
//   - add double up and down chevron icon for options tray
//     - options tray is a sheet that pops up or down and shows all options available as icons,
//     scrollable horizontal
//   - make options in menu a grid


val ICON_SIZE = 20.dp
val ICON_PADDING = 8.dp
val INDICATOR_ICON_SIZE = 16.dp
val INDICATOR_ICON_PADDING = 4.dp
val OPTION_HEIGHT = 40.dp


/**
 * @param progress 0.0 is 0%, 1.0 is 100%
 */
@Composable
internal fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .height(ComponentDimens.PROGRESS_BAR_HEIGHT)
            .fillMaxWidth(),
        color = Color.Red,
        trackColor = Color.Black
    )
}

interface ToolbarOptionsScope {
    // TODO: add options icons for
    //  - find in page (search icon)
    //  - switch to desktop site (desktop icon)
    //  - share (share icon)
    //  - settings (settings icon)
    //  - private mode (incog symbol)
    //  - print page
    //  - scrolling screenshot

    @Composable
    fun ToolbarSeparator()

    @Composable
    fun ToolbarBack(enabled: Boolean)

    @Composable
    fun ToolbarForward(enabled: Boolean)

    @Composable
    fun ToolbarReload(enabled: Boolean, loading: Boolean)

    @Composable
    fun ToolbarStopLoading(enabled: Boolean)

    @Composable
    fun ToolbarShare(url: String?)

    @Composable
    fun ToolbarShowTabsTray(tabCount: Int, onNavToTabsTray: () -> Unit)
}

object ToolbarOptionsScopeInstance : ToolbarOptionsScope {
    private const val disabledAlpha = 0.5F

    @Composable
    override fun ToolbarSeparator() {
        VerticalDivider(
            modifier = Modifier.height(ICON_SIZE),
            color = Color.White,
            thickness = 1.dp,
        )
    }

    @Composable
    override fun ToolbarBack(enabled: Boolean) {
        val useCases = sessionUseCases()
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .alpha(if (enabled) 1F else disabledAlpha)
                .clickable(enabled = enabled) { useCases.goBack.invoke() },
            painter = painterResource(id = R.drawable.baseline_chevron_left_24),
            contentDescription = "back",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarForward(enabled: Boolean) {
        val useCases = sessionUseCases()
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .alpha(if (enabled) 1F else disabledAlpha)
                .clickable(enabled = enabled) { useCases.goForward.invoke() },
            painter = painterResource(id = R.drawable.baseline_chevron_right_24),
            contentDescription = "forward",
            tint = Color.White,
        )
    }

    @Composable
    override fun ToolbarReload(enabled: Boolean, loading: Boolean) {
        val useCases = sessionUseCases()
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .alpha(if (enabled) 1F else disabledAlpha)
                .clickable(enabled = enabled) {
                    if (loading) useCases.stopLoading.invoke() else useCases.reload.invoke()
                },
            painter = if (loading) painterResource(id = R.drawable.ic_cross_24) else painterResource(
                id = R.drawable.ic_arrow_clockwise_24
            ),
            contentDescription = "reload page",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarStopLoading(enabled: Boolean) {
        val useCases = sessionUseCases()
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .alpha(if (enabled) 1F else disabledAlpha)
                .clickable(enabled = enabled) { useCases.stopLoading.invoke() },
            painter = painterResource(id = R.drawable.ic_cross_24),
            contentDescription = "stop loading",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarShare(url: String?) {
        val context = LocalContext.current
        fun share() {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Intent.EXTRA_TEXT, url)
            }

            try {
                context.startActivity(
                    intent.createChooserExcludingCurrentApp(
                        context,
                        context.getString(R.string.mozac_feature_contextmenu_share_link),
                    ),
                )
            } catch (e: ActivityNotFoundException) {
                mozilla.components.support.base.log.Log.log(
                    mozilla.components.support.base.log.Log.Priority.WARN,
                    message = "No activity to share to found",
                    throwable = e,
                    tag = "createShareLinkCandidate",
                )
            }
        }

        val enabled = url != null
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .alpha(if (enabled) 1F else disabledAlpha)
                .clickable(enabled = enabled, onClick = ::share),
            painter = painterResource(id = R.drawable.ic_share),
            contentDescription = "share",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarShowTabsTray(tabCount: Int, onNavToTabsTray: () -> Unit) {
        Box(
            modifier = Modifier
                .size(ICON_SIZE)
//                .alpha(0.5F)
                .clickable { onNavToTabsTray.invoke() }
                .wrapContentHeight(unbounded = true),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.ic_tabcounter_box_24),
                contentDescription = "show tabs tray",
                tint = Color.White,
            )
            InfernoText(
                modifier = Modifier.fillMaxSize(),
                text = tabCount.toString(),
                fontColor = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 10.sp
            )
        }
    }

    @Composable
    fun ToolbarMenuIcon(onShowMenuBottomSheet: () -> Unit) {
        Icon(
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable(onClick = onShowMenuBottomSheet),
            painter = painterResource(id = R.drawable.ic_app_menu_24),
            contentDescription = "menu",
            tint = Color.White
        )
    }
}

// start padding + (width - vertical padding since 1:1 aspect ratio) + expand icon start padding + expand icon size + expand icon end padding
private val TOOLBAR_SEARCH_ENGINE_SELECTOR_WIDTH =
    4.dp + (ComponentDimens.TOOLBAR_HEIGHT - 16.dp - 8.dp - 4.dp) + (4.dp + 6.dp + 4.dp) + 4.dp
private val TOOLBAR_SEARCH_ENGINE_SELECTOR_WIDTH_PX = TOOLBAR_SEARCH_ENGINE_SELECTOR_WIDTH.toPx()

// start padding + indicator icon size + end padding
private val TOOLBAR_ACTION_WIDTH = 4.dp + INDICATOR_ICON_SIZE + 8.dp
private val TOOLBAR_ACTION_WIDTH_PX = TOOLBAR_ACTION_WIDTH.toPx()

//
private fun toolbarIndicatorWidth(siteTrackingProtection: SiteTrackingProtection): Dp {
    return 8.dp + INDICATOR_ICON_SIZE + INDICATOR_ICON_PADDING + (if (siteTrackingProtection != SiteTrackingProtection.OFF_GLOBALLY) INDICATOR_ICON_SIZE + INDICATOR_ICON_PADDING + 1.dp + INDICATOR_ICON_PADDING else 0.dp) + 4.dp
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
                INDICATOR_ICON_PADDING, Alignment.CenterHorizontally
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
                .size(INDICATOR_ICON_SIZE)
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

interface ToolbarOriginScope {
    @Composable
    fun ToolbarEmptyIndicator(enabled: Boolean)

    @Composable
    fun ToolbarTrackingProtectionIndicator(trackingProtection: SiteTrackingProtection?)

    @Composable
    fun ToolbarSecurityIndicator(siteSecurity: SiteSecurity)

    @Composable
    fun ToolbarSearchEngineSelector(
        currentSearchEngine: SearchEngine,
        modifier: Modifier = Modifier,
    )

    @Composable
    fun ToolbarClearText(onClick: () -> Unit, modifier: Modifier = Modifier)

    @Composable
    fun ToolbarUndoClearText(onClick: () -> Unit, modifier: Modifier = Modifier)
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

object ToolbarOriginScopeInstance : ToolbarOriginScope {
    @Composable
    override fun ToolbarEmptyIndicator(enabled: Boolean) {
        if (enabled) Icon(
            modifier = Modifier.size(INDICATOR_ICON_SIZE),
            painter = painterResource(id = R.drawable.ic_search_24),
            contentDescription = "empty indicator",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarTrackingProtectionIndicator(trackingProtection: SiteTrackingProtection?) {
        when (trackingProtection) {
            SiteTrackingProtection.ON_TRACKERS_BLOCKED, SiteTrackingProtection.ON_NO_TRACKERS_BLOCKED -> {
                Icon(
                    modifier = Modifier.size(INDICATOR_ICON_SIZE),
                    painter = painterResource(id = R.drawable.ic_tracking_protection_on_trackers_blocked),
                    contentDescription = "tracking protection indicator",
                    tint = Color.White
                )
            }

            SiteTrackingProtection.OFF_FOR_A_SITE -> {
                Icon(
                    modifier = Modifier.size(INDICATOR_ICON_SIZE),
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
    override fun ToolbarSecurityIndicator(siteSecurity: SiteSecurity) {
        if (siteSecurity == SiteSecurity.SECURE) {
            Icon(
                modifier = Modifier.size(INDICATOR_ICON_SIZE),
                painter = painterResource(id = R.drawable.ic_lock_20),
                contentDescription = "security indicator",
                tint = Color.White
            )
        } else if (siteSecurity == SiteSecurity.INSECURE) {
            Icon(
                modifier = Modifier.size(INDICATOR_ICON_SIZE),
                painter = painterResource(id = R.drawable.ic_broken_lock),
                contentDescription = "security indicator",
                tint = Color.White
            )
        }
    }

    @Composable
    override fun ToolbarSearchEngineSelector(
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
    override fun ToolbarClearText(onClick: () -> Unit, modifier: Modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_clear_24),
            contentDescription = "",
            modifier = modifier
                .size(INDICATOR_ICON_SIZE)
                .clickable(onClick = onClick),
            tint = Color.LightGray,
        )
    }

    @Composable
    override fun ToolbarUndoClearText(onClick: () -> Unit, modifier: Modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_undo_24),
            contentDescription = "",
            modifier = modifier
                .size(INDICATOR_ICON_SIZE)
                .clickable(onClick = onClick),
            tint = Color.LightGray,
        )
    }
}

interface ToolbarMenuItemsScope {
    // TODO: add add to home screen, add-ons, synced tabs, and report issue
    @Composable
    fun DividerToolbarMenuItem(modifier: Modifier = Modifier)

    @Composable
    fun ShareToolbarMenuItem()

    @Composable
    fun RequestDesktopSiteToolbarMenuItem(desktopMode: Boolean)

    @Composable
    fun FindInPageToolbarMenuItem(
        onActivateFindInPage: () -> Unit, dismissMenuSheet: () -> Unit
    )

    @Composable
    fun SettingsToolbarMenuItem(onNavToSettings: () -> Unit)

    @Composable
    fun PrivateModeToolbarMenuItem(isPrivateMode: Boolean, dismissMenuSheet: () -> Unit)

    @Composable
    fun ReaderViewToolbarMenuItem(
        enabled: Boolean, onActivateReaderView: () -> Unit, dismissMenuSheet: () -> Unit
    )

    // stop, refresh, forward, back
    @Composable
    fun NavOptionsToolbarMenuItem(loading: Boolean)
}

object ToolbarMenuItemsScopeInstance : ToolbarMenuItemsScope {
    @Composable
    override fun DividerToolbarMenuItem(modifier: Modifier) {
        HorizontalDivider(modifier = modifier, thickness = 0.25.dp, color = Color.White)
    }

    @Composable
    override fun ShareToolbarMenuItem() {
        val context = LocalContext.current
        Box(contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable {
                    val url = context.components.core.store.state.selectedTab
                        ?.getUrl()
                        .orEmpty()
                    context.share(url)
                }) {
            Text(
                text = "Share", color = Color.White, modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun RequestDesktopSiteToolbarMenuItem(desktopMode: Boolean) {
        val useCases = sessionUseCases()
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable { useCases.requestDesktopSite.invoke(!desktopMode) }) {
            Text(
                text = "Request Desktop Site",
                color = Color.White,
                modifier = Modifier.wrapContentHeight(),
            )
            InfernoCheckbox(
                checked = desktopMode,
                onCheckedChange = {},
            )
        }
    }

    @Composable
    override fun FindInPageToolbarMenuItem(
        onActivateFindInPage: () -> Unit, dismissMenuSheet: () -> Unit
    ) {
        Box(contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable {
                    onActivateFindInPage()
                    dismissMenuSheet()
                }) {
            Text(
                text = "Find In Page", color = Color.White, modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun SettingsToolbarMenuItem(onNavToSettings: () -> Unit) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable { onNavToSettings.invoke() },
        ) {
            Text(
                text = "Settings", color = Color.White, modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun PrivateModeToolbarMenuItem(isPrivateMode: Boolean, dismissMenuSheet: () -> Unit) {
        fun newTab(tabsUseCases: TabsUseCases, isPrivateSession: Boolean) {
            Log.d("BrowserTabBar", "newTab: isPrivateSession: $isPrivateSession")
            tabsUseCases.addTab(
                url = if (isPrivateSession) "inferno:privatebrowsing" else "inferno:home",
                selectTab = true,
                private = isPrivateSession
            )
        }

        val state = LocalContext.current.components.core.store.state
        val tabsUseCases = LocalContext.current.components.useCases.tabsUseCases
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable {
                    if (isPrivateMode) {
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
                    } else {
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

                    dismissMenuSheet()
                },
        ) {
            Text(
                text = "Private Browsing Activated",
                color = Color.White,
                modifier = Modifier.wrapContentHeight(),
            )
            InfernoCheckbox(
                checked = isPrivateMode,
                onCheckedChange = {},
            )
        }
    }

    @Composable
    override fun ReaderViewToolbarMenuItem(
        enabled: Boolean, onActivateReaderView: () -> Unit, dismissMenuSheet: () -> Unit
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .alpha(if (enabled) 1F else 0.5F)
                .height(OPTION_HEIGHT)
                .fillMaxWidth()
                .clickable(enabled) {
                    onActivateReaderView.invoke()
                    dismissMenuSheet.invoke()
                },
        ) {
            Text(
                text = stringResource(R.string.browser_menu_turn_on_reader_view),
                color = Color.White,
                modifier = Modifier.wrapContentHeight()
            )
        }
    }

    @Composable
    override fun NavOptionsToolbarMenuItem(loading: Boolean) {
        val tabSessionState = LocalContext.current.components.core.store.state.selectedTab
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(OPTION_HEIGHT)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolbarOptionsScopeInstance.ToolbarBack(
                enabled = tabSessionState?.content?.canGoBack ?: false,
            )
            ToolbarOptionsScopeInstance.ToolbarReload(
                enabled = tabSessionState != null, loading = loading,
            )
            ToolbarOptionsScopeInstance.ToolbarShare(
                url = tabSessionState?.content?.url,
            )
            ToolbarOptionsScopeInstance.ToolbarForward(
                enabled = tabSessionState?.content?.canGoForward ?: false,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarMenuBottomSheet(
    tabSessionState: TabSessionState?,
    loading: Boolean,
    onDismissMenuBottomSheet: () -> Unit,
    onActivateFindInPage: () -> Unit,
    readerViewEnabled: Boolean,
    onActivateReaderView: () -> Unit,
    onNavToSettings: () -> Unit,
) {
    if (tabSessionState == null) return
    ModalBottomSheet(
        onDismissRequest = onDismissMenuBottomSheet,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Black.copy(alpha = 0.75F),
        scrimColor = Color.Black.copy(alpha = 0.5F),
        shape = RectangleShape,
        dragHandle = { /* no drag handle */
            // in case want to add one, make custom component centered in middle
//            BottomSheetDefaults.DragHandle(
//                color = Color.White,
//                height = SHEET_HANDLE_HEIGHT,
////            shape = RectangleShape,
//            )
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            // todo: move to bottom when switch to grid view
            // stop, refresh, forward, back
            NavOptionsToolbarMenuItem(loading)

            DividerToolbarMenuItem()

            ShareToolbarMenuItem()
            DividerToolbarMenuItem()

            PrivateModeToolbarMenuItem(
                isPrivateMode = tabSessionState.content.private,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
            DividerToolbarMenuItem()

            RequestDesktopSiteToolbarMenuItem(desktopMode = tabSessionState.content.desktopMode)
            DividerToolbarMenuItem()

            FindInPageToolbarMenuItem(
                onActivateFindInPage = onActivateFindInPage,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
            DividerToolbarMenuItem()

            ReaderViewToolbarMenuItem(
                enabled = readerViewEnabled,
                onActivateReaderView = onActivateReaderView,
                dismissMenuSheet = onDismissMenuBottomSheet,
            )
            DividerToolbarMenuItem()

            SettingsToolbarMenuItem(onNavToSettings = onNavToSettings)

            // fill remaining space
            Spacer(modifier = Modifier.weight(1F))
        }
    }
}

@Composable
fun ToolbarSearchEngineSelectorPopupMenu(
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
                    .height(OPTION_HEIGHT)
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
                        .size(ICON_SIZE)
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

        DividerToolbarMenuItem()
        // TODO: search engine settings
    }
}
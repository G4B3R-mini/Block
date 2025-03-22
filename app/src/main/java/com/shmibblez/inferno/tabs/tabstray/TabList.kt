package com.shmibblez.inferno.tabs.tabstray

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.DismissibleItemBackground
import com.shmibblez.inferno.compose.SwipeToDismissBox
import com.shmibblez.inferno.compose.SwipeToDismissState
import com.shmibblez.inferno.compose.TabThumbnail
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.toShortUrl
import com.shmibblez.inferno.tabstray.HEADER_ITEM_KEY
import com.shmibblez.inferno.tabstray.SPAN_ITEM_KEY
import com.shmibblez.inferno.tabstray.TabsTrayTestTag
import com.shmibblez.inferno.tabstray.browser.compose.DragItemContainer
import com.shmibblez.inferno.tabstray.browser.compose.createListReorderState
import com.shmibblez.inferno.tabstray.browser.compose.detectListPressAndDrag
import com.shmibblez.inferno.tabstray.ext.toDisplayTitle
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.engine.mediasession.MediaSession.PlaybackState
import mozilla.components.support.ktx.kotlin.MAX_URI_LENGTH
import mozilla.components.ui.colors.PhotonColors
import kotlin.math.max

// todo: everything with FirefoxTheme commented out was fixed by shameless haccs
//   - tab thumbnails
//     - round out thumbnails, add white border,
//     - thumbnails not filling whole width
//     - use inferno icon for inferno:home or about:blank url (copy from MiniTabBar composable)
//   - menu icon too big, more padding just for that one (4.dp to start)
//   - add new tab floating button at bottom right
//   - set bottom padding item height properly
//
// todo: bugs
//   - hold to move tab not moving properly

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabList(
    activeTabId: String?,
    activeTabIndex: Int,
    tabs: List<TabSessionState>,
    mode: InfernoTabsTrayMode,
    header: (@Composable () -> Unit)? = null,
    onTabClick: (tab: TabSessionState) -> Unit,
    onTabClose: (tab: TabSessionState) -> Unit,
    onTabMediaClick: (tab: TabSessionState) -> Unit,
    onTabMove: (String, String?, Boolean) -> Unit,
    onTabDragStart: () -> Unit,
    onTabLongClick: (TabSessionState) -> Unit,
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = activeTabIndex)
    val tabThumbnailSize = max(
        LocalContext.current.resources.getDimensionPixelSize(R.dimen.tab_tray_list_item_thumbnail_height),
        LocalContext.current.resources.getDimensionPixelSize(R.dimen.tab_tray_list_item_thumbnail_width),
    )
    val isInMultiSelectMode = mode.isSelect()
    val reorderState = createListReorderState(
        listState = state,
        onMove = { initialTab, newTab ->
            onTabMove(
                (initialTab.key as String),
                (newTab.key as String),
                initialTab.index < newTab.index,
            )
        },
        onLongPress = {
            tabs.firstOrNull { tab -> tab.id == it.key }?.let { tab ->
                onTabLongClick(tab)
            }
        },
        onExitLongPress = onTabDragStart,
        ignoredItems = listOf(HEADER_ITEM_KEY, SPAN_ITEM_KEY),
    )
    var shouldLongPress by remember { mutableStateOf(!isInMultiSelectMode) }
    LaunchedEffect(mode, reorderState.draggingItemKey) {
        if (reorderState.draggingItemKey == null) {
            shouldLongPress = mode.isNormal()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .detectListPressAndDrag(
                listState = state,
                reorderState = reorderState,
                shouldLongPressToDrag = shouldLongPress,
            ),
        state = state,
    ) {
        header?.let {
            item(key = HEADER_ITEM_KEY) {
                header()
            }
        }
        itemsIndexed(
            items = tabs,
            key = { _, tab -> tab.id },
        ) { index, tab ->
            DragItemContainer(
                state = reorderState,
                position = index + if (header != null) 1 else 0,
                key = tab.id,
            ) {
//                TabListItem(
//                    tab = tab,
//                    thumbnailSize = tabThumbnailSize,
//                    isSelected = tab.id == activeTabId,
//                    multiSelectionEnabled = isInMultiSelectMode,
//                    multiSelectionSelected = mode.selectedTabs.contains(tab),
//                    shouldClickListen = reorderState.draggingItemKey != tab.id,
//                    swipingEnabled = !state.isScrollInProgress,
//                    onCloseClick = onTabClose,
//                    onMediaClick = onTabMediaClick,
//                    onClick = onTabClick,
//                )
                ListTab(
                    tab = tab,
                    thumbnailSize = tabThumbnailSize,
                    isSelected = tab.id == activeTabId,
                    multiSelectionEnabled = isInMultiSelectMode,
                    multiSelectionSelected = mode.selectedTabs.contains(tab),
                    shouldClickListen = reorderState.draggingItemKey != tab.id,
                    swipingEnabled = !state.isScrollInProgress,
                    onCloseClick = onTabClose,
                    onMediaClick = onTabMediaClick,
                    onClick = onTabClick,
                )
            }
        }

        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListTab(
    tab: TabSessionState,
    thumbnailSize: Int,
    isSelected: Boolean = false,
    multiSelectionEnabled: Boolean = false,
    multiSelectionSelected: Boolean = false,
    shouldClickListen: Boolean = true,
    swipingEnabled: Boolean = true,
    onCloseClick: (tab: TabSessionState) -> Unit,
    onMediaClick: (tab: TabSessionState) -> Unit,
    onClick: (tab: TabSessionState) -> Unit,
    onLongClick: ((tab: TabSessionState) -> Unit)? = null,
) {
    // Used to propagate the ripple effect to the whole tab
    val interactionSource = remember { MutableInteractionSource() }

    val clickableModifier = if (onLongClick == null) {
        Modifier.clickable(
            enabled = shouldClickListen,
            interactionSource = interactionSource,
            indication = ripple(
                color = clickableColor(),
            ),
            onClick = { onClick(tab) },
        )
    } else {
        Modifier.combinedClickable(
            enabled = shouldClickListen,
            interactionSource = interactionSource,
            indication = ripple(
                color = clickableColor(),
            ),
            onLongClick = { onLongClick(tab) },
            onClick = { onClick(tab) },
        )
    }

    val decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay()

    val density = LocalDensity.current
    val swipeState = remember(multiSelectionEnabled, swipingEnabled) {
        SwipeToDismissState(
            density = density,
            enabled = !multiSelectionEnabled && swipingEnabled,
            decayAnimationSpec = decayAnimationSpec,
        )
    }

    SwipeToDismissBox(
        state = swipeState,
        onItemDismiss = {
            onCloseClick(tab)
        },
        backgroundContent = {
            DismissibleItemBackground(
                isSwipeActive = swipeState.swipingActive,
                isSwipingToStart = swipeState.isSwipingToStart,
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
//                .background(FirefoxTheme.colors.layer3)
//                .background(contentBackgroundColor)
                .background(if (isSelected) Color.DarkGray else Color.Black)
                .then(clickableModifier)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                .testTag(TabsTrayTestTag.tabItemRoot)
                .semantics {
                    selected = isSelected
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Thumbnail(
                tab = tab,
                size = thumbnailSize,
                multiSelectionEnabled = multiSelectionEnabled,
                isSelected = multiSelectionSelected,
                onMediaIconClicked = { onMediaClick(it) },
                interactionSource = interactionSource,
            )

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(weight = 1f),
            ) {
                InfernoText(
                    text = tab.toDisplayTitle().take(MAX_URI_LENGTH),
                    fontColor = Color.White, // FirefoxTheme.colors.textPrimary,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        letterSpacing = 0.5.sp,
                        lineHeight = 24.sp,
                    ), // FirefoxTheme.typography.body1,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )

                InfernoText(
                    text = tab.content.url.toShortUrl(),
                    fontColor = Color.White, // FirefoxTheme.colors.textSecondary,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W400,
                        letterSpacing = 0.25.sp,
                        lineHeight = 20.sp,
                    ), // FirefoxTheme.typography.body2,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }

            if (!multiSelectionEnabled) {
                IconButton(
                    onClick = { onCloseClick(tab) },
                    modifier = Modifier
                        .size(size = 48.dp)
                        .testTag(TabsTrayTestTag.tabItemClose),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.mozac_ic_cross_24),
                        contentDescription = stringResource(
                            id = R.string.close_tab_title,
                            tab.toDisplayTitle(),
                        ),
                        tint = Color.White, // FirefoxTheme.colors.iconPrimary,
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun clickableColor() = when (isSystemInDarkTheme()) {
//    true -> PhotonColors.White
//    false -> PhotonColors.Black
    else -> PhotonColors.White // todo: update with colors / theme update
}


@Composable
private fun Thumbnail(
    tab: TabSessionState,
    size: Int,
    multiSelectionEnabled: Boolean,
    isSelected: Boolean,
    onMediaIconClicked: ((TabSessionState) -> Unit),
    interactionSource: MutableInteractionSource,
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        TabThumbnail(
            tab = tab,
            size = size,
            modifier = Modifier
                .size(width = 92.dp, height = 72.dp)
                .testTag(TabsTrayTestTag.tabItemThumbnail),
            contentDescription = stringResource(id = R.string.mozac_browser_tabstray_open_tab),
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(width = 92.dp, height = 72.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(143, 0, 255)), // FirefoxTheme.colors.layerAccentNonOpaque),
//                contentAlignment = Alignment.Center,
            )

            Card(
                modifier = Modifier
                    .size(size = 40.dp)
                    .align(alignment = Alignment.Center),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(
                        143, 0, 255
                    )
                ), // FirefoxTheme.colors.layerAccent ),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mozac_ic_checkmark_24),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 8.dp),
                    contentDescription = null,
                    tint = colorResource(id = R.color.mozac_ui_icons_fill),
                )
            }
        }

        if (!multiSelectionEnabled) {
            MediaImage(
                tab = tab,
                onMediaIconClicked = onMediaIconClicked,
                modifier = Modifier.align(Alignment.TopEnd),
                interactionSource = interactionSource,
            )
        }
    }
}

/**
 * Controller buttons for the media (play/pause) state for the given [tab].
 *
 * @param tab [TabSessionState] which the image should be shown.
 * @param onMediaIconClicked handles the click event when tab has media session like play/pause.
 * @param modifier [Modifier] to be applied to the layout.
 * @param interactionSource [MutableInteractionSource] used to propagate the ripple effect on click.
 */
@Composable
fun MediaImage(
    tab: TabSessionState,
    onMediaIconClicked: ((TabSessionState) -> Unit),
    modifier: Modifier,
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
) {
    val (icon, contentDescription) = when (tab.mediaSessionState?.playbackState) {
        PlaybackState.PAUSED -> {
            R.drawable.media_state_play to R.string.mozac_feature_media_notification_action_play
        }

        PlaybackState.PLAYING -> {
            R.drawable.media_state_pause to R.string.mozac_feature_media_notification_action_pause
        }

        else -> return
    }
    val drawable = AppCompatResources.getDrawable(LocalContext.current, icon)
    // Follow up ticket https://github.com/mozilla-mobile/fenix/issues/25774
    Image(
        painter = rememberDrawablePainter(drawable = drawable),
        contentDescription = stringResource(contentDescription),
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
        ) { onMediaIconClicked(tab) },
    )
}
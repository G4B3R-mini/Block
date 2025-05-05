package com.shmibblez.inferno.tabs.tabstray

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.DismissibleItemBackground
import com.shmibblez.inferno.compose.SwipeToDismissBox
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.rememberSwipeToDismissState
import com.shmibblez.inferno.ext.toShortUrl
import com.shmibblez.inferno.tabstray.HEADER_ITEM_KEY
import com.shmibblez.inferno.tabstray.TabsTrayTestTag
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.recover.TabState
import mozilla.components.support.ktx.kotlin.MAX_URI_LENGTH
import mozilla.components.support.ktx.kotlin.trimmed
import mozilla.components.ui.colors.PhotonColors

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

/**
 * Returns a [String] for displaying a [TabSessionState]'s title or its url when a title is not available.
 */
fun TabState.toDisplayTitle(): String = title.ifEmpty { url.trimmed() }

@Composable
fun ClosedTabList(
    tabs: List<TabState>,
    mode: InfernoTabsTrayMode,
    header: (@Composable () -> Unit)? = null,
    onHistoryClick: () -> Unit,
    onTabClick: (tab: TabState) -> Unit,
    onTabClose: (tab: TabState) -> Unit,
    onTabLongClick: (TabState) -> Unit,
) {
    val state = rememberLazyListState()
    val isInMultiSelectMode = mode.isSelect()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = state,
    ) {
        header?.let {
            item(key = HEADER_ITEM_KEY) {
                header()
            }
        }
        items(
            items = tabs,
            key = { tab -> tab.id },
        ) { tab ->
            ClosedTabListItem(
                tab = tab,
                multiSelectionEnabled = isInMultiSelectMode,
                multiSelectionSelected = mode.selectedClosedTabs.contains(tab),
                swipingEnabled = !state.isScrollInProgress,
                onCloseClick = onTabClose,
                onClick = onTabClick,
                onLongClick = onTabLongClick,
            )

        }

        item {
            ViewFullHistoryItem(onHistoryClick)
        }
    }
}

@Composable
private fun ViewFullHistoryItem(
    onHistoryClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable(onClick = onHistoryClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_history_24),
            contentDescription = stringResource(R.string.recently_closed_show_full_history),
            modifier = Modifier.size(24.dp),
            tint = Color.White,
        )
        InfernoText(text = stringResource(R.string.recently_closed_show_full_history))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClosedTabListItem(
    tab: TabState,
    multiSelectionEnabled: Boolean = false,
    multiSelectionSelected: Boolean = false,
    swipingEnabled: Boolean = true,
    onCloseClick: (tab: TabState) -> Unit,
    onClick: (tab: TabState) -> Unit,
    onLongClick: ((tab: TabState) -> Unit)? = null,
) {
    // Used to propagate the ripple effect to the whole tab
    val interactionSource = remember { MutableInteractionSource() }

    val clickableModifier = if (onLongClick == null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(
                color = clickableColor(),
            ),
            onClick = { onClick(tab) },
        )
    } else {
        Modifier.combinedClickable(
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
    val swipeState = rememberSwipeToDismissState(
        key1 = multiSelectionEnabled,
        key2 = swipingEnabled,
        density = density,
        enabled = !multiSelectionEnabled && swipingEnabled,
        decayAnimationSpec = decayAnimationSpec,
    )

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
                .background(if (multiSelectionSelected) Color.Red else Color.Black)
                .then(clickableModifier)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                .testTag(TabsTrayTestTag.tabItemRoot),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(weight = 1f),
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
                    text = tab.url.toShortUrl(),
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
                InfernoCheckbox(
                    checked = multiSelectionSelected,
                    onCheckedChange = null,
                    modifier = Modifier.size(48.dp),
                )
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
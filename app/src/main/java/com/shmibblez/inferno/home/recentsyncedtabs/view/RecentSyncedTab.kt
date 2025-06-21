/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.recentsyncedtabs.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.Image
import com.shmibblez.inferno.compose.ThumbnailCard
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.compose.button.SecondaryButton
import com.shmibblez.inferno.compose.menu.DropdownMenu
import com.shmibblez.inferno.compose.menu.MenuItem
import com.shmibblez.inferno.compose.text.Text
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTab
import com.shmibblez.inferno.theme.FirefoxTheme
import mozilla.components.concept.base.images.ImageLoadRequest
import mozilla.components.concept.sync.DeviceType
import mozilla.components.support.ktx.kotlin.trimmed

private const val THUMBNAIL_SIZE = 108

/**
 * A recent synced tab card.
 *
 * @param tab The [RecentSyncedTab] to display.
 * @param onRecentSyncedTabClick Invoked when the user clicks on the recent synced tab.
 * @param onSeeAllSyncedTabsButtonClick Invoked when user clicks on the "See all" button in the synced tab card.
 * @param onRemoveSyncedTab Invoked when user clicks on the "Remove" dropdown menu option.
 */
@Suppress("LongMethod")
@Composable
fun RecentSyncedTab(
    tab: RecentSyncedTab?,
    onRecentSyncedTabClick: (RecentSyncedTab) -> Unit,
    onSeeAllSyncedTabsButtonClick: () -> Unit,
    onRemoveSyncedTab: (RecentSyncedTab) -> Unit,
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    fun removeSyncedTab(recentSyncedTab: RecentSyncedTab) {
        isDropdownExpanded = false
        onRemoveSyncedTab(recentSyncedTab)
    }

    Card(
        modifier = Modifier
            .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { tab?.let { onRecentSyncedTabClick(tab) } },
                onLongClick = { isDropdownExpanded = true },
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                if (tab == null) {
                    RecentTabImagePlaceholder()
                } else {
                    val imageModifier = Modifier
                        .size(108.dp, 80.dp)
                        .clip(RoundedCornerShape(8.dp))

                    if (tab.previewImageUrl != null) {
                        Image(
                            url = tab.previewImageUrl,
                            contentScale = ContentScale.Crop,
                            modifier = imageModifier,
                        )
                    } else {
                        ThumbnailCard(
                            url = tab.url,
                            request = ImageLoadRequest(
                                id = tab.url.hashCode().toString(),
                                size = LocalDensity.current.run {
                                    THUMBNAIL_SIZE.dp.toPx().toInt()
                                },
                                isPrivate = false,
                            ),
                            modifier = imageModifier,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    if (tab == null) {
                        RecentTabTitlePlaceholder()
                    } else {
                        InfernoText(
                            text = tab.title.trimmed(),
                            infernoStyle = InfernoTextStyle.Small,
//                            fontSize = 14.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (tab == null) {
                            Box(
                                modifier = Modifier
//                                    .background(FirefoxTheme.colors.layer3)
                                    .background(LocalContext.current.infernoTheme().value.secondaryBackgroundColor)
                                    .size(18.dp),
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_synced_tabs),
                                contentDescription = stringResource(
                                    R.string.recent_tabs_synced_device_icon_content_description,
                                ),
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (tab == null) {
                            TextLinePlaceHolder()
                        } else {
                            InfernoText(
                                text = tab.deviceDisplayName,
                                infernoStyle = InfernoTextStyle.SmallSecondary,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

//            Spacer(modifier = Modifier.height(32.dp))

            if (tab != null)
                SecondaryButton(
                    text = stringResource(R.string.recent_tabs_see_all_synced_tabs_button_text),
                    onClick = onSeeAllSyncedTabsButtonClick,
                )
        }
    }

    DropdownMenu(
        menuItems = listOf(
            MenuItem.TextItem(Text.Resource(R.string.recent_synced_tab_menu_item_remove)) {
                tab?.let { removeSyncedTab(it) }
            },
        ),
        expanded = isDropdownExpanded && tab != null,
        onDismissRequest = { isDropdownExpanded = false },
    )
}

/**
 * A placeholder for a recent tab image.
 */
@Composable
private fun RecentTabImagePlaceholder() {
    Box(
        modifier = Modifier
            .size(108.dp, 80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = LocalContext.current.infernoTheme().value.secondaryBackgroundColor),
    )
}

/**
 * A placeholder for a tab title.
 */
@Composable
private fun RecentTabTitlePlaceholder() {
    Column {
        TextLinePlaceHolder()

        Spacer(modifier = Modifier.height(8.dp))

        TextLinePlaceHolder()
    }
}

/**
 * A placeholder for a single line of text.
 */
@Composable
private fun TextLinePlaceHolder() {
    Box(
        modifier = Modifier
            .height(12.dp)
            .fillMaxWidth()
            .background(FirefoxTheme.colors.layer3),
    )
}

@Preview
@Composable
private fun LoadedRecentSyncedTab() {
    val tab = RecentSyncedTab(
        deviceDisplayName = "Firefox on MacBook",
        deviceType = DeviceType.DESKTOP,
        title = "This is a long site title",
        url = "https://mozilla.org",
        previewImageUrl = "https://mozilla.org",
    )
    FirefoxTheme {
        RecentSyncedTab(
            tab = tab,
            onRecentSyncedTabClick = {},
            onSeeAllSyncedTabsButtonClick = {},
            onRemoveSyncedTab = {},
        )
    }
}

@Preview
@Composable
private fun LoadingRecentSyncedTab() {
    FirefoxTheme {
        RecentSyncedTab(
            tab = null,
//            buttonBackgroundColor = FirefoxTheme.colors.layer3,
            onRecentSyncedTabClick = {},
            onSeeAllSyncedTabsButtonClick = {},
            onRemoveSyncedTab = {},
        )
    }
}

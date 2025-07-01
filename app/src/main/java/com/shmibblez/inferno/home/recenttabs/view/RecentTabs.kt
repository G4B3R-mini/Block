/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("MagicNumber", "TooManyFunctions")

package com.shmibblez.inferno.home.recenttabs.view

import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.Image
import com.shmibblez.inferno.compose.TabThumbnail
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.compose.menu.DropdownMenu
import com.shmibblez.inferno.compose.menu.MenuItem
import com.shmibblez.inferno.compose.text.Text
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.home.recenttabs.RecentTab
import com.shmibblez.inferno.settings.SupportUtils
import mozilla.components.support.ktx.kotlin.trimmed
import mozilla.components.ui.colors.PhotonColors

private const val THUMBNAIL_SIZE = 108

/**
 * A list of recent tabs to jump back to.
 *
 * @param recentTabs List of [RecentTab] to display.
 * @param menuItems List of [RecentTabMenuItem] shown long clicking a [RecentTab].
 * @param backgroundColor The background [Color] of each item.
 * @param onRecentTabClick Invoked when the user clicks on a recent tab.
 */
@Composable
fun RecentTabs(
    recentTabs: List<RecentTab>,
    menuItems: List<RecentTabMenuItem>,
    backgroundColor: Color = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
    onRecentTabClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                testTagsAsResourceId = true
                testTag = "recent.tabs"
            },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        recentTabs.forEach { tab ->
            when (tab) {
                is RecentTab.Tab -> {
                    RecentTabItem(
                        tab = tab,
                        menuItems = menuItems,
                        backgroundColor = backgroundColor,
                        onRecentTabClick = onRecentTabClick,
                    )
                }
            }
        }
    }
}

/**
 * A recent tab item.
 *
 * @param tab [RecentTab.Tab] that was recently viewed.
 * @param menuItems List of [RecentTabMenuItem] to be shown in a menu.
 * @param backgroundColor The background [Color] of the item.
 * @param onRecentTabClick Invoked when the user clicks on a recent tab.
 */
@Composable
@Suppress("LongMethod")
private fun RecentTabItem(
    tab: RecentTab.Tab,
    menuItems: List<RecentTabMenuItem>,
    backgroundColor: Color,
    onRecentTabClick: (String) -> Unit = {},
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .combinedClickable(
                enabled = true,
                onClick = { onRecentTabClick(tab.state.id) },
                onLongClick = { isMenuExpanded = true },
            )
            .background(backgroundColor)
            .clip(RoundedCornerShape(8.dp)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecentTabImage(
                tab = tab,
                modifier = Modifier
                    .size(108.dp, 80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                InfernoText(
                    text = tab.state.content.title.ifEmpty { tab.state.content.url.trimmed() },
//                    modifier = Modifier.semantics {
//                        testTagsAsResourceId = true
//                        testTag = "recent.tab.title"
//                    },
                    infernoStyle = InfernoTextStyle.Small,
//                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RecentTabIcon(
                        url = tab.state.content.url,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.Crop,
                        icon = tab.state.content.icon,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    InfernoText(
                        text = tab.state.content.url.trimmed(),
//                        modifier = Modifier.semantics {
//                            testTagsAsResourceId = true
//                            testTag = "recent.tab.url"
//                        },
                        infernoStyle = InfernoTextStyle.SmallSecondary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }

            DropdownMenu(
                menuItems = menuItems.map { item ->
                    MenuItem.TextItem(Text.String(item.title)) { item.onClick(tab) }
                },
                expanded = isMenuExpanded,
                modifier = Modifier.semantics {
                    testTagsAsResourceId = true
                    testTag = "recent.tab.menu"
                },
                onDismissRequest = { isMenuExpanded = false },
            )
        }
    }
}

/**
 * A recent tab image.
 *
 * @param tab [RecentTab] that was recently viewed.
 * @param modifier [Modifier] used to draw the image content.
 * @param contentScale [ContentScale] used to draw image content.
 */
@Composable
fun RecentTabImage(
    tab: RecentTab.Tab,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
) {
    val previewImageUrl = tab.state.content.previewImageUrl

    val context = LocalContext.current
    val isHomePage =
        previewImageUrl == SupportUtils.INFERNO_HOME_URL || previewImageUrl == SupportUtils.INFERNO_HOME_URL_2
    val isPrivateHomePage =
        previewImageUrl == SupportUtils.INFERNO_PRIVATE_HOME_URL || previewImageUrl == SupportUtils.INFERNO_PRIVATE_HOME_URL_2
    val icon = when {
        previewImageUrl == null -> AppCompatResources.getDrawable(context, R.drawable.inferno)
            ?.toBitmap()?.asImageBitmap()

        isHomePage -> AppCompatResources.getDrawable(context, R.drawable.inferno)?.toBitmap()
            ?.asImageBitmap()

        isPrivateHomePage -> AppCompatResources.getDrawable(
            context, R.drawable.ic_private_browsing_24
        )?.toBitmap()?.asImageBitmap()

        else -> null
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            icon != null -> {
                Image(
                    bitmap = icon,
                    contentDescription = "",
                    modifier = Modifier.size(THUMBNAIL_SIZE.dp)
                )
            }

            !previewImageUrl.isNullOrEmpty() -> {
                Image(
                    url = previewImageUrl,
                    modifier = modifier,
                    targetSize = THUMBNAIL_SIZE.dp,
                    contentScale = ContentScale.Crop,
                    fallback = {
                        TabThumbnail(
                            tab = tab.state,
                            size = LocalDensity.current.run { THUMBNAIL_SIZE.dp.toPx().toInt() },
                            modifier = modifier,
                            contentScale = contentScale,
                        )
                    },
                )
            }

            else -> TabThumbnail(
                tab = tab.state,
                size = LocalDensity.current.run { THUMBNAIL_SIZE.dp.toPx().toInt() },
                modifier = modifier,
                contentScale = contentScale,
            )
        }
    }
}

/**
 * A recent tab icon.
 *
 * @param url The loaded URL of the tab.
 * @param modifier [Modifier] used to draw the image content.
 * @param contentScale [ContentScale] used to draw image content.
 * @param alignment [Alignment] used to draw the image content.
 * @param icon The icon of the tab. Fallback to loading the icon from the [url] if the [icon]
 * is null.
 */
@Composable
private fun RecentTabIcon(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    icon: Bitmap? = null,
) {
    val context = LocalContext.current
    val isHomePage = url == SupportUtils.INFERNO_HOME_URL || url == SupportUtils.INFERNO_HOME_URL_2
    val isPrivateHomePage =
        url == SupportUtils.INFERNO_PRIVATE_HOME_URL || url == SupportUtils.INFERNO_PRIVATE_HOME_URL_2
    val homeIcon = when {
        isHomePage -> AppCompatResources.getDrawable(context, R.drawable.inferno)?.toBitmap()
            ?.asImageBitmap()

        isPrivateHomePage -> AppCompatResources.getDrawable(
            context, R.drawable.ic_private_browsing_24
        )?.toBitmap()?.asImageBitmap()

        else -> null
    }

    when {
        homeIcon != null -> Image(
            bitmap = homeIcon, contentDescription = "", modifier = modifier,
        )

        icon != null -> {
            Image(
                painter = BitmapPainter(icon.asImageBitmap()),
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale,
                alignment = alignment,
            )
        }
//        !inComposePreview -> {
//            components.core.icons.Loader(url) {
//                Placeholder {
//                    PlaceHolderTabIcon(modifier)
//                }
//
//                WithIcon { icon ->
//                    Image(
//                        painter = icon.painter,
//                        contentDescription = null,
//                        modifier = modifier,
//                        contentScale = contentScale,
//                    )
//                }
//            }
//        }
        else -> {
            PlaceHolderTabIcon(modifier)
        }
    }
}

/**
 * A placeholder for the recent tab icon.
 *
 * @param modifier [Modifier] used to shape the content.
 */
@Composable
private fun PlaceHolderTabIcon(modifier: Modifier) {
    Box(
        modifier = modifier.background(
            color = when (isSystemInDarkTheme()) {
                true -> PhotonColors.DarkGrey60
                false -> PhotonColors.LightGrey30
            },
        ),
    )
}

//@LightDarkPreview
//@Composable
//private fun RecentTabsPreview() {
//    FirefoxTheme {
//        RecentTabs(
//            recentTabs = FakeHomepagePreview.recentTabs(),
//            menuItems = listOf(
//                RecentTabMenuItem(
//                    title = "Menu item",
//                    onClick = {},
//                ),
//            ),
//            onRecentTabClick = {},
//        )
//    }
//}
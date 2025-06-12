/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.topsites

//import com.shmibblez.inferno.GleanMetrics.Pings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.ContextualMenu
import com.shmibblez.inferno.compose.Favicon
import com.shmibblez.inferno.compose.MenuItem
import com.shmibblez.inferno.compose.PagerIndicator
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.home.fake.FakeHomepagePreview
import com.shmibblez.inferno.home.sessioncontrol.TopSiteInteractor
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.annotation.LightDarkPreview
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.wallpapers.WallpaperState
import mozilla.components.feature.top.sites.TopSite
import kotlin.math.ceil

//import com.shmibblez.inferno.GleanMetrics.TopSites as TopSitesMetrics

private const val TOP_SITES_PER_PAGE = 8
private const val TOP_SITES_PER_ROW = 4
private const val TOP_SITES_ITEM_SIZE = 84
private const val TOP_SITES_ROW_WIDTH = TOP_SITES_PER_ROW * TOP_SITES_ITEM_SIZE
private const val TOP_SITES_FAVICON_CARD_SIZE = 60
private const val TOP_SITES_FAVICON_SIZE = 36

/**
 * A list of top sites.
 *
 * @param topSites List of [TopSite] to display.
 * @param interactor The interactor which handles user actions with the widget.
 * @param onTopSitesItemBound Invoked during the composition of a top site item.
 */
@Composable
fun TopSites(
    topSites: List<TopSite>,
    interactor: TopSiteInteractor,
    onTopSitesItemBound: () -> Unit,
) {
    TopSites(
        topSites = topSites,
        onTopSiteClick = { topSite ->
            interactor.onSelectTopSite(
                topSite = topSite,
                position = topSites.indexOf(topSite),
            )
        },
        onTopSiteLongClick = interactor::onTopSiteLongClicked,
        onOpenInPrivateTabClicked = interactor::onOpenInPrivateTabClicked,
        onEditTopSiteClicked = interactor::onEditTopSiteClicked,
        onRemoveTopSiteClicked = interactor::onRemoveTopSiteClicked,
        onSettingsClicked = interactor::onSettingsClicked,
        onSponsorPrivacyClicked = interactor::onSponsorPrivacyClicked,
        onTopSitesItemBound = onTopSitesItemBound,
    )
}

/**
 * A list of top sites.
 *
 * @param topSites List of [TopSite] to display.
 * @param onTopSiteClick Invoked when the user clicks on a top site.
 * @param onTopSiteLongClick Invoked when the user long clicks on a top site.
 * @param onOpenInPrivateTabClicked Invoked when the user clicks on the "Open in private tab"
 * menu item.
 * @param onEditTopSiteClicked Invoked when the user clicks on the "Edit" menu item.
 * @param onRemoveTopSiteClicked Invoked when the user clicks on the "Remove" menu item.
 * @param onSettingsClicked Invoked when the user clicks on the "Settings" menu item.
 * @param onSponsorPrivacyClicked Invoked when the user clicks on the "Our sponsors & your privacy"
 * menu item.
 * @param onTopSitesItemBound Invoked during the composition of a top site item.
 */
@Composable
@Suppress("LongParameterList", "LongMethod")
fun TopSites(
    topSites: List<TopSite>,
    onTopSiteClick: (TopSite) -> Unit,
    onTopSiteLongClick: (TopSite) -> Unit,
    onOpenInPrivateTabClicked: (topSite: TopSite) -> Unit,
    onEditTopSiteClicked: (topSite: TopSite) -> Unit,
    onRemoveTopSiteClicked: (topSite: TopSite) -> Unit,
    onSettingsClicked: () -> Unit,
    onSponsorPrivacyClicked: () -> Unit,
    onTopSitesItemBound: () -> Unit,
) {
    val pageCount = ceil((topSites.size.toDouble() / TOP_SITES_PER_PAGE)).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                testTagsAsResourceId = true
            }
            .testTag(TopSitesTestTag.topSites),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val pagerState = rememberPagerState(
            pageCount = { pageCount },
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(
                state = pagerState,
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val topSitesWindows = topSites.windowed(
                        size = TOP_SITES_PER_PAGE,
                        step = TOP_SITES_PER_PAGE,
                        partialWindows = true,
                    )[page].chunked(TOP_SITES_PER_ROW)

                    for (items in topSitesWindows) {
                        Row(modifier = Modifier.defaultMinSize(minWidth = TOP_SITES_ROW_WIDTH.dp)) {
                            items.forEachIndexed { position, topSite ->
                                TopSiteItem(
                                    topSite = topSite,
                                    menuItems = getMenuItems(
                                        topSite = topSite,
                                        onOpenInPrivateTabClicked = onOpenInPrivateTabClicked,
                                        onEditTopSiteClicked = onEditTopSiteClicked,
                                        onRemoveTopSiteClicked = onRemoveTopSiteClicked,
                                        onSettingsClicked = onSettingsClicked,
                                        onSponsorPrivacyClicked = onSponsorPrivacyClicked,
                                    ),
                                    position = position,
                                    onTopSiteClick = { item -> onTopSiteClick(item) },
                                    onTopSiteLongClick = onTopSiteLongClick,
                                    onTopSitesItemBound = onTopSitesItemBound,
                                )
                            }
                        }

//                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        if (pagerState.pageCount > 1) {
            Spacer(modifier = Modifier.height(8.dp))

            PagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.padding(horizontal = 16.dp),
                spacing = 4.dp,
            )
        }
    }
}

/**
 * Represents the colors used by top sites.
 */
data class TopSiteColors(
    val titleTextColor: Color,
    val sponsoredTextColor: Color,
    val faviconCardBackgroundColor: Color,
) {
    companion object {
        /**
         * Builder function used to construct an instance of [TopSiteColors].
         */
        @Composable
        fun colors(
            titleTextColor: Color = LocalContext.current.infernoTheme().value.primaryTextColor,
            sponsoredTextColor: Color = LocalContext.current.infernoTheme().value.secondaryTextColor,
            faviconCardBackgroundColor: Color = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        ) = TopSiteColors(
            titleTextColor = titleTextColor,
            sponsoredTextColor = sponsoredTextColor,
            faviconCardBackgroundColor = faviconCardBackgroundColor,
        )

        /**
         * Builder function used to construct an instance of [TopSiteColors] given a
         * [WallpaperState].
         */
        @Composable
        fun colors(wallpaperState: WallpaperState): TopSiteColors {
            val textColor: Long? = wallpaperState.currentWallpaper.textColor
            val (titleTextColor, sponsoredTextColor) = if (textColor == null) {
                LocalContext.current.infernoTheme().value.primaryTextColor to LocalContext.current.infernoTheme().value.secondaryTextColor
            } else {
                Color(textColor) to Color(textColor)
            }

            var faviconCardBackgroundColor =
                LocalContext.current.infernoTheme().value.secondaryBackgroundColor

            wallpaperState.composeRunIfWallpaperCardColorsAreAvailable { cardColorLight, cardColorDark ->
                faviconCardBackgroundColor = if (isSystemInDarkTheme()) {
                    cardColorDark
                } else {
                    cardColorLight
                }
            }

            return TopSiteColors(
                titleTextColor = titleTextColor,
                sponsoredTextColor = sponsoredTextColor,
                faviconCardBackgroundColor = faviconCardBackgroundColor,
            )
        }
    }
}

/**
 * A top site item.
 *
 * @param topSite The [TopSite] to display.
 * @param menuItems List of [MenuItem]s to display in a top site dropdown menu.
 * @param position The position of the top site.
 * @param onTopSiteClick Invoked when the user clicks on a top site.
 * @param onTopSiteLongClick Invoked when the user long clicks on a top site.
 * @param onTopSitesItemBound Invoked during the composition of a top site item.
 */
@Suppress("LongMethod", "Deprecation") // https://bugzilla.mozilla.org/show_bug.cgi?id=1927713
@Composable
private fun TopSiteItem(
    topSite: TopSite,
    menuItems: List<MenuItem>,
    position: Int,
    onTopSiteClick: (TopSite) -> Unit,
    onTopSiteLongClick: (TopSite) -> Unit,
    onTopSitesItemBound: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .semantics {
                testTagsAsResourceId = true
            }
            .testTag(TopSitesTestTag.topSiteItemRoot),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onTopSiteClick(topSite) },
                    onLongClick = {
                        onTopSiteLongClick(topSite)
                        menuExpanded = true
                    },
                )
                .width(TOP_SITES_ITEM_SIZE.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            TopSiteFaviconCard(topSite = topSite)

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.width(TOP_SITES_ITEM_SIZE.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (topSite is TopSite.Pinned || topSite is TopSite.Default) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_new_pin),
                        contentDescription = null,
                    )

                    Spacer(modifier = Modifier.width(2.dp))
                }

                InfernoText(
//                    modifier = Modifier
//                        .semantics {
//                            testTagsAsResourceId = true
//                        }
//                        .testTag(TopSitesTestTag.topSiteTitle),
                    text = topSite.title ?: topSite.url,
                    infernoStyle = InfernoTextStyle.Small,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }

            InfernoText(
                text = stringResource(id = R.string.top_sites_sponsored_label),
                modifier = Modifier
                    .width(TOP_SITES_ITEM_SIZE.dp)
                    .alpha(alpha = if (topSite is TopSite.Provided) 1f else 0f),
                infernoStyle = InfernoTextStyle.Small,
                fontColor = LocalContext.current.infernoTheme().value.primaryActionColor,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }

        ContextualMenu(
            modifier = Modifier.testTag(TopSitesTestTag.topSiteContextualMenu),
            menuItems = menuItems,
            showMenu = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        )

//        if (topSite is TopSite.Provided) {
//            LaunchedEffect(topSite) {
//                submitTopSitesImpressionPing(topSite = topSite, position = position)
//            }
//        }
    }

    LaunchedEffect(Unit) {
        onTopSitesItemBound()
    }
}

/**
 * The top site favicon card.
 *
 * @param topSite The [TopSite] to display.
 */
@Composable
private fun TopSiteFaviconCard(topSite: TopSite) {
    Box(
        modifier = Modifier
            .size(TOP_SITES_FAVICON_CARD_SIZE.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LocalContext.current.infernoTheme().value.secondaryBackgroundColor),
        contentAlignment = Alignment.Center,
//        colors = CardDefaults.cardColors(containerColor = backgroundColor),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Surface(
            modifier = Modifier.size(TOP_SITES_FAVICON_SIZE.dp),
            color = Color.Transparent, // backgroundColor,
            shape = RoundedCornerShape(4.dp),
        ) {
            if (topSite is TopSite.Provided) {
                TopSiteFavicon(topSite.url, topSite.imageUrl)
            } else {
                TopSiteFavicon(topSite.url)
            }
        }

    }
}

@Composable
private fun FaviconImage(painter: Painter) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.size(TOP_SITES_FAVICON_SIZE.dp),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun TopSiteFavicon(url: String, imageUrl: String? = null) {
    when (url) {
        SupportUtils.INFERNO_HOME_URL, SupportUtils.INFERNO_HOME_URL_2 -> FaviconImage(
            painterResource(R.drawable.inferno)
        )

        SupportUtils.INFERNO_PRIVATE_HOME_URL, SupportUtils.INFERNO_PRIVATE_HOME_URL_2 -> FaviconImage(
            painterResource(R.drawable.ic_private_browsing_24)
        )

        SupportUtils.POCKET_TRENDING_URL -> FaviconImage(painterResource(R.drawable.ic_pocket))
        SupportUtils.BAIDU_URL -> FaviconImage(painterResource(R.drawable.ic_baidu))
        SupportUtils.JD_URL -> FaviconImage(painterResource(R.drawable.ic_jd))
        SupportUtils.PDD_URL -> FaviconImage(painterResource(R.drawable.ic_pdd))
        SupportUtils.TC_URL -> FaviconImage(painterResource(R.drawable.ic_tc))
        SupportUtils.MEITUAN_URL -> FaviconImage(painterResource(R.drawable.ic_meituan))
        else -> Favicon(url = url, size = TOP_SITES_FAVICON_SIZE.dp, imageUrl = imageUrl)
    }
}

@Composable
private fun getMenuItems(
    topSite: TopSite,
    onOpenInPrivateTabClicked: (topSite: TopSite) -> Unit,
    onEditTopSiteClicked: (topSite: TopSite) -> Unit,
    onRemoveTopSiteClicked: (topSite: TopSite) -> Unit,
    onSettingsClicked: () -> Unit,
    onSponsorPrivacyClicked: () -> Unit,
): List<MenuItem> {
    val isPinnedSite = topSite is TopSite.Pinned || topSite is TopSite.Default
    val isProvidedSite = topSite is TopSite.Provided
    val isRecentSite = topSite is TopSite.Frecent
    val result = mutableListOf<MenuItem>()

    result.add(
        MenuItem(
            title = stringResource(id = R.string.bookmark_menu_open_in_private_tab_button),
            testTag = TopSitesTestTag.openInPrivateTab,
            onClick = { onOpenInPrivateTabClicked(topSite) },
        ),
    )

    if (isPinnedSite || isRecentSite) {
        result.add(
            MenuItem(
                title = stringResource(id = R.string.top_sites_edit_top_site),
                testTag = TopSitesTestTag.edit,
                onClick = { onEditTopSiteClicked(topSite) },
            ),
        )
    }

    if (!isProvidedSite) {
        result.add(
            MenuItem(
                title = stringResource(
                    id = if (isPinnedSite) {
                        R.string.remove_top_site
                    } else {
                        R.string.delete_from_history
                    },
                ),
                testTag = TopSitesTestTag.remove,
                onClick = { onRemoveTopSiteClicked(topSite) },
            ),
        )
    }

    if (isProvidedSite) {
        result.addAll(
            listOf(
                MenuItem(
                    title = stringResource(id = R.string.delete_from_history),
                    testTag = TopSitesTestTag.remove,
                    onClick = { onRemoveTopSiteClicked(topSite) },
                ),
                MenuItem(
                    title = stringResource(id = R.string.top_sites_menu_settings),
                    onClick = onSettingsClicked,
                ),
                MenuItem(
                    title = stringResource(id = R.string.top_sites_menu_sponsor_privacy),
                    onClick = onSponsorPrivacyClicked,
                ),
            ),
        )
    }

    return result
}

//private fun submitTopSitesImpressionPing(topSite: TopSite.Provided, position: Int) {
//    TopSitesMetrics.contileImpression.record(
//        TopSitesMetrics.ContileImpressionExtra(
//            position = position + 1,
//            source = "newtab",
//        ),
//    )

//    topSite.id?.let { TopSitesMetrics.contileTileId.set(it) }
//    topSite.title?.let { TopSitesMetrics.contileAdvertiser.set(it.lowercase()) }
//    TopSitesMetrics.contileReportingUrl.set(topSite.impressionUrl)
//    Pings.topsitesImpression.submit()
//}

@Composable
@LightDarkPreview
private fun TopSitesPreview() {
    Box(modifier = Modifier.background(color = LocalContext.current.infernoTheme().value.primaryBackgroundColor)) {
        TopSites(
            topSites = FakeHomepagePreview.topSites(),
            onTopSiteClick = {},
            onTopSiteLongClick = {},
            onOpenInPrivateTabClicked = {},
            onEditTopSiteClicked = {},
            onRemoveTopSiteClicked = {},
            onSettingsClicked = {},
            onSponsorPrivacyClicked = {},
            onTopSitesItemBound = {},
        )
    }
}

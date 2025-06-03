/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.ui

//import mozilla.telemetry.glean.private.NoExtras
//import com.shmibblez.inferno.GleanMetrics.History
//import com.shmibblez.inferno.GleanMetrics.RecentlyVisitedHomepage
//import com.shmibblez.inferno.home.pocket.ui.PocketSection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.components
import com.shmibblez.inferno.compose.button.TertiaryButton
import com.shmibblez.inferno.compose.home.HomeSectionHeader
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.ext.shouldShowRecentSyncedTabs
import com.shmibblez.inferno.ext.shouldShowRecentTabs
import com.shmibblez.inferno.home.bookmarks.Bookmark
import com.shmibblez.inferno.home.bookmarks.interactor.BookmarksInteractor
import com.shmibblez.inferno.home.bookmarks.view.Bookmarks
import com.shmibblez.inferno.home.bookmarks.view.BookmarksMenuItem
import com.shmibblez.inferno.home.collections.Collections
import com.shmibblez.inferno.home.collections.CollectionsState
import com.shmibblez.inferno.home.interactor.HomepageInteractor
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTabState
import com.shmibblez.inferno.home.recentsyncedtabs.view.RecentSyncedTab
import com.shmibblez.inferno.home.recenttabs.RecentTab
import com.shmibblez.inferno.home.recenttabs.interactor.RecentTabInteractor
import com.shmibblez.inferno.home.recenttabs.view.RecentTabMenuItem
import com.shmibblez.inferno.home.recenttabs.view.RecentTabs
import com.shmibblez.inferno.home.recentvisits.RecentlyVisitedItem
import com.shmibblez.inferno.home.recentvisits.RecentlyVisitedItem.RecentHistoryGroup
import com.shmibblez.inferno.home.recentvisits.RecentlyVisitedItem.RecentHistoryHighlight
import com.shmibblez.inferno.home.recentvisits.interactor.RecentVisitsInteractor
import com.shmibblez.inferno.home.recentvisits.view.RecentVisitMenuItem
import com.shmibblez.inferno.home.recentvisits.view.RecentlyVisited
import com.shmibblez.inferno.home.sessioncontrol.CollectionInteractor
import com.shmibblez.inferno.home.sessioncontrol.CustomizeHomeIteractor
import com.shmibblez.inferno.home.sessioncontrol.viewholders.FeltPrivacyModeInfoCard
import com.shmibblez.inferno.home.sessioncontrol.viewholders.PrivateBrowsingDescription
import com.shmibblez.inferno.home.store.HomepageState
import com.shmibblez.inferno.home.topsites.TopSites

private val ITEM_PADDING = 24.dp
private val HEADER_BOTTOM_PADDING = 16.dp

// todo: use wallpaper state for text color, if set
/**
 * Top level composable for the homepage.
 *
 * @param state State representing the homepage.
 * @param interactor for interactions with the homepage UI.
 * @param onTopSitesItemBound Invoked during the composition of a top site item.
 */
@Suppress("LongMethod")
@Composable
internal fun Homepage(
    state: HomepageState,
    interactor: HomepageInteractor,
    onTopSitesItemBound: () -> Unit,
    isPrivate: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(LocalContext.current.infernoTheme().value.primaryBackgroundColor)
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        when (isPrivate) {
            true -> {
                val feltPrivateBrowsingEnabled =
                    if (state is HomepageState.Private) state.feltPrivateBrowsingEnabled else false
                if (feltPrivateBrowsingEnabled) {
                    FeltPrivacyModeInfoCard(
                        onLearnMoreClick = interactor::onLearnMoreClicked,
                    )
                } else {
                    PrivateBrowsingDescription(
                        onLearnMoreClick = interactor::onLearnMoreClicked,
                    )
                }
            }

            false -> {
                val appState = LocalContext.current.components.appStore.state
                val settings = LocalContext.current.settings()
                val topSites = appState.topSites
                val showTopSites = settings.showTopSitesFeature && topSites.isNotEmpty()
                val recentTabs = appState.recentTabs
                val showRecentTabs = appState.shouldShowRecentTabs(settings)
                val cardBackgroundColor = Color.Black // wallpaperState.cardBackgroundColor,
                val syncedTab = when (appState.recentSyncedTabState) {
                    RecentSyncedTabState.None,
                    RecentSyncedTabState.Loading,
                        -> null

                    is RecentSyncedTabState.Success -> appState.recentSyncedTabState.tabs.firstOrNull()
                }
                val showRecentSyncedTab = appState.shouldShowRecentSyncedTabs()

//                val buttonBackgroundColor = appState.wallpaperState.buttonBackgroundColor
//                val buttonTextColor = appState.wallpaperState.buttonTextColor
                val bookmarks = appState.bookmarks
                val showBookmarks = settings.showBookmarksHomeFeature && bookmarks.isNotEmpty()
                val recentlyVisited = appState.recentHistory
                val showRecentlyVisited = settings.shouldShowHistory && recentlyVisited.isNotEmpty()
                val collectionsState = CollectionsState.build(
                    appState = appState,
                    browserState = components.core.store.state,
                    isPrivate = false,
                )
                val showCustomizeHome =
                    showTopSites || showRecentTabs || showBookmarks || showRecentlyVisited // || showPocketStories

                if (showTopSites) {
                    TopSites(
                        topSites = topSites,
                        interactor = interactor,
                        onTopSitesItemBound = onTopSitesItemBound,
                    )
                }

                if (showRecentTabs) {
                    RecentTabsSection(
                        interactor = interactor,
//                            cardBackgroundColor = cardBackgroundColor,
                        recentTabs = recentTabs,
                    )

                    if (showRecentSyncedTab) {
                        Spacer(modifier = Modifier.height(8.dp))

                        RecentSyncedTab(
                            tab = syncedTab,
                            onRecentSyncedTabClick = interactor::onRecentSyncedTabClicked,
                            onSeeAllSyncedTabsButtonClick = interactor::onSyncedTabShowAllClicked,
                            onRemoveSyncedTab = interactor::onRemovedRecentSyncedTab,
                        )
                    }
                }

                if (showBookmarks) {
                    BookmarksSection(
                        bookmarks = bookmarks,
//                        cardBackgroundColor = cardBackgroundColor,
                        interactor = interactor,
                    )
                }

                if (showRecentlyVisited) {
                    RecentlyVisitedSection(
                        recentVisits = recentlyVisited,
                        cardBackgroundColor = cardBackgroundColor,
                        interactor = interactor,
                    )
                }

                CollectionsSection(
                    collectionsState = collectionsState, interactor = interactor
                )

//                    if (showPocketStories) {
//                        PocketSection(
//                            state = pocketState,
//                            cardBackgroundColor = cardBackgroundColor,
//                            interactor = interactor,
//                        )
//                    }

                if (showCustomizeHome) {
                    CustomizeHomeButton(interactor = interactor)
                }

                // This is a temporary value until I can fix layout issues
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun RecentTabsSection(
    interactor: RecentTabInteractor,
    recentTabs: List<RecentTab>,
) {
//    Spacer(modifier = Modifier.height(ITEM_PADDING))

    HomeSectionHeader(
        headerText = stringResource(R.string.recent_tabs_header),
        description = stringResource(R.string.recent_tabs_show_all_content_description_2),
        onShowAllClick = interactor::onRecentTabShowAllClicked,
    )

    Spacer(Modifier.height(HEADER_BOTTOM_PADDING))

    RecentTabs(
        recentTabs = recentTabs,
        backgroundColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor, // cardBackgroundColor,
        onRecentTabClick = { interactor.onRecentTabClicked(it) },
        menuItems = listOf(
            RecentTabMenuItem(
                title = stringResource(id = R.string.recent_tab_menu_item_remove),
                onClick = interactor::onRemoveRecentTab,
            ),
        ),
    )
}

@Composable
private fun BookmarksSection(
    bookmarks: List<Bookmark>,
    cardBackgroundColor: Color = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
    interactor: BookmarksInteractor,
) {
    Spacer(modifier = Modifier.height(ITEM_PADDING))

    HomeSectionHeader(
        headerText = stringResource(R.string.home_bookmarks_title),
        description = stringResource(R.string.home_bookmarks_show_all_content_description),
        onShowAllClick = interactor::onShowAllBookmarksClicked,
    )

    Spacer(Modifier.height(HEADER_BOTTOM_PADDING))

    Bookmarks(
        bookmarks = bookmarks,
        menuItems = listOf(
            BookmarksMenuItem(
                stringResource(id = R.string.home_bookmarks_menu_item_remove),
                onClick = interactor::onBookmarkRemoved,
            ),
        ),
        backgroundColor = cardBackgroundColor,
        onBookmarkClick = interactor::onBookmarkClicked,
    )
}

@Composable
private fun RecentlyVisitedSection(
    recentVisits: List<RecentlyVisitedItem>,
    cardBackgroundColor: Color,
    interactor: RecentVisitsInteractor,
) {
    Spacer(modifier = Modifier.height(ITEM_PADDING))

    HomeSectionHeader(
        headerText = stringResource(R.string.history_metadata_header_2),
        description = stringResource(R.string.past_explorations_show_all_content_description_2),
        onShowAllClick = interactor::onHistoryShowAllClicked,
    )

    Spacer(Modifier.height(HEADER_BOTTOM_PADDING))

    RecentlyVisited(
        recentVisits = recentVisits,
        menuItems = listOfNotNull(
            RecentVisitMenuItem(
                title = stringResource(R.string.recently_visited_menu_item_remove),
                onClick = { visit ->
                    when (visit) {
                        is RecentHistoryGroup -> interactor.onRemoveRecentHistoryGroup(visit.title)
                        is RecentHistoryHighlight -> interactor.onRemoveRecentHistoryHighlight(
                            visit.url,
                        )
                    }
                },
            ),
        ),
        backgroundColor = cardBackgroundColor,
        onRecentVisitClick = { recentlyVisitedItem, _ -> // pageNumber ->
            when (recentlyVisitedItem) {
                is RecentHistoryHighlight -> {
//                    RecentlyVisitedHomepage.historyHighlightOpened.record(NoExtras())
                    interactor.onRecentHistoryHighlightClicked(recentlyVisitedItem)
                }

                is RecentHistoryGroup -> {
//                    RecentlyVisitedHomepage.searchGroupOpened.record(NoExtras())
                    // TODO: history
//                    History.recentSearchesTapped.record(
//                        History.RecentSearchesTappedExtra(
//                            pageNumber.toString(),
//                        ),
//                    )
                    interactor.onRecentHistoryGroupClicked(recentlyVisitedItem)
                }
            }
        },
    )
}

@Composable
private fun CollectionsSection(
    collectionsState: CollectionsState,
    interactor: CollectionInteractor,
) {
    when (collectionsState) {
        is CollectionsState.Content -> {
            Column {
                Spacer(Modifier.height(ITEM_PADDING))

                HomeSectionHeader(headerText = stringResource(R.string.collections_header))

                Spacer(Modifier.height(HEADER_BOTTOM_PADDING))

                with(collectionsState) {
                    Collections(
                        collections = collections,
                        expandedCollections = expandedCollections,
                        showAddTabToCollection = showSaveTabsToCollection,
                        interactor = interactor,
                    )
                }
            }
        }

        CollectionsState.Gone -> {} // no-op. Nothing is shown where there are no collections.
        is CollectionsState.Placeholder -> {
            Column {
                Spacer(Modifier.height(ITEM_PADDING))

                CollectionsPlaceholder(collectionsState.showSaveTabsToCollection, interactor)
            }
        }
    }
}

@Composable
private fun CustomizeHomeButton(interactor: CustomizeHomeIteractor) {
    Spacer(modifier = Modifier.height(ITEM_PADDING))

    TertiaryButton(
        text = stringResource(R.string.browser_menu_customize_home_1),
        backgroundColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        onClick = interactor::openCustomizeHomePage,
    )
}

//@Composable
//@PreviewLightDark
//private fun HomepagePreview() {
//    FirefoxTheme {
//        Homepage(
//            HomepageState.Normal(
//                topSites = FakeHomepagePreview.topSites(),
//                recentTabs = FakeHomepagePreview.recentTabs(),
//                syncedTab = FakeHomepagePreview.recentSyncedTab(),
//                bookmarks = FakeHomepagePreview.bookmarks(),
//                recentlyVisited = FakeHomepagePreview.recentHistory(),
//                collectionsState = CollectionsState.Placeholder(true),
////                pocketState = FakeHomepagePreview.pocketState(),
//                showTopSites = true,
//                showRecentTabs = true,
//                showRecentSyncedTab = true,
//                showBookmarks = true,
//                showRecentlyVisited = true,
////                showPocketStories = true,
//                topSiteColors = TopSiteColors.colors(),
//                cardBackgroundColor = Color.Black, // WallpaperState.default.cardBackgroundColor,
//                buttonTextColor = WallpaperState.default.buttonTextColor,
//                buttonBackgroundColor = WallpaperState.default.buttonBackgroundColor,
//            ),
//            isPrivate = false,
//            interactor = FakeHomepagePreview.homepageInteractor,
//            onTopSitesItemBound = {},
//        )
//    }
//}
//
//@Composable
//@Preview
//private fun PrivateHomepagePreview() {
//    FirefoxTheme(theme = Theme.Private) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(color = FirefoxTheme.colors.layer1),
//        ) {
//            Homepage(
//                HomepageState.Private(
//                    feltPrivateBrowsingEnabled = false,
//                ),
//                isPrivate = true,
//                interactor = FakeHomepagePreview.homepageInteractor,
//                onTopSitesItemBound = {},
//            )
//        }
//    }
//}

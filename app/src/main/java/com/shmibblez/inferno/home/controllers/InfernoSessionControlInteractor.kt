package com.shmibblez.inferno.home.controllers

import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.components.appstate.AppState
import com.shmibblez.inferno.home.bookmarks.Bookmark
import com.shmibblez.inferno.home.bookmarks.controller.BookmarksController
import com.shmibblez.inferno.home.interactor.HomepageInteractor
import com.shmibblez.inferno.home.privatebrowsing.controller.PrivateBrowsingController
import com.shmibblez.inferno.home.recentsyncedtabs.RecentSyncedTab
import com.shmibblez.inferno.home.recentsyncedtabs.controller.RecentSyncedTabController
import com.shmibblez.inferno.home.recenttabs.RecentTab
import com.shmibblez.inferno.home.recenttabs.controller.RecentTabController
import com.shmibblez.inferno.home.recentvisits.RecentlyVisitedItem
import com.shmibblez.inferno.home.recentvisits.controller.RecentVisitsController
import com.shmibblez.inferno.home.sessioncontrol.SessionControlController
import com.shmibblez.inferno.home.sessioncontrol.SessionControlInteractor
import com.shmibblez.inferno.home.toolbar.ToolbarController
import com.shmibblez.inferno.search.toolbar.SearchSelectorController
import com.shmibblez.inferno.search.toolbar.SearchSelectorMenu
import com.shmibblez.inferno.wallpapers.WallpaperState
import mozilla.components.feature.tab.collections.Tab
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.service.nimbus.messaging.Message

/**
 * // TODO: reference [SessionControlInteractor]
 */
class InfernoSessionControlInteractor(
    private val controller: SessionControlController,
    private val recentTabController: RecentTabController,
    private val recentSyncedTabController: RecentSyncedTabController,
    private val bookmarksController: BookmarksController,
    private val recentVisitsController: RecentVisitsController,
//    private val pocketStoriesController: PocketStoriesController,
    private val privateBrowsingController: PrivateBrowsingController,
    private val searchSelectorController: SearchSelectorController,
    private val toolbarController: ToolbarController,
) : HomepageInteractor {
    override fun onCollectionAddTabTapped(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun onCollectionOpenTabClicked(tab: Tab) {
        // TODO("Not yet implemented")
    }

    override fun onCollectionOpenTabsTapped(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun onCollectionRemoveTab(collection: TabCollection, tab: Tab) {
        // TODO("Not yet implemented")
    }

    override fun onCollectionShareTabsClicked(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun onDeleteCollectionTapped(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun onRenameCollectionTapped(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun onToggleCollectionExpanded(collection: TabCollection, expand: Boolean) {
        // TODO("Not yet implemented")
    }

    override fun onAddTabsToCollectionTapped() {
        // TODO("Not yet implemented")
    }

    override fun onRemoveCollectionsPlaceholder() {
        // TODO("Not yet implemented")
    }

    override fun onOpenInPrivateTabClicked(topSite: TopSite) {
        // TODO("Not yet implemented")
    }

    override fun onEditTopSiteClicked(topSite: TopSite) {
        // TODO("Not yet implemented")
    }

    override fun onRemoveTopSiteClicked(topSite: TopSite) {
        // TODO("Not yet implemented")
    }

    override fun onSelectTopSite(topSite: TopSite, position: Int) {
        // TODO("Not yet implemented")
    }

    override fun onSettingsClicked() {
        // TODO("Not yet implemented")
    }

    override fun onSponsorPrivacyClicked() {
        // TODO("Not yet implemented")
    }

    override fun onTopSiteLongClicked(topSite: TopSite) {
        // TODO("Not yet implemented")
    }

    override fun reportSessionMetrics(state: AppState) {
        // TODO("Not yet implemented")
    }

    override fun onPasteAndGo(clipboardText: String) {
        // TODO("Not yet implemented")
    }

    override fun onPaste(clipboardText: String) {
        // TODO("Not yet implemented")
    }

    override fun onNavigateSearch() {
        // TODO("Not yet implemented")
    }

    override fun onMessageClicked(message: Message) {
        // TODO("Not yet implemented")
    }

    override fun onMessageClosedClicked(message: Message) {
        // TODO("Not yet implemented")
    }

    override fun onRecentTabClicked(tabId: String) {
        // TODO("Not yet implemented")
    }

    override fun onRecentTabShowAllClicked() {
        // TODO("Not yet implemented")
    }

    override fun onRemoveRecentTab(tab: RecentTab.Tab) {
        // TODO("Not yet implemented")
    }

    override fun onRecentSyncedTabClicked(tab: RecentSyncedTab) {
        // TODO("Not yet implemented")
    }

    override fun onSyncedTabShowAllClicked() {
        // TODO("Not yet implemented")
    }

    override fun onRemovedRecentSyncedTab(tab: RecentSyncedTab) {
        // TODO("Not yet implemented")
    }

    override fun onBookmarkClicked(bookmark: Bookmark) {
        // TODO("Not yet implemented")
    }

    override fun onShowAllBookmarksClicked() {
        // TODO("Not yet implemented")
    }

    override fun onBookmarkRemoved(bookmark: Bookmark) {
        // TODO("Not yet implemented")
    }

    override fun onHistoryShowAllClicked() {
        // TODO("Not yet implemented")
    }

    override fun onRecentHistoryGroupClicked(recentHistoryGroup: RecentlyVisitedItem.RecentHistoryGroup) {
        // TODO("Not yet implemented")
    }

    override fun onRemoveRecentHistoryGroup(groupTitle: String) {
        // TODO("Not yet implemented")
    }

    override fun onRecentHistoryHighlightClicked(recentHistoryHighlight: RecentlyVisitedItem.RecentHistoryHighlight) {
        // TODO("Not yet implemented")
    }

    override fun onRemoveRecentHistoryHighlight(highlightUrl: String) {
        // TODO("Not yet implemented")
    }

    override fun openCustomizeHomePage() {
        // TODO("Not yet implemented")
    }

    override fun onLearnMoreClicked() {
        // TODO("Not yet implemented")
    }

    override fun onPrivateModeButtonClicked(newMode: BrowsingMode) {
        // TODO("Not yet implemented")
    }

    override fun onMenuItemTapped(item: SearchSelectorMenu.Item) {
        // TODO("Not yet implemented")
    }

    override fun showWallpapersOnboardingDialog(state: WallpaperState): Boolean {
        // TODO("Not yet implemented")
        return false
    }
}
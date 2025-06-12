package com.shmibblez.inferno.home.controllers

import androidx.navigation.NavController
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.TabCollectionStorage
import com.shmibblez.inferno.components.appstate.AppState
import com.shmibblez.inferno.home.sessioncontrol.DefaultSessionControlController
import com.shmibblez.inferno.home.sessioncontrol.SessionControlController
import com.shmibblez.inferno.messaging.MessageController
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import com.shmibblez.inferno.utils.Settings
import com.shmibblez.inferno.wallpapers.WallpaperState
import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tab.collections.Tab
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.feature.top.sites.TopSitesUseCases
import mozilla.components.service.nimbus.messaging.Message

/**
 * todo: reference [DefaultSessionControlController]
 */
class InfernoSessionControlController(
    private val activity: HomeActivity,
    private val settings: Settings,
    private val engine: Engine,
    private val messageController: MessageController,
    private val store: BrowserStore,
    private val tabCollectionStorage: TabCollectionStorage,
    private val addTabUseCase: TabsUseCases.AddNewTabUseCase,
    private val restoreUseCase: TabsUseCases.RestoreUseCase,
    private val selectTabUseCase: TabsUseCases.SelectTabUseCase,
    private val reloadUrlUseCase: SessionUseCases.ReloadUrlUseCase,
    private val topSitesUseCases: TopSitesUseCases,
    private val appStore: AppStore,
    private val viewLifecycleScope: CoroutineScope,
    private val registerCollectionStorageObserver: () -> Unit,
    private val removeCollectionWithUndo: (tabCollection: TabCollection) -> Unit,
    private val showUndoSnackbarForTopSite: (topSite: TopSite) -> Unit,
    private val showTabTray: (InfernoTabsTraySelectedTab?) -> Unit,
    private val onNavToHistory: () -> Unit,
    private val onNavToBookmarks: () -> Unit,
) : SessionControlController {
    override fun handleCollectionAddTabTapped(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun handleCollectionOpenTabClicked(tab: Tab) {
        // TODO("Not yet implemented")
    }

    override fun handleCollectionOpenTabsTapped(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun handleCollectionRemoveTab(collection: TabCollection, tab: Tab) {
        // TODO("Not yet implemented")
    }

    override fun handleCollectionShareTabsClicked(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun handleDeleteCollectionTapped(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun handleOpenInPrivateTabClicked(topSite: TopSite) {
        // TODO("Not yet implemented")
    }

    override fun handleEditTopSiteClicked(topSite: TopSite) {
        // TODO("Not yet implemented")
    }

    override fun handleRemoveTopSiteClicked(topSite: TopSite) {
        // TODO("Not yet implemented")
    }

    override fun handleRenameCollectionTapped(collection: TabCollection) {
        // TODO("Not yet implemented")
    }

    override fun handleSelectTopSite(topSite: TopSite, position: Int) {
        // TODO("Not yet implemented")
    }

    override fun handleTopSiteSettingsClicked() {
        // TODO("Not yet implemented")
    }

    override fun handleSponsorPrivacyClicked() {
        // TODO("Not yet implemented")
    }

    override fun handleTopSiteLongClicked(topSite: TopSite) {
        // TODO("Not yet implemented")
    }

    override fun handleToggleCollectionExpanded(collection: TabCollection, expand: Boolean) {
        // TODO("Not yet implemented")
    }

    override fun handleCreateCollection() {
        // TODO("Not yet implemented")
    }

    override fun handleRemoveCollectionsPlaceholder() {
        // TODO("Not yet implemented")
    }

    override fun handleMessageClicked(message: Message) {
        // TODO("Not yet implemented")
    }

    override fun handleMessageClosed(message: Message) {
        // TODO("Not yet implemented")
    }

    override fun handleCustomizeHomeTapped() {
        // TODO("Not yet implemented")
    }

    override fun handleShowWallpapersOnboardingDialog(state: WallpaperState): Boolean {
        // TODO("Not yet implemented")
        return false
    }

    override fun handleReportSessionMetrics(state: AppState) {
        // TODO("Not yet implemented")
    }

}
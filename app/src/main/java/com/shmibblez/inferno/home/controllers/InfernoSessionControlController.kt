package com.shmibblez.inferno.home.controllers

import androidx.navigation.NavController
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.Components
import com.shmibblez.inferno.components.TabCollectionStorage
import com.shmibblez.inferno.components.UseCases
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.components.appstate.AppState
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.home.sessioncontrol.DefaultSessionControlController
import com.shmibblez.inferno.home.sessioncontrol.SessionControlController
import com.shmibblez.inferno.messaging.MessageController
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.tabs.tabstray.InfernoTabsTraySelectedTab
import com.shmibblez.inferno.utils.Settings
import com.shmibblez.inferno.wallpapers.WallpaperState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    // todo: message controller?
    private val messageController: MessageController,
    private val store: BrowserStore,
    private val tabCollectionStorage: TabCollectionStorage,
    private val addTabUseCase: TabsUseCases.AddNewTabUseCase,
    private val restoreUseCase: TabsUseCases.RestoreUseCase,
    private val selectTabUseCase: TabsUseCases.SelectTabUseCase,
    private val reloadUrlUseCase: SessionUseCases.ReloadUrlUseCase,
    private val loadUrlUseCase: SessionUseCases.LoadUrlUseCase,
    private val topSitesUseCases: TopSitesUseCases,
    private val appStore: AppStore,
    private val viewLifecycleScope: CoroutineScope,
    private val registerCollectionStorageObserver: () -> Unit,
    private val removeCollectionWithUndo: (tabCollection: TabCollection) -> Unit,
    private val showUndoSnackbarForTopSite: (topSite: TopSite) -> Unit,
    private val showTabTray: (InfernoTabsTraySelectedTab?) -> Unit,
    private val onNavToHistory: () -> Unit,
    private val onNavToBookmarks: () -> Unit,
    private val onNavToHomeSettings: () -> Unit,
) : SessionControlController {
    override fun handleCollectionAddTabTapped(collection: TabCollection) {
        // todo: collections
    }

    override fun handleCollectionOpenTabClicked(tab: Tab) {
        // todo: collections
    }

    override fun handleCollectionOpenTabsTapped(collection: TabCollection) {
        // todo: collections
    }

    override fun handleCollectionRemoveTab(collection: TabCollection, tab: Tab) {
        // todo: collections
    }

    override fun handleCollectionShareTabsClicked(collection: TabCollection) {
        // todo: collections
    }

    override fun handleDeleteCollectionTapped(collection: TabCollection) {
        // todo: collections
    }

    override fun handleOpenInPrivateTabClicked(topSite: TopSite) {
        loadUrlUseCase.invoke(
            url = topSite.url
        )
    }

    override fun handleEditTopSiteClicked(topSite: TopSite) {
        // todo: top sites, show edit top site dialog
    }

    override fun handleRemoveTopSiteClicked(topSite: TopSite) {
        viewLifecycleScope.launch(Dispatchers.IO) {
            with(activity.components.useCases.topSitesUseCase) {
                removeTopSites(topSite)
            }
        }

        showUndoSnackbarForTopSite(topSite)
    }

    override fun handleRenameCollectionTapped(collection: TabCollection) {
        // todo: collections
    }

    override fun handleSelectTopSite(topSite: TopSite, position: Int) {
        if (settings.enableHomepageAsNewTab) {
            loadUrlUseCase.invoke(
                url = appendSearchAttributionToUrlIfNeeded(topSite.url),
            )
        } else {
            val existingTabForUrl = when (topSite) {
                is TopSite.Frecent, is TopSite.Pinned -> {
                    store.state.tabs.firstOrNull { topSite.url == it.content.url }
                }

                else -> null
            }

            if (existingTabForUrl == null) {
                loadUrlUseCase.invoke(
                    url = appendSearchAttributionToUrlIfNeeded(topSite.url),
                )
            } else {
                selectTabUseCase.invoke(existingTabForUrl.id)
            }
        }
    }

    override fun handleTopSiteSettingsClicked() {
        // todo: may need to add top sites settings
        onNavToHomeSettings.invoke()
    }

    override fun handleSponsorPrivacyClicked() {
        // no-op, no sponsors
    }

    override fun handleTopSiteLongClicked(topSite: TopSite) {
        // no-op
    }

    override fun handleToggleCollectionExpanded(collection: TabCollection, expand: Boolean) {
        appStore.dispatch(AppAction.CollectionExpanded(collection, expand))
    }

    override fun handleCreateCollection() {
        // todo: collections
    }

    override fun handleRemoveCollectionsPlaceholder() {
        settings.showCollectionsPlaceholderOnHome = false
        appStore.dispatch(AppAction.RemoveCollectionsPlaceholder)
    }

    override fun handleMessageClicked(message: Message) {
        messageController.onMessagePressed(message)
    }

    override fun handleMessageClosed(message: Message) {
        messageController.onMessageDismissed(message)
    }

    override fun handleCustomizeHomeTapped() {
        onNavToHomeSettings.invoke()
    }

    override fun handleShowWallpapersOnboardingDialog(state: WallpaperState): Boolean {
        // todo: wallpaper for home, allow user to select custom image, show some default ones from
        //  state.availableWallpapers, or load from somewhere (check impl)
        //  option should be in homepage settings
        return false
    }

    override fun handleReportSessionMetrics(state: AppState) {
        // no-op
    }

    /**
     * helper funs
     */

    /**
     * Append a search attribution query to any provided search engine URL based on the
     * user's current region.
     */
    private fun appendSearchAttributionToUrlIfNeeded(url: String): String {
        if (url == SupportUtils.GOOGLE_URL) {
            store.state.search.region?.let { region ->
                return when (region.current) {
                    "US" -> SupportUtils.GOOGLE_US_URL
                    else -> SupportUtils.GOOGLE_XX_URL
                }
            }
        }

        return url
    }
}
package com.shmibblez.inferno.tabs.tabstray

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.tabstray.ext.toComposeList
import com.shmibblez.inferno.tabstray.syncedtabs.SyncedTabsIntegration
import com.shmibblez.inferno.tabstray.syncedtabs.SyncedTabsList
import com.shmibblez.inferno.tabstray.syncedtabs.SyncedTabsListItem
import com.shmibblez.inferno.tabstray.syncedtabs.SyncedTabsListSupportedFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.browser.storage.sync.SyncedDeviceTabs
import mozilla.components.browser.storage.sync.Tab
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.DeviceCommandQueue
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.feature.syncedtabs.SyncedTabsFeature
import mozilla.components.feature.syncedtabs.commands.SyncedTabsCommands
import mozilla.components.feature.syncedtabs.controller.SyncedTabsController
import mozilla.components.feature.syncedtabs.storage.SyncedTabsStorage
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.MULTIPLE_DEVICES_UNAVAILABLE
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.NO_TABS_AVAILABLE
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.SYNC_ENGINE_UNAVAILABLE
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.SYNC_NEEDS_REAUTHENTICATION
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.SYNC_UNAVAILABLE
import mozilla.components.service.fxa.SyncEngine
import mozilla.components.service.fxa.manager.FxaAccountManager
import mozilla.components.service.fxa.manager.SyncEnginesStorage
import mozilla.components.service.fxa.manager.ext.withConstellation
import mozilla.components.service.fxa.sync.SyncReason
import mozilla.components.service.fxa.sync.SyncStatusObserver

/**
 * replaces [SyncedTabsIntegration] and [SyncedTabsFeature]
 */
// todo: implement pull to refresh, call refreshSyncedTabs()
@Composable
internal fun SyncedTabsPage(
    activeTabId: String?,
    syncedTabsStorage: SyncedTabsStorage,
    accountManager: FxaAccountManager,
    commands: SyncedTabsCommands,
    onTabClick: (tab: Tab) -> Unit,
    onTabClose: (deviceId: String, tab: Tab) -> Unit,
    tabDisplayType: InfernoTabsTrayDisplayType,
    mode: InfernoTabsTrayMode,
) {
    // todo: observe account manager changes to show error if necessary or continue
//    accountManager.registerForSyncEvents(
//        SyncStatusObserver(),
//        owner = TODO(),
//        autoPause = TODO()
//    )
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = lifecycleOwner.lifecycleScope
    var errorType by remember { mutableStateOf<ErrorType?>(null) }
    var allSyncedTabs by remember { mutableStateOf<List<SyncedTabsListItem>?>(null) }

    fun displaySyncedTabs(syncedTabs: List<SyncedDeviceTabs>) {
        allSyncedTabs = syncedTabs.toComposeList(buildSet {
            if (context.settings().enableCloseSyncedTabs) {
                add(SyncedTabsListSupportedFeature.CLOSE_TABS)
            }
        })
    }

    /**
     * See [SyncedTabsController.refreshSyncedTabs]
     */
    fun refreshSyncedTabs() {
        scope.launch {
            accountManager.withConstellation {
                val syncedDeviceTabs = syncedTabsStorage.getSyncedDeviceTabs()
                val otherDevices = state()?.otherDevices

                // todo: layout
                scope.launch(Dispatchers.Main) {
                    errorType = if (syncedDeviceTabs.isEmpty() && otherDevices?.isEmpty() == true) {
//                        view.onError(ErrorType.MULTIPLE_DEVICES_UNAVAILABLE)
                        MULTIPLE_DEVICES_UNAVAILABLE
                    } else if (syncedDeviceTabs.all { it.tabs.isEmpty() }) {
//                        view.onError(ErrorType.NO_TABS_AVAILABLE)
                        NO_TABS_AVAILABLE
                    } else {
                        displaySyncedTabs(syncedDeviceTabs)
                        null
                    }
                }
            }

            scope.launch(Dispatchers.Main) {
                // todo
//                syncedTabsPullToRefresh.isRefreshing = false
            }
        }
    }

    fun isSyncedTabsEngineEnabled(context: Context): Boolean {
        // This status isn't always set before it's inspected. This causes erroneous reports of the
        // sync engine being unavailable. Tabs are included in sync by default, so it's safe to
        // default to true until they are deliberately disabled.
        return SyncEnginesStorage(context).getStatus()[SyncEngine.Tabs] ?: true
    }

    val eventObserver = remember {
        object : SyncStatusObserver {
            override fun onError(error: Exception?) {
                errorType = SYNC_ENGINE_UNAVAILABLE
            }

            override fun onIdle() {
                if (isSyncedTabsEngineEnabled(context)) {
                    refreshSyncedTabs()
                } else {
                    errorType = SYNC_ENGINE_UNAVAILABLE
                }
            }

            override fun onStarted() {
                // todo
//                syncedTabsPullToRefresh.isRefreshing = true
            }

        }
    }

    /**
     * See [SyncedTabsController.syncAccount]
     */
    fun syncAccount() {
        // todo: loading
//        view.startLoading()
        scope.launch {
            accountManager.withConstellation { refreshDevices() }
            accountManager.syncNow(
                SyncReason.User,
                customEngineSubset = listOf(SyncEngine.Tabs),
                debounce = true,
            )
        }
    }

    val accountObserver = remember {
        object : AccountObserver {
            override fun onLoggedOut() {
                CoroutineScope(Dispatchers.Main).launch { errorType = SYNC_UNAVAILABLE }
            }

            override fun onAuthenticated(account: OAuthAccount, authType: AuthType) {
                CoroutineScope(Dispatchers.Main).launch {
                    syncAccount()
                }
            }

            override fun onAuthenticationProblems() {
                CoroutineScope(Dispatchers.Main).launch {
                    errorType = SYNC_NEEDS_REAUTHENTICATION
                }
            }

        }
    }

    val commandsObserver = remember {
        object : DeviceCommandQueue.Observer {
            override fun onAdded() = refresh()
            override fun onRemoved() = refresh()

            private fun refresh() {
                if (isSyncedTabsEngineEnabled(context)) {
                    refreshSyncedTabs()
                } else {
                    errorType = SYNC_ENGINE_UNAVAILABLE
                }
            }
        }
    }

    DisposableEffect(null) {
        // setup account observers
        /**<presenter>**/
        accountManager.registerForSyncEvents(
            observer = eventObserver,
            owner = lifecycleOwner,
            autoPause = true,
        )

        accountManager.register(
            observer = accountObserver,
            owner = lifecycleOwner,
            autoPause = true,
        )

        commands.register(
            observer = commandsObserver,
            owner = lifecycleOwner,
            autoPause = true,
        )

        // No authenticated account present at all.
        if (accountManager.authenticatedAccount() == null) {
            errorType = SYNC_UNAVAILABLE
//            return@DisposableEffect
        }
        // Have an account, but it ran into auth issues.
        else if (accountManager.accountNeedsReauth()) {
            errorType = SYNC_NEEDS_REAUTHENTICATION
//            return
        }
        // Synced tabs not enabled.
        else if (!isSyncedTabsEngineEnabled(context)) {
            errorType = SYNC_ENGINE_UNAVAILABLE
//            return
        }
        // If no errors sync account
        else {
            syncAccount()
        }
        /**<presenter/>**/
        onDispose {
            /**<presenter>**/
            accountManager.unregisterForSyncEvents(eventObserver)
            accountManager.unregister(accountObserver)
            commands.unregister(commandsObserver)
            /**<presenter/>**/
        }
    }

    // layout content
    if (errorType != null) {
        SyncedTabsError(errorType!!)
    } else if (allSyncedTabs != null) {
        // if no errors show synced tabs
        // todo: swipe refresh
        SyncedTabsList(
            syncedTabs = allSyncedTabs!!,
            onTabClick = onTabClick,
            onTabCloseClick = onTabClose,
        )
    }
}

@Composable
private fun SyncedTabsError(errorType: ErrorType) {
    val error = when (errorType) {
        MULTIPLE_DEVICES_UNAVAILABLE, NO_TABS_AVAILABLE -> stringResource(R.string.synced_tabs_connect_another_device)
        SYNC_ENGINE_UNAVAILABLE -> stringResource(R.string.synced_tabs_enable_tab_syncing)
        SYNC_UNAVAILABLE -> stringResource(R.string.synced_tabs_connect_to_sync_account)
        SYNC_NEEDS_REAUTHENTICATION -> stringResource(R.string.synced_tabs_reauth)
    }
    val onClick = when (errorType) {
        MULTIPLE_DEVICES_UNAVAILABLE, NO_TABS_AVAILABLE -> {
            { /* todo: connect another device, go to settings */ }
        }

        SYNC_ENGINE_UNAVAILABLE -> {
            { /* todo: enable tab syncyng, go to settings */ }
        }

        SYNC_UNAVAILABLE -> {
            { /* todo: connect account, go to settings */ }
        }

        SYNC_NEEDS_REAUTHENTICATION -> {
            { /* todo: reauth, go to settings */ }
        }
    }
    InfernoText(
        text = error,
        modifier = Modifier
            .padding(16.dp)
            .clickable(onClick = onClick),
        fontColor = Color.Red,
    )
}
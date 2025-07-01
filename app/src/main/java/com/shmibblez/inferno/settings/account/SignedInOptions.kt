package com.shmibblez.inferno.settings.account

import android.text.format.DateUtils
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.accounts.FenixFxAEntryPoint
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.components.concept.sync.ConstellationState
import mozilla.components.concept.sync.DeviceConstellationObserver
import mozilla.components.service.fxa.SyncEngine
import mozilla.components.service.fxa.manager.SyncEnginesStorage
import mozilla.components.service.fxa.sync.SyncReason
import mozilla.components.service.fxa.sync.SyncStatusObserver
import mozilla.components.service.fxa.sync.getLastSynced
import mozilla.components.service.fxa.sync.setLastSynced
import java.lang.Exception

/**
 * todo: make sure everything correct (state handling, observers, prefs)
 * reference [AccountSettingsFragment]
 */
@Composable
internal fun SignedInOptions(edgeInsets: PaddingValues, onSignOut: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val accountManager = context.components.backgroundServices.accountManager

    // Returns the last saved sync time (in millis)
    // If the corresponding shared preference doesn't have a value yet,
    // it is initialized with the current time (in millis)
    fun lastSavedSyncTime(): Long {
        val lastSyncedTime = getLastSynced(context)
        return if (lastSyncedTime != 0L) {
            lastSyncedTime
        } else {
            val current = System.currentTimeMillis()
            setLastSynced(context, current)
            current
        }
    }

    var lastSyncedDate by remember {
        mutableStateOf<LastSyncTime>(
            LastSyncTime.Success(
                lastSavedSyncTime()
            )
        )
    }
    var syncEnginesStatus by remember { mutableStateOf(SyncEnginesStorage(context).getStatus()) }
    var isSyncing by remember { mutableStateOf(false) }
    var deviceName by remember {
        mutableStateOf(
            accountManager.authenticatedAccount()?.deviceConstellation()
                ?.state()?.currentDevice?.displayName
        )
    }
    var showEditDeviceNameDialogFor by remember { mutableStateOf<String?>(null) }

    /**
     * Manual sync triggered by the user. This also checks account authentication and refreshes the
     * device list.
     */
    fun syncNow() {
        lifecycleOwner.lifecycleScope.launch {
            // Trigger a sync.
            accountManager.syncNow(SyncReason.User)
            // Poll for device events & update devices.
            accountManager.authenticatedAccount()?.deviceConstellation()?.run {
                refreshDevices()
                pollForCommands()
            }
        }
    }

    /**
     * Takes a non-empty value and sets the device name. May fail due to authentication.
     *
     * @param newDeviceName the new name of the device. Cannot be an empty string.
     */
    fun syncDeviceName(newDeviceName: String): Boolean {
        if (newDeviceName.trim().isEmpty()) {
            return false
        }
        // This may fail, and we'll have a disparity in the UI until `updateDeviceName` is called.
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            accountManager.authenticatedAccount()?.deviceConstellation()
                ?.setDeviceName(newDeviceName, context)
            deviceName = null // null means loading
        }
        return true
    }

    fun onManageAccountClicked() {
        lifecycleOwner.lifecycleScope.launch {
            val acct = accountManager.authenticatedAccount()
            val url = acct?.getManageAccountURL(FenixFxAEntryPoint.SettingsMenu)
            if (url != null) {
                val intent = SupportUtils.createCustomTabIntent(context, url)
                context.startActivity(intent)
            }
        }
    }

    /**
     * Updates the sync engine status with the new state of the preference and triggers a sync
     * event.
     *
     * @param engine the sync engine whose preference has changed.
     * @param newValue the new value of the sync preference, where true indicates sync for that
     * preference and false indicates not synced.
     */
    fun updateSyncEngineState(engine: SyncEngine, newValue: Boolean) {
        SyncEnginesStorage(context).setStatus(engine, newValue)
        lifecycleOwner.lifecycleScope.launch {
            accountManager.syncNow(SyncReason.EngineChange)
        }
    }

    LaunchedEffect(null) {
        // sync event observer
        accountManager.registerForSyncEvents(
            observer = object : SyncStatusObserver {
                override fun onError(error: Exception?) {
                    Log.d("SignedInOptions", "sync event observer, onError, error: $error")
                    isSyncing = false
                    lastSyncedDate = LastSyncTime.Failed(lastSavedSyncTime())
                }

                override fun onIdle() {
                    Log.d("SignedInOptions", "sync event observer, onIdle")
                    isSyncing = false
                    syncEnginesStatus = SyncEnginesStorage(context).getStatus()
                    lastSyncedDate = LastSyncTime.Success(lastSavedSyncTime())
                }

                override fun onStarted() {
                    Log.d("SignedInOptions", "sync event observer, onStarted")
                    isSyncing = true
                }

            },
            owner = lifecycleOwner,
            autoPause = true,
        )
        // device constellation observer
        accountManager.authenticatedAccount()?.deviceConstellation()?.registerDeviceObserver(
            observer = object : DeviceConstellationObserver {
                override fun onDevicesUpdate(constellation: ConstellationState) {
                    constellation.currentDevice?.displayName?.also { deviceName = it }
                }
            },
            owner = lifecycleOwner,
            autoPause = true,
        )

        // loop that updates last synced time every 30 seconds
        lifecycleOwner.lifecycleScope.launch {
            while (true) {
                // delay 30 seconds
                delay(30000)
                // update displayed last sync date
                lastSyncedDate = when (lastSyncedDate) {
                    is LastSyncTime.Failed -> LastSyncTime.Failed(lastSavedSyncTime())
                    LastSyncTime.Never -> LastSyncTime.Never
                    is LastSyncTime.Success -> LastSyncTime.Success(lastSavedSyncTime())
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(edgeInsets),
        horizontalAlignment = Alignment.Start,
    ) {
        // manage account
        item {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                    )
                    .clickable(onClick = ::onManageAccountClicked),
            ) {
                InfernoText(text = stringResource(R.string.preferences_manage_account))
                InfernoText(
                    text = stringResource(R.string.manage_account_summary),
                    infernoStyle = InfernoTextStyle.SmallSecondary,
                )
            }
        }

        // sync now item
        item {
            Column(
                modifier = Modifier
                    .clickable(onClick = ::syncNow)
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                    ),
            ) {
                InfernoText(
                    text = when (isSyncing) {
                        true -> stringResource(R.string.sync_syncing_in_progress)
                        false -> stringResource(R.string.preferences_sync_now)
                    }
                )
                InfernoText(
                    text = when (val lsd = lastSyncedDate) {
                        LastSyncTime.Never -> {
                            context.getString(R.string.sync_never_synced_summary)
                        }

                        is LastSyncTime.Failed -> {
                            when (lsd.lastSync == 0L) {
                                true -> context.getString(R.string.sync_failed_never_synced_summary)
                                false -> context.getString(
                                    R.string.sync_failed_summary,
                                    DateUtils.getRelativeTimeSpanString(lsd.lastSync),
                                )
                            }
                        }

                        is LastSyncTime.Success -> {
                            context.getString(
                                R.string.sync_last_synced_summary,
                                DateUtils.getRelativeTimeSpanString(lsd.lastSync),
                            )
                        }
                    },
                    infernoStyle = InfernoTextStyle.SmallSecondary,
                )
            }
        }

        // choose what to sync title
        item {
            PreferenceTitle(text = stringResource(R.string.preferences_sync_category))
        }

        val bookmarksSyncEnabled = syncEnginesStatus.getOrElse(SyncEngine.Bookmarks) { true }
        // bookmarks
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_bookmarks),
                selected = bookmarksSyncEnabled,
                onSelectedChange = {
                    updateSyncEngineState(SyncEngine.Bookmarks, !bookmarksSyncEnabled)
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.Bookmarks),
            )
        }
        val cardsSyncEnabled = syncEnginesStatus.getOrElse(SyncEngine.CreditCards) { true }
        // payment methods
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_credit_cards_2),
                selected = cardsSyncEnabled,
                onSelectedChange = {
                    updateSyncEngineState(SyncEngine.CreditCards, !cardsSyncEnabled)
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.CreditCards),
            )
        }
        val historySyncEnabled = syncEnginesStatus.getOrElse(SyncEngine.History) { true }
        // history
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_history),
                selected = historySyncEnabled,
                onSelectedChange = {
                    updateSyncEngineState(SyncEngine.History, !historySyncEnabled)
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.History),
            )
        }
        val passwordsSyncEnabled = syncEnginesStatus.getOrElse(SyncEngine.Passwords) { true }
        // passwords
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_passwords),
                selected = passwordsSyncEnabled,
                onSelectedChange = {
                    updateSyncEngineState(SyncEngine.Passwords, !passwordsSyncEnabled)
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.Passwords),
            )
        }
        val tabSyncEnabled = syncEnginesStatus.getOrElse(SyncEngine.Tabs) { true }
        // tabs
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_tabs_2),
                selected = tabSyncEnabled,
                onSelectedChange = {
                    updateSyncEngineState(SyncEngine.Tabs, !tabSyncEnabled)
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.Tabs),
            )
        }
        val addressSyncEnabled = syncEnginesStatus.getOrElse(SyncEngine.Addresses) { true }
        // addresses
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_address),
                selected = addressSyncEnabled,
                onSelectedChange = {
                    updateSyncEngineState(SyncEngine.Addresses, !addressSyncEnabled)
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.Addresses),
            )
        }

        // device name
        item {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                    )
                    .clickable {
                        if (deviceName != null) {
                            showEditDeviceNameDialogFor = deviceName
                        }
                    },
            ) {
                InfernoText(text = stringResource(R.string.preferences_sync_device_name))
                InfernoText(
                    text = when (deviceName) {
                        null -> "Loading..." // todo: string res
                        else -> deviceName!!
                    },
                    infernoStyle = InfernoTextStyle.SmallSecondary,
                )
            }
        }

        // sign out option
        item {
            InfernoText(
                text = stringResource(R.string.preferences_sign_out),
                fontColor = context.infernoTheme().value.errorColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                    )
                    .clickable(onClick = onSignOut),
            )
        }
    }

    // show edit name dialog if requested
    if (showEditDeviceNameDialogFor != null) {
        EditDeviceNameDialog(
            onDismiss = { showEditDeviceNameDialogFor = null },
            onUpdateName = ::syncDeviceName,
            initialDeviceName = showEditDeviceNameDialogFor!!,
        )
    }
}
package com.shmibblez.inferno.settings.account

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.accounts.FenixFxAEntryPoint
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.PairFragment
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.toolbar.InfernoLoadingScreen
import kotlinx.coroutines.Dispatchers.Main
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
 * todo: before next update, check crashlytics for error, might need to test app on specific model
 *  google phone, first run debug, if no error test bundle / apk to see if error with proguard
 */
@Composable
fun AccountSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val settings by context.infernoSettingsDataStore.data.collectAsState(
        initial = InfernoSettings.getDefaultInstance()
    )

    val accountState by rememberAccountState()

    InfernoSettingsPage(
        title = when (accountState.authState) {
            AccountState.AccountAuthState.SignedOut -> stringResource(R.string.browser_menu_sign_in)
            AccountState.AccountAuthState.SignedIn -> stringResource(R.string.preferences_account_settings)
            AccountState.AccountAuthState.RequiresReauth -> stringResource(R.string.preferences_account_sync_error)
            null -> "Loading..." // todo: string res -> // stringResource(R.string.)
        }.toString(),
        goBack = goBack,
    ) {
        when (accountState.authState) {
            AccountState.AccountAuthState.SignedOut -> {
                // todo
                SignedOutOptions()
            }

            AccountState.AccountAuthState.SignedIn -> {
                SignedInOptions(accountState = accountState)
            }

            AccountState.AccountAuthState.RequiresReauth -> {
                // todo
                RequiresReauthOptions()
            }

            // loading
            null -> {
                InfernoLoadingScreen(Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * todo: reference [TurnOnSyncFragment]
 *  also for pairing with qr: [PairFragment]
 */
@Composable
private fun SignedOutOptions() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // todo: show sign in and other options
    }
}

/**
 * todo: make sure everything correct (state handling, observers, prefs)
 * reference [AccountSettingsFragment]
 */
@Composable
private fun SignedInOptions(accountState: AccountState) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lastSyncedDate by remember { mutableStateOf<LastSyncTime>(LastSyncTime.Never) }
    var syncEnginesStatus by remember { mutableStateOf(SyncEnginesStorage(context).getStatus()) }
    var isSyncing by remember { mutableStateOf(false) }
    var deviceName by remember { mutableStateOf<String?>(null) }
    var showEditDeviceNameDialogFor by remember { mutableStateOf<String?>(null) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    // todo: store last synced time in settings?
//    val settings by context.infernoSettingsDataStore.data.collectAsState(
//        initial = InfernoSettings.getDefaultInstance()
//    )

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

    LaunchedEffect(null) {
        // sync event observer
        context.components.backgroundServices.accountManager.registerForSyncEvents(
            observer = object : SyncStatusObserver {
                override fun onError(error: Exception?) {
                    isSyncing = false
                    lastSyncedDate = LastSyncTime.Failed(lastSavedSyncTime())
                }

                override fun onIdle() {
                    isSyncing = false
                    syncEnginesStatus = SyncEnginesStorage(context).getStatus()
                    lastSyncedDate = LastSyncTime.Success(lastSavedSyncTime())
                }

                override fun onStarted() {
                    isSyncing = true
                }

            },
            owner = lifecycleOwner,
            autoPause = true,
        )
        // device constellation observer
        context.components.backgroundServices.accountManager.authenticatedAccount()
            ?.deviceConstellation()?.registerDeviceObserver(
                observer = object : DeviceConstellationObserver {
                    override fun onDevicesUpdate(constellation: ConstellationState) {
                        constellation.currentDevice?.displayName?.also { deviceName = it }
                    }
                },
                owner = lifecycleOwner,
                autoPause = true,
            )
    }

    /**
     * Manual sync triggered by the user. This also checks account authentication and refreshes the
     * device list.
     */
    fun syncNow() {
        lifecycleOwner.lifecycleScope.launch {
            // Trigger a sync.
            context.components.backgroundServices.accountManager.syncNow(SyncReason.User)
            // Poll for device events & update devices.
            context.components.backgroundServices.accountManager.authenticatedAccount()
                ?.deviceConstellation()?.run {
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
        lifecycleOwner.lifecycleScope.launch(Main) {
            context.components.backgroundServices.accountManager.authenticatedAccount()
                ?.deviceConstellation()?.setDeviceName(newDeviceName, context)
            deviceName = null // null means loading
        }
        return true
    }

    fun onManageAccountClicked() {
        lifecycleOwner.lifecycleScope.launch {
            val acct = context.components.backgroundServices.accountManager.authenticatedAccount()
            val url = acct?.getManageAccountURL(FenixFxAEntryPoint.SettingsMenu)
            if (url != null) {
                val intent = SupportUtils.createCustomTabIntent(context, url)
                context.startActivity(intent)
            }
        }
    }

    fun onSignOutClicked() {

    }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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

        // todo: add sync icon to end, animate when syncing
        // sync now item
        item {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                    )
                    .clickable(onClick = ::syncNow),
            ) {
                InfernoText(
                    text = when (isSyncing) {
                        true -> stringResource(R.string.sync_syncing_in_progress)
                        false -> stringResource(R.string.preferences_sync_now)
                    }
                )
                InfernoText(
                    text = when (val lastSyncedDate = lastSyncedDate) {
                        LastSyncTime.Never -> {
                            context.getString(R.string.sync_never_synced_summary)
                        }

                        is LastSyncTime.Failed -> {
                            when (lastSyncedDate.lastSync == 0L) {
                                true -> context.getString(R.string.sync_failed_never_synced_summary)
                                false -> context.getString(
                                    R.string.sync_failed_summary,
                                    DateUtils.getRelativeTimeSpanString(lastSyncedDate.lastSync),
                                )
                            }
                        }

                        is LastSyncTime.Success -> {
                            context.getString(
                                R.string.sync_failed_summary,
                                DateUtils.getRelativeTimeSpanString(lastSyncedDate.lastSync),
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
        // bookmarks
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_bookmarks),
                selected = syncEnginesStatus.getOrElse(SyncEngine.Bookmarks) { true },
                onSelectedChange = {
                    // todo
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.Bookmarks),
            )
        }
        // payment methods
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_credit_cards_2),
                selected = syncEnginesStatus.getOrElse(SyncEngine.CreditCards) { true },
                onSelectedChange = {
                    // todo
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.CreditCards),
            )
        }
        // history
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_history),
                selected = syncEnginesStatus.getOrElse(SyncEngine.History) { true },
                onSelectedChange = {
                    // todo
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.History),
            )
        }
        // passwords
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_passwords),
                selected = syncEnginesStatus.getOrElse(SyncEngine.Passwords) { true },
                onSelectedChange = {
                    // todo
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.Passwords),
            )
        }
        // tabs
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_tabs_2),
                selected = syncEnginesStatus.getOrElse(SyncEngine.Tabs) { true },
                onSelectedChange = {
                    // todo
                },
                enabled = !isSyncing && syncEnginesStatus.containsKey(SyncEngine.Tabs),
            )
        }
        // addresses
        item {
            PreferenceSwitch(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.preferences_sync_address),
                selected = syncEnginesStatus.getOrElse(SyncEngine.Addresses) { true },
                onSelectedChange = {
                    // todo
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
                    .clickable {
                        // todo: sign out
                    },
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

    // show sign out dialog
    if (showSignOutDialog) {
        SignOutDialog(
            onDismiss = { showSignOutDialog = false },
            onConfirm = {
                lifecycleOwner.lifecycleScope.launch {
                    context.components.backgroundServices.accountAbnormalities.userRequestedLogout()
                    context.components.backgroundServices.accountManager.logout()
                }
            },
        )
    }
}

/**
 * todo: reference [AccountProblemFragment]
 *  sign out or reauth options
 */
@Composable
private fun RequiresReauthOptions() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // todo
    }
}
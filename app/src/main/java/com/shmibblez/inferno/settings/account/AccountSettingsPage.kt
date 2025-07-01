package com.shmibblez.inferno.settings.account

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.toolbar.InfernoLoadingScreen
import kotlinx.coroutines.launch

/**
 * todo: before next update, check crashlytics for error, might need to test app on specific model
 *  google phone, first run debug, if no error test bundle / apk to see if error with proguard
 */
@Composable
fun AccountSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings by context.infernoSettingsDataStore.data.collectAsState(
        initial = InfernoSettings.getDefaultInstance()
    )
    var showSignOutDialog by remember { mutableStateOf(false) }


    val accountState by rememberAccountState()

    InfernoSettingsPage(
        title = when (accountState.authState) {
            AccountState.AccountAuthState.SignedOut -> stringResource(R.string.browser_menu_sign_in)
            AccountState.AccountAuthState.SignedIn -> stringResource(R.string.preferences_account_settings)
            AccountState.AccountAuthState.RequiresReauth -> stringResource(R.string.preferences_account_sync_error)
            null -> "Loading..." // todo: string res -> // stringResource(R.string.)
        }.toString(),
        goBack = goBack,
    ) { edgeInsets ->
        // todo: some todos left in child components
        when (accountState.authState) {
            AccountState.AccountAuthState.SignedOut -> {
                // todo
                SignedOutOptions(edgeInsets = edgeInsets)
            }

            AccountState.AccountAuthState.SignedIn -> {
                SignedInOptions(edgeInsets = edgeInsets, onSignOut = {showSignOutDialog = true})
            }

            AccountState.AccountAuthState.RequiresReauth -> {
                RequiresReauthOptions(edgeInsets = edgeInsets, onSignOut = {showSignOutDialog = true})
            }

            // loading
            null -> {
                InfernoLoadingScreen(Modifier.fillMaxSize().padding(edgeInsets))
            }
        }
    }

    // show sign out dialog
    if (showSignOutDialog) {
        SignOutDialog(
            onDismiss = { showSignOutDialog = false },
            onConfirm = {
                lifecycleOwner.lifecycleScope.launch {
                    context.components.backgroundServices.accountAbnormalities.userRequestedLogout()
                    accountState.accountManager.logout()
                }
            },
        )
    }
}


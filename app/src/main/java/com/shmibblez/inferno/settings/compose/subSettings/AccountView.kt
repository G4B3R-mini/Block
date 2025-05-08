package com.shmibblez.inferno.settings.compose.subSettings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.preference.SwitchPreference
import com.shmibblez.inferno.R
import com.shmibblez.inferno.datastore.preferencesDataStore
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.getPreferenceKey
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.settings
import kotlinx.coroutines.CoroutineScope
import mozilla.components.concept.fetch.Client
import mozilla.components.concept.sync.Profile
import mozilla.components.service.fxa.manager.FxaAccountManager
//import com.shmibblez.inferno.proto.InfernoSettings
//import com.shmibblez.inferno.proto.infernoSettingsDataStore

class AccountState(
    val profile: Profile?,
    val scope: CoroutineScope,
    val accountManager: FxaAccountManager,
    private val httpClient: Client,
    private val updateFxAAllowDomesticChinaServerMenu: () -> Unit,
) {}

@Composable
fun rememberAccountState(
    profile: Profile?,
    scope: CoroutineScope,
    accountManager: FxaAccountManager,
    httpClient: Client,
    updateFxAAllowDomesticChinaServerMenu: () -> Unit,
): MutableState<AccountState> {


    return remember {
        mutableStateOf(
            AccountState(
                profile = profile,
                scope = scope,
                accountManager = accountManager,
                httpClient = httpClient,
                updateFxAAllowDomesticChinaServerMenu = updateFxAAllowDomesticChinaServerMenu,
            )
        )
    }
}

@Composable
fun AccountView() {
    val context = LocalContext.current
    val lifecycleScope = LocalLifecycleOwner.current.lifecycle.coroutineScope
//    val settings = context.infernoSettingsDataStore.data.collectAsState(
//        initial = InfernoSettings.getDefaultInstance(),
//    )
//    val mozSettings = context.moz

    val accountState by rememberAccountState(
        profile = context.components.backgroundServices.accountManager.accountProfile(),
        scope = lifecycleScope,
        accountManager = context.components.backgroundServices.accountManager,
        httpClient = context.components.core.client,
        updateFxAAllowDomesticChinaServerMenu = {},
    )

    Column { }
}
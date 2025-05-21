package com.shmibblez.inferno.settings.autofill

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.settings.httpsonly.addressManager
import com.shmibblez.inferno.settings.httpsonly.empty
import com.shmibblez.inferno.settings.httpsonly.rememberAddressManagerState
import kotlinx.coroutines.launch
import mozilla.components.concept.storage.Address

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AutofillSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    val addressManagerState by rememberAddressManagerState()
    var showAddressEditorFor by remember { mutableStateOf<Pair<Boolean, Address>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_back_button),
                        contentDescription = stringResource(R.string.browser_menu_back),
                        modifier = Modifier.clickable(onClick = goBack),
                    )
                },
                title = { InfernoText("Autofill Settings") }, // todo: string res
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // addresses settings title
            item { PreferenceTitle(stringResource(R.string.preferences_addresses)) }

            // save and autofill address
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_addresses_save_and_autofill_addresses_2),
                    summary = stringResource(R.string.preferences_addresses_save_and_autofill_addresses_summary_2),
                    selected = settings.isAddressSaveAndAutofillEnabled,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setIsAddressSaveAndAutofillEnabled(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // address manager
            addressManager(state = addressManagerState, // todo: get addresses
                // show address editor dialog
                onAddAddressClicked = { showAddressEditorFor = true to Address.empty() },
                onEditAddressClicked = { showAddressEditorFor = false to it },
                onDeleteAddressClicked = {
                    addressManagerState.deleteAddress(it.guid)
                })

            // saved logins
            item { PreferenceTitle(stringResource(R.string.preferences_passwords_saved_logins_2)) }

            // save logins settings
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_delete_browsing_data_on_quit),
                    summary = null,
                    selected = settings.deleteBrowsingDataOnQuit,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDeleteBrowsingDataOnQuit(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }
        }

        if (showAddressEditorFor != null) {
            AddressEditorDialog(
                create = showAddressEditorFor!!.first,
                initialAddress = showAddressEditorFor!!.second,
                onUpdateAddress = { create, guid, addressFields ->
                    when (create) {
                        true -> addressManagerState.addAddress(addressFields)
                        false -> addressManagerState.updateAddress(guid, addressFields)
                    }
                },
                onDismiss = { showAddressEditorFor = null },
            )
        }
    }
}
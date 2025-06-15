package com.shmibblez.inferno.settings.autofill

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.biometric.BiometricPromptCallbackManager
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import kotlinx.coroutines.launch
import mozilla.components.concept.storage.Address
import mozilla.components.concept.storage.CreditCard

// todo: add account state, if logged in show sync options, or show in account signed in
//  options, check that first
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AutofillSettingsPage(
    goBack: () -> Unit,
    biometricPromptCallbackManager: BiometricPromptCallbackManager,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    val addressManagerState by rememberAddressManagerState()
    var showAddressEditorFor by remember { mutableStateOf<Pair<Boolean, Address>?>(null) }
    val creditCardManagerState by rememberCardManagerState(
        biometricPromptCallbackManager = biometricPromptCallbackManager,
    )
    var showCreditCardEditorFor by remember { mutableStateOf<Pair<Boolean, CreditCard?>?>(null) }

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_autofill),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            /**
             * address settings
             */
            /**
             * address settings
             */


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
            addressManager(
                state = addressManagerState,
                // show address editor dialog
                onAddAddressClicked = { showAddressEditorFor = true to Address.empty() },
                onEditAddressClicked = { showAddressEditorFor = false to it },
                onDeleteAddressClicked = { addressManagerState.deleteAddress(it.guid) },
            )

            /**
             * payment methods settings
             */

            /**
             * payment methods settings
             */

            // payment methods settings title
            item { PreferenceTitle(stringResource(R.string.preferences_credit_cards_2)) }

            // save and autofill credit cards
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_credit_cards_save_and_autofill_cards_2),
                    summary = stringResource(R.string.preferences_credit_cards_save_and_autofill_cards_summary_2),
                    selected = settings.isCardSaveAndAutofillEnabled,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setIsCardSaveAndAutofillEnabled(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // todo: urgent, require authentication with pin or biometric
            // card manager
            cardManager(
                state = creditCardManagerState,
                // show card editor dialog
                onAddCreditCardClicked = { showCreditCardEditorFor = true to null },
                onEditCreditCardClicked = { showCreditCardEditorFor = false to it },
                onDeleteCreditCardClicked = { creditCardManagerState.deleteCreditCard(it.guid) },
            )
        }

        if (showAddressEditorFor != null) {
            AddressEditorDialog(
                create = showAddressEditorFor!!.first,
                initialAddress = showAddressEditorFor!!.second,
                onUpdateAddress = { create, guid, addressFields ->
                    when (create) {
                        true -> addressManagerState.addAddress(addressFields)
                        false -> addressManagerState.updateAddress(guid!!, addressFields)
                    }
                },
                onDismiss = { showAddressEditorFor = null },
            )
        } else if (showCreditCardEditorFor != null) {
            CreditCardEditorDialog(
                create = showCreditCardEditorFor!!.first,
                storage = creditCardManagerState.storage,
                initialCreditCard = showCreditCardEditorFor!!.second,
                onAddCreditCard = { cardFields -> creditCardManagerState.addCreditCard(cardFields) },
                onUpdateCreditCard = { guid, cardFields ->
                    creditCardManagerState.updateCreditCard(guid, cardFields)
                },
                onDismiss = { showCreditCardEditorFor = null },
            )
        }
    }
}
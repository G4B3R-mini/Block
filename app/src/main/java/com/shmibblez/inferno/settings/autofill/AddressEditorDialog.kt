package com.shmibblez.inferno.settings.autofill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import mozilla.components.concept.storage.Address
import mozilla.components.concept.storage.UpdatableAddressFields

@Composable
fun AddressEditorDialog(
    create: Boolean,
    initialAddress: Address,
    onUpdateAddress: (create: Boolean, guid: String?, address: UpdatableAddressFields) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialAddress.name) }
//    var organization by remember { mutableStateOf(initialAddress.organization) }
    var streetAddress by remember { mutableStateOf(initialAddress.streetAddress) }
//    var addressLevel3 by remember { mutableStateOf(initialAddress.addressLevel3) }
    var addressLevel2 by remember { mutableStateOf(initialAddress.addressLevel2) }
    var addressLevel1 by remember { mutableStateOf(initialAddress.addressLevel1) }
    var postalCode by remember { mutableStateOf(initialAddress.postalCode) }
    var country by remember { mutableStateOf(initialAddress.country) }
    var tel by remember { mutableStateOf(initialAddress.tel) }
    var email by remember { mutableStateOf(initialAddress.email) }

    InfernoDialog(onDismiss = onDismiss) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING)
        ) {
            // name
            item {
                InfernoOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.addresses_name),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }

            // street address
            item {
                InfernoOutlinedTextField(
                    value = streetAddress,
                    onValueChange = { streetAddress = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.addresses_street_address),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }

            // city
            item {
                InfernoOutlinedTextField(
                    value = addressLevel2,
                    onValueChange = { addressLevel2 = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.addresses_city),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }

            // subregion dropdown (state)
            item {
                InfernoOutlinedTextField(
                    value = addressLevel1,
                    onValueChange = { addressLevel1 = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.addresses_state),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }

            // zipcode
            item {
                InfernoOutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.addresses_zip),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }

            // country or region
            item {
                InfernoOutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.addresses_country),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }

            // phone
            item {
                InfernoOutlinedTextField(
                    value = tel,
                    onValueChange = { tel = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.addresses_phone),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }

            // email
            item {
                InfernoOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.addresses_email),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }
        }
        Row(
            modifier = Modifier.padding(vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING),
            horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        ) {
            InfernoOutlinedButton(
                modifier = Modifier.weight(1F),
                text = stringResource(android.R.string.cancel),
                onClick = onDismiss,
            )
            InfernoButton(
                modifier = Modifier.weight(1F),
                text = stringResource(R.string.browser_menu_save),
                onClick = {
                    val updatedAddress = UpdatableAddressFields(
                        name = name,
                        organization = "",
                        streetAddress = streetAddress,
                        addressLevel3 = "",
                        addressLevel2 = addressLevel2,
                        addressLevel1 = addressLevel1,
                        postalCode = postalCode,
                        country = country,
                        tel = tel,
                        email = email,
                    )
                    onUpdateAddress(
                        create, if (create) null else initialAddress.guid, updatedAddress
                    )
                    // dismiss
                    onDismiss.invoke()
                },
            )
        }
    }
}
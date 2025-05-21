package com.shmibblez.inferno.settings.autofill

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.databinding.FragmentAddressEditorBinding
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.settings.httpsonly.toUpdatableAddressFields
import mozilla.components.concept.storage.Address
import mozilla.components.concept.storage.UpdatableAddressFields

@Composable
fun AddressEditorDialog(
    create: Boolean,
    initialAddress: Address,
    onUpdateAddress: (create: Boolean, guid: String, address: UpdatableAddressFields) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var address by remember { mutableStateOf(initialAddress.toUpdatableAddressFields()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Card {
            LazyColumn {
                item {
                    InfernoOutlinedTextField(
                        value = address.name,
                        onValueChange = { address = address.copy(name = it) },
                        label = {
                            InfernoText(
                                stringResource(R.string.addresses_name),
                                fontColor = context.infernoTheme().value.primaryOutlineColor,
                            )
                        },
                    )
                }

                /**
                 * todo: add remaining fields
                 *  check [FragmentAddressEditorBinding]
                 */
            }
            Row {
                InfernoOutlinedButton(
                    text = stringResource(android.R.string.cancel),
                    onClick = onDismiss,
                )
                InfernoButton(text = stringResource(R.string.save_changes_to_login), onClick = {
                    // todo: update address
                })
            }
        }
    }
}
package com.shmibblez.inferno.settings.autofill

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.sub.NumberPicker
import com.shmibblez.inferno.browser.prompts.webPrompts.compose.sub.rememberNumberPickerState
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.ext.year
import com.shmibblez.inferno.settings.creditcards.validateCreditCardNumber
import mozilla.components.concept.storage.CreditCard
import mozilla.components.concept.storage.CreditCardNumber
import mozilla.components.concept.storage.NewCreditCardFields
import mozilla.components.concept.storage.UpdatableCreditCardFields
import mozilla.components.service.sync.autofill.AutofillCreditCardsAddressesStorage
import mozilla.components.support.ktx.kotlin.last4Digits
import mozilla.components.support.utils.creditCardIIN
import java.util.Calendar

// Number of years to show in the expiry year dropdown.
private const val NUMBER_OF_YEARS_TO_SHOW = 10

@Composable
fun CreditCardEditorDialog(
    create: Boolean,
    storage: AutofillCreditCardsAddressesStorage,
    initialCreditCard: CreditCard?,
    onAddCreditCard: (card: NewCreditCardFields) -> Unit,
    onUpdateCreditCard: (guid: String, card: UpdatableCreditCardFields) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var number by remember { mutableStateOf("") }
    var numberError by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf(initialCreditCard?.billingName ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    val monthPickerState = rememberNumberPickerState()
    val months = remember { (1..12).map { it.toString() } }
    val yearPickerState = rememberNumberPickerState()
    val years = remember {
        val cal = Calendar.getInstance()
        val currentYear = cal.year
        (currentYear..(currentYear + NUMBER_OF_YEARS_TO_SHOW)).map { it.toString() }
    }

    // true if all good, false if something wrong
    fun validateFields(): Boolean {
        var allValid = true
        // check number
        if (number.validateCreditCardNumber()) {
            numberError = null
        } else {
            allValid = false
            numberError = context.getString(R.string.credit_cards_number_validation_error_message_2)
        }
        // check name
        if (name.isNotBlank()) {
            nameError = null
        } else {
            allValid = false
            nameError =
                context.getString(R.string.credit_cards_name_on_card_validation_error_message_2)
        }
        return allValid
    }

    LaunchedEffect(null) {
        validateFields()
        // decrypt card number and use if possible
        if (initialCreditCard != null) {
            val crypto = storage.getCreditCardCrypto()
            val key = crypto.getOrGenerateKey()
            val decrypted = crypto.decrypt(key, initialCreditCard.encryptedCardNumber)?.number
            if (decrypted != null) {
                // only set if not null
                number = decrypted
            }
        }
        validateFields()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Card {
            LazyColumn {
                // number
                item {
                    InfernoOutlinedTextField(
                        value = number,
                        onValueChange = {
                            number = it
                            validateFields()
                        },
                        label = {
                            InfernoText(
                                stringResource(R.string.credit_cards_card_number),
                                fontColor = context.infernoTheme().value.primaryOutlineColor,
                            )
                        },
                        isError = numberError != null,
                        supportingText = {
                            if (numberError != null) {
                                InfernoText(
                                    text = numberError!!, infernoStyle = InfernoTextStyle.Error
                                )
                            }
                        },
                    )
                }

                // name
                item {
                    InfernoOutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            validateFields()
                        },
                        label = {
                            InfernoText(
                                stringResource(R.string.credit_cards_name_on_card),
                                fontColor = context.infernoTheme().value.primaryOutlineColor,
                            )
                        },
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) {
                                InfernoText(
                                    text = nameError!!, infernoStyle = InfernoTextStyle.Error
                                )
                            }
                        },
                    )
                }

                // expiry date
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        InfernoText(
                            text = stringResource(R.string.credit_cards_expiration_date)
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // month picker
                            NumberPicker(
                                state = monthPickerState,
                                values = months,
                                modifier = Modifier.weight(1F),
                            )
                            // year picker
                            NumberPicker(
                                state = yearPickerState,
                                values = years,
                                modifier = Modifier.weight(1F),
                            )
                        }
                    }
                }
            }
            Row {
                InfernoOutlinedButton(
                    modifier = Modifier.weight(1F),
                    text = stringResource(android.R.string.cancel),
                    onClick = onDismiss,
                )
                InfernoButton(
                    modifier = Modifier.weight(1F),
                    text = stringResource(R.string.save_changes_to_login),
                    enabled = nameError == null && numberError == null,
                    onClick = {
                        // if field invalid return (dont save)
                        if (!validateFields()) return@InfernoButton

                        // if all good create or update
                        val nameStr = name.trim()
                        val numberStr = number.trim()
                        val last4 = numberStr.last4Digits()
                        val month = monthPickerState.selectedItem.toInt().toLong()
                        val year = yearPickerState.selectedItem.toInt().toLong()
                        when (create) {
                            true -> {
                                // create card
                                val card = NewCreditCardFields(
                                    billingName = nameStr,
                                    plaintextCardNumber = CreditCardNumber.Plaintext(numberStr),
                                    cardNumberLast4 = last4,
                                    expiryMonth = month,
                                    expiryYear = year,
                                    cardType = numberStr.creditCardIIN()?.creditCardIssuerNetwork?.name
                                        ?: "",
                                )
                                onAddCreditCard.invoke(card)
                            }

                            false -> {
                                // update card
                                val card = UpdatableCreditCardFields(
                                    billingName = nameStr,
                                    cardNumber = CreditCardNumber.Plaintext(numberStr),
                                    cardNumberLast4 = last4,
                                    expiryMonth = month,
                                    expiryYear = year,
                                    cardType = numberStr.creditCardIIN()?.creditCardIssuerNetwork?.name
                                        ?: "",
                                )
                                onUpdateCreditCard(initialCreditCard!!.guid, card)
                            }
                        }
                        // dismiss
                        onDismiss.invoke()
                    },
                )
            }
        }
    }
}
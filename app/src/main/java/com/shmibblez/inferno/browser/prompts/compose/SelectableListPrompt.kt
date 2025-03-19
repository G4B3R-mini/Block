package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.shmibblez.inferno.NavGraphDirections
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onNegativeAction
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitAddressAutofillDismissedFact
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.facts.emitCreditCardAutofillDismissedFact
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Address
import mozilla.components.concept.storage.CreditCardEntry
import mozilla.components.support.utils.creditCardIssuerNetwork

@Composable
fun SelectableListPrompt(
    data: PromptRequest,
    sessionId: String,
    header: String,
    manageText: String,
) {
    if (data !is PromptRequest.SelectAddress && data !is PromptRequest.SelectCreditCard) {
        throw IllegalArgumentException("unsupported prompt type, supported above")
    }
    val store = LocalContext.current.components.core.store
    var expanded by remember { mutableStateOf(true) }
    val navController = LocalView.current.findNavController()

    PromptBottomSheetTemplate(
        onDismissRequest = {
            when (data) {
                is PromptRequest.SelectAddress -> {
                    emitAddressAutofillDismissedFact()
                }

                is PromptRequest.SelectCreditCard -> {
                    emitCreditCardAutofillDismissedFact()
                }

                else -> {
                    throw IllegalArgumentException("unsupported prompt type, supported above")
                }
            }
            onDismiss(data)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, data))
//            store.consumePromptFrom<PromptRequest.SelectAddress>(sessionId) {
//                it.onDismiss()
//            }
        },
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = {
                onNegativeAction(data)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, data))
            }),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.TOP,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // header
            InfernoText(
                text = header,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1F),
            )
            // expand addresses
            Icon(
                painter = if (expanded) painterResource(R.drawable.mozac_ic_chevron_up_24) else painterResource(
                    R.drawable.mozac_ic_chevron_down_24
                ),
                tint = Color.White,
                contentDescription = "",
                modifier = Modifier
                    .size(36.dp)
                    .clickable {
                        expanded = !expanded
                    },
            )
        }
        if (expanded) {
            // address list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                when (data) {
                    is PromptRequest.SelectAddress -> {
                        items(data.addresses) {
                            AddressItem(
                                address = it,
                                onClick = {
                                    onPositiveAction(data, it)
                                    store.dispatch(
                                        ContentAction.ConsumePromptRequestAction(
                                            sessionId, data
                                        )
                                    )
                                },
                            )
                        }
                    }

                    is PromptRequest.SelectCreditCard -> {
                        items(data.creditCards) {
                            CreditCardItem(
                                creditCard = it,
                                onClick = {
                                    onPositiveAction(data, it)
                                    store.dispatch(
                                        ContentAction.ConsumePromptRequestAction(
                                            sessionId, data
                                        )
                                    )
                                },
                            )
                        }
                    }

                    else -> {
                        throw IllegalArgumentException("unsupported prompt type")
                    }
                }
                item { ManageOptions(data, navController, manageText) }
            }

        }
        // todo: tint text color primary
        // manage addresses / credit cards
        if (!expanded) {
            ManageOptions(data, navController, manageText, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ManageOptions(data: PromptRequest, navController: NavController, manageText: String, modifier: Modifier = Modifier,) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp)
            .clickable {
                when (data) {
                    is PromptRequest.SelectAddress -> {
                        // go to manage directions page
                        val directions =
                            NavGraphDirections.actionGlobalAutofillSettingFragment()
                        navController.navigate(directions)
                    }

                    is PromptRequest.SelectCreditCard -> {
                        // go to manage credit cards page
                        val directions =
                            NavGraphDirections.actionGlobalAutofillSettingFragment()
                        navController.navigate(directions)
                    }

                    else -> {
                        throw IllegalArgumentException("unsupported prompt type")
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.mozac_ic_settings_24),
            tint = Color.White,
            contentDescription = "",
            modifier = Modifier
                .size(20.dp),
        )
        // todo: text color primary
        InfernoText(
            text = manageText,
            modifier = Modifier
                .weight(1F)
                .padding(start = 8.dp, end = 16.dp),
            textAlign = TextAlign.Start,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun ColumnScope.AddressItem(address: Address, onClick: () -> Unit) {
    InfernoText(
        text = address.streetAddress,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        textAlign = TextAlign.Start,
        fontSize = 16.sp,
    )
}

@Composable
private fun ColumnScope.CreditCardItem(creditCard: CreditCardEntry, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // todo urgent!!!: biometric prompt auth first
                //   need onAuthSuccess and onAuthFailure, if failed do nothing
                onClick.invoke()
            },
    ) {
        // card network icon
        Image(
            painter = painterResource(creditCard.cardType.creditCardIssuerNetwork().icon),
            contentDescription = "card issuer image",
            modifier = Modifier
                .size(40.dp)
                .padding(start = 16.dp)
        )
        Column(
            horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1F)
        ) {
            // card number
            InfernoText(
                text = creditCard.obfuscatedCardNumber,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
            )
            // card expiration date
            InfernoText(
                text = creditCard.expiryDate,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
            )
        }
    }
}
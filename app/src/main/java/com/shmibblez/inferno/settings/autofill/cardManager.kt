package com.shmibblez.inferno.settings.autofill

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.biometric.BiometricPromptCallbackManager
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mozilla.components.concept.storage.CreditCard
import mozilla.components.concept.storage.NewCreditCardFields
import mozilla.components.concept.storage.UpdatableCreditCardFields
import mozilla.components.service.sync.autofill.AutofillCreditCardsAddressesStorage
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.utils.creditCardIssuerNetwork
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val ICON_SIZE = 18.dp
private val CREDIT_CARD_ICON_SIZE = 40.dp

internal class CreditCardManagerState(
    private val context: Context,
    val storage: AutofillCreditCardsAddressesStorage,
    val coroutineScope: CoroutineScope,
    private val biometricPromptCallbackManager: BiometricPromptCallbackManager,
    initiallyExpanded: Boolean = false,
) : LifecycleAwareFeature {

    private val authCallback = object: AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            authenticated = false
            Toast.makeText(context, "Failed to authenticate, unknown error occurred: $errString", Toast.LENGTH_LONG).show()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            authenticated = false
            Toast.makeText(context, "Failed to authenticate, retry?", Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            authenticated = true
        }
    }

    var authenticated by mutableStateOf(false)
        private set

    fun startAuth() {
        biometricPromptCallbackManager.showPrompt(
            title = context.getString(R.string.credit_cards_biometric_prompt_message)
        )
    }

    private val jobs: MutableList<Job> = mutableListOf()

    internal var creditCards by mutableStateOf(emptyList<CreditCard>())

    val isLoading: Boolean
        get() = jobs.isNotEmpty()

    var expanded: Boolean by mutableStateOf(initiallyExpanded)


    // removes all jobs that are not active
    private fun clearFinishedJobs() {
        jobs.removeAll { !it.isActive }
    }

    // launches suspend function and tracks its state
    private fun launchSuspend(block: suspend CoroutineScope.() -> Unit) {
        val job = coroutineScope.launch(block = block).apply {
            this.invokeOnCompletion { clearFinishedJobs() }
        }
        jobs.add(job)
    }

    // refreshes credit card list
    private fun refreshCreditCards() {
        launchSuspend { creditCards = storage.getAllCreditCards() }
    }

    fun addCreditCard(creditCardFields: NewCreditCardFields) {
        launchSuspend {
            storage.addCreditCard(creditCardFields)
            refreshCreditCards()
        }
    }

    fun updateCreditCard(guid: String, card: UpdatableCreditCardFields) {
        launchSuspend {
            storage.updateCreditCard(guid, card)
            refreshCreditCards()
        }
    }

    fun deleteCreditCard(guid: String) {
        launchSuspend {
            storage.deleteCreditCard(guid)
            refreshCreditCards()
        }
    }

    override fun start() {
        refreshCreditCards()
        biometricPromptCallbackManager.addCallbackListener(authCallback)
    }

    override fun stop() {
        jobs.forEach { it.cancel() }
        biometricPromptCallbackManager.removeCallbackListener(authCallback)
    }

}

@Composable
internal fun rememberCardManagerState(
    biometricPromptCallbackManager: BiometricPromptCallbackManager,
): MutableState<CreditCardManagerState> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state = remember {
        mutableStateOf(
            CreditCardManagerState(
                context = context,
                storage = context.components.core.autofillStorage,
                coroutineScope = coroutineScope,
                biometricPromptCallbackManager = biometricPromptCallbackManager,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()

        onDispose {
            state.value.stop()
        }
    }

    return state
}

internal fun LazyListScope.cardManager(
    state: CreditCardManagerState,
    onAddCreditCardClicked: () -> Unit,
    onEditCreditCardClicked: (CreditCard) -> Unit,
    onDeleteCreditCardClicked: (CreditCard) -> Unit,
) {
    item {

        // manage cards title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { state.expanded = !state.expanded }
                .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
                .padding(
                    top = PrefUiConst.PREFERENCE_HALF_VERTICAL_PADDING,
                    // bottom padding set below
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InfernoText(text = stringResource(R.string.preferences_credit_cards_manage_saved_cards_2))
            InfernoIcon(
                painter = when (state.expanded) {
                    true -> painterResource(R.drawable.ic_chevron_up_24)
                    false -> painterResource(R.drawable.ic_chevron_down_24)
                },
                contentDescription = "",
                modifier = Modifier.size(ICON_SIZE),
            )
        }
    }

    // credit card items
    if (state.expanded) {
        when (state.authenticated) {
            true -> {
                items(state.creditCards) {
                    CardItem(
                        creditCard = it,
                        onEditCreditCardClicked = onEditCreditCardClicked,
                        onDeleteCreditCardClicked = onDeleteCreditCardClicked,
                    )
                }

                item {
                    AddCardItem(onAddCreditCardClicked = onAddCreditCardClicked)
                }
            }

            false -> {
                item {
                    AuthenticateItem(onAuthClicked = { state.startAuth() })
                }
            }
        }
    }

    // bottom padding
    item {
        Spacer(
            modifier = Modifier.padding(bottom = PrefUiConst.PREFERENCE_HALF_VERTICAL_PADDING),
        )
    }
}

@Composable
private fun CardItem(
    creditCard: CreditCard,
    onEditCreditCardClicked: (CreditCard) -> Unit,
    onDeleteCreditCardClicked: (CreditCard) -> Unit,
) {
    val expiryDate = remember {
        val dateFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        // Subtract 1 from the expiry month since Calendar.Month is based on a 0-indexed.
        calendar.set(Calendar.MONTH, creditCard.expiryMonth.toInt() - 1)
        calendar.set(Calendar.YEAR, creditCard.expiryYear.toInt())
        dateFormat.format(calendar.time)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING * 2F),
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // edit icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_edit_24),
            contentDescription = null,
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable { onEditCreditCardClicked.invoke(creditCard) },
        )

        // credit card icon
        Image(
            bitmap = ImageBitmap.imageResource(creditCard.cardType.creditCardIssuerNetwork().icon),
            contentDescription = null,
            modifier = Modifier
                .size(CREDIT_CARD_ICON_SIZE)
                .clickable { onEditCreditCardClicked.invoke(creditCard) },
        )

        Column(modifier = Modifier.weight(1F)) {
            // card number
            InfernoText(
                text = creditCard.obfuscatedCardNumber,
                fontWeight = FontWeight.Bold,
            )
            // expiry date
            InfernoText(
                text = expiryDate,
                infernoStyle = InfernoTextStyle.SmallSecondary,
                maxLines = 2,
            )
        }

        // delete icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_delete_24),
            contentDescription = null,
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable { onDeleteCreditCardClicked.invoke(creditCard) },
        )
    }
}

@Composable
private fun AddCardItem(onAddCreditCardClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onAddCreditCardClicked.invoke() }
            .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING * 2F),
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // add icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_new_24),
            contentDescription = null,
            modifier = Modifier.size(ICON_SIZE),
        )

        // add card text
        InfernoText(text = stringResource(R.string.credit_cards_add_card))
    }
}


@Composable
private fun AuthenticateItem(onAuthClicked: () -> Unit) {
    // add login text
    InfernoText(
        text = stringResource(R.string.credit_cards_biometric_prompt_message),
        fontColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAuthClicked.invoke() }
            .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING * 2F),
    )
}
package com.shmibblez.inferno.settings.autofill

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
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.compose.components.PreferenceConstants
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
    val storage: AutofillCreditCardsAddressesStorage,
    val coroutineScope: CoroutineScope,
    initiallyExpanded: Boolean = false,
) : LifecycleAwareFeature {

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
    }

    override fun stop() {
        jobs.forEach { it.cancel() }
    }

}

@Composable
internal fun rememberCardManagerState(): MutableState<CreditCardManagerState> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state = remember {
        mutableStateOf(
            CreditCardManagerState(
                storage = context.components.core.autofillStorage, coroutineScope = coroutineScope
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
                .clickable { state.expanded = !state.expanded }
                .padding(horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING)
                .padding(
                    top = PreferenceConstants.PREFERENCE_HALF_VERTICAL_PADDING,
                    // bottom padding set below
                ),
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
        items(state.creditCards) {
            LoginItem(
                creditCard = it,
                onEditCreditCardClicked = onEditCreditCardClicked,
                onDeleteCreditCardClicked = onDeleteCreditCardClicked,
            )
        }
        item {
            AddLoginItem(onAddCreditCardClicked = onAddCreditCardClicked)
        }
    }

    // bottom padding
    item {
        Spacer(
            modifier = Modifier.padding(bottom = PreferenceConstants.PREFERENCE_HALF_VERTICAL_PADDING),
        )
    }
}

@Composable
private fun LoginItem(
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
            .padding(horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
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
                infernoStyle = InfernoTextStyle.Subtitle,
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
private fun AddLoginItem(onAddCreditCardClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onAddCreditCardClicked.invoke() }
            .padding(horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
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

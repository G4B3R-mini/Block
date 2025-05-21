package com.shmibblez.inferno.settings.httpsonly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.shmibblez.inferno.R
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.address.ext.getAddressLabel
import com.shmibblez.inferno.settings.compose.components.PreferenceConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mozilla.components.concept.storage.Address
import mozilla.components.concept.storage.UpdatableAddressFields
import mozilla.components.service.sync.autofill.AutofillCreditCardsAddressesStorage
import mozilla.components.support.base.feature.LifecycleAwareFeature
import java.util.Date

private val ICON_SIZE = 18.dp

internal class AddressManagerState(
    val storage: AutofillCreditCardsAddressesStorage,
    val coroutineScope: CoroutineScope,
    initiallyExpanded: Boolean = false,
) : LifecycleAwareFeature {

    private val jobs: MutableList<Job> = mutableListOf()

    internal var addresses by mutableStateOf(emptyList<Address>())

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

    // refreshes address list
    private fun refreshAddresses() {
        launchSuspend { addresses = storage.getAllAddresses() }
    }

    fun addAddress(addressFields: UpdatableAddressFields) {
        launchSuspend {
            storage.addAddress(addressFields)
            refreshAddresses()
        }
    }

    fun updateAddress(guid: String, address: UpdatableAddressFields) {
        launchSuspend {
            storage.updateAddress(guid, address)
            refreshAddresses()
        }
    }

    fun deleteAddress(guid: String) {
        launchSuspend {
            storage.deleteAddress(guid)
            refreshAddresses()
        }
    }

    override fun start() {
        refreshAddresses()
    }

    override fun stop() {
        jobs.forEach { it.cancel() }
    }

}

@Composable
internal fun rememberAddressManagerState(): MutableState<AddressManagerState> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state = remember {
        mutableStateOf(
            AddressManagerState(
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

internal fun LazyListScope.addressManager(
    state: AddressManagerState,
    onAddAddressClicked: () -> Unit,
    onEditAddressClicked: (Address) -> Unit,
    onDeleteAddressClicked: (Address) -> Unit,
) {
    item {
        Row(
            modifier = Modifier
                .clickable { state.expanded = !state.expanded }
                .padding(horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING)
                .padding(
                    top = PreferenceConstants.PREFERENCE_HALF_VERTICAL_PADDING,
                    bottom = if (state.expanded) 0.dp else PreferenceConstants.PREFERENCE_HALF_VERTICAL_PADDING,
                ),
        ) {
            InfernoText(text = stringResource(R.string.addresses_manage_addresses))
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
    if (state.expanded) {
        items(state.addresses) {
            AddressItem(
                address = it,
                onEditAddress = onEditAddressClicked,
                onDeleteAddress = onDeleteAddressClicked,
            )
        }
        item {
            AddAddressItem(onAddAddressClicked = onAddAddressClicked)
        }
        item {
            Spacer(
                modifier = Modifier.padding(bottom = PreferenceConstants.PREFERENCE_HALF_VERTICAL_PADDING),
            )
        }
    }
}

@Composable
private fun AddressItem(
    address: Address,
    onEditAddress: (Address) -> Unit,
    onDeleteAddress: (Address) -> Unit,
) {
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
                .clickable { onEditAddress.invoke(address) },
        )

        // address info
        Column(modifier = Modifier.weight(1F)) {
            InfernoText(
                text = address.name,
                fontWeight = FontWeight.Bold,
            )
            InfernoText(
                text = address.getAddressLabel(),
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
                .clickable { onDeleteAddress.invoke(address) },
        )
    }
}

@Composable
private fun AddAddressItem(onAddAddressClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onAddAddressClicked.invoke() }
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

        // add address text
        InfernoText(text = stringResource(R.string.addresses_add_address))
    }
}

internal fun Address.Companion.empty(): Address {
    val now = Date().time
    return Address(
        guid = "",
        name = "",
        organization = "",
        streetAddress = "",
        addressLevel3 = "",
        addressLevel2 = "",
        addressLevel1 = "",
        postalCode = "",
        country = "",
        tel = "",
        email = "",
        timeCreated = now,
        timeLastUsed = now,
        timeLastModified = now,
        timesUsed = 0,
    )
}

internal fun Address.toUpdatableAddressFields(): UpdatableAddressFields {
    return UpdatableAddressFields(
        name = this.name,
        organization = this.organization,
        streetAddress = this.streetAddress,
        addressLevel3 = this.addressLevel3,
        addressLevel2 = this.addressLevel2,
        addressLevel1 = this.addressLevel1,
        postalCode = this.postalCode,
        country = this.country,
        tel = this.tel,
        email = this.email
    )
}

//internal fun UpdatableAddressFields.copy(
//    name: String?,
//    organization: String?,
//    streetAddress: String?,
//    addressLevel3: String?,
//    addressLevel2: String?,
//    addressLevel1: String?,
//    postalCode: String?,
//    country: String?,
//    tel: String?,
//    email: String?,
//): UpdatableAddressFields {
//    return UpdatableAddressFields(
//        name = name ?: this.name,
//        organization = organization ?: this.organization,
//        streetAddress = streetAddress ?: this.streetAddress,
//        addressLevel3 = addressLevel3 ?: this.addressLevel3,
//        addressLevel2 = addressLevel2 ?: this.addressLevel2,
//        addressLevel1 = addressLevel1 ?: this.addressLevel1,
//        postalCode = postalCode ?: this.postalCode,
//        country = country ?: this.country,
//        tel = tel ?: this.tel,
//        email = email ?: this.email,
//    )
//}
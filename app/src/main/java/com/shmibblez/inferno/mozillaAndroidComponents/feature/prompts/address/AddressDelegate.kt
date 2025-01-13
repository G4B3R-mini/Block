/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.address

import com.shmibblez.inferno.mozillaAndroidComponents.concept.storage.Address
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.concept.AutocompletePrompt

/**
 * Delegate for address picker
 */
interface AddressDelegate {
    /**
     * The [AutocompletePrompt] used for [AddressPicker] to display a
     * a prompt with a list of address options available for autocomplete.
     */
    val addressPickerView: AutocompletePrompt<Address>?

    /**
     * Callback invoked when the user clicks "Manage addresses" from
     * select address prompt.
     */
    val onManageAddresses: () -> Unit
}

/**
 * Default implementation for address picker delegate
 */
class DefaultAddressDelegate(
    override val addressPickerView: AutocompletePrompt<Address>? = null,
    override val onManageAddresses: () -> Unit = {},
) : AddressDelegate

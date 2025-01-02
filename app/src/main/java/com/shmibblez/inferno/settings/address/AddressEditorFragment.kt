/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.address

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import mozilla.components.support.ktx.android.view.hideKeyboard
import com.shmibblez.inferno.R
import com.shmibblez.inferno.SecureFragment
import com.shmibblez.inferno.databinding.FragmentAddressEditorBinding
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.showToolbar
import com.shmibblez.inferno.settings.address.controller.DefaultAddressEditorController
import com.shmibblez.inferno.settings.address.interactor.AddressEditorInteractor
import com.shmibblez.inferno.settings.address.interactor.DefaultAddressEditorInteractor
import com.shmibblez.inferno.settings.address.view.AddressEditorView

/**
 * Displays an address editor for adding and editing an address.
 */
class AddressEditorFragment : SecureFragment(R.layout.fragment_address_editor), MenuProvider {

    private lateinit var addressEditorView: AddressEditorView
    private lateinit var interactor: AddressEditorInteractor

    private val args by navArgs<AddressEditorFragmentArgs>()

    /**
     * Returns true if an existing address is being edited, and false otherwise.
     */
    private val isEditing: Boolean
        get() = args.address != null

    private lateinit var menu: Menu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val storage = requireContext().components.core.autofillStorage

        interactor = DefaultAddressEditorInteractor(
            controller = DefaultAddressEditorController(
                storage = storage,
                lifecycleScope = lifecycleScope,
                navController = findNavController(),
            ),
        )

        val binding = FragmentAddressEditorBinding.bind(view)

        val searchRegion = requireComponents.core.store.state.search.region
        addressEditorView = AddressEditorView(binding, interactor, searchRegion, args.address)
        addressEditorView.bind()
    }

    override fun onPause() {
        super.onPause()
        menu.close()
    }

    override fun onResume() {
        super.onResume()
        if (isEditing) {
            showToolbar(getString(R.string.addresses_edit_address))
        } else {
            showToolbar(getString(R.string.addresses_add_address))
        }
    }

    override fun onStop() {
        super.onStop()
        this.view?.hideKeyboard()
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.address_editor, menu)
        this.menu = menu

        menu.findItem(R.id.delete_address_button).isVisible = isEditing
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.delete_address_button -> {
            args.address?.let {
                addressEditorView.showConfirmDeleteAddressDialog(requireContext(), it.guid)
            }
            true
        }
        R.id.save_address_button -> {
            addressEditorView.saveAddress()
            true
        }
        else -> false
    }
}

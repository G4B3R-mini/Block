/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.creditcards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.lib.state.ext.consumeFrom
import com.shmibblez.inferno.R
import com.shmibblez.inferno.SecureFragment
import com.shmibblez.inferno.components.StoreProvider
import com.shmibblez.inferno.databinding.ComponentCreditCardsBinding
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.redirectToReAuth
import com.shmibblez.inferno.ext.showToolbar
import com.shmibblez.inferno.settings.autofill.AutofillAction
import com.shmibblez.inferno.settings.autofill.AutofillFragmentState
import com.shmibblez.inferno.settings.autofill.AutofillFragmentStore
import com.shmibblez.inferno.settings.creditcards.controller.DefaultCreditCardsManagementController
import com.shmibblez.inferno.settings.creditcards.interactor.CreditCardsManagementInteractor
import com.shmibblez.inferno.settings.creditcards.interactor.DefaultCreditCardsManagementInteractor
import com.shmibblez.inferno.settings.creditcards.view.CreditCardsManagementView

/**
 * Displays a list of saved credit cards.
 */
class CreditCardsManagementFragment : SecureFragment() {

    private lateinit var store: AutofillFragmentStore
    private lateinit var interactor: CreditCardsManagementInteractor
    private lateinit var creditCardsView: CreditCardsManagementView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(CreditCardsManagementView.LAYOUT_ID, container, false)

        store = StoreProvider.get(this) {
            AutofillFragmentStore(AutofillFragmentState())
        }

        interactor = DefaultCreditCardsManagementInteractor(
            controller = DefaultCreditCardsManagementController(
                navController = findNavController(),
            ),
        )
        val binding = ComponentCreditCardsBinding.bind(view)

        creditCardsView = CreditCardsManagementView(binding, interactor)

        loadCreditCards()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        consumeFrom(store) { state ->
            if (!state.isLoading && state.creditCards.isEmpty()) {
                findNavController().popBackStack()
                return@consumeFrom
            }

            creditCardsView.update(state)
        }
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.credit_cards_saved_cards))
    }

    /**
     * When the fragment is paused, navigate back to the settings page to reauthenticate.
     */
    override fun onPause() {
        // Don't redirect if the user is navigating to the credit card editor fragment.
        redirectToReAuth(
            listOf(R.id.creditCardEditorFragment),
            findNavController().currentDestination?.id,
            R.id.creditCardsManagementFragment,
        )

        super.onPause()
    }

    /**
     * Fetches all the credit cards from the autofill storage and updates the
     * [AutofillFragmentStore] with the list of credit cards.
     */
    private fun loadCreditCards() {
        lifecycleScope.launch(Dispatchers.IO) {
            val creditCards = requireContext().components.core.autofillStorage.getAllCreditCards()

            lifecycleScope.launch(Dispatchers.Main) {
                store.dispatch(AutofillAction.UpdateCreditCards(creditCards))
            }
        }
    }
}

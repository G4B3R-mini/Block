/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.exceptions.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import mozilla.components.lib.state.ext.consumeFrom
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.StoreProvider
import com.shmibblez.inferno.databinding.FragmentExceptionsBinding
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.showToolbar

/**
 * Displays a list of sites that are exempted from saving logins,
 * along with controls to remove the exception.
 */
class LoginExceptionsFragment : Fragment() {
    private lateinit var exceptionsStore: ExceptionsFragmentStore
    private lateinit var exceptionsView: LoginExceptionsView
    private lateinit var exceptionsInteractor: LoginExceptionsInteractor

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.preference_exceptions))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentExceptionsBinding.inflate(
            inflater,
            container,
            false,
        )
        exceptionsStore = StoreProvider.get(this) {
            ExceptionsFragmentStore(
                ExceptionsFragmentState(items = emptyList()),
            )
        }
        exceptionsInteractor = DefaultLoginExceptionsInteractor(
            ioScope = viewLifecycleOwner.lifecycleScope + Dispatchers.IO,
            loginExceptionStorage = requireComponents.core.loginExceptionStorage,
        )
        exceptionsView = LoginExceptionsView(
            binding.exceptionsLayout,
            exceptionsInteractor,
        )
        subscribeToLoginExceptions()
        return binding.root
    }

    private fun subscribeToLoginExceptions() {
        requireComponents.core.loginExceptionStorage.getLoginExceptions().asLiveData()
            .observe(viewLifecycleOwner) { exceptions ->
                exceptionsStore.dispatch(ExceptionsFragmentAction.Change(exceptions))
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        consumeFrom(exceptionsStore) {
            exceptionsView.update(it.items)
        }
    }
}

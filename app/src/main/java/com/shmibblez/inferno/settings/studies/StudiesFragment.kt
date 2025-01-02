/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.studies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.databinding.SettingsStudiesBinding
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.settings

/**
 * Lets the users control studies settings.
 */
class StudiesFragment : Fragment() {
    private var _binding: SettingsStudiesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val experiments = requireComponents.nimbus.sdk
        _binding = SettingsStudiesBinding.inflate(inflater, container, false)
        val interactor = DefaultStudiesInteractor((activity as HomeActivity), experiments)
        StudiesView(
            lifecycleScope,
            requireContext(),
            binding,
            interactor,
            requireContext().settings(),
            experiments,
            ::isAttached,
        ).bind()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isAttached(): Boolean = context != null
}

package com.shmibblez.inferno.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.shmibblez.inferno.R
import com.shmibblez.inferno.settings.nav.SettingsNavHost

/**
 * Settings Fragment, wrapper for compose [SettingsNavHost]
 */
class SettingsFragment : Fragment() {
    private val args by navArgs<SettingsFragmentArgs>()

    private val settingsNavHostComposeView: ComposeView
        get() = requireView().findViewById(R.id.settings_nav_host)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // todo: use args.preferenceToScrollTo
        settingsNavHostComposeView.setContent {
            SettingsNavHost(
                goBackLegacy = { findNavController().popBackStack() }
            )
        }
        super.onViewCreated(view, savedInstanceState)
    }

}
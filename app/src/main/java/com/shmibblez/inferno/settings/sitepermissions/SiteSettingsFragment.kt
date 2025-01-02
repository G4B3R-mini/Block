/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.sitepermissions

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import mozilla.components.browser.state.action.DefaultDesktopModeAction
import mozilla.telemetry.glean.private.NoExtras
import com.shmibblez.inferno.Config
import com.shmibblez.inferno.GleanMetrics.Autoplay
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.desktopmode.DefaultDesktopModeFeatureFlagImpl
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.getPreferenceKey
import com.shmibblez.inferno.ext.navigateWithBreadcrumb
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.ext.showToolbar
import com.shmibblez.inferno.settings.PhoneFeature
import com.shmibblez.inferno.settings.requirePreference

/**
 * Screen for managing settings related to site permissions and content defaults.
 */
@SuppressWarnings("TooManyFunctions")
class SiteSettingsFragment : PreferenceFragmentCompat() {

    private val defaultDesktopModeFeatureFlag by lazy { DefaultDesktopModeFeatureFlagImpl() }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.site_permissions_preferences, rootKey)

        val preferenceDescription = requirePreference<Preference>(R.string.pref_key_site_permissions_description)
        preferenceDescription.isVisible = Config.channel.isMozillaOnline

        // This should be setup in onCreatePreferences so we setup only once when the fragment is created
        bindDesktopMode()
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.preferences_site_settings))
        setupPreferences()
    }

    private fun setupPreferences() {
        bindCategoryPhoneFeatures()
        bindExceptions()
    }

    private fun bindDesktopMode() {
        if (defaultDesktopModeFeatureFlag.isDesktopModeEnabled()) {
            requirePreference<SwitchPreference>(R.string.pref_key_desktop_browsing).apply {
                icon?.setTint(ContextCompat.getColor(context, R.color.fx_mobile_icon_color_primary))
                isChecked = requireComponents.core.store.state.desktopMode
                isPersistent = false
                onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, _ ->
                        requireComponents.core.store.dispatch(DefaultDesktopModeAction.ToggleDesktopMode)
                        true
                    }
            }
        } else {
            // Remove the preference category if the feature is not enabled
            val preferenceScreen: PreferenceScreen =
                requirePreference(R.string.pref_key_site_permissions_preference_screen)
            val contentPreferenceCategory: PreferenceCategory = requirePreference(R.string.pref_key_category_content)
            preferenceScreen.removePreference(contentPreferenceCategory)
        }
    }

    private fun bindExceptions() {
        val keyExceptions = getPreferenceKey(R.string.pref_key_show_site_exceptions)
        val exceptionsCategory = requireNotNull(findPreference(keyExceptions))

        exceptionsCategory.onPreferenceClickListener = OnPreferenceClickListener {
            val directions = SiteSettingsFragmentDirections.actionSitePermissionsToExceptions()
            Navigation.findNavController(requireView()).navigate(directions)
            true
        }
    }

    private fun bindCategoryPhoneFeatures() {
        PhoneFeature.entries
            // Autoplay inaudible should be set in the same menu as autoplay audible, so it does
            // not need to be bound
            .filter { it != PhoneFeature.AUTOPLAY_INAUDIBLE }
            .forEach(::initPhoneFeature)
    }

    private fun initPhoneFeature(phoneFeature: PhoneFeature) {
        val context = requireContext()
        val settings = context.settings()

        val cameraPhoneFeatures = requirePreference<Preference>(phoneFeature.getPreferenceId())
        cameraPhoneFeatures.summary = phoneFeature.getActionLabel(context, settings = settings)

        cameraPhoneFeatures.onPreferenceClickListener = OnPreferenceClickListener {
            navigateToPhoneFeature(phoneFeature)
            true
        }
    }

    private fun navigateToPhoneFeature(phoneFeature: PhoneFeature) {
        val directions = SiteSettingsFragmentDirections
            .actionSitePermissionsToManagePhoneFeatures(phoneFeature)

        if (phoneFeature == PhoneFeature.AUTOPLAY_AUDIBLE) {
            Autoplay.visitedSetting.record(NoExtras())
        }
        context?.let {
            Navigation.findNavController(requireView()).navigateWithBreadcrumb(
                directions = directions,
                navigateFrom = "SitePermissionsFragment",
                navigateTo = "ActionSitePermissionsToManagePhoneFeatures",
                crashReporter = it.components.analytics.crashReporter,
            )
        }
    }
}

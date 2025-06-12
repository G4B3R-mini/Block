package com.shmibblez.inferno.browser.nav

import com.shmibblez.inferno.settings.SettingsFragmentDirections
import kotlinx.serialization.Serializable

interface BrowserRoute {

    @Serializable
    object InfernoBrowser : BrowserRoute

    @Serializable
    object History : BrowserRoute
    
    @Serializable
    object Settings {
        /**
         * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToAccountSettingsFragment]
         */
        @Serializable
        object AccountSettingsPage : BrowserRoute

        /**
         * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToAccountProblemFragment]
         */
        @Serializable
        object AccountProblemSettingsPage : BrowserRoute

        /**
         * todo: based on [SettingsFragmentDirections.actionSettingsFragmentToTurnOnSyncFragment]
         */
        @Serializable
        object TurnOnSyncSettingsPage : BrowserRoute

        @Serializable
        object ToolbarSettingsPage : BrowserRoute

        @Serializable
        object TabSettingsPage : BrowserRoute

        @Serializable
        object SearchSettingsPage : BrowserRoute

        @Serializable
        object ThemeSettingsPage : BrowserRoute

        @Serializable
        object ExtensionsSettingsPage : BrowserRoute {
            @Serializable
            object ExtensionSettingsPage : BrowserRoute {
                const val ADDON_KEY = "addon"
            }
        }

        @Serializable
        object GestureSettingsPage : BrowserRoute

        @Serializable
        object HomePageSettingsPage : BrowserRoute

        @Serializable
        object OnQuitSettingsPage : BrowserRoute

        @Serializable
        object PasswordSettingsPage : BrowserRoute {
            @Serializable
            object PasswordExceptionSettingsPage : BrowserRoute
        }

        @Serializable
        object AutofillSettingsPage : BrowserRoute

        @Serializable
        object SitePermissionsSettingsPage : BrowserRoute {
            @Serializable
            object SitePermissionsExceptionsSettingsPage : BrowserRoute
        }

        @Serializable
        object AccessibilitySettingsPage : BrowserRoute

        @Serializable
        object LocaleSettingsPage : BrowserRoute

        @Serializable
        object TranslationSettingsPage : BrowserRoute {
            @Serializable
            object AutomaticTranslationSettingsPage : BrowserRoute

            @Serializable
            object DownloadTranslationLanguagesSettingsPage : BrowserRoute

            @Serializable
            object TranslationExceptionsSettingsPage : BrowserRoute
        }

        @Serializable
        object PrivacyAndSecuritySettingsPage : BrowserRoute
    }
}
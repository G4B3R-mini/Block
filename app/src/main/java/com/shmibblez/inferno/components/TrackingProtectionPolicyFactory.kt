/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.components

import android.content.res.Resources
import androidx.annotation.VisibleForTesting
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy.CookiePolicy
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicyForSessionTypes
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.utils.Settings

/**
 * Handles the logic behind creating new [TrackingProtectionPolicy]s.
 */
class TrackingProtectionPolicyFactory(
    private val settings: Settings,
    private val resources: Resources,
) {

    /**
     * Constructs a [TrackingProtectionPolicy] based on current preferences.
     *
     * @param normalMode whether or not tracking protection should be enabled
     * in normal browsing mode, defaults to the current preference value.
     * @param privateMode whether or not tracking protection should be enabled
     * in private browsing mode, default to the current preference value.
     * @return the constructed tracking protection policy based on preferences.
     */
    @Suppress("ComplexMethod")
    fun createTrackingProtectionPolicy(
        normalMode: Boolean = settings.shouldUseTrackingProtection,
        privateMode: Boolean = settings.shouldUseTrackingProtection,
    ): TrackingProtectionPolicy {
        val trackingProtectionPolicy =
            when (settings.selectedTrackingProtection) {
                InfernoSettings.TrackingProtectionDefault.STRICT -> TrackingProtectionPolicy.strict()
                InfernoSettings.TrackingProtectionDefault.STANDARD -> TrackingProtectionPolicy.recommended()
                InfernoSettings.TrackingProtectionDefault.CUSTOM -> return createCustomTrackingProtectionPolicy()
                else -> TrackingProtectionPolicy.recommended()
            }

        return when {
            normalMode && privateMode -> trackingProtectionPolicy.applyTCPIfNeeded(settings)
            normalMode && !privateMode -> trackingProtectionPolicy.applyTCPIfNeeded(settings).forRegularSessionsOnly()
            !normalMode && privateMode -> trackingProtectionPolicy.applyTCPIfNeeded(settings).forPrivateSessionsOnly()
            else -> TrackingProtectionPolicy.none()
        }
    }

    private fun createCustomTrackingProtectionPolicy(): TrackingProtectionPolicy {
        return TrackingProtectionPolicy.select(
            cookiePolicy = getCustomCookiePolicy(),
            trackingCategories = getCustomTrackingCategories(),
            cookiePurging = getCustomCookiePurgingPolicy(),
            strictSocialTrackingProtection = settings.blockTrackingContentInCustomTrackingProtectionInNormalTabs || settings.blockTrackingContentInCustomTrackingProtectionInPrivateTabs,
        ).let {
            val blockInNormal = settings.blockTrackingContentInCustomTrackingProtectionInNormalTabs
            val blockInPrivate = settings.blockTrackingContentInCustomTrackingProtectionInPrivateTabs
            if (!blockInNormal && blockInPrivate) {
                it.forPrivateSessionsOnly()
            } else if(blockInNormal && !blockInPrivate) {
                it.forRegularSessionsOnly()
            } else {
                it
            }
        }
    }

    private fun getCustomCookiePolicy(): CookiePolicy {
         return if (!settings.blockCookiesInCustomTrackingProtection) {
            CookiePolicy.ACCEPT_ALL
        } else {
            when (settings.blockCookiesSelectionInCustomTrackingProtection) {
                InfernoSettings.CustomTrackingProtection.CookiePolicy.ISOLATE_CROSS_SITE_COOKIES -> CookiePolicy.ACCEPT_FIRST_PARTY_AND_ISOLATE_OTHERS
                InfernoSettings.CustomTrackingProtection.CookiePolicy.CROSS_SITE_AND_SOCIAL_MEDIA_TRACKERS -> CookiePolicy.ACCEPT_NON_TRACKERS
                InfernoSettings.CustomTrackingProtection.CookiePolicy.COOKIES_FROM_UNVISITED_SITES -> CookiePolicy.ACCEPT_VISITED
                InfernoSettings.CustomTrackingProtection.CookiePolicy.ALL_THIRD_PARTY_COOKIES -> CookiePolicy.ACCEPT_ONLY_FIRST_PARTY
                InfernoSettings.CustomTrackingProtection.CookiePolicy.ALL_COOKIES -> CookiePolicy.ACCEPT_NONE
                else -> CookiePolicy.ACCEPT_NONE
            }
        }
    }

    private fun getCustomTrackingCategories(): Array<TrackingProtectionPolicy.TrackingCategory> {
        val categories = arrayListOf(
            TrackingProtectionPolicy.TrackingCategory.AD,
            TrackingProtectionPolicy.TrackingCategory.ANALYTICS,
            TrackingProtectionPolicy.TrackingCategory.SOCIAL,
            TrackingProtectionPolicy.TrackingCategory.MOZILLA_SOCIAL,
        )

        if (settings.blockTrackingContentInCustomTrackingProtection) {
            categories.add(TrackingProtectionPolicy.TrackingCategory.SCRIPTS_AND_SUB_RESOURCES)
        }

        if (settings.blockFingerprintersInCustomTrackingProtection) {
            categories.add(TrackingProtectionPolicy.TrackingCategory.FINGERPRINTING)
        }

        if (settings.blockCryptominersInCustomTrackingProtection) {
            categories.add(TrackingProtectionPolicy.TrackingCategory.CRYPTOMINING)
        }

        return categories.toTypedArray()
    }

    private fun getCustomCookiePurgingPolicy(): Boolean {
        return settings.blockRedirectTrackersInCustomTrackingProtection
    }
}

@VisibleForTesting
internal fun TrackingProtectionPolicyForSessionTypes.applyTCPIfNeeded(
    settings: Settings,
): TrackingProtectionPolicyForSessionTypes {
    val updatedCookiePolicy = if (settings.enabledTotalCookieProtection) {
        CookiePolicy.ACCEPT_FIRST_PARTY_AND_ISOLATE_OTHERS
    } else {
        cookiePolicy
    }

    return TrackingProtectionPolicy.select(
        trackingCategories = trackingCategories,
        cookiePolicy = updatedCookiePolicy,
        strictSocialTrackingProtection = strictSocialTrackingProtection,
        cookiePurging = cookiePurging,
    )
}

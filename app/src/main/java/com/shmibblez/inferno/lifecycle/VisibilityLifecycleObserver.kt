/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.shmibblez.inferno.AuthenticationStatus
import com.shmibblez.inferno.BiometricAuthenticationManager

/**
 * [LifecycleObserver] to keep track of application visibility.
 */
class VisibilityLifecycleObserver : DefaultLifecycleObserver {
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)

        if (BiometricAuthenticationManager.biometricAuthenticationNeededInfo.authenticationStatus
            != AuthenticationStatus.AUTHENTICATION_IN_PROGRESS
        ) {
            BiometricAuthenticationManager.biometricAuthenticationNeededInfo.shouldShowAuthenticationPrompt =
                true
            BiometricAuthenticationManager.biometricAuthenticationNeededInfo.authenticationStatus =
                AuthenticationStatus.NOT_AUTHENTICATED
        }
    }
}

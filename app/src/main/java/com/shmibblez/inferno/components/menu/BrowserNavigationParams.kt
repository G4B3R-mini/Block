/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.components.menu

import com.shmibblez.inferno.settings.SupportUtils.SumoTopic

/**
 * InfernoBrowser navigation parameters of the URL or [SumoTopic] to be loaded.
 *
 * @property url The URL to be loaded.
 * @property sumoTopic The [SumoTopic] to be loaded.
 */
data class BrowserNavigationParams(
    val url: String? = null,
    val sumoTopic: SumoTopic? = null,
)

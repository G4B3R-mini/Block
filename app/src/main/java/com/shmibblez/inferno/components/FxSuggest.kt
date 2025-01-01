/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.components

import android.content.Context
import mozilla.appservices.remotesettings.RemoteSettingsServer
import mozilla.components.feature.fxsuggest.FxSuggestIngestionScheduler
import mozilla.components.feature.fxsuggest.FxSuggestStorage
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.perf.lazyMonitored

/**
 * Component group for Firefox Suggest.
 *
 * @param context The Android application context.
 */
class FxSuggest(context: Context) {
    val storage by lazyMonitored {
        FxSuggestStorage(
            context,
            remoteSettingsServer = if (context.settings().useProductionRemoteSettingsServer) {
                RemoteSettingsServer.Prod
            } else {
                RemoteSettingsServer.Stage
            },
        )
    }

    val ingestionScheduler by lazyMonitored {
        FxSuggestIngestionScheduler(context)
    }
}

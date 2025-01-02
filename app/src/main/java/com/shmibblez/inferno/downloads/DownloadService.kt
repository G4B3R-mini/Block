/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.downloads

import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.downloads.AbstractFetchDownloadService
import mozilla.components.support.base.android.NotificationsDelegate
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components

class DownloadService : AbstractFetchDownloadService() {
    override val httpClient by lazy { components.core.client }
    override val store: BrowserStore by lazy { components.core.store }
    override val style: Style by lazy { Style(R.color.fx_mobile_text_color_accent) }
    override val notificationsDelegate: NotificationsDelegate by lazy { components.notificationsDelegate }
}

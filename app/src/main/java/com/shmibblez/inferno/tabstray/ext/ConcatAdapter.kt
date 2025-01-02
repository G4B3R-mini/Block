/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.tabstray.ext

import androidx.recyclerview.widget.ConcatAdapter
import com.shmibblez.inferno.tabstray.browser.BrowserTabsAdapter
import com.shmibblez.inferno.tabstray.browser.InactiveTabsAdapter

/**
 * A convenience binding for retrieving the [BrowserTabsAdapter] from the [ConcatAdapter].
 */
internal val ConcatAdapter.browserAdapter
    get() = adapters.find { it is BrowserTabsAdapter } as BrowserTabsAdapter

/**
 * A convenience binding for retrieving the [InactiveTabsAdapter] from the [ConcatAdapter].
 */
internal val ConcatAdapter.inactiveTabsAdapter
    get() = adapters.find { it is InactiveTabsAdapter } as InactiveTabsAdapter

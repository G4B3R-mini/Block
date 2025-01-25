/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.library.history.state.bindings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import mozilla.components.lib.state.helpers.AbstractBinding
import com.shmibblez.inferno.library.history.HistoryFragmentState
import com.shmibblez.inferno.library.history.HistoryFragmentStore
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * A binding to map state updates to menu updates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuBinding(
    store: HistoryFragmentStore,
    val invalidateOptionsMenu: () -> Unit,
) : AbstractBinding<HistoryFragmentState>(store) {
    override suspend fun onState(flow: Flow<HistoryFragmentState>) {
        flow.distinctUntilChangedBy { it.mode }
            .collect { invalidateOptionsMenu() }
    }
}

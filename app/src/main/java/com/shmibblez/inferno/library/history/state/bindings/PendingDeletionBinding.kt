/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.library.history.state.bindings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import mozilla.components.lib.state.helpers.AbstractBinding
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppState
import com.shmibblez.inferno.library.history.HistoryView
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * A binding to map updates of history items that are binding deletion to the view.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PendingDeletionBinding(
    appStore: AppStore,
    private val view: HistoryView,
) : AbstractBinding<AppState>(appStore) {
    override suspend fun onState(flow: Flow<AppState>) {
        flow.distinctUntilChangedBy { it.pendingDeletionHistoryItems }
            .collect { view.update(it) }
    }
}

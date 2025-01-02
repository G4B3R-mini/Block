/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.bindings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mozilla.components.lib.state.helpers.AbstractBinding
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction.FindInPageAction
import com.shmibblez.inferno.components.appstate.AppState

/**
 * A binding for observing the [AppState.showFindInPage] state in the [AppStore] and displaying
 * the find in page feature.
 *
 * @param appStore The [AppStore] used to observe [AppState.showFindInPage].
 * @param onFindInPageLaunch Invoked when the find in page feature should be launched.
 */
class FindInPageBinding(
    private val appStore: AppStore,
    private val onFindInPageLaunch: () -> Unit,
) : AbstractBinding<AppState>(appStore) {

    override suspend fun onState(flow: Flow<AppState>) {
        flow.map { state -> state.showFindInPage }
            .distinctUntilChanged()
            .collect { showFindInPage ->
                if (showFindInPage) {
                    onFindInPageLaunch()
                    appStore.dispatch(FindInPageAction.FindInPageShown)
                }
            }
    }
}

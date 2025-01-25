/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mozilla.components.lib.state.helpers.AbstractBinding
import com.shmibblez.inferno.browser.readermode.ReaderModeController
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.components.appstate.AppState
import com.shmibblez.inferno.components.appstate.readerview.ReaderViewState.Active
import com.shmibblez.inferno.components.appstate.readerview.ReaderViewState.Dismiss
import com.shmibblez.inferno.components.appstate.readerview.ReaderViewState.None
import com.shmibblez.inferno.components.appstate.readerview.ReaderViewState.ShowControls
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * A binding for observing [AppState.readerViewState] in the [AppStore] and toggling the
 * reader view feature and controls.
 *
 * @param appStore The [AppStore] used to observe [AppState.isReaderViewActive].
 * @param readerMenuController The [ReaderModeController] that will used for toggling the reader
 * view feature and controls.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewBinding(
    private val appStore: AppStore,
    private val readerMenuController: ReaderModeController,
) : AbstractBinding<AppState>(appStore) {

    override suspend fun onState(flow: Flow<AppState>) {
        flow.map { state -> state.readerViewState }
            .distinctUntilChanged()
            .collect { state ->
                when (state) {
                    Active -> {
                        readerMenuController.showReaderView()
                        appStore.dispatch(AppAction.ReaderViewAction.Reset)
                    }

                    Dismiss -> {
                        readerMenuController.hideReaderView()
                        appStore.dispatch(AppAction.ReaderViewAction.Reset)
                    }

                    ShowControls -> {
                        readerMenuController.showControls()
                        appStore.dispatch(AppAction.ReaderViewAction.Reset)
                    }

                    None -> Unit
                }
            }
    }
}

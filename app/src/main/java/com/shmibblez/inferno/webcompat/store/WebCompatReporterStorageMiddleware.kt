/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.webcompat.store

import androidx.annotation.VisibleForTesting
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext
import mozilla.components.lib.state.Store
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction.WebCompatAction
import com.shmibblez.inferno.components.appstate.webcompat.WebCompatState
import com.shmibblez.inferno.webcompat.store.WebCompatReporterAction.WebCompatReporterStorageAction

/**
 * [Middleware] for handling side effects related to [WebCompatReporterStorageAction]s.
 *
 * @property appStore [AppStore] used to persist the [WebCompatState].
 */
class WebCompatReporterStorageMiddleware(
    val appStore: AppStore,
) : Middleware<WebCompatReporterState, WebCompatReporterAction> {

    override fun invoke(
        context: MiddlewareContext<WebCompatReporterState, WebCompatReporterAction>,
        next: (WebCompatReporterAction) -> Unit,
        action: WebCompatReporterAction,
    ) {
        next(action)

        when (action) {
            is WebCompatReporterStorageAction -> processStorageAction(store = context.store, action = action)
            else -> {} // no-op
        }
    }

    private fun processStorageAction(
        store: Store<WebCompatReporterState, WebCompatReporterAction>,
        action: WebCompatReporterStorageAction,
    ) {
        when (action) {
            is WebCompatReporterAction.Initialized -> {
                appStore.state.webCompatState?.let { state ->
                    val restoredState = state.toReporterState()

                    // Only restore the previous state if the tab URLs match
                    if (restoredState.tabUrl == store.state.tabUrl) {
                        store.dispatch(WebCompatReporterAction.StateRestored(restoredState = restoredState))
                    }
                }
            }
            WebCompatReporterAction.BackPressed,
            WebCompatReporterAction.SendMoreInfoClicked,
            -> appStore.dispatch(
                WebCompatAction.WebCompatStateUpdated(
                    newState = store.state.toPersistedState(),
                ),
            )
            WebCompatReporterAction.CancelClicked -> appStore.dispatch(WebCompatAction.WebCompatStateReset)
        }
    }
}

@VisibleForTesting
internal fun WebCompatState.toReporterState() = WebCompatReporterState(
    tabUrl = tabUrl,
    enteredUrl = enteredUrl,
    reason = reason?.let { WebCompatReporterState.BrokenSiteReason.valueOf(it) },
    problemDescription = problemDescription,
)

@VisibleForTesting
internal fun WebCompatReporterState.toPersistedState() = WebCompatState(
    tabUrl = tabUrl,
    enteredUrl = enteredUrl.ifEmpty { tabUrl }, // do not save the URL is the text field is empty
    reason = reason?.name,
    problemDescription = problemDescription,
)

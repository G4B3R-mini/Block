/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.webcompat.ui

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.components.lazyStore
import com.shmibblez.inferno.compose.ComposeFragment
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.theme.FirefoxTheme
import com.shmibblez.inferno.webcompat.WEB_COMPAT_REPORTER_URL
import com.shmibblez.inferno.webcompat.store.WebCompatReporterAction
import com.shmibblez.inferno.webcompat.store.WebCompatReporterNavigationMiddleware
import com.shmibblez.inferno.webcompat.store.WebCompatReporterState
import com.shmibblez.inferno.webcompat.store.WebCompatReporterStorageMiddleware
import com.shmibblez.inferno.webcompat.store.WebCompatReporterStore

/**
 * [Fragment] for displaying the WebCompat Reporter.
 */
class WebCompatReporterFragment : ComposeFragment() {

    private val args by navArgs<WebCompatReporterFragmentArgs>()

    private val webCompatReporterStore by lazyStore {
        WebCompatReporterStore(
            initialState = WebCompatReporterState(
                tabUrl = args.tabUrl,
                enteredUrl = args.tabUrl,
            ),
            middleware = listOf(
                WebCompatReporterStorageMiddleware(
                    appStore = requireComponents.appStore,
                ),
                WebCompatReporterNavigationMiddleware(),
            ),
        )
    }

    @Composable
    override fun UI() {
        FirefoxTheme {
            WebCompatReporter(
                store = webCompatReporterStore,
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNavigationEvents()
    }

    private fun observeNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                webCompatReporterStore.navEvents.collect { navEvent ->
                    when (navEvent) {
                        is WebCompatReporterAction.SendMoreInfoClicked -> {
                            (activity as HomeActivity).openToBrowserAndLoad(
                                searchTermOrURL = "$WEB_COMPAT_REPORTER_URL${webCompatReporterStore.state.enteredUrl}",
                                newTab = true,
                                from = BrowserDirection.FromWebCompatReporterFragment,
                            )
                        }
                        is WebCompatReporterAction.SendReportClicked -> {
                            val directions = WebCompatReporterFragmentDirections.actionGlobalBrowser()
                            findNavController().navigate(directions)
                        }
                        is WebCompatReporterAction.CancelClicked -> {
                            val directions = WebCompatReporterFragmentDirections.actionGlobalBrowser()
                            findNavController().navigate(directions)
                        }
                        is WebCompatReporterAction.BackPressed ->
                            findNavController().popBackStack()
                    }
                }
            }
        }
    }
}

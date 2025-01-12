/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import mozilla.components.browser.state.search.RegionState
import mozilla.components.lib.state.ext.observeAsState
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.components
import com.shmibblez.inferno.ext.showToolbar
import com.shmibblez.inferno.theme.FirefoxTheme

class SecretDebugSettingsFragment : Fragment() {

    override fun onResume() {
        super.onResume()

        showToolbar(getString(R.string.preferences_debug_info))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FirefoxTheme {
                    SecretDebugSettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun SecretDebugSettingsScreen() {
    val regionState: RegionState by components.core.store.observeAsState(
        initialValue = RegionState.Default,
        map = { it.search.region ?: RegionState.Default },
    )

    DebugInfo(regionState = regionState)
}

@Composable
private fun DebugInfo(regionState: RegionState) {
    Column(
        modifier = Modifier
            .padding(8.dp),
    ) {
        Text(
            text = stringResource(R.string.debug_info_region_home),
            color = FirefoxTheme.colors.textPrimary,
            style = FirefoxTheme.typography.headline6,
            modifier = Modifier.padding(4.dp),
        )
        Text(
            text = regionState.home,
            color = FirefoxTheme.colors.textPrimary,
            modifier = Modifier.padding(4.dp),
        )
        Text(
            text = stringResource(R.string.debug_info_region_current),
            color = FirefoxTheme.colors.textPrimary,
            style = FirefoxTheme.typography.headline6,
            modifier = Modifier.padding(4.dp),
        )
        Text(
            text = regionState.current,
            color = FirefoxTheme.colors.textPrimary,
            modifier = Modifier.padding(4.dp),
        )
    }
}

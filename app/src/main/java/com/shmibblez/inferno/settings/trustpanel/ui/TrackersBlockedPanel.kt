/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.trustpanel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.Divider
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.annotation.LightDarkPreview
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.menu.compose.MenuGroup
import com.shmibblez.inferno.components.menu.compose.MenuItem
import com.shmibblez.inferno.components.menu.compose.MenuScaffold
import com.shmibblez.inferno.components.menu.compose.header.SubmenuHeader
import com.shmibblez.inferno.theme.FirefoxTheme

internal const val TRACKERS_PANEL_ROUTE = "trackers_panel"

@Composable
internal fun TrackersBlockedPanel(
    title: String,
    onBackButtonClick: () -> Unit,
) {
    MenuScaffold(
        header = {
            SubmenuHeader(
                header = title,
                onClick = onBackButtonClick,
            )
        },
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Trackers blocked: 5",
                    modifier = Modifier.weight(1f),
                    color = FirefoxTheme.colors.textAccent,
                    style = FirefoxTheme.typography.headline8,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            MenuGroup {
                MenuItem(
                    label = "3 Cross-site tracking cookies",
                    beforeIconPainter = painterResource(id = R.drawable.mozac_ic_cookies_24),
                    onClick = {},
                )

                Divider(color = FirefoxTheme.colors.borderSecondary)

                MenuItem(
                    label = "1 Social media tracker",
                    beforeIconPainter = painterResource(id = R.drawable.mozac_ic_social_tracker_24),
                    onClick = {},
                )

                Divider(color = FirefoxTheme.colors.borderSecondary)

                MenuItem(
                    label = "1 Fingerprinters",
                    beforeIconPainter = painterResource(id = R.drawable.mozac_ic_fingerprinter_24),
                    onClick = {},
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun TrackersBlockedPanelPreview() {
    FirefoxTheme {
        Column(
            modifier = Modifier
                .background(color = FirefoxTheme.colors.layer3),
        ) {
            TrackersBlockedPanel(
                title = "Mozilla",
                onBackButtonClick = {},
            )
        }
    }
}

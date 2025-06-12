/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.appstate.AppAction
import com.shmibblez.inferno.compose.PlaceholderCard
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.button.PrimaryButton
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.home.fake.FakeHomepagePreview
import com.shmibblez.inferno.home.sessioncontrol.CollectionInteractor
import com.shmibblez.inferno.home.sessioncontrol.DefaultSessionControlController
import com.shmibblez.inferno.theme.FirefoxTheme

@Composable
internal fun CollectionsPlaceholder(
    showAddTabsToCollection: Boolean,
    interactor: CollectionInteractor,
) {
    PlaceholderCard(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.collections_header),
                    color = FirefoxTheme.colors.textPrimary,
                    style = FirefoxTheme.typography.headline7,
                )

                IconButton(
                    onClick = interactor::onRemoveCollectionsPlaceholder,
                    modifier = Modifier.size(20.dp),
                ) {
                    InfernoIcon(
                        painter = painterResource(R.drawable.mozac_ic_cross_20),
                        contentDescription = stringResource(
                            R.string.remove_home_collection_placeholder_content_description,
                        ),
                        tint = FirefoxTheme.colors.textPrimary,
                    )
                }
            }
        },
        description = {
            Text(
                text = stringResource(R.string.no_collections_description2),
                color = FirefoxTheme.colors.textSecondary,
                style = FirefoxTheme.typography.body2,
            )

            if (showAddTabsToCollection) {
                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = stringResource(R.string.tabs_menu_save_to_collection1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    icon = painterResource(R.drawable.ic_tab_collection),
                    onClick = interactor::onAddTabsToCollectionTapped,
                )
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun CollectionsPlaceholderPreview() {
    FirefoxTheme {
        Surface(color = FirefoxTheme.colors.layer1) {
            Column(modifier = Modifier.padding(16.dp)) {
                CollectionsPlaceholder(
                    interactor = FakeHomepagePreview.collectionInteractor,
                    showAddTabsToCollection = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                CollectionsPlaceholder(
                    interactor = FakeHomepagePreview.collectionInteractor,
                    showAddTabsToCollection = false,
                )
            }
        }
    }
}

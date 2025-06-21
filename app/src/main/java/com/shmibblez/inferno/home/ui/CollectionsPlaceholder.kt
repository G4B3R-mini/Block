/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.PlaceholderCard
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.compose.button.PrimaryButton
import com.shmibblez.inferno.home.sessioncontrol.CollectionInteractor

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
                InfernoText(
                    text = stringResource(R.string.collections_header),
//                    style = FirefoxTheme.typography.headline7,
                    infernoStyle = InfernoTextStyle.Title,
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
                    )
                }
            }
        },
        description = {
            InfernoText(
                text = stringResource(R.string.no_collections_description2),
                infernoStyle = InfernoTextStyle.SmallSecondary,
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

//@PreviewLightDark
//@Composable
//private fun CollectionsPlaceholderPreview() {
//    FirefoxTheme {
//        Surface(color = FirefoxTheme.colors.layer1) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                CollectionsPlaceholder(
//                    interactor = FakeHomepagePreview.collectionInteractor,
//                    showAddTabsToCollection = true,
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                CollectionsPlaceholder(
//                    interactor = FakeHomepagePreview.collectionInteractor,
//                    showAddTabsToCollection = false,
//                )
//            }
//        }
//    }
//}

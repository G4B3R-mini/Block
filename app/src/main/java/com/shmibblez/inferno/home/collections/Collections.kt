/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.MenuItem
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.home.sessioncontrol.CollectionInteractor
import mozilla.components.feature.tab.collections.TabCollection

/**
 * List of expandable collections.
 *
 * @param modifier for the UI.
 * @param collections List of [TabCollection] to display.
 * @param expandedCollections List of ids corresponding to [TabCollection]s which are currently expanded.
 * @param showAddTabToCollection Whether to show the "Add tab" menu item in the collections menu.
 */
@Composable
fun Collections(
    modifier: Modifier = Modifier,
    collections: List<TabCollection>,
    expandedCollections: Set<Long> = emptySet(),
    showAddTabToCollection: Boolean,
    interactor: CollectionInteractor,
) {
    Column(modifier = modifier) {
        for (collection in collections) {
            Spacer(Modifier.height(12.dp))

            Collection(
                collection = collection,
                expanded = expandedCollections.contains(collection.id),
                menuItems = getMenuItems(
                    collection = collection,
                    showAddTabs = showAddTabToCollection,
                    onOpenTabsTapped = interactor::onCollectionOpenTabsTapped,
                    onRenameCollectionTapped = interactor::onRenameCollectionTapped,
                    onAddTabTapped = interactor::onCollectionAddTabTapped,
                    onDeleteCollectionTapped = interactor::onDeleteCollectionTapped,
                ),
                onToggleCollectionExpanded = interactor::onToggleCollectionExpanded,
                onCollectionShareTabsClicked = interactor::onCollectionShareTabsClicked,
            )

            if (expandedCollections.contains(collection.id)) {
                val lastId = collection.tabs.last().id

                for (tab in collection.tabs) {
                    CollectionItem(
                        tab = tab,
                        isLastInCollection = tab.id == lastId,
                        onClick = { interactor.onCollectionOpenTabClicked(tab) },
                        onRemove = {
                            interactor.onCollectionRemoveTab(
                                collection = collection,
                                tab = tab,
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * Constructs and returns the default list of menu options for a [TabCollection].
 *
 * @param collection [TabCollection] for which the menu will be shown.
 * Might serve as an argument for the callbacks for when the user interacts with certain menu options.
 * @param showAddTabs Whether to show the option to add a currently open tab to the [collection].
 * @param onOpenTabsTapped Invoked when the user chooses to open the tabs from [collection].
 * @param onRenameCollectionTapped Invoked when the user chooses to rename the [collection].
 * @param onAddTabTapped Invoked when the user chooses to add tabs to [collection].
 * @param onDeleteCollectionTapped Invoked when the user chooses to delete [collection].
 */
@Composable
private fun getMenuItems(
    collection: TabCollection,
    showAddTabs: Boolean,
    onOpenTabsTapped: (TabCollection) -> Unit,
    onRenameCollectionTapped: (TabCollection) -> Unit,
    onAddTabTapped: (TabCollection) -> Unit,
    onDeleteCollectionTapped: (TabCollection) -> Unit,
): List<MenuItem> {
    return listOfNotNull(
        MenuItem(
            title = stringResource(R.string.collection_open_tabs),
            color = LocalContext.current.infernoTheme().value.primaryTextColor,
        ) {
            onOpenTabsTapped(collection)
        },
        MenuItem(
            title = stringResource(R.string.collection_rename),
            color = LocalContext.current.infernoTheme().value.primaryTextColor,
        ) {
            onRenameCollectionTapped(collection)
        },

        if (showAddTabs) {
            MenuItem(
                title = stringResource(R.string.add_tab),
                color = LocalContext.current.infernoTheme().value.primaryTextColor,
            ) {
                onAddTabTapped(collection)
            }
        } else {
            null
        },

        MenuItem(
            title = stringResource(R.string.collection_delete),
            color = LocalContext.current.infernoTheme().value.errorColor,
        ) {
            onDeleteCollectionTapped(collection)
        },
    )
}

//@LightDarkPreview
//@Composable
//private fun CollectionsPreview() {
//    val expandedCollections: MutableState<Set<Long>> = remember { mutableStateOf(setOf(1L)) }
//
//    FirefoxTheme {
//        Surface(color = FirefoxTheme.colors.layer1) {
//            Collections(
//                modifier = Modifier.padding(8.dp),
//                collections = listOf(
//                    FakeHomepagePreview.collection(
//                        tabs = listOf(
//                            FakeHomepagePreview.tab(),
//                            FakeHomepagePreview.tab(),
//                            FakeHomepagePreview.tab(),
//                        ),
//                    ),
//                    FakeHomepagePreview.collection(
//                        tabs = listOf(
//                            FakeHomepagePreview.tab(),
//                            FakeHomepagePreview.tab(),
//                        ),
//                    ),
//                ),
//                showAddTabToCollection = true,
//                expandedCollections = expandedCollections.value,
//                interactor = object : CollectionInteractor {
//                    override fun onCollectionAddTabTapped(collection: TabCollection) { /* no op */ }
//
//                    override fun onCollectionOpenTabClicked(tab: Tab) { /* no op */ }
//
//                    override fun onCollectionOpenTabsTapped(collection: TabCollection) { /* no op */ }
//
//                    override fun onCollectionRemoveTab(
//                        collection: TabCollection,
//                        tab: Tab,
//                    ) { /* no op */ }
//
//                    override fun onCollectionShareTabsClicked(collection: TabCollection) { /* no op */ }
//
//                    override fun onDeleteCollectionTapped(collection: TabCollection) { /* no op */ }
//
//                    override fun onRenameCollectionTapped(collection: TabCollection) { /* no op */ }
//
//                    override fun onToggleCollectionExpanded(
//                        collection: TabCollection,
//                        expand: Boolean,
//                    ) {
//                        expandedCollections.value = if (expand) {
//                            setOf(1L)
//                        } else {
//                            setOf()
//                        }
//                    }
//
//                    override fun onAddTabsToCollectionTapped() { /* no op */ }
//
//                    override fun onRemoveCollectionsPlaceholder() { /* no op */ }
//                },
//            )
//        }
//    }
//}

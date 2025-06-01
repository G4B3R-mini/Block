package com.shmibblez.inferno.settings.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.toolbar.allToolbarItemsNoMiniOrigin
import com.shmibblez.inferno.toolbar.defaultToolbarItems
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ToolbarSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    fun getSelectedToolbarItems(): List<InfernoSettings.ToolbarItem> {
        val items = settings.toolbarItemsList.distinct()
        // if list is empty it means items have not been set yet, use default
        if (items.isEmpty()) return (null as InfernoSettings.ToolbarItem?).defaultToolbarItems
        return items
    }

    var selectedToolbarItems by remember { mutableStateOf(getSelectedToolbarItems()) }

//    fun shouldUseMiniOrigin(): Boolean {
//        return selectedToolbarItems.contains(InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI)
//    }
//
//    var useMiniOrigin by remember { mutableStateOf(shouldUseMiniOrigin()) }

    fun getRemainingToolbarItems(): List<InfernoSettings.ToolbarItem> {
//        val items = when (useMiniOrigin) {
//            true -> (null as InfernoSettings.ToolbarItem?).allToolbarItemsNoOrigin
//            false -> (null as InfernoSettings.ToolbarItem?).allToolbarItemsNoMiniOrigin
//        }
        return (null as InfernoSettings.ToolbarItem?).allToolbarItemsNoMiniOrigin - selectedToolbarItems.toSet()
    }

    var remainingToolbarItems by remember { mutableStateOf(getRemainingToolbarItems()) }

    LaunchedEffect(settings.toolbarItemsList) {
        // called in this order specifically
        selectedToolbarItems = getSelectedToolbarItems()
//        useMiniOrigin = shouldUseMiniOrigin()
        remainingToolbarItems = getRemainingToolbarItems()
        Log.d(
            "PreferenceToolbarItems",
            "selectedItems changed\nselectedItems: $selectedToolbarItems\nremainingItems: $remainingToolbarItems"
        )
    }

    InfernoSettingsPage(
        title = "Toolbar Settings", // todo: string res
        goBack = goBack,
    ) { edgeInsets ->
        val topPreferences = listOf<Pair<Any,@Composable () -> Unit>>(
            "General" to { PreferenceTitle(stringResource(R.string.preferences_category_general)) },
            "Toolbar Position" to {
                // toolbar vertical position
                PreferenceSelect(
                    text = "Toolbar location:", // todo: string res
                    description = null,
                    enabled = true,
                    selectedMenuItem = settings.toolbarVerticalPosition,
                    menuItems = listOf(
                        InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM,
                        InfernoSettings.VerticalToolbarPosition.TOOLBAR_TOP,
                    ),
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        MainScope().launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setToolbarVerticalPosition(selected).build()
                            }
                        }
                    },
                )
            },
            "Toolbar Location" to {
                // in app toolbar vertical position
                PreferenceSelect(
                    text = "In app toolbar location:", // todo: string res
                    description = "Position of toolbar if browser opened inside another app.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.inAppToolbarVerticalPosition,
                    menuItems = listOf(
                        InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM,
                        InfernoSettings.VerticalToolbarPosition.TOOLBAR_TOP,
                    ),
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        MainScope().launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setInAppToolbarVerticalPosition(selected).build()
                            }
                        }
                    },
                )
            },
            "Mini Origin Enabled" to {
                // todo: mini origin (enable / disable pref by adding mini origin or origin to toolbar item list)
            },
            "Toolbar Items Title" to { PreferenceTitle("Toolbar Items") }, // todo: string res
        )
        // selected toolbar items
        PreferenceToolbarItems(
            modifier = Modifier.padding(edgeInsets),
            topPreferences = topPreferences,
            initiallySelectedItems = selectedToolbarItems,
            initiallyRemainingItems = remainingToolbarItems,
            onSelectedItemsChanged = { newToolbarItems ->
                Log.d("ToolbarSettingsPage", "onSelectedItemsChanged: new items: $newToolbarItems")
                coroutineScope.launch {
                    context.infernoSettingsDataStore.updateData {
                        // remove duplicates
                        val verifiedList = newToolbarItems.distinct().toMutableList()
                        // if doesnt contain origin, add
                        if (!verifiedList.contains(InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN)) {
                            verifiedList.add(InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN)
                        }
                        // if doesnt contain menu, add
                        if (!verifiedList.contains(InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU)) {
                            verifiedList.add(InfernoSettings.ToolbarItem.TOOLBAR_ITEM_MENU)
                        }
                        it.toBuilder().apply {
                            this.clearToolbarItems()
                            this.addAllToolbarItems(verifiedList)
                        }.build()
                    }
                }
            },
        )
    }
}

fun InfernoSettings.VerticalToolbarPosition.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM -> context.getString(R.string.preference_bottom_toolbar)
        InfernoSettings.VerticalToolbarPosition.TOOLBAR_TOP -> context.getString(R.string.preference_top_toolbar)
    }
}

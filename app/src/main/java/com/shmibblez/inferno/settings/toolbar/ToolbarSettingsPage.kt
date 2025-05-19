package com.shmibblez.inferno.settings.toolbar

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.toolbar.allToolbarItemsNoMiniOrigin
import com.shmibblez.inferno.toolbar.allToolbarItemsNoOrigin
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ToolbarSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())
    var selectedToolbarItems by remember {
        mutableStateOf<List<InfernoSettings.ToolbarItem>>(
            emptyList()
        )
    }
    var remainingToolbarItems by remember {
        mutableStateOf<List<InfernoSettings.ToolbarItem>>(
            emptyList()
        )
    }
    var useMiniOrigin by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect (settings.toolbarItemsList) {
        selectedToolbarItems = settings.toolbarItemsList
        useMiniOrigin = selectedToolbarItems.contains(InfernoSettings.ToolbarItem.TOOLBAR_ITEM_ORIGIN_MINI)
        remainingToolbarItems = when (useMiniOrigin!!) {
            true -> (null as InfernoSettings.ToolbarItem?).allToolbarItemsNoOrigin
            false -> (null as InfernoSettings.ToolbarItem?).allToolbarItemsNoMiniOrigin
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_back_button),
                        contentDescription = stringResource(R.string.browser_menu_back),
                        modifier = Modifier.clickable(onClick = goBack),
                    )
                },
                title = { InfernoText("Toolbar Settings") }, // todo: string res
            )
        },
    ) {
        val topPreferences = listOf<@Composable () -> Unit>(
            { PreferenceTitle(stringResource(R.string.preferences_category_general)) },
            {
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
            {
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
            {
                // todo: mini origin (enable / disable pref by adding mini origin or origin to toolbar item list)
            },
            { PreferenceTitle("Toolbar Items") }, // todo: string res
            )
        // selected toolbar items
        PreferenceToolbarItems(
            topPreferences = topPreferences,
            selectedItems = selectedToolbarItems,
            remainingItems = remainingToolbarItems,
            onSelectedItemsChanged = { newToolbarItems ->
                coroutineScope.launch {
                    context.infernoSettingsDataStore.updateData {
                        it.toBuilder().apply {
                            this.toolbarItemsList.clear()
                            this.toolbarItemsList.addAll(newToolbarItems)
                        }.build()
                    }
                }
            }
        )
    }
}

fun InfernoSettings.VerticalToolbarPosition.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM -> context.getString(R.string.preference_bottom_toolbar)
        InfernoSettings.VerticalToolbarPosition.TOOLBAR_TOP -> context.getString(R.string.preference_top_toolbar)
        InfernoSettings.VerticalToolbarPosition.UNRECOGNIZED -> context.getString(R.string.preference_bottom_toolbar)
    }
}

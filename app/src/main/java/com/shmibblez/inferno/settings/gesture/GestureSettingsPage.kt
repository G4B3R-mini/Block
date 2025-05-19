package com.shmibblez.inferno.settings.gesture

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.settings.toolbar.toPrefString
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_button),
                        contentDescription = stringResource(R.string.browser_menu_back),
                        modifier = Modifier.clickable(onClick = goBack),
                        tint = Color.White, // todo: theme
                    )
                },
                title = { InfernoText("Gesture Settings") }, // todo: string res
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {

            // general title
            item { PreferenceTitle(stringResource(R.string.preferences_category_general)) }

            // pull to refresh enabled
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preference_gestures_website_pull_to_refresh),
                    summary = null,
                    selected = settings.isPullToRefreshEnabled,
                    onSelectedChange = { isPullToRefreshEnabled ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setIsPullToRefreshEnabled(isPullToRefreshEnabled)
                                    .build()
                            }
                        }
                    },
                )
            }

            // dynamic toolbar enabled
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preference_gestures_dynamic_toolbar),
                    summary = null,
                    selected = settings.isDynamicToolbarEnabled,
                    onSelectedChange = { isDynamicToolbarEnabled ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setIsDynamicToolbarEnabled(isDynamicToolbarEnabled)
                                    .build()
                            }
                        }
                    },
                )
            }

            // toolbar gesture swipe left
            item {
                // todo: generalize component to clean up code for this page since lots of gestures
                //  - title / text
                //  - selected
                //  - update settings
                //  - everything else the same for all
                PreferenceSelect(
                    text = "Toolbar action on swipe left:", // todo: string res
                    description = "Defines what happens when you swipe left on the toolbar.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.toolbarGestureSwipeLeft,
                    menuItems = listOf(
                        InfernoSettings.GestureAction.GESTURE_ACTION_NONE,
                        InfernoSettings.GestureAction.GESTURE_ACTION_TAB_LEFT,
                        InfernoSettings.GestureAction.GESTURE_ACTION_TAB_RIGHT,
                        InfernoSettings.GestureAction.GESTURE_ACTION_RELOAD_TAB,
                        InfernoSettings.GestureAction.GESTURE_ACTION_PAGE_FORWARD,
                        InfernoSettings.GestureAction.GESTURE_ACTION_PAGE_BACK,
                        InfernoSettings.GestureAction.GESTURE_ACTION_CLOSE_TAB,
                        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_TO_RIGHT,
                        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_TO_LEFT,
                        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_START,
                        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_END,
                        InfernoSettings.GestureAction.UNRECOGNIZED,
                    ),
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        MainScope().launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setToolbarGestureSwipeLeft(selected).build()
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GestureSettingSelect(
    selectedItem: InfernoSettings.GestureAction,
    onSelectMenuItem: (InfernoSettings.GestureAction) -> Unit,
    // todo: remaining params
) {
    // todo: copy paste select pref here to generalize
}

fun InfernoSettings.GestureAction.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.GestureAction.GESTURE_ACTION_NONE -> "Do nothing" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_TAB_LEFT -> "Switch to tab on the left" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_TAB_RIGHT -> "Switch to tab on the right" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_RELOAD_TAB -> "Reload current tab" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_PAGE_FORWARD -> "Go to next page in this tab" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_PAGE_BACK -> "Go back to previous page in this tab" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_CLOSE_TAB -> "Close current tab" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_TO_RIGHT -> "Open new tab to the right" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_TO_LEFT -> "Open new tab to the left" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_START -> "Open new tab at start of tab list" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_END -> "Open new tab at end of tab list" // todo: string res
        InfernoSettings.GestureAction.UNRECOGNIZED -> ""
    }
}
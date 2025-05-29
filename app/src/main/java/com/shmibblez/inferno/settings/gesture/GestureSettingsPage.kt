package com.shmibblez.inferno.settings.gesture

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GestureSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    val gestureOptions = remember {
        listOf(
            InfernoSettings.GestureAction.GESTURE_ACTION_NONE,
            InfernoSettings.GestureAction.GESTURE_ACTION_TAB_RIGHT,
            InfernoSettings.GestureAction.GESTURE_ACTION_TAB_LEFT,
            InfernoSettings.GestureAction.GESTURE_ACTION_RELOAD_TAB,
            InfernoSettings.GestureAction.GESTURE_ACTION_PAGE_FORWARD,
            InfernoSettings.GestureAction.GESTURE_ACTION_PAGE_BACK,
            InfernoSettings.GestureAction.GESTURE_ACTION_CLOSE_TAB,
            InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_TO_RIGHT,
            InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_TO_LEFT,
            InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_END,
            InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_START,
        )
    }

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_gestures),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {

            // general title
            item { PreferenceTitle(stringResource(R.string.preferences_category_general)) }

            // pull to refresh enabled
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preference_gestures_website_pull_to_refresh), // todo: string res more descriptive
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
                    text = stringResource(R.string.preference_gestures_dynamic_toolbar), // todo: string res more descriptive (mention tab bar if enabled)
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

            // toolbar gestures section
            item { PreferenceTitle("Toolbar Gestures") } // todo: string res

            // toolbar swipe left
            item {
                PreferenceSelect(
                    text = "Toolbar action on swipe left:", // todo: string res
                    description = "Defines what happens when you swipe left on the toolbar.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.toolbarGestureSwipeLeft,
                    menuItems = gestureOptions,
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

            // toolbar swipe right
            item {
                PreferenceSelect(
                    text = "Toolbar action on swipe right:", // todo: string res
                    description = "Defines what happens when you swipe right on the toolbar.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.toolbarGestureSwipeRight,
                    menuItems = gestureOptions,
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        MainScope().launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setToolbarGestureSwipeRight(selected).build()
                            }
                        }
                    },
                )
            }

            // toolbar swipe up
            item {
                PreferenceSelect(
                    text = "Toolbar action on swipe up:", // todo: string res
                    description = "Defines what happens when you swipe up on the toolbar.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.toolbarGestureSwipeUp,
                    menuItems = gestureOptions,
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        MainScope().launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setToolbarGestureSwipeUp(selected).build()
                            }
                        }
                    },
                )
            }

            // toolbar swipe down
            item {
                PreferenceSelect(
                    text = "Toolbar action on swipe down:", // todo: string res
                    description = "Defines what happens when you swipe down on the toolbar.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.toolbarGestureSwipeDown,
                    menuItems = gestureOptions,
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        MainScope().launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setToolbarGestureSwipeDown(selected).build()
                            }
                        }
                    },
                )
            }

            // tab bar gestures section
            item { PreferenceTitle("Tab Bar Gestures") } // todo: string res

            // tab bar swipe up
            item {
                PreferenceSelect(
                    text = "Tab action on swipe up:", // todo: string res
                    description = "Defines what happens when you swipe up on the active tab.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.tabBarGestureSwipeUp,
                    menuItems = gestureOptions,
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        MainScope().launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setTabBarGestureSwipeUp(selected).build()
                            }
                        }
                    },
                )
            }

            // tab bar swipe down
            item {
                PreferenceSelect(
                    text = "Tab action on swipe down:", // todo: string res
                    description = "Defines what happens when you swipe down on the active tab.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.tabBarGestureSwipeDown,
                    menuItems = gestureOptions,
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        MainScope().launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setTabBarGestureSwipeDown(selected).build()
                            }
                        }
                    },
                )
            }
        }
    }
}

fun InfernoSettings.GestureAction.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.GestureAction.GESTURE_ACTION_NONE -> "Do nothing" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_TAB_RIGHT -> "Switch to tab on the right" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_TAB_LEFT -> "Switch to tab on the left" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_RELOAD_TAB -> "Reload current tab" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_PAGE_FORWARD -> "Go to next page in this tab" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_PAGE_BACK -> "Go back to previous page in this tab" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_CLOSE_TAB -> "Close current tab" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_TO_RIGHT -> "Open new tab to the right" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_TO_LEFT -> "Open new tab to the left" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_END -> "Open new tab at end of tab list" // todo: string res
        InfernoSettings.GestureAction.GESTURE_ACTION_NEW_TAB_START -> "Open new tab at start of tab list" // todo: string res
    }
}
package com.shmibblez.inferno.settings.tabs

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSettingsPage(goBack: () -> Unit) {
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
                title = { InfernoText("Toolbar Settings") }, // todo: string res
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // general title
            PreferenceTitle(stringResource(R.string.preferences_category_general))

            // close tabs method
            PreferenceSelect(
                text = stringResource(R.string.preferences_close_tabs),
                description = "Whether to close tabs manually or automatically after a certain period of time", // todo: string res
                enabled = true,
                selectedMenuItem = settings.closeTabsMethod,
                menuItems = listOf(
                    InfernoSettings.CloseTabsMethod.CLOSE_TABS_MANUALLY,
                    InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_DAY,
                    InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_WEEK,
                    InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_MONTH,
                ),
                mapToTitle = { it.toPrefString(context) },
                onSelectMenuItem = { closeTabsMethod ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setCloseTabsMethod(closeTabsMethod).build()
                        }
                    }
                },
            )

            // separate inactive tabs
            PreferenceSwitch(text = stringResource(R.string.preferences_inactive_tabs),
                summary = stringResource(R.string.preferences_inactive_tabs_title),
                selected = settings.shouldSeparateInactiveTabs,
                onSelectedChange = { shouldSeparateInactiveTabs ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setShouldSeparateInactiveTabs(shouldSeparateInactiveTabs)
                                .build()
                        }
                    }
                })

            // tab bar title
            PreferenceTitle("Tab Bar Settings") // todo: string res

            // enable tab bar
            PreferenceSwitch(
                text = "Enable tab bar", // todo: string res
                summary = "Whether to show the tab bar in the browser.", // todo: string res
                selected = settings.isTabBarEnabled,
                onSelectedChange = { isTabBarEnabled ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setIsTabBarEnabled(isTabBarEnabled).build()
                        }
                    }
                },
            )

            // tab close where to show
            PreferenceSelect(
                text = "Show tab close icon:", // todo: string res
                // todo: when add gestures, if set to do not show close, check if gesture to close
                //  tabs is enabled for any option, if not, auto set swipe down on tab or toolbar
                //  to close and show toast long informing user swipe down gesture was set to
                //  close tabs
                description = "Which tabs to show close on. If not visible, a gesture to close tabs must be enabled.", // todo: string res
                enabled = true,
                selectedMenuItem = settings.miniTabShowClose,
                menuItems = listOf(
                    InfernoSettings.MiniTabShowClose.MINI_TAB_SHOW_ON_ALL,
                    InfernoSettings.MiniTabShowClose.MINI_TAB_SHOW_ONLY_ON_ACTIVE,
                    // todo: gestures -> enable when functional
//                    InfernoSettings.MiniTabShowClose.MINI_TAB_SHOW_ON_NONE,
                ),
                mapToTitle = { it.toPrefString(context) },
                onSelectMenuItem = { miniTabShowClose ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setMiniTabShowClose(miniTabShowClose).build()
                        }
                    }
                },
            )

            // vertical tab bar position
            PreferenceSelect(
                text = "Tab bar location:", // todo: string res
                description = "Where to place the tab bar in the browser.", // todo: string res
                enabled = true,
                selectedMenuItem = settings.tabBarVerticalPosition,
                menuItems = listOf(
                    InfernoSettings.VerticalTabBarPosition.TAB_BAR_BOTTOM,
                    InfernoSettings.VerticalTabBarPosition.TAB_BAR_TOP,
                ),
                mapToTitle = { it.toPrefString(context) },
                onSelectMenuItem = { tabBarVerticalPosition ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setTabBarVerticalPosition(tabBarVerticalPosition).build()
                        }
                    }
                },
            )

            // tab bar position relative to toolbar
            // only show if both toolbar and tab bar are both at top or bottom
            val toolbarTop =
                settings.toolbarVerticalPosition == InfernoSettings.VerticalToolbarPosition.TOOLBAR_TOP
            val tabBarTop =
                settings.tabBarVerticalPosition == InfernoSettings.VerticalTabBarPosition.TAB_BAR_TOP
            val toolbarBottom =
                settings.toolbarVerticalPosition == InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM
            val tabBarBottom =
                settings.tabBarVerticalPosition == InfernoSettings.VerticalTabBarPosition.TAB_BAR_BOTTOM
            if ((toolbarTop && tabBarTop) || (toolbarBottom && tabBarBottom)) {
                PreferenceSelect(
                    text = "Tab bar position:", // todo: string res
                    description = "Tab bar position relative to toolbar.", // todo: string res
                    enabled = true,
                    selectedMenuItem = settings.tabBarPosition,
                    menuItems = listOf(
                        InfernoSettings.TabBarPosition.TAB_BAR_ABOVE_TOOLBAR,
                        InfernoSettings.TabBarPosition.TAB_BAR_BELOW_TOOLBAR,
                    ),
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { tabBarPosition ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setTabBarPosition(tabBarPosition).build()
                            }
                        }
                    },
                )
            }

            // tab tray title
            PreferenceTitle("Tab Tray") // todo: string res

            // tab tray layout
            PreferenceSelect(
                text = stringResource(R.string.preferences_tab_view),
                description = "How to display tabs inside tab tray.", // todo: string res
                enabled = true,
                selectedMenuItem = settings.tabTrayStyle,
                menuItems = listOf(
                    InfernoSettings.TabTrayStyle.TAB_TRAY_LIST,
                    InfernoSettings.TabTrayStyle.TAB_TRAY_GRID,
                ),
                mapToTitle = { it.toPrefString(context) },
                onSelectMenuItem = { tabTrayStyle ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setTabTrayStyle(tabTrayStyle).build()
                        }
                    }
                },
            )
        }
    }
}

private fun InfernoSettings.CloseTabsMethod.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.CloseTabsMethod.CLOSE_TABS_MANUALLY -> context.getString(R.string.close_tabs_manually_summary)
        InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_DAY -> context.getString(R.string.close_tabs_after_one_day)
        InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_WEEK -> context.getString(R.string.close_tabs_after_one_week)
        InfernoSettings.CloseTabsMethod.CLOSE_TABS_AFTER_ONE_MONTH -> context.getString(R.string.close_tabs_after_one_month)
        InfernoSettings.CloseTabsMethod.UNRECOGNIZED -> ""
    }
}

private fun InfernoSettings.MiniTabShowClose.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.MiniTabShowClose.MINI_TAB_SHOW_ON_ALL -> "Show close on all tabs." // todo: string res
        InfernoSettings.MiniTabShowClose.MINI_TAB_SHOW_ONLY_ON_ACTIVE -> "Show close only on active tabs." // todo: string res
        InfernoSettings.MiniTabShowClose.MINI_TAB_SHOW_ON_NONE -> "Do not show close icon on any tab." // todo: string res
        InfernoSettings.MiniTabShowClose.UNRECOGNIZED -> ""
    }
}

private fun InfernoSettings.VerticalTabBarPosition.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.VerticalTabBarPosition.TAB_BAR_BOTTOM -> context.getString(R.string.preference_bottom_toolbar)
        InfernoSettings.VerticalTabBarPosition.TAB_BAR_TOP -> context.getString(R.string.preference_top_toolbar)
        InfernoSettings.VerticalTabBarPosition.UNRECOGNIZED -> ""
    }
}

private fun InfernoSettings.TabBarPosition.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.TabBarPosition.TAB_BAR_ABOVE_TOOLBAR -> "Above toolbar" // todo: string res
        InfernoSettings.TabBarPosition.TAB_BAR_BELOW_TOOLBAR -> "Below toolbar" // todo: string res
        InfernoSettings.TabBarPosition.UNRECOGNIZED -> ""
    }
}

private fun InfernoSettings.TabTrayStyle.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.TabTrayStyle.TAB_TRAY_GRID -> context.getString(R.string.tab_view_grid)
        InfernoSettings.TabTrayStyle.TAB_TRAY_LIST -> context.getString(R.string.tab_view_list)
        InfernoSettings.TabTrayStyle.UNRECOGNIZED -> ""
    }
}

package com.shmibblez.inferno.settings.home

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.URLUtil
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceConstants
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomePageSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())


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
                title = { InfernoText("Home Page Settings") }, // todo: string res
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // todo: not implemented yet, defaultTopSitesAdded and shouldShowSearchWidget

            item { PreferenceTitle(stringResource(R.string.preferences_category_general)) }


            // use inferno homepage
            item {
                PreferenceSwitch(
                    text = "Use Inferno Homepage:", // todo: string res more descriptive (mention tab bar if enabled)
                    summary = null,
                    selected = settings.shouldUseInfernoHome,
                    onSelectedChange = { shouldUseInfernoHome ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setShouldUseInfernoHome(shouldUseInfernoHome).build()
                            }
                        }
                    },
                )
            }

            when (settings.shouldUseInfernoHome) {
                // if true, show options for inferno home
                true -> {
                    // inferno home settings
                    item { PreferenceTitle("Inferno Home Settings") } // todo: string res

                    // show top sites
                    item {
                        PreferenceSwitch(
                            text = "Show top sites:", // todo: string res more descriptive (mention tab bar if enabled)
                            summary = null,
                            selected = settings.shouldShowTopSites,
                            onSelectedChange = { shouldShowTopSites ->
                                coroutineScope.launch {
                                    context.infernoSettingsDataStore.updateData {
                                        it.toBuilder().setShouldShowTopSites(shouldShowTopSites)
                                            .build()
                                    }
                                }
                            },
                        )
                    }

                    // show recent tabs
                    item {
                        PreferenceSwitch(
                            text = "Show recent tabs:", // todo: string res more descriptive (mention tab bar if enabled)
                            summary = null,
                            selected = settings.shouldShowRecentTabs,
                            onSelectedChange = { shouldShowRecentTabs ->
                                coroutineScope.launch {
                                    context.infernoSettingsDataStore.updateData {
                                        it.toBuilder().setShouldShowRecentTabs(shouldShowRecentTabs)
                                            .build()
                                    }
                                }
                            },
                        )
                    }

                    // show bookmarks
                    item {
                        PreferenceSwitch(
                            text = "Show bookmarks:", // todo: string res more descriptive (mention tab bar if enabled)
                            summary = null,
                            selected = settings.shouldShowBookmarks,
                            onSelectedChange = { shouldShowBookmarks ->
                                coroutineScope.launch {
                                    context.infernoSettingsDataStore.updateData {
                                        it.toBuilder().setShouldShowBookmarks(shouldShowBookmarks)
                                            .build()
                                    }
                                }
                            },
                        )
                    }

                    // show history
                    item {
                        PreferenceSwitch(
                            text = "Show history:", // todo: string res more descriptive (mention tab bar if enabled)
                            summary = null,
                            selected = settings.shouldShowHistory,
                            onSelectedChange = { shouldShowHistory ->
                                coroutineScope.launch {
                                    context.infernoSettingsDataStore.updateData {
                                        it.toBuilder().setShouldShowHistory(shouldShowHistory)
                                            .build()
                                    }
                                }
                            },
                        )
                    }

                    item { PreferenceTitle("Navigation") } // todo: string res

                    // page to open on
                    item {
                        PreferenceSelect(
                            text = "Page to open on:", // todo: string res
                            description = "What page to open on when app opened", // todo: string res
                            enabled = true,
                            selectedMenuItem = settings.pageWhenBrowserReopened,
                            menuItems = listOf(
                                InfernoSettings.PageWhenBrowserReopened.OPEN_ON_LAST_TAB,
                                InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_ALWAYS,
                                InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_AFTER_FOUR_HOURS,
                            ),
                            mapToTitle = { it.toPrefString(context) },
                            onSelectMenuItem = { selected ->
                                MainScope().launch {
                                    context.infernoSettingsDataStore.updateData {
                                        it.toBuilder().setPageWhenBrowserReopened(selected).build()
                                    }
                                }
                            },
                        )
                    }
                }

                // if should not use inferno home,
                // show option for setting home url
                false -> {
                    item {
                        var customUrl by remember { mutableStateOf(settings.customHomeUrl.ifBlank { PreferenceConstants.CUSTOM_HOME_URL_DEFAULT }) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING,
                                    vertical = PreferenceConstants.PREFERENCE_VERTICAL_PADDING,
                                ),
//                            verticalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            InfernoText("Custom Url:") // todo: string res

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
                            ) {
                                // url editor
                                InfernoOutlinedTextField(
                                    value = customUrl,
                                    onValueChange = { customUrl = it.trim() },
                                    modifier = Modifier.weight(1F),
                                    isError = URLUtil.isValidUrl(customUrl),
                                    supportingText = {
                                        InfernoText(
                                            "Invalid url.", infernoStyle = InfernoTextStyle.Error
                                        )
                                    },
                                )

                                // save button
                                InfernoButton(
                                    text = stringResource(R.string.browser_menu_save),
                                    onClick = {
                                        val url = customUrl
                                        if (URLUtil.isValidUrl(url)) {
                                            coroutineScope.launch {
                                                context.infernoSettingsDataStore.updateData {
                                                    it.toBuilder().setCustomHomeUrl(url).build()
                                                }
                                            }
                                        }
                                    },
                                    enabled = URLUtil.isValidUrl(customUrl),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun InfernoSettings.PageWhenBrowserReopened.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.PageWhenBrowserReopened.OPEN_ON_LAST_TAB -> "Open on last tab" // todo: string res
        InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_ALWAYS -> "Always open on home" // todo: string res
        InfernoSettings.PageWhenBrowserReopened.OPEN_ON_HOME_AFTER_FOUR_HOURS -> "Open on home after 4 hours" // todo: string res
        InfernoSettings.PageWhenBrowserReopened.UNRECOGNIZED -> context.getString(R.string.empty_string)
    }
}
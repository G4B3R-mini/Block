package com.shmibblez.inferno.settings.onQuit

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import kotlinx.coroutines.launch

private val SUB_SETTING_START_PADDING = 16.dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OnQuitSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    InfernoSettingsPage(
        title = "On Quit", // todo: string res
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // delete browsing data on quit
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_delete_browsing_data_on_quit),
                    summary = null,
                    selected = settings.deleteBrowsingDataOnQuit,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDeleteBrowsingDataOnQuit(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // delete open tabs on quit
            item {
                PreferenceSwitch(
                    modifier = Modifier.padding(start = SUB_SETTING_START_PADDING),
                    text = stringResource(R.string.preferences_delete_browsing_data_tabs_title_2),
                    summary = null,
                    selected = settings.deleteOpenTabsOnQuit,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDeleteOpenTabsOnQuit(selected).build()
                            }
                        }
                    },
                    enabled = settings.deleteBrowsingDataOnQuit,
                )
            }

            // delete browsing history on quit
            item {
                PreferenceSwitch(
                    modifier = Modifier.padding(start = SUB_SETTING_START_PADDING),
                    text = stringResource(R.string.preferences_delete_browsing_data_browsing_history_title),
                    summary = null,
                    selected = settings.deleteBrowsingHistoryOnQuit,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDeleteBrowsingHistoryOnQuit(selected).build()
                            }
                        }
                    },
                    enabled = settings.deleteBrowsingDataOnQuit,
                )
            }

            // delete cookies and site data on quit
            item {
                PreferenceSwitch(
                    modifier = Modifier.padding(start = SUB_SETTING_START_PADDING),
                    text = stringResource(R.string.preferences_delete_browsing_data_cookies_and_site_data),
                    summary = stringResource(R.string.preferences_delete_browsing_data_cookies_subtitle),
                    selected = settings.deleteCookiesAndSiteDataOnQuit,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDeleteCookiesAndSiteDataOnQuit(selected).build()
                            }
                        }
                    },
                    enabled = settings.deleteBrowsingDataOnQuit,
                )
            }

            // delete caches on quit
            item {
                PreferenceSwitch(
                    modifier = Modifier.padding(start = SUB_SETTING_START_PADDING),
                    text = stringResource(R.string.preferences_delete_browsing_data_cached_files),
                    summary = stringResource(R.string.preferences_delete_browsing_data_cached_files_subtitle),
                    selected = settings.deleteCachesOnQuit,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDeleteCachesOnQuit(selected).build()
                            }
                        }
                    },
                    enabled = settings.deleteBrowsingDataOnQuit,
                )
            }

            // delete permissions on quit
            item {
                PreferenceSwitch(
                    modifier = Modifier.padding(start = SUB_SETTING_START_PADDING),
                    text = stringResource(R.string.preferences_delete_browsing_data_site_permissions),
                    summary = null,
                    selected = settings.deletePermissionsOnQuit,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDeletePermissionsOnQuit(selected).build()
                            }
                        }
                    },
                    enabled = settings.deleteBrowsingDataOnQuit,
                )
            }

            // delete downloads on quit
            item {
                PreferenceSwitch(
                    modifier = Modifier.padding(start = SUB_SETTING_START_PADDING),
                    text = stringResource(R.string.preferences_delete_browsing_data_downloads),
                    summary = null,
                    selected = settings.deleteDownloadsOnQuit,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDeleteDownloadsOnQuit(selected).build()
                            }
                        }
                    },
                    enabled = settings.deleteBrowsingDataOnQuit,
                )
            }
        }
    }
}
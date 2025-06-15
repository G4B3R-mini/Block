package com.shmibblez.inferno.settings.sitepermissions

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import kotlinx.coroutines.launch

// todo: add exceptions item (at the bottom)
//  when click goes to new page
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SitePermissionsSettingsPage(
    goBack: () -> Unit,
    onNavToSitePermissionsExceptionsSettings: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    // todo: add start icons for site settings items
    //  - for ones blocked by android, show information icon instead
    //  - when info icon clicked, show dialog that shows steps to enable,
    //  and [go to settings] and [ok] button, in that order
    //  - check SiteSettingsFragment to see how to go to app permissions page

    var showSettingsInstructionsDialogFor by remember { mutableStateOf<String?>(null) }

    // blocked prefs reset when leave and return to page, todo: test, just to make sure

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_site_permissions),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // permissions title
            item {
                PreferenceTitle(stringResource(R.string.preferences_category_permissions))
            }

            // general site permission editor
            sitePermissionManager(
                setShowSettingsInstructionsDialogFor = { showSettingsInstructionsDialogFor = it },
                appLinksSetting = settings.appLinksSetting,
                onSetAppLinksSetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setAppLinksSetting(selected).build()
                        }
                    }
                },
                autoplaySetting = settings.autoplaySetting,
                onSetAutoplaySetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setAutoplaySetting(selected).build()
                        }
                    }
                },
                cameraSetting = settings.cameraSetting,
                onSetCameraSetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setCameraSetting(selected).build()
                        }
                    }
                },
                locationSetting = settings.locationSetting,
                onSetLocationSetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setLocationSetting(selected).build()
                        }
                    }
                },
                microphoneSetting = settings.microphoneSetting,
                onSetMicrophoneSetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setMicrophoneSetting(selected).build()
                        }
                    }
                },
                notificationsSetting = settings.notificationsSetting,
                onSetNotificationsSetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setNotificationsSetting(selected).build()
                        }
                    }
                },
                persistentStorageSetting = settings.persistentStorageSetting,
                onSetPersistentStorageSetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setPersistentStorageSetting(selected).build()
                        }
                    }
                },
                crossSiteCookiesSetting = settings.crossSiteCookiesSetting,
                onSetCrossSiteCookiesSetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setCrossSiteCookiesSetting(selected).build()
                        }
                    }
                },
                drmControlledContentSetting = settings.drmControlledContentSetting,
                onSetDrmControlledContentSetting = { selected ->
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            it.toBuilder().setDrmControlledContentSetting(selected).build()
                        }
                    }
                },
            )

            // exceptions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                            vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                        )
                        .clickable {
                            /**
                             * todo: implement exceptions page
                             *  - check: [SitePermissionsExceptionsFragment]
                             *  - exceptions page shows site items in list, and button to clear all
                             *    on the bottom
                             *  - clear all requires dialog confirmation showing more details
                             *  - when site item expanded, settings for that site are shown, also
                             *    with option to clear for that site
                             *  - will require listener for list items -> when site cleared, remove
                             *    from list
                             */
                            onNavToSitePermissionsExceptionsSettings.invoke()
                        },
                    horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_globe_24),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                    )
                    InfernoText(
                        text = stringResource(R.string.preference_exceptions)
                    )
                }
            }
        }

        if (showSettingsInstructionsDialogFor != null) {
            PermissionsSettingsInstructionsDialog(
                onDismiss = { showSettingsInstructionsDialogFor = null },
                settingName = showSettingsInstructionsDialogFor!!,
            )
        }
    }
}

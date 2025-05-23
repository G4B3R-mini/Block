package com.shmibblez.inferno.settings.sitepermissions

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.CAMERA as CAMERA_PERMISSION
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import kotlinx.coroutines.launch
import mozilla.components.support.ktx.android.content.isPermissionGranted

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SitePermissionsSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    // todo: add start icons for site settings items
    //  - for ones blocked by android, show information icon instead
    //  - when info icon clicked, show dialog that shows steps to enable,
    //  and [go to settings] and [ok] button, in that order

    // blocked prefs reset when leave and return to page, todo: test, just to make sure
    var cameraBlockedByAndroid by remember {
        mutableStateOf(
            context.isPermissionGranted(
                CAMERA_PERMISSION
            )
        )
    }
    var locationBlockedByAndroid by remember {
        mutableStateOf(
            context.isPermissionGranted(
                arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION).asIterable()
            )
        )
    }
    var microphoneBlockedByAndroid by remember {
        mutableStateOf(
            context.isPermissionGranted(
                RECORD_AUDIO
            )
        )
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
                title = { InfernoText("Site Permissions Settings") }, // todo: string res
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // permissions title
            item {
                PreferenceTitle(stringResource(R.string.preferences_category_permissions))
            }

            // app links setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preferences_open_links_in_apps),
                    description = null,
                    enabled = true,
                    selectedMenuItem = settings.appLinksSetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.AppLinks.APP_LINKS_ASK_TO_OPEN,
                            InfernoSettings.AppLinks.APP_LINKS_ALLOWED,
                            InfernoSettings.AppLinks.APP_LINKS_BLOCKED,
                        )
                    },
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setAppLinksSetting(selected).build()
                            }
                        }
                    },
                )
            }

            // autoplay setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_browser_feature_autoplay),
                    description = when (settings.autoplaySetting) {
                        InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY -> {
                            stringResource(R.string.preference_option_autoplay_allowed_wifi_subtext)
                        }

                        else -> {
                            null
                        }
                    },
                    enabled = true,
                    selectedMenuItem = settings.autoplaySetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY,
                            InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO,
                            InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO,
                            InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY,
                        )
                    },
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setAutoplaySetting(selected).build()
                            }
                        }
                    },
                )
            }

            // camera setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_phone_feature_camera),
                    description = when (cameraBlockedByAndroid) {
                        true -> stringResource(R.string.phone_feature_blocked_by_android)
                        false -> null
                    },
                    enabled = true,
                    selectedMenuItem = settings.cameraSetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.Camera.CAMERA_ASK_TO_ALLOW,
                            InfernoSettings.Camera.CAMERA_ALLOWED,
                            InfernoSettings.Camera.CAMERA_BLOCKED,
                        )
                    },
                    mapToTitle = { it.toPrefString(context, cameraBlockedByAndroid) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setCameraSetting(selected).build()
                            }
                        }
                    },
                )
            }

            // location setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_phone_feature_location),
                    description = when (locationBlockedByAndroid) {
                        true -> stringResource(R.string.phone_feature_blocked_by_android)
                        false -> null
                    },
                    enabled = true,
                    selectedMenuItem = settings.locationSetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.Location.LOCATION_ASK_TO_ALLOW,
                            InfernoSettings.Location.LOCATION_ALLOWED,
                            InfernoSettings.Location.LOCATION_BLOCKED,
                        )
                    },
                    mapToTitle = { it.toPrefString(context, locationBlockedByAndroid) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setLocationSetting(selected).build()
                            }
                        }
                    },
                )
            }

            // microphone setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_phone_feature_microphone),
                    description = when (microphoneBlockedByAndroid) {
                        true -> stringResource(R.string.phone_feature_blocked_by_android)
                        false -> null
                    },
                    enabled = true,
                    selectedMenuItem = settings.microphoneSetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.Microphone.MICROPHONE_ASK_TO_ALLOW,
                            InfernoSettings.Microphone.MICROPHONE_ALLOWED,
                            InfernoSettings.Microphone.MICROPHONE_BLOCKED,
                        )
                    },
                    mapToTitle = { it.toPrefString(context, microphoneBlockedByAndroid) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setMicrophoneSetting(selected).build()
                            }
                        }
                    },
                )
            }

            // microphone setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_phone_feature_notification),
                    description = null,
                    enabled = true,
                    selectedMenuItem = settings.notificationsSetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.Notifications.NOTIFICATIONS_ASK_TO_ALLOW,
                            InfernoSettings.Notifications.NOTIFICATIONS_ALLOWED,
                            InfernoSettings.Notifications.NOTIFICATIONS_BLOCKED,
                        )
                    },
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setNotificationsSetting(selected).build()
                            }
                        }
                    },
                )
            }

            // persistent storage setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_phone_feature_persistent_storage),
                    description = null,
                    enabled = true,
                    selectedMenuItem = settings.persistentStorageSetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ASK_TO_ALLOW,
                            InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ALLOWED,
                            InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_BLOCKED,
                        )
                    },
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setPersistentStorageSetting(selected).build()
                            }
                        }
                    },
                )
            }

            // cross-site cookies setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_phone_feature_cross_origin_storage_access),
                    description = null,
                    enabled = true,
                    selectedMenuItem = settings.crossSiteCookiesSetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ASK_TO_ALLOW,
                            InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ALLOWED,
                            InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_BLOCKED,
                        )
                    },
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setCrossSiteCookiesSetting(selected).build()
                            }
                        }
                    },
                )
            }

            // drm-controlled content setting
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_phone_feature_media_key_system_access),
                    description = null,
                    enabled = true,
                    selectedMenuItem = settings.drmControlledContentSetting,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ASK_TO_ALLOW,
                            InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ALLOWED,
                            InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_BLOCKED,
                        )
                    },
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDrmControlledContentSetting(selected).build()
                            }
                        }
                    },
                )
            }

        }
    }
}

fun InfernoSettings.AppLinks.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.AppLinks.APP_LINKS_ASK_TO_OPEN -> context.getString(R.string.preferences_open_links_in_apps_ask)
        InfernoSettings.AppLinks.APP_LINKS_ALLOWED -> context.getString(R.string.preferences_open_links_in_apps_always)
        InfernoSettings.AppLinks.APP_LINKS_BLOCKED -> context.getString(R.string.preferences_open_links_in_apps_never)
        InfernoSettings.AppLinks.UNRECOGNIZED -> ""
    }
}

fun InfernoSettings.AutoPlay.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY -> context.getString(R.string.preference_option_autoplay_block_audio2)
        InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO -> context.getString(R.string.preference_option_autoplay_blocked3)
        InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO -> context.getString(R.string.preference_option_autoplay_allowed2)
        InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY -> context.getString(R.string.preference_option_autoplay_allowed_wifi_only2)
        InfernoSettings.AutoPlay.UNRECOGNIZED -> ""
    }
}

fun InfernoSettings.Camera.toPrefString(context: Context, blockedByAndroid: Boolean): String {
    if (blockedByAndroid) return context.getString(R.string.phone_feature_blocked_by_android)
    return when (this) {
        InfernoSettings.Camera.CAMERA_ASK_TO_ALLOW -> context.getString(R.string.preference_option_phone_feature_ask_to_allow)
        InfernoSettings.Camera.CAMERA_ALLOWED -> context.getString(R.string.preference_option_phone_feature_allowed)
        InfernoSettings.Camera.CAMERA_BLOCKED -> context.getString(R.string.preference_option_phone_feature_blocked)
        InfernoSettings.Camera.UNRECOGNIZED -> ""
    }
}

fun InfernoSettings.Location.toPrefString(context: Context, blockedByAndroid: Boolean): String {
    if (blockedByAndroid) return context.getString(R.string.phone_feature_blocked_by_android)
    return when (this) {
        InfernoSettings.Location.LOCATION_ASK_TO_ALLOW -> context.getString(R.string.preference_option_phone_feature_ask_to_allow)
        InfernoSettings.Location.LOCATION_ALLOWED -> context.getString(R.string.preference_option_phone_feature_allowed)
        InfernoSettings.Location.LOCATION_BLOCKED -> context.getString(R.string.preference_option_phone_feature_blocked)
        InfernoSettings.Location.UNRECOGNIZED -> ""
    }
}

fun InfernoSettings.Microphone.toPrefString(context: Context, blockedByAndroid: Boolean): String {
    if (blockedByAndroid) return context.getString(R.string.phone_feature_blocked_by_android)
    return when (this) {
        InfernoSettings.Microphone.MICROPHONE_ASK_TO_ALLOW -> context.getString(R.string.preference_option_phone_feature_ask_to_allow)
        InfernoSettings.Microphone.MICROPHONE_ALLOWED -> context.getString(R.string.preference_option_phone_feature_allowed)
        InfernoSettings.Microphone.MICROPHONE_BLOCKED -> context.getString(R.string.preference_option_phone_feature_blocked)
        InfernoSettings.Microphone.UNRECOGNIZED -> ""
    }
}

fun InfernoSettings.Notifications.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.Notifications.NOTIFICATIONS_ASK_TO_ALLOW -> context.getString(R.string.preference_option_phone_feature_ask_to_allow)
        InfernoSettings.Notifications.NOTIFICATIONS_ALLOWED -> context.getString(R.string.preference_option_phone_feature_allowed)
        InfernoSettings.Notifications.NOTIFICATIONS_BLOCKED -> context.getString(R.string.preference_option_phone_feature_blocked)
        InfernoSettings.Notifications.UNRECOGNIZED -> ""
    }
}

fun InfernoSettings.PersistentStorage.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ASK_TO_ALLOW -> context.getString(R.string.preference_option_phone_feature_ask_to_allow)
        InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ALLOWED -> context.getString(R.string.preference_option_phone_feature_allowed)
        InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_BLOCKED -> context.getString(R.string.preference_option_phone_feature_blocked)
        InfernoSettings.PersistentStorage.UNRECOGNIZED -> ""
    }
}

fun InfernoSettings.CrossSiteCookies.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ASK_TO_ALLOW -> context.getString(R.string.preference_option_phone_feature_ask_to_allow)
        InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ALLOWED -> context.getString(R.string.preference_option_phone_feature_allowed)
        InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_BLOCKED -> context.getString(R.string.preference_option_phone_feature_blocked)
        InfernoSettings.CrossSiteCookies.UNRECOGNIZED -> ""
    }
}

fun InfernoSettings.DrmControlledContent.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ASK_TO_ALLOW -> context.getString(
            R.string.preference_option_phone_feature_ask_to_allow
        )

        InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ALLOWED -> context.getString(R.string.preference_option_phone_feature_allowed)
        InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_BLOCKED -> context.getString(R.string.preference_option_phone_feature_blocked)
        InfernoSettings.DrmControlledContent.UNRECOGNIZED -> ""
    }
}
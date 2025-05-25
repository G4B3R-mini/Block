package com.shmibblez.inferno.settings.sitepermissions

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import mozilla.components.support.ktx.android.content.isPermissionGranted

private val TOOLTIP_ICON_SIZE = 32.dp

fun LazyListScope.sitePermissionManager(
    setShowSettingsInstructionsDialogFor: (String?) -> Unit,
    appLinksSetting: InfernoSettings.AppLinks?,
    onSetAppLinksSetting: (InfernoSettings.AppLinks) -> Unit,
    autoplaySetting: InfernoSettings.AutoPlay,
    onSetAutoplaySetting: (InfernoSettings.AutoPlay) -> Unit,
    cameraSetting: InfernoSettings.Camera,
    onSetCameraSetting: (InfernoSettings.Camera) -> Unit,
    locationSetting: InfernoSettings.Location,
    onSetLocationSetting: (InfernoSettings.Location) -> Unit,
    microphoneSetting: InfernoSettings.Microphone,
    onSetMicrophoneSetting: (InfernoSettings.Microphone) -> Unit,
    notificationsSetting: InfernoSettings.Notifications,
    onSetNotificationsSetting: (InfernoSettings.Notifications) -> Unit,
    persistentStorageSetting: InfernoSettings.PersistentStorage,
    onSetPersistentStorageSetting: (InfernoSettings.PersistentStorage) -> Unit,
    crossSiteCookiesSetting: InfernoSettings.CrossSiteCookies,
    onSetCrossSiteCookiesSetting: (InfernoSettings.CrossSiteCookies) -> Unit,
    drmControlledContentSetting: InfernoSettings.DrmControlledContent,
    onSetDrmControlledContentSetting: (InfernoSettings.DrmControlledContent) -> Unit,
) {
    // app links setting
    item {
        if (appLinksSetting != null) {
            val context = LocalContext.current
            PreferenceSelect(
                text = stringResource(R.string.preferences_open_links_in_apps),
                description = null,
                enabled = true,
                selectedMenuItem = appLinksSetting,
                menuItems = remember {
                    listOf(
                        InfernoSettings.AppLinks.APP_LINKS_ASK_TO_OPEN,
                        InfernoSettings.AppLinks.APP_LINKS_ALLOWED,
                        InfernoSettings.AppLinks.APP_LINKS_BLOCKED,
                    )
                },
                mapToTitle = { it.toPrefString(context) },
                preferenceLeadingIcon = {
                    InfernoIcon(
                        painter = painterResource(
                            when {
                                appLinksSetting == InfernoSettings.AppLinks.APP_LINKS_BLOCKED -> R.drawable.ic_link_disabled
                                else -> R.drawable.ic_link_enabled
                            }
                        ),
                        contentDescription = "",
                        modifier = Modifier.size(TOOLTIP_ICON_SIZE),
                    )
                },
                onSelectMenuItem = onSetAppLinksSetting,
            )
        }
    }

    // autoplay setting
    item {
        val context = LocalContext.current
        PreferenceSelect(
            text = stringResource(R.string.preference_browser_feature_autoplay),
            description = when (autoplaySetting) {
                InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY -> {
                    stringResource(R.string.preference_option_autoplay_allowed_wifi_subtext)
                }

                else -> {
                    null
                }
            },
            enabled = true,
            selectedMenuItem = autoplaySetting,
            menuItems = remember {
                listOf(
                    InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY,
                    InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO,
                    InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO,
                    InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY,
                )
            },
            mapToTitle = { it.toPrefString(context) },
            preferenceLeadingIcon = {
                InfernoIcon(
                    painter = painterResource(
                        when {
                            autoplaySetting == InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO -> R.drawable.ic_autoplay_disabled
                            else -> R.drawable.ic_autoplay_enabled
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier.size(TOOLTIP_ICON_SIZE),
                )
            },
            onSelectMenuItem = onSetAutoplaySetting,
        )
    }

    // camera setting
    item {
        val context = LocalContext.current
        val cameraBlockedByAndroid by remember {
            mutableStateOf(
                context.isPermissionGranted(
                    CAMERA
                )
            )
        }
        PreferenceSelect(
            text = stringResource(R.string.preference_phone_feature_camera),
            description = when (cameraBlockedByAndroid) {
                true -> stringResource(R.string.phone_feature_blocked_by_android)
                false -> null
            },
            enabled = true,
            selectedMenuItem = cameraSetting,
            menuItems = remember {
                listOf(
                    InfernoSettings.Camera.CAMERA_ASK_TO_ALLOW,
                    InfernoSettings.Camera.CAMERA_ALLOWED,
                    InfernoSettings.Camera.CAMERA_BLOCKED,
                )
            },
            mapToTitle = { it.toPrefString(context, cameraBlockedByAndroid) },
            preferenceLeadingIcon = {
                InfernoIcon(
                    painter = painterResource(
                        when {
                            cameraBlockedByAndroid -> R.drawable.ic_info
                            cameraSetting == InfernoSettings.Camera.CAMERA_BLOCKED -> R.drawable.ic_camera_disabled
                            else -> R.drawable.ic_camera_enabled
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier
                        .size(TOOLTIP_ICON_SIZE)
                        .clickable(enabled = cameraBlockedByAndroid) {
                            setShowSettingsInstructionsDialogFor(context.getString(R.string.preference_phone_feature_camera))
                        },
                )
            },
            onSelectMenuItem = onSetCameraSetting,
        )
    }

    // location setting
    item {
        val context = LocalContext.current
        val locationBlockedByAndroid by remember {
            mutableStateOf(
                context.isPermissionGranted(
                    arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION).asIterable()
                )
            )
        }
        PreferenceSelect(
            text = stringResource(R.string.preference_phone_feature_location),
            description = when (locationBlockedByAndroid) {
                true -> stringResource(R.string.phone_feature_blocked_by_android)
                false -> null
            },
            enabled = true,
            selectedMenuItem = locationSetting,
            menuItems = remember {
                listOf(
                    InfernoSettings.Location.LOCATION_ASK_TO_ALLOW,
                    InfernoSettings.Location.LOCATION_ALLOWED,
                    InfernoSettings.Location.LOCATION_BLOCKED,
                )
            },
            mapToTitle = { it.toPrefString(context, locationBlockedByAndroid) },
            preferenceLeadingIcon = {
                InfernoIcon(
                    painter = painterResource(
                        when {
                            locationBlockedByAndroid -> R.drawable.ic_info
                            locationSetting == InfernoSettings.Location.LOCATION_BLOCKED -> R.drawable.ic_location_disabled
                            else -> R.drawable.ic_location_enabled
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier
                        .size(TOOLTIP_ICON_SIZE)
                        .clickable(enabled = locationBlockedByAndroid) {
                            setShowSettingsInstructionsDialogFor(context.getString(R.string.preference_phone_feature_location))
                        },
                )
            },
            onSelectMenuItem = onSetLocationSetting,
        )
    }

    // microphone setting
    item {
        val context = LocalContext.current
        val microphoneBlockedByAndroid by remember {
            mutableStateOf(
                context.isPermissionGranted(
                    RECORD_AUDIO
                )
            )
        }
        PreferenceSelect(
            text = stringResource(R.string.preference_phone_feature_microphone),
            description = when (microphoneBlockedByAndroid) {
                true -> stringResource(R.string.phone_feature_blocked_by_android)
                false -> null
            },
            enabled = true,
            selectedMenuItem = microphoneSetting,
            menuItems = remember {
                listOf(
                    InfernoSettings.Microphone.MICROPHONE_ASK_TO_ALLOW,
                    InfernoSettings.Microphone.MICROPHONE_ALLOWED,
                    InfernoSettings.Microphone.MICROPHONE_BLOCKED,
                )
            },
            mapToTitle = { it.toPrefString(context, microphoneBlockedByAndroid) },
            preferenceLeadingIcon = {
                InfernoIcon(
                    painter = painterResource(
                        when {
                            microphoneBlockedByAndroid -> R.drawable.ic_info
                            microphoneSetting == InfernoSettings.Microphone.MICROPHONE_BLOCKED -> R.drawable.ic_microphone_disabled
                            else -> R.drawable.ic_microphone_enabled
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier
                        .size(TOOLTIP_ICON_SIZE)
                        .clickable(enabled = microphoneBlockedByAndroid) {
                            setShowSettingsInstructionsDialogFor(context.getString(R.string.preference_phone_feature_microphone))
                        },
                )
            },
            onSelectMenuItem = onSetMicrophoneSetting,
        )
    }

    // notifications setting
    item {
        val context = LocalContext.current
        PreferenceSelect(
            text = stringResource(R.string.preference_phone_feature_notification),
            description = null,
            enabled = true,
            selectedMenuItem = notificationsSetting,
            menuItems = remember {
                listOf(
                    InfernoSettings.Notifications.NOTIFICATIONS_ASK_TO_ALLOW,
                    InfernoSettings.Notifications.NOTIFICATIONS_ALLOWED,
                    InfernoSettings.Notifications.NOTIFICATIONS_BLOCKED,
                )
            },
            mapToTitle = { it.toPrefString(context) },
            preferenceLeadingIcon = {
                InfernoIcon(
                    painter = painterResource(
                        when {
                            notificationsSetting == InfernoSettings.Notifications.NOTIFICATIONS_BLOCKED -> R.drawable.ic_notifications_disabled
                            else -> R.drawable.ic_notifications_enabled
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier.size(TOOLTIP_ICON_SIZE),
                )
            },
            onSelectMenuItem = onSetNotificationsSetting,
        )
    }

    // persistent storage setting
    item {
        val context = LocalContext.current
        PreferenceSelect(
            text = stringResource(R.string.preference_phone_feature_persistent_storage),
            description = null,
            enabled = true,
            selectedMenuItem = persistentStorageSetting,
            menuItems = remember {
                listOf(
                    InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ASK_TO_ALLOW,
                    InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ALLOWED,
                    InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_BLOCKED,
                )
            },
            mapToTitle = { it.toPrefString(context) },
            preferenceLeadingIcon = {
                InfernoIcon(
                    painter = painterResource(
                        when {
                            persistentStorageSetting == InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_BLOCKED -> R.drawable.ic_storage_disabled
                            else -> R.drawable.ic_storage_enabled
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier.size(TOOLTIP_ICON_SIZE),
                )
            },
            onSelectMenuItem = onSetPersistentStorageSetting,
        )
    }

    // cross-site cookies setting
    item {
        val context = LocalContext.current
        PreferenceSelect(
            text = stringResource(R.string.preference_phone_feature_cross_origin_storage_access),
            description = null,
            enabled = true,
            selectedMenuItem = crossSiteCookiesSetting,
            menuItems = remember {
                listOf(
                    InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ASK_TO_ALLOW,
                    InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ALLOWED,
                    InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_BLOCKED,
                )
            },
            mapToTitle = { it.toPrefString(context) },
            preferenceLeadingIcon = {
                InfernoIcon(
                    painter = painterResource(
                        when {
                            crossSiteCookiesSetting == InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_BLOCKED -> R.drawable.ic_cookies_disabled
                            else -> R.drawable.ic_cookies_enabled
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier.size(TOOLTIP_ICON_SIZE),
                )
            },
            onSelectMenuItem = onSetCrossSiteCookiesSetting,
        )
    }

    // drm-controlled content setting
    item {
        val context = LocalContext.current
        PreferenceSelect(
            text = stringResource(R.string.preference_phone_feature_media_key_system_access),
            description = null,
            enabled = true,
            selectedMenuItem = drmControlledContentSetting,
            menuItems = remember {
                listOf(
                    InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ASK_TO_ALLOW,
                    InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ALLOWED,
                    InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_BLOCKED,
                )
            },
            mapToTitle = { it.toPrefString(context) },
            preferenceLeadingIcon = {
                InfernoIcon(
                    painter = painterResource(
                        when {
                            drmControlledContentSetting == InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_BLOCKED -> R.drawable.ic_link_enabled
                            else -> R.drawable.ic_link_disabled
                        }
                    ),
                    contentDescription = "",
                    modifier = Modifier.size(TOOLTIP_ICON_SIZE),
                )
            },
            onSelectMenuItem = onSetDrmControlledContentSetting,
        )
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
package com.shmibblez.inferno.settings.sitepermissions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
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
import com.shmibblez.inferno.components.PermissionStorage
import com.shmibblez.inferno.compose.Favicon
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mozilla.components.concept.engine.permission.SitePermissions
import mozilla.components.support.base.feature.LifecycleAwareFeature

private val ICON_SIZE = 18.dp

internal class ExceptionSitePermissionManagerState(
    val storage: PermissionStorage,
    val coroutineScope: CoroutineScope,
    initiallyPrivate: Boolean = false,
) : LifecycleAwareFeature {

    private val sitePermissionStorage = storage.permissionsStorage

    private val jobs: MutableList<Job> = mutableListOf()

    internal var sitePermissions by mutableStateOf(emptyList<SitePermissions>())

    var private by run {
        val state = mutableStateOf(initiallyPrivate)
        object : MutableState<Boolean> by state {
            override var value
                get() = state.value
                set(value) {
                    state.value = value
                    refreshSitePermissions()
                }
        }
    }
        private set


    val isLoading: Boolean
        get() = jobs.isNotEmpty()


    // removes all jobs that are not active
    private fun clearFinishedJobs() {
        jobs.removeAll { !it.isActive }
    }

    // launches suspend function and tracks its state
    private fun launchSuspend(block: suspend CoroutineScope.() -> Unit) {
        val job = coroutineScope.launch(block = block).apply {
            this.invokeOnCompletion { clearFinishedJobs() }
        }
        jobs.add(job)
    }

    // refreshes address list
    private fun refreshSitePermissions() {
        launchSuspend {
            storage.findSitePermissionsBy("", private)
            sitePermissions = sitePermissionStorage.all()
        }
    }

    fun add(sitePermissions: SitePermissions) {
        launchSuspend {
            storage.add(sitePermissions, private)
            refreshSitePermissions()
        }
    }

    fun updateSitePermissions(sitePermissions: SitePermissions) {
        launchSuspend {
            storage.updateSitePermissions(sitePermissions, private)
            refreshSitePermissions()
        }
    }

    fun deleteSitePermissions(sitePermissions: SitePermissions) {
        launchSuspend {
            storage.deleteSitePermissions(sitePermissions)
            refreshSitePermissions()
        }
    }

    fun deleteAllSitePermissions() {
        launchSuspend {
            storage.deleteAllSitePermissions()
            refreshSitePermissions()
        }
    }

    override fun start() {
        refreshSitePermissions()
    }

    override fun stop() {
        jobs.forEach { it.cancel() }
    }

}

@Composable
internal fun rememberExceptionSitePermissionManagerState(): MutableState<ExceptionSitePermissionManagerState> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state = remember {
        mutableStateOf(
            ExceptionSitePermissionManagerState(
                storage = context.components.core.permissionStorage, coroutineScope = coroutineScope
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()

        onDispose {
            state.value.stop()
        }
    }

    return state
}

internal fun LazyListScope.exceptionSitePermissionManager(
    state: ExceptionSitePermissionManagerState,
    onRequireSettingsInstructions: (String?) -> Unit,
    onClearSitePermissionsClicked: (SitePermissions) -> Unit,
    onClearAllSitePermissionsClicked: () -> Unit,
) {
    // top padding
    item {
        Spacer(
            modifier = Modifier.padding(top = PrefUiConst.PREFERENCE_VERTICAL_PADDING),
        )
    }

    // site permissions for individual sites
    for (sitePermission in state.sitePermissions) {
        sitePermissionsItem(
            sitePermission = sitePermission,
            setShowSettingsInstructionsDialogFor = onRequireSettingsInstructions,
            onClearSitePermissionsClicked = onClearSitePermissionsClicked,
            appLinksSetting = null,
            onSetAppLinksSetting = {},
            autoplaySetting = (sitePermission.autoplayAudible to sitePermission.autoplayInaudible).toAutoplaySetting(),
            onSetAutoplaySetting = {
                val (audible, inaudible) = it.toAutoplayValue()
                state.updateSitePermissions(
                    sitePermission.copy(
                        autoplayAudible = audible,
                        autoplayInaudible = inaudible,
                    ),
                )
            },
            cameraSetting = sitePermission.camera.toCameraSetting(),
            onSetCameraSetting = {
                state.updateSitePermissions(
                    sitePermission.copy(
                        camera = it.toCameraValue()
                    ),
                )
            },
            locationSetting = sitePermission.location.toLocationSetting(),
            onSetLocationSetting = {
                // todo: left off here
                state.updateSitePermissions(
                    sitePermission.copy(
                        location = it.toLocationValue()
                    ),
                )
            },
            microphoneSetting = sitePermission.microphone.toMicrophoneSetting(),
            onSetMicrophoneSetting = {
                state.updateSitePermissions(
                    sitePermission.copy(
                        microphone = it.toMicrophoneValue()
                    ),
                )
            },
            notificationsSetting = sitePermission.notification.toNotificationSetting(),
            onSetNotificationsSetting = {
                state.updateSitePermissions(
                    sitePermission.copy(
                        notification = it.toNotificationValue()
                    ),
                )
            },
            persistentStorageSetting = sitePermission.localStorage.toPersistentStorageSetting(),
            onSetPersistentStorageSetting = {
                state.updateSitePermissions(
                    sitePermission.copy(
                        localStorage = it.toLocalStorageValue()
                    ),
                )
            },
            crossSiteCookiesSetting = sitePermission.crossOriginStorageAccess.toCrossSiteCookiesSetting(),
            onSetCrossSiteCookiesSetting = {
                state.updateSitePermissions(
                    sitePermission.copy(
                        crossOriginStorageAccess = it.toCrossOriginStorageValue()
                    ),
                )
            },
            drmControlledContentSetting = sitePermission.mediaKeySystemAccess.toDrmControlledContentSetting(),
            onSetDrmControlledContentSetting = {
                state.updateSitePermissions(
                    sitePermission.copy(
                        mediaKeySystemAccess = it.toMediaKeysSystemAccessValues()
                    ),
                )
            },
        )
    }

    item {
        InfernoButton(
            text = stringResource(R.string.clear_permissions),
            onClick = onClearAllSitePermissionsClicked,
        )
    }

    // bottom padding
    item {
        Spacer(
            modifier = Modifier.padding(bottom = PrefUiConst.PREFERENCE_VERTICAL_PADDING),
        )
    }
}

private fun LazyListScope.sitePermissionsItem(
    sitePermission: SitePermissions,
    setShowSettingsInstructionsDialogFor: (String?) -> Unit,
    onClearSitePermissionsClicked: (SitePermissions) -> Unit,
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
    // todo: if expand doesn't work this line is why
    //  in case it doesn't work, state should generate list of pairs: (sitePermission, isExpanded),
    //  then use that here, or just put in top level
    var expanded by mutableStateOf(false)

    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // favicon
            Favicon(
                url = sitePermission.origin,
                size = ICON_SIZE,
//                modifier = ,
                isPrivate = false,
//                imageUrl = ,
            )

            // origin text
            InfernoText(sitePermission.origin, modifier = Modifier.weight(1F))

            // expand/hide site settings
            InfernoIcon(painter = painterResource(
                when (expanded) {
                    true -> R.drawable.ic_chevron_up_24
                    false -> R.drawable.ic_chevron_down_24
                }
            ), contentDescription = "", modifier = Modifier.clickable { expanded = !expanded })
        }
    }

    if (expanded) {
        // for each site, permission manager
        sitePermissionManager(
            setShowSettingsInstructionsDialogFor = setShowSettingsInstructionsDialogFor,
            appLinksSetting = appLinksSetting,
            onSetAppLinksSetting = onSetAppLinksSetting,
            autoplaySetting = autoplaySetting,
            onSetAutoplaySetting = onSetAutoplaySetting,
            cameraSetting = cameraSetting,
            onSetCameraSetting = onSetCameraSetting,
            locationSetting = locationSetting,
            onSetLocationSetting = onSetLocationSetting,
            microphoneSetting = microphoneSetting,
            onSetMicrophoneSetting = onSetMicrophoneSetting,
            notificationsSetting = notificationsSetting,
            onSetNotificationsSetting = onSetNotificationsSetting,
            persistentStorageSetting = persistentStorageSetting,
            onSetPersistentStorageSetting = onSetPersistentStorageSetting,
            crossSiteCookiesSetting = crossSiteCookiesSetting,
            onSetCrossSiteCookiesSetting = onSetCrossSiteCookiesSetting,
            drmControlledContentSetting = drmControlledContentSetting,
            onSetDrmControlledContentSetting = onSetDrmControlledContentSetting
        )

        // clear permissions button
        item {
            InfernoButton(
                text = stringResource(R.string.clear_permissions),
                onClick = { onClearSitePermissionsClicked.invoke(sitePermission) },
            )
        }
    }
}


/**
 * @this first is audible, second is inaudible
 */
fun Pair<SitePermissions.AutoplayStatus, SitePermissions.AutoplayStatus>.toAutoplaySetting(): InfernoSettings.AutoPlay {
    val allowed = SitePermissions.AutoplayStatus.ALLOWED
    val blocked = SitePermissions.AutoplayStatus.BLOCKED
    val audible = this.first
    val inaudible = this.second
    return when {
        audible == allowed && inaudible == allowed -> InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO
        audible == blocked && inaudible == allowed -> InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY
        audible == allowed && inaudible == blocked -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY
        audible == blocked && inaudible == blocked -> InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO
        else -> InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY
    }
}

/**
 * @return (audible, inaudible)
 */
fun InfernoSettings.AutoPlay.toAutoplayValue(): Pair<SitePermissions.AutoplayStatus, SitePermissions.AutoplayStatus> {
    val allowed = SitePermissions.AutoplayStatus.ALLOWED
    val blocked = SitePermissions.AutoplayStatus.BLOCKED
    return when (this) {
        InfernoSettings.AutoPlay.BLOCK_AUDIO_ONLY -> blocked to allowed
        InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO -> blocked to blocked
        InfernoSettings.AutoPlay.ALLOW_AUDIO_AND_VIDEO -> allowed to allowed
        InfernoSettings.AutoPlay.BLOCK_AUDIO_AND_VIDEO_ON_CELLULAR_DATA_ONLY -> allowed to allowed
    }
}

fun SitePermissions.Status.toCameraSetting(): InfernoSettings.Camera {
    return when (this) {
        SitePermissions.Status.BLOCKED -> InfernoSettings.Camera.CAMERA_BLOCKED
        SitePermissions.Status.NO_DECISION -> InfernoSettings.Camera.CAMERA_ASK_TO_ALLOW
        SitePermissions.Status.ALLOWED -> InfernoSettings.Camera.CAMERA_ALLOWED
    }
}

fun InfernoSettings.Camera.toCameraValue(): SitePermissions.Status {
    return when (this) {
        InfernoSettings.Camera.CAMERA_BLOCKED -> SitePermissions.Status.BLOCKED
        InfernoSettings.Camera.CAMERA_ASK_TO_ALLOW -> SitePermissions.Status.NO_DECISION
        InfernoSettings.Camera.CAMERA_ALLOWED -> SitePermissions.Status.ALLOWED
    }
}

fun SitePermissions.Status.toLocationSetting(): InfernoSettings.Location {
    return when (this) {
        SitePermissions.Status.BLOCKED -> InfernoSettings.Location.LOCATION_BLOCKED
        SitePermissions.Status.NO_DECISION -> InfernoSettings.Location.LOCATION_ASK_TO_ALLOW
        SitePermissions.Status.ALLOWED -> InfernoSettings.Location.LOCATION_ALLOWED
    }
}

fun InfernoSettings.Location.toLocationValue(): SitePermissions.Status {
    return when (this) {
        InfernoSettings.Location.LOCATION_BLOCKED -> SitePermissions.Status.BLOCKED
        InfernoSettings.Location.LOCATION_ASK_TO_ALLOW -> SitePermissions.Status.NO_DECISION
        InfernoSettings.Location.LOCATION_ALLOWED -> SitePermissions.Status.ALLOWED
    }
}

fun SitePermissions.Status.toMicrophoneSetting(): InfernoSettings.Microphone {
    return when (this) {
        SitePermissions.Status.BLOCKED -> InfernoSettings.Microphone.MICROPHONE_BLOCKED
        SitePermissions.Status.NO_DECISION -> InfernoSettings.Microphone.MICROPHONE_ASK_TO_ALLOW
        SitePermissions.Status.ALLOWED -> InfernoSettings.Microphone.MICROPHONE_ALLOWED
    }
}

fun InfernoSettings.Microphone.toMicrophoneValue(): SitePermissions.Status {
    return when (this) {
        InfernoSettings.Microphone.MICROPHONE_BLOCKED -> SitePermissions.Status.BLOCKED
        InfernoSettings.Microphone.MICROPHONE_ASK_TO_ALLOW -> SitePermissions.Status.NO_DECISION
        InfernoSettings.Microphone.MICROPHONE_ALLOWED -> SitePermissions.Status.ALLOWED
    }
}

fun SitePermissions.Status.toNotificationSetting(): InfernoSettings.Notifications {
    return when (this) {
        SitePermissions.Status.BLOCKED -> InfernoSettings.Notifications.NOTIFICATIONS_BLOCKED
        SitePermissions.Status.NO_DECISION -> InfernoSettings.Notifications.NOTIFICATIONS_ASK_TO_ALLOW
        SitePermissions.Status.ALLOWED -> InfernoSettings.Notifications.NOTIFICATIONS_ALLOWED
    }
}

fun InfernoSettings.Notifications.toNotificationValue(): SitePermissions.Status {
    return when (this) {
        InfernoSettings.Notifications.NOTIFICATIONS_BLOCKED -> SitePermissions.Status.BLOCKED
        InfernoSettings.Notifications.NOTIFICATIONS_ASK_TO_ALLOW -> SitePermissions.Status.NO_DECISION
        InfernoSettings.Notifications.NOTIFICATIONS_ALLOWED -> SitePermissions.Status.ALLOWED
    }
}

fun SitePermissions.Status.toPersistentStorageSetting(): InfernoSettings.PersistentStorage {
    return when (this) {
        SitePermissions.Status.BLOCKED -> InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_BLOCKED
        SitePermissions.Status.NO_DECISION -> InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ASK_TO_ALLOW
        SitePermissions.Status.ALLOWED -> InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ALLOWED
    }
}

fun InfernoSettings.PersistentStorage.toLocalStorageValue(): SitePermissions.Status {
    return when (this) {
        InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_BLOCKED -> SitePermissions.Status.BLOCKED
        InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ASK_TO_ALLOW -> SitePermissions.Status.NO_DECISION
        InfernoSettings.PersistentStorage.PERSISTENT_STORAGE_ALLOWED -> SitePermissions.Status.ALLOWED
    }
}

fun SitePermissions.Status.toCrossSiteCookiesSetting(): InfernoSettings.CrossSiteCookies {
    return when (this) {
        SitePermissions.Status.BLOCKED -> InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_BLOCKED
        SitePermissions.Status.NO_DECISION -> InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ASK_TO_ALLOW
        SitePermissions.Status.ALLOWED -> InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ALLOWED
    }
}

fun InfernoSettings.CrossSiteCookies.toCrossOriginStorageValue(): SitePermissions.Status {
    return when (this) {
        InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_BLOCKED -> SitePermissions.Status.BLOCKED
        InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ASK_TO_ALLOW -> SitePermissions.Status.NO_DECISION
        InfernoSettings.CrossSiteCookies.CROSS_SITE_COOKIES_ALLOWED -> SitePermissions.Status.ALLOWED
    }
}

fun SitePermissions.Status.toDrmControlledContentSetting(): InfernoSettings.DrmControlledContent {
    return when (this) {
        SitePermissions.Status.BLOCKED -> InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_BLOCKED
        SitePermissions.Status.NO_DECISION -> InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ASK_TO_ALLOW
        SitePermissions.Status.ALLOWED -> InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ALLOWED
    }
}

fun InfernoSettings.DrmControlledContent.toMediaKeysSystemAccessValues(): SitePermissions.Status {
    return when (this) {
        InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_BLOCKED -> SitePermissions.Status.BLOCKED
        InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ASK_TO_ALLOW -> SitePermissions.Status.NO_DECISION
        InfernoSettings.DrmControlledContent.DRM_CONTROLLED_CONTENT_ALLOWED -> SitePermissions.Status.ALLOWED
    }
}
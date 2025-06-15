package com.shmibblez.inferno.settings.extensions

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.shmibblez.inferno.R
import com.shmibblez.inferno.addons.AddonPermissionsUpdateRequest
import com.shmibblez.inferno.addons.ui.AddonPermissionsScreen
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.Addon.Companion.isAllURLsPermission

@Composable
fun ExtensionPermissionsDialog(
    onDismiss: () -> Unit,
    addon: Addon,
    openWebsite: (addonSiteUrl: Uri) -> Unit,
) {
    val context = LocalContext.current

    fun addOptionalPermissions(
        addPermissionsRequest: AddonPermissionsUpdateRequest,
        onUpdatePermissionsSuccess: (Addon) -> Unit,
    ) {
        context.components.addonManager.addOptionalPermission(
            addon,
            addPermissionsRequest.optionalPermissions,
            addPermissionsRequest.originPermissions,
            onSuccess = {
                onUpdatePermissionsSuccess(it)
            },
            onError = {
                /** No-Op **/
            },
        )
    }

    fun removeOptionalPermission(
        removePermissionsRequest: AddonPermissionsUpdateRequest,
        onUpdatePermissionsSuccess: (Addon) -> Unit,
    ) {
        context.components.addonManager.removeOptionalPermission(
            addon,
            removePermissionsRequest.optionalPermissions,
            removePermissionsRequest.originPermissions,
            onSuccess = {
                onUpdatePermissionsSuccess(it)
            },
            onError = {
                /** No-Op **/
            },
        )
    }

    val optionalPermissions = rememberSaveable {
        mutableStateOf(addon.translateOptionalPermissions(context))
    }

    val originPermissions = rememberSaveable {
        mutableStateOf(
            addon.optionalOrigins.map {
                Addon.LocalizedPermission(it.name, it)
            },
        )
    }

    // Note: Even if <all_urls> is in optionalPermissions of the extension manifest, it is found in
    // originPermissions of the Addon
    val allSitesHostPermissionsList = rememberSaveable {
        mutableStateOf(
            addon.optionalOrigins.getAllSitesPermissionsList(),
        )
    }

    // Update all of the mutable states when an addon is returned from updating permissions
    val onUpdatePermissionsSuccess: (Addon) -> Unit = { updatedAddon ->
        optionalPermissions.value = updatedAddon.translateOptionalPermissions(context)
        originPermissions.value = updatedAddon.optionalOrigins.map {
            Addon.LocalizedPermission(it.name, it)
        }
        allSitesHostPermissionsList.value =
            updatedAddon.optionalOrigins.getAllSitesPermissionsList()
    }

    InfernoDialog(
        onDismiss = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            AddonPermissionsScreen(
                permissions = addon.translatePermissions(context),
                optionalPermissions = optionalPermissions.value,
                originPermissions = originPermissions.value,
                isAllSitesSwitchVisible = allSitesHostPermissionsList.value.isNotEmpty(),
                isAllSitesEnabled = allSitesHostPermissionsList.value.getOrNull(0)?.granted
                    ?: false,
                onAddOptionalPermissions = { permissionRequest ->
                    addOptionalPermissions(permissionRequest, onUpdatePermissionsSuccess)
                },
                onRemoveOptionalPermissions = { permissionRequest ->
                    removeOptionalPermission(permissionRequest, onUpdatePermissionsSuccess)
                },
                onAddAllSitesPermissions = {
                    addOptionalPermissions(
                        AddonPermissionsUpdateRequest(
                            originPermissions = allSitesHostPermissionsList.value.map { it.name },
                        ),
                        onUpdatePermissionsSuccess,
                    )
                },
                onRemoveAllSitesPermissions = {
                    removeOptionalPermission(
                        AddonPermissionsUpdateRequest(
                            originPermissions = allSitesHostPermissionsList.value.map { it.name },
                        ),
                        onUpdatePermissionsSuccess,
                    )
                },
                onLearnMoreClick = { learnMoreUrl ->
                    openWebsite(learnMoreUrl.toUri())
                },
            )

            // close button
            InfernoOutlinedButton(
                text = stringResource(R.string.tab_tray_inactive_auto_close_button_content_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING)
                    .align(Alignment.BottomCenter),
                onClick = onDismiss
            )
        }
    }
}

private fun <T : List<Addon.Permission>> T.getAllSitesPermissionsList(): List<Addon.Permission> {
    return this.mapNotNull {
        if (it.isAllURLsPermission()) {
            it
        } else {
            null
        }
    }
}
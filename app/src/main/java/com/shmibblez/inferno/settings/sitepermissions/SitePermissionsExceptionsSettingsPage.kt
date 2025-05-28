package com.shmibblez.inferno.settings.sitepermissions

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import mozilla.components.concept.engine.permission.SitePermissions

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SitePermissionsExceptionsSettingsPage(goBack: () -> Unit) {
    // todo: site permissions exceptions

//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    val exceptionSitePermissionManagerState by rememberExceptionSitePermissionManagerState()

    var showPermissionInstructionsDialogFor by remember { mutableStateOf<String?>(null) }
    var showClearSiteSettingsDialogFor by remember { mutableStateOf<SitePermissions?>(null) }
    var showClearAllSiteSettingsDialog by remember { mutableStateOf(false) }

    // todo: move all callbacks for setting permissions and showing dialogs here
    InfernoSettingsPage(
        title = "Site Permission Exceptions", // todo: string res
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            exceptionSitePermissionManager(
                state = exceptionSitePermissionManagerState,
                onRequireSettingsInstructions = { showPermissionInstructionsDialogFor = it },
                onClearSitePermissionsClicked = { showClearSiteSettingsDialogFor = it },
                onClearAllSitePermissionsClicked = { showClearAllSiteSettingsDialog = true },
            )
        }

        if (showPermissionInstructionsDialogFor != null) {
            PermissionsSettingsInstructionsDialog(
                onDismiss = { showPermissionInstructionsDialogFor = null },
                settingName = showPermissionInstructionsDialogFor!!
            )
        } else if (showClearSiteSettingsDialogFor != null) {
            ClearSitePermissionsDialog(onDismiss = { showClearSiteSettingsDialogFor = null },
                onConfirm = {
                    exceptionSitePermissionManagerState.deleteSitePermissions(
                        showClearSiteSettingsDialogFor!!
                    )
                })
        } else if (showClearAllSiteSettingsDialog) {
            ClearAllSitePermissionsDialog(onDismiss = { showClearAllSiteSettingsDialog = false },
                onConfirm = { exceptionSitePermissionManagerState.deleteAllSitePermissions() })
        }
    }
}
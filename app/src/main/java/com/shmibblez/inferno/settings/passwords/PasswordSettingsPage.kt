package com.shmibblez.inferno.settings.passwords

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
import com.shmibblez.inferno.biometric.BiometricPromptCallbackManager
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.settings.logins.SavedLogin
import com.shmibblez.inferno.settings.sitepermissions.SitePermissionsSettingsPage
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PasswordSettingsPage(
    goBack: () -> Unit,
    biometricPromptCallbackManager: BiometricPromptCallbackManager,
    onNavToPasswordExceptionSettingsPage: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    val loginManagerState by rememberLoginManagerState(
        biometricPromptCallbackManager = biometricPromptCallbackManager,
    )
    // pair of whether to create and login to copy in case of edit
    var editLoginFor by remember { mutableStateOf<Pair<Boolean, SavedLogin?>?>(null) }

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_sync_logins),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // saved logins
            item { PreferenceTitle(stringResource(R.string.preferences_passwords_saved_logins_2)) }

            // save logins settings
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_passwords_save_logins_2),
                    summary = stringResource(
                        R.string.preferences_passwords_autofill_description,
                        context.getString(R.string.app_name),
                    ),
                    selected = settings.isLoginSaveAndAutofillEnabled,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setIsLoginSaveAndAutofillEnabled(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // android autofill
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_android_autofill),
                    summary = stringResource(R.string.preferences_android_autofill_description),
                    selected = settings.isAndroidAutofillEnabled,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setIsAndroidAutofillEnabled(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // todo: urgent, use biometric to authenticate
            // login manager, shows all logins
            loginManager(
                state = loginManagerState,
                onAddLoginClicked = { editLoginFor = true to null },
                onEditLoginClicked = { editLoginFor = false to it },
                onDeleteLoginClicked = { loginManagerState.deleteLogin(it.guid) },
            )

            // exceptions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
                        .padding(
                            top = PrefUiConst.PREFERENCE_VERTICAL_PADDING * 2F,
                            bottom = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                        )
                        .clickable {
                            /**
                             * todo: implement exceptions page
                             *  check: [SitePermissionsSettingsPage]
                             */
                            /**
                             * todo: implement exceptions page
                             *  check: [SitePermissionsSettingsPage]
                             */
                            onNavToPasswordExceptionSettingsPage.invoke()
                        },
                    horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
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

        if (editLoginFor != null) {
            LoginEditorDialog(
                create = editLoginFor!!.first,
                initialLogin = editLoginFor!!.second,
                onAddLogin = { login -> loginManagerState.addLogin(login) },
                onSaveLogin = { guid, login -> loginManagerState.updateLogin(guid, login) },
                onDismiss = { editLoginFor = null }
            )
        }
    }
}
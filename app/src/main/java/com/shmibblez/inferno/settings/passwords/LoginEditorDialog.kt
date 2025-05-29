package com.shmibblez.inferno.settings.passwords

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.settings.logins.SavedLogin
import mozilla.components.concept.storage.LoginEntry

@Composable
fun LoginEditorDialog(
    create: Boolean,
//    storage: AutofillCreditCardsAddressesStorage,
    initialLogin: SavedLogin?,
    onAddLogin: (login: LoginEntry) -> Unit,
    onSaveLogin: (guid: String, login: LoginEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var hostname by remember { mutableStateOf(initialLogin?.origin ?: "") }
    var hostnameError by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf(initialLogin?.username ?: "") }
    var password by remember { mutableStateOf(initialLogin?.password ?: "") }
    var passwordError by remember { mutableStateOf<String?>(null) }


    // true if all good, false if something wrong
    fun validateFields(): Boolean {
        var allValid = true
        // check hostname
        when {
            hostname.isEmpty() || !Patterns.WEB_URL.matcher(hostname).matches() -> {
                allValid = false
                hostnameError = context.getString(R.string.add_login_hostname_invalid_text_2)
            }

            else -> {
                hostnameError = null
            }
        }
        when {
            password.isEmpty() -> {
                allValid = false
                passwordError = context.getString(R.string.saved_login_password_required_2)
            }

            else -> {
                passwordError = null
            }
        }

        return allValid
    }

    InfernoDialog(onDismiss = onDismiss) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        ) {
            // hostname / site
            item {
                InfernoOutlinedTextField(
                    value = hostname,
                    onValueChange = {
                        hostname = it
                        validateFields()
                    },
                    label = {
                        InfernoText(
                            stringResource(R.string.preferences_passwords_saved_logins_site),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                    placeholder = {
                        InfernoText(
                            text = stringResource(R.string.add_login_hostname_hint_text),
                            fontColor = context.infernoTheme().value.secondaryTextColor,
                        )
                    },
                    isError = hostnameError != null,
                    supportingText = {
                        if (hostnameError != null) {
                            InfernoText(
                                text = hostnameError!!,
                                infernoStyle = InfernoTextStyle.Error,
                                fontSize = 12.sp,
                            )
                        } else {
                            InfernoText(
                                text = stringResource(R.string.add_login_hostname_invalid_text_3),
                                fontColor = context.infernoTheme().value.secondaryTextColor,
                                fontSize = 12.sp,
                            )
                        }
                    },
                )
            }

            // username
            item {
                InfernoOutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        InfernoText(
                            stringResource(R.string.preferences_passwords_saved_logins_username),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                )
            }

            // password
            item {
                InfernoOutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        validateFields()
                    },
                    label = {
                        InfernoText(
                            stringResource(R.string.preferences_passwords_saved_logins_password),
                            fontColor = context.infernoTheme().value.primaryOutlineColor,
                        )
                    },
                    isError = passwordError != null,
                    supportingText = {
                        if (passwordError != null) {
                            InfernoText(
                                text = passwordError!!, infernoStyle = InfernoTextStyle.Error
                            )
                        }
                    },
                )
            }
        }

        // cancel / save buttons
        Row(
            modifier = Modifier.padding(vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING),
            horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        ) {
            InfernoOutlinedButton(
                modifier = Modifier.weight(1F),
                text = stringResource(android.R.string.cancel),
                onClick = onDismiss,
            )
            InfernoButton(
                modifier = Modifier.weight(1F),
                text = stringResource(R.string.browser_menu_save),
                enabled = hostnameError == null && passwordError == null,
                onClick = {
                    // if field invalid return (dont save)
                    if (!validateFields()) return@InfernoButton

                    // if all good create or update
                    val hostnameStr = hostname.trim()
                    val usernameStr = username.trim()
                    val passwordStr = password

                    when (create) {
                        true -> {
                            // create card
                            val login = LoginEntry(
                                origin = hostnameStr,
                                username = usernameStr,
                                password = passwordStr,
                            )
                            onAddLogin.invoke(login)
                        }

                        false -> {
                            // update card
                            val login = LoginEntry(
                                origin = hostnameStr,
                                username = usernameStr,
                                password = passwordStr,
                            )
                            onSaveLogin(initialLogin!!.guid, login)
                        }
                    }
                    onDismiss.invoke()
                },
            )
        }
    }
}
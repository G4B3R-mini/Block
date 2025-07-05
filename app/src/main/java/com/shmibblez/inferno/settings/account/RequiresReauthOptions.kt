package com.shmibblez.inferno.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.accounts.FenixFxAEntryPoint
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import mozilla.components.service.fxa.manager.SCOPE_PROFILE
import mozilla.components.service.fxa.manager.SCOPE_SYNC

/**
 * todo: reference [AccountProblemFragment]
 *  sign out or reauth options
 */
@Composable
internal fun RequiresReauthOptions(edgeInsets: PaddingValues, onSignOut: () -> Unit) {
    val context = LocalContext.current

    fun beginAuth() {
        context.components.services.accountsAuthFeature.beginAuthentication(
            context,
            FenixFxAEntryPoint.SettingsMenu,
            setOf(SCOPE_PROFILE, SCOPE_SYNC),
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(edgeInsets),
        horizontalAlignment = Alignment.Start,
    ) {
        item {
            PreferenceTitle(stringResource(R.string.preferences_account_settings))
        }

        // button spacer
        item {
            Spacer(Modifier.height(16.dp))
        }

        // sign in button
        item {
            InfernoOutlinedButton(
                onClick = ::beginAuth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp, Alignment.CenterHorizontally
                    ),
                ) {
                    // sign in icon
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_sign_in),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                    )
                    // sign in text
                    InfernoText(stringResource(R.string.preferences_sync_sign_in_to_reconnect))
                }
            }
        }

        // button spacer
        item {
            Spacer(Modifier.height(16.dp))
        }

        // remove account button
        item {
            InfernoOutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
                    ),
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = LocalContext.current.infernoTheme().value.errorColor,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = LocalContext.current.infernoTheme().value.errorColor,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp, Alignment.CenterHorizontally
                    ),
                ) {
                    // trash icon
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_delete_24),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                        tint = LocalContext.current.infernoTheme().value.errorColor,
                    )
                    // remove account text
                    InfernoText(
                        text = stringResource(R.string.preferences_sync_remove_account),
                        fontColor = LocalContext.current.infernoTheme().value.errorColor,
                    )
                }
            }
        }
    }
}
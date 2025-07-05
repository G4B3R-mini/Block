package com.shmibblez.inferno.settings.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun AccountView(
    state: AccountState,
    onNavToAccountSettings: () -> Unit,
) {
    when (state.authState) {
        AccountState.AccountAuthState.SignedIn -> {
            SignedInComponent(state = state, onClick = onNavToAccountSettings)
        }

        AccountState.AccountAuthState.RequiresReauth -> {
            ReauthComponent(state = state, onClick = onNavToAccountSettings)
        }

        AccountState.AccountAuthState.SignedOut -> {
            SignedOutComponent(onClick = onNavToAccountSettings)
        }

        null -> {} // no-op, loading
    }
}

@Composable
private fun SignedInComponent(
    state: AccountState,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var avatar by remember { mutableStateOf(state.genericAvatar(context)) }
    val avatarUrl = state.profile?.avatar?.url
    var avatarJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(avatarUrl) {
        if (avatarUrl != null) {
            avatarJob = scope.launch {
                avatar = state.toRoundedDrawable(avatarUrl, context) ?: state.genericAvatar(context)
            }
        } else {
            avatar = state.genericAvatar(context)
        }

        onDispose {
            avatarJob?.cancel()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .background(
                color = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // avatar
        Image(
            bitmap = avatar,
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape),
        )
        // account info
        Column(
            modifier = Modifier.height(72.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // display name
            InfernoText(
                text = state.profile?.displayName ?: "Loading...", // todo: string res
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.Bold,
            )
            // email
            InfernoText(
                text = state.profile?.email ?: "Loading...", // todo: string res
                infernoStyle = InfernoTextStyle.SmallSecondary,
            )
        }
    }
}

@Composable
private fun ReauthComponent(state: AccountState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .background(
                color = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // account icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_account_warning),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = LocalContext.current.infernoTheme().value.errorColor,
        )
        // messages
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // error message
            InfernoText(
                text = stringResource(R.string.preferences_account_sync_error),
                fontColor = LocalContext.current.infernoTheme().value.errorColor,
            )
            // email
            state.profile?.email?.let {
                InfernoText(
                    text = it,
                    infernoStyle = InfernoTextStyle.Small,
                    fontColor = LocalContext.current.infernoTheme().value.errorColor,
                    maxLines = 4,
                )
            }
        }
    }
}

@Composable
private fun SignedOutComponent(onClick: () -> Unit) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .background(
                color = context.infernoTheme().value.secondaryBackgroundColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // account icon
        Image(
            bitmap = ResourcesCompat.getDrawable(
                context.resources, R.drawable.ic_fx_accounts_avatar, null
            )!!.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // title
            InfernoText(
                text = stringResource(R.string.preferences_sync_2),
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.Bold,
            )
            // summary
            InfernoText(
                text = stringResource(R.string.preferences_sign_in_description_2),
                infernoStyle = InfernoTextStyle.SmallSecondary,
            )
        }
    }
}
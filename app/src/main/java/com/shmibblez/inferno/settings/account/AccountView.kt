package com.shmibblez.inferno.settings.account

import android.content.Context
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.infernoFeatureState.InfernoFeatureState
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.bitmapForUrl
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mozilla.components.concept.fetch.Client
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.concept.sync.Profile
import mozilla.components.service.fxa.manager.FxaAccountManager

//import com.shmibblez.inferno.proto.InfernoSettings
//import com.shmibblez.inferno.proto.infernoSettingsDataStore

class AccountState(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    profile: Profile?,
    val scope: CoroutineScope,
    val accountManager: FxaAccountManager,
    private val httpClient: Client,
    private val updateFxAAllowDomesticChinaServerMenu: () -> Unit,
) : InfernoFeatureState {

    enum class AccountAuthState {
        SIGNED_OUT, SIGNED_IN, REQUIRES_REAUTH,
    }

    var authState: AccountAuthState? by mutableStateOf(null)
        private set
    fun isSignedIn() = authState == AccountAuthState.SIGNED_IN
    fun requiresReauth() = authState == AccountAuthState.SIGNED_IN
    fun isSignedOut() = authState == AccountAuthState.SIGNED_IN


    val profile by mutableStateOf(profile)

    private val accountObserver = object : AccountObserver {
        private fun updateAccountUi(profile: Profile? = null) {
            lifecycleOwner.lifecycleScope.launch {
                updateAccountState(
                    context = context,
                    profile = profile
                        ?: context.components.backgroundServices.accountManager.accountProfile(),
                )
            }
        }

        override fun onAuthenticated(account: OAuthAccount, authType: AuthType) = updateAccountUi()

        override fun onLoggedOut() = updateAccountUi()
        override fun onProfileUpdated(profile: Profile) = updateAccountUi(profile)
        override fun onAuthenticationProblems() = updateAccountUi()
    }

    override fun start() {
        context.components.backgroundServices.accountManager.register(
            accountObserver,
            owner = lifecycleOwner,
            autoPause = true,
        )
    }

    override fun stop() {
        context.components.backgroundServices.accountManager.unregister(accountObserver)
    }

    fun updateAccountState(context: Context, profile: Profile?) {
        val account = accountManager.authenticatedAccount()

        updateFxAAllowDomesticChinaServerMenu()

        // Signed-in, no problems.
        authState = when {
            account != null && !accountManager.accountNeedsReauth() -> AccountAuthState.SIGNED_IN
            account != null && accountManager.accountNeedsReauth() -> AccountAuthState.REQUIRES_REAUTH
            else -> AccountAuthState.SIGNED_OUT
        }
    }

    /**
     * Returns generic avatar for accounts.
     */
    internal fun genericAvatar(context: Context) =
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_account).asImageBitmap()


    /**
     * Gets a rounded drawable from a URL if possible, else null.
     */
    internal suspend fun toRoundedDrawable(
        url: String,
        context: Context,
    ) = httpClient.bitmapForUrl(url)?.let { bitmap ->
        RoundedBitmapDrawableFactory.create(context.resources, bitmap).apply {
            isCircular = true
            setAntiAlias(true)
        }.bitmap?.asImageBitmap()
    }
}

@Composable
fun rememberAccountState(
    profile: Profile?,
    scope: CoroutineScope,
    accountManager: FxaAccountManager,
    httpClient: Client,
    updateFxAAllowDomesticChinaServerMenu: () -> Unit,
): AccountState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = AccountState(
        context = context,
        lifecycleOwner = lifecycleOwner,
        profile = profile,
        scope = scope,
        accountManager = accountManager,
        httpClient = httpClient,
        updateFxAAllowDomesticChinaServerMenu = updateFxAAllowDomesticChinaServerMenu,
    )

    DisposableEffect(null) {
        state.start()

        onDispose {
            state.stop()
        }
    }

    return remember { state }
}

@Composable
fun AccountView(
    state: AccountState,
    onNavigateSignedIn: () -> Unit,
    onNavigateRequiresReauth: () -> Unit,
    onNavigateSignedOut: () -> Unit,
) {
    when (state.authState) {
        AccountState.AccountAuthState.SIGNED_IN -> {
            SignedInComponent(state = state, onClick = onNavigateSignedIn)
        }

        AccountState.AccountAuthState.REQUIRES_REAUTH -> {
            ReauthComponent(state = state, onClick = onNavigateRequiresReauth)
        }

        AccountState.AccountAuthState.SIGNED_OUT -> {
            SignedOutComponent(onClick = onNavigateSignedOut)
        }

        null -> { /* no-op, loading */
        }
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
            .clickable(onClick = onClick)
            .padding(16.dp)
            .background(
                color = Color.DarkGray,
                shape = MaterialTheme.shapes.medium,
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // avatar
        Image(
            bitmap = avatar,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Column(
            modifier = Modifier.height(72.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // display name
            InfernoText(
                text = state.profile?.displayName ?: "",
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.Bold,
            )
            // email
            InfernoText(
                text = state.profile?.email ?: "",
            )
        }
    }
}

@Composable
private fun ReauthComponent(state: AccountState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(colorResource(R.color.sync_error_background_color))
            .fillMaxWidth()
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
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // error message
            InfernoText(
                text = stringResource(R.string.preferences_account_sync_error),
                fontSize = 16.sp,
                fontColor = colorResource(R.color.sync_error_text_color),
            )
            // email
            if (state.profile?.email != null) {
                InfernoText(
                    text = state.profile?.email ?: "",
                    fontColor = colorResource(R.color.sync_error_text_color),
                    maxLines = 4,
                )
            }
        }
    }
}

@Composable
private fun SignedOutComponent(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = Color.DarkGray,
                shape = MaterialTheme.shapes.medium,
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // account icon
        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.ic_fx_accounts_avatar),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Column(
            modifier = Modifier.height(72.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
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
            )
        }
    }
}
package com.shmibblez.inferno.settings.account

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.infernoFeatureState.InfernoFeatureState
import com.shmibblez.inferno.ext.bitmapForUrl
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.history.ConsecutiveUniqueJobHandler
import kotlinx.coroutines.CoroutineScope
import mozilla.components.concept.fetch.Client
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthFlowError
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.concept.sync.Profile
import mozilla.components.service.fxa.manager.FxaAccountManager
import mozilla.components.service.fxa.sync.SyncReason

class AccountState(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val scope: CoroutineScope,
    val accountManager: FxaAccountManager,
    private val httpClient: Client,
    private val updateFxAAllowDomesticChinaServerMenu: () -> Unit,
) : InfernoFeatureState {

    private enum class JobType {
        SYNC,
    }

    private val taskManager = ConsecutiveUniqueJobHandler<JobType>(scope)

    sealed interface AccountAuthState {
        data object SignedOut: AccountAuthState
        data object SignedIn : AccountAuthState
        data object RequiresReauth: AccountAuthState
    }

    var authState: AccountAuthState? by mutableStateOf(null)
        private set

    var isSyncing by mutableStateOf(false)
    private set

    fun isSignedIn() = authState is AccountAuthState.SignedIn
    fun requiresReauth() = authState is AccountAuthState.RequiresReauth
    fun isSignedOut() = authState is AccountAuthState.SignedOut


    var profile by mutableStateOf<Profile?>(null)

    private val accountObserver = object : AccountObserver {

        override fun onFlowError(error: AuthFlowError) {
            Log.d("AccountState", "AccountObserver, onFlowError, error: $error")
            updateAccountState()
        }

        override fun onReady(authenticatedAccount: OAuthAccount?) {
            Log.d(
                "AccountState",
                "AccountObserver, onReady, authenticatedAccount: $authenticatedAccount"
            )
            updateAccountState()
        }

        override fun onAuthenticated(account: OAuthAccount, authType: AuthType) {
            Log.d("AccountState", "AccountObserver, onAuthenticated, profile: $profile")
            updateAccountState()
        }

        override fun onLoggedOut() {
            Log.d("AccountState", "AccountObserver, onLoggedOut, profile: $profile")
            updateAccountState()
        }

        override fun onProfileUpdated(profile: Profile) {
            Log.d("AccountState", "AccountObserver, onProfileUpdated, profile: $profile")
//            updateAccountState(profile)
            updateAccountState()
        }

        override fun onAuthenticationProblems() {
            Log.d("AccountState", "AccountObserver, onAuthenticationProblems, profile: $profile")
            updateAccountState()
        }
    }

    override fun start() {
        Log.d("AccountState", "AccountObserver, start()")
        // init state
        profile = accountManager.accountProfile()
        updateAccountState()

        // register listener
        context.components.backgroundServices.accountManager.register(
            accountObserver,
            owner = lifecycleOwner,
            autoPause = true,
        )
//        authState = AccountAuthState.SIGNED_OUT
    }

    override fun stop() {
        Log.d("AccountState", "AccountObserver, stop()")
        context.components.backgroundServices.accountManager.unregister(accountObserver)
    }

    fun syncNow() {
        taskManager.processTask(
            type = JobType.SYNC,
            task = {accountManager.syncNow(SyncReason.User)},
            onBegin = { isSyncing = true },
            onComplete = { isSyncing = false }
        )
    }

    fun updateAccountState() {
        val account = accountManager.authenticatedAccount()

        updateFxAAllowDomesticChinaServerMenu()

        // Signed-in, no problems.
        authState = when {
            account != null && !accountManager.accountNeedsReauth() -> AccountAuthState.SignedIn
            account != null && accountManager.accountNeedsReauth() -> AccountAuthState.RequiresReauth
            else -> AccountAuthState.SignedOut
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
    scope: CoroutineScope = rememberCoroutineScope(),
    accountManager: FxaAccountManager = LocalContext.current.components.backgroundServices.accountManager,
    httpClient: Client = LocalContext.current.components.core.client,
    updateFxAAllowDomesticChinaServerMenu: () -> Unit = {},
): MutableState<AccountState> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember {
        mutableStateOf(
            AccountState(
                context = context,
                lifecycleOwner = lifecycleOwner,
                scope = scope,
                accountManager = accountManager,
                httpClient = httpClient,
                updateFxAAllowDomesticChinaServerMenu = updateFxAAllowDomesticChinaServerMenu,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()

        onDispose { state.value.stop() }
    }

    return state
}
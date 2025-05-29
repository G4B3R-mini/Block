package com.shmibblez.inferno.settings.passwords

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.shmibblez.inferno.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.settings.logins.SavedLogin
import com.shmibblez.inferno.settings.logins.mapToSavedLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mozilla.appservices.logins.LoginsApiException
import mozilla.components.concept.storage.LoginEntry
import mozilla.components.service.sync.logins.SyncableLoginsStorage
import mozilla.components.support.base.feature.LifecycleAwareFeature

private val ICON_SIZE = 18.dp

// todo:
//  - add biometric / pin auth
//  - add sort logins in refreshLogins()
//  - check duplicates not implemented
//  - login search not implemented
internal class LoginManagerState(
    val storage: SyncableLoginsStorage,
    val coroutineScope: CoroutineScope,
    initiallyExpanded: Boolean = true,
) : LifecycleAwareFeature {

    private val jobs: MutableList<Job> = mutableListOf()

    internal var logins by mutableStateOf(emptyList<SavedLogin>())

    val isLoading: Boolean
        get() = jobs.isNotEmpty()

    var expanded: Boolean by mutableStateOf(initiallyExpanded)


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

    // refreshes logins list
    private fun refreshLogins() {
        // just copying moz implementation, reasoning below
        // Don't touch the store if we already have the logins loaded.
        // This has a slight downside of possibly being out of date with the storage if, say, Sync
        // ran in the meantime, but that's fairly unlikely and the speedy UI is worth it.
//        if (logins.isNotEmpty()) return
        // this runs anyway lol, might have to re-add above line if too slow
        launchSuspend {
            try {
                logins = storage.list().map { it.mapToSavedLogin() }
            } catch (e: LoginsApiException) {
                Log.e("loginManager", "called from refreshLogins(): $e")
            }
        }
    }

    fun addLogin(login: LoginEntry) {
        launchSuspend {
            try {
                storage.add(login)
                refreshLogins()
            } catch (e: LoginsApiException) {
                Log.e("loginManager", "called from addLogin(): $e")
            }
        }
    }

    fun updateLogin(guid: String, login: LoginEntry) {
        launchSuspend {
            try {
                storage.update(guid, login)
                refreshLogins()
            } catch (e: LoginsApiException) {
                Log.e("loginManager", "called from updateLogin(): $e")
            }
        }
    }

    fun deleteLogin(guid: String) {
        launchSuspend {
            try {
                storage.delete(guid)
                refreshLogins()
            } catch (e: LoginsApiException) {
                Log.e("loginManager", "called from deleteLogin(): $e")
            }
        }
    }

    override fun start() {
        refreshLogins()
    }

    override fun stop() {
        jobs.forEach { it.cancel() }
    }

}

@Composable
internal fun rememberLoginManagerState(): MutableState<LoginManagerState> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state = remember {
        mutableStateOf(
            LoginManagerState(
                storage = context.components.core.passwordsStorage, coroutineScope = coroutineScope
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

internal fun LazyListScope.loginManager(
    state: LoginManagerState,
    onAddLoginClicked: () -> Unit,
    onEditLoginClicked: (SavedLogin) -> Unit,
    onDeleteLoginClicked: (SavedLogin) -> Unit,
) {
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { state.expanded = !state.expanded }
                .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
                .padding(
                    top = PrefUiConst.PREFERENCE_HALF_VERTICAL_PADDING,
                    bottom = if (state.expanded) 0.dp else PrefUiConst.PREFERENCE_HALF_VERTICAL_PADDING,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InfernoText(text = stringResource(R.string.mozac_feature_prompts_manage_logins_2))
            InfernoIcon(
                painter = when (state.expanded) {
                    true -> painterResource(R.drawable.ic_chevron_up_24)
                    false -> painterResource(R.drawable.ic_chevron_down_24)
                },
                contentDescription = "",
                modifier = Modifier.size(ICON_SIZE),
            )
        }
    }
    if (state.expanded) {
        items(state.logins) {
            LoginItem(
                login = it,
                onEditLoginClicked = onEditLoginClicked,
                onDeleteLoginClicked = onDeleteLoginClicked,
            )
        }
        item {
            AddLoginItem(onAddLoginClicked = onAddLoginClicked)
        }
    }
    item {
        Spacer(
            modifier = Modifier.padding(bottom = PrefUiConst.PREFERENCE_HALF_VERTICAL_PADDING),
        )
    }
}

@Composable
private fun LoginItem(
    login: SavedLogin,
    onEditLoginClicked: (SavedLogin) -> Unit,
    onDeleteLoginClicked: (SavedLogin) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // edit icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_edit_24),
            contentDescription = null,
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable { onEditLoginClicked.invoke(login) },
        )

        // login info
        Column(modifier = Modifier.weight(1F)) {
            InfernoText(
                text = login.username,
                fontWeight = FontWeight.Bold,
            )
            InfernoText(
                text = login.password,
                infernoStyle = InfernoTextStyle.Subtitle,
                maxLines = 2,
            )
        }

        // delete icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_delete_24),
            contentDescription = null,
            modifier = Modifier
                .size(ICON_SIZE)
                .clickable { onDeleteLoginClicked.invoke(login) },
        )
    }
}

@Composable
private fun AddLoginItem(onAddLoginClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onAddLoginClicked.invoke() }
            .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // add icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_new_24),
            contentDescription = null,
            modifier = Modifier.size(ICON_SIZE),
        )

        // add login text
        InfernoText(text = stringResource(R.string.add_login_2))
    }
}

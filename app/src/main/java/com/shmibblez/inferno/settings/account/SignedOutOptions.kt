package com.shmibblez.inferno.settings.account

import android.Manifest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.accounts.FenixFxAEntryPoint
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.settings.PairFragment
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import mozilla.components.service.fxa.manager.SCOPE_PROFILE
import mozilla.components.service.fxa.manager.SCOPE_SYNC
import mozilla.components.support.ktx.android.content.isPermissionGranted
import mozilla.components.support.ktx.android.view.hideKeyboard

/**
 * todo: reference [TurnOnSyncFragment]
 *  also for pairing with qr: [PairFragment]
 *
 *  currently normal sign in is supported, qr requires some setup, coming soon
 */
@Composable
internal fun SignedOutOptions(edgeInsets: PaddingValues) {
    val context = LocalContext.current

    /**
     * launches auth custom tab intent
     */
    fun navigateToPairWithEmail() {
        context.components.services.accountsAuthFeature.beginAuthentication(
            context,
            entrypoint = FenixFxAEntryPoint.SettingsMenu,
            setOf(SCOPE_PROFILE, SCOPE_SYNC),
        )
        // TODO The sign-in web content populates session history,
        // so pressing "back" after signing in won't take us back into the settings screen, but rather up the
        // session history stack.
        // We could auto-close this tab once we get to the end of the authentication process?
        // Via an interceptor, perhaps.
    }

//    fun onCameraPermissionsNeeded(syncController: DefaultSyncController) {
//        syncController.handleCameraPermissionsNeeded()
//    }
//
//    fun requestPairing() {
//        if (context.settings().shouldShowCameraPermissionPrompt) {
//            navigateToPairFragment()
//        } else {
//            if (context.isPermissionGranted(Manifest.permission.CAMERA)) {
//                navigateToPairFragment()
//            } else {
//                interactor.onCameraPermissionsNeeded()
//                view?.hideKeyboard()
//            }
//        }
//        // todo: hide keyboard
////        view?.hideKeyboard()
//        context.settings().setCameraPermissionNeededState = false
//    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(edgeInsets),
        horizontalAlignment = Alignment.Start,
    ) {

        // sign in with camera under development
        // todo: remove this when qr login is supported
        item {
            InfernoText(
                text = "Login with qr code is currently under development >:(",
                infernoStyle = InfernoTextStyle.Title,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING,
                    ),
            )
        }

        // todo: sign in with qr
//        // sign in title
//        item {
//            InfernoText(
//                text = stringResource(R.string.sign_in_with_camera),
//                infernoStyle = InfernoTextStyle.Title,
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(
//                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
//                        vertical = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING,
//                    ),
//            )
//        }
//
//        // sign in with camera picture
//        item {
//            Box(
//                modifier = Modifier.fillMaxWidth(),
//                contentAlignment = Alignment.Center,
//            ) {
//                Image(
//                    painter = painterResource(R.drawable.ic_scan),
//                    contentDescription = "",
//                    modifier = Modifier.size(width = 187.dp, height = 171.dp),
//                )
//            }
//        }
//
//        // sign in instructions
//        item {
//            // todo: add styling to link?
//            InfernoText(
//                text = stringResource(R.string.sign_in_instructions),
//                modifier = Modifier.padding(
//                    horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
//                    vertical = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING
//                ),
//            )
//        }
//
//        // sign in scan button
//        item {
//            InfernoButton(
//                text = stringResource(R.string.sign_in_ready_for_scan),
//                onClick = {
//                    // todo
//                },
//                leadingIcon = {
//                    InfernoIcon(
//                        painter = painterResource(R.drawable.ic_qr),
//                        contentDescription = "",
//                        modifier = Modifier.size(18.dp),
//                    )
//                },
//                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(
//                    horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
//                    vertical = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING
//                ),
//            )
//        }

        // sign in with email instead
        item {
            InfernoOutlinedButton(
                text = stringResource(R.string.sign_in_with_email),
                onClick = ::navigateToPairWithEmail,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                        vertical = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING
                    ),
            )
        }

        // create account text
        item {
            // todo: add clickable link to text, goes to browser
            InfernoText(
                text = stringResource(R.string.sign_in_create_account_text),
                modifier = Modifier.padding(
                    horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                    vertical = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING
                ),
            )
        }
    }
}
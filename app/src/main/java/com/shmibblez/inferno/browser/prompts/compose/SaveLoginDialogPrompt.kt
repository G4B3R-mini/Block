package com.shmibblez.inferno.browser.prompts.compose

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.emitCancelFact
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.emitSaveFact
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.LoginEntry

// todo: state expanded may be necessary
// todo: finish logic & review layout (padding & margin)
// todo: add todo message to all prompt components that havent been tested
//     ex: // todo: requires testing, test with dummy data prompt objects, visible only for testing
@Composable
fun SaveLoginDialogPrompt(
    loginData: PromptRequest.SaveLoginPrompt, sessionId: String, icon: Bitmap? = null
) {
    val store = LocalContext.current.components.core.store
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    PromptBottomSheetTemplate(
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
        onDismissRequest = {
            onDismiss(loginData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, loginData))
        },
        negativeAction = PromptBottomSheetTemplateAction(text = stringResource(R.string.mozac_feature_prompt_never_save),
            action = {
                onNegativeAction(loginData)
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, loginData))
                emitCancelFact()
            }),
        positiveAction = PromptBottomSheetTemplateAction(text = stringResource(R.string.mozac_feature_prompt_save_confirmation),
            action = {
                onPositiveAction(loginData, LoginEntry(
                    origin = ,
                    formActionOrigin = ,
                    httpRealm = ,
                    username = username,
                    password = password,
                ))
                store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, loginData))
                emitSaveFact()
            }),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // globe icon
            Icon(
                bitmap = icon?.asImageBitmap()?:painterResource(R.drawable.mozac_ic_globe_24),
                tint = Color.White,
                contentDescription = "",
                modifier = Modifier.size(24.dp),
            )
            // host name
            // todo: text color secondary
            InfernoText(
                modifier = Modifier.weight(1F),
                text = TODO("host name"),
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                fontColor = Color.White,
            )
        }
        // save message
        // todo: text color primary
        InfernoText(
            modifier = Modifier
                .weight(1F)
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.mozac_feature_prompt_login_save_headline_2),
            textAlign = TextAlign.Start,
            fontSize = 18.sp,
            fontColor = Color.White,
        )
        // username
        // todo: text color primary

        OutlinedTextField(
            modifier = Modifier
                .weight(1F)
                .padding(horizontal = 16.dp),
            value = username,
            onValueChange = { username = it },
            label = { InfernoText(stringResource(R.string.mozac_feature_prompt_username_hint)) },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
            ),
        )
        // password
        // todo: text color primary
        // todo: toggle password visibility
        OutlinedTextField(
            modifier = Modifier
                .weight(1F)
                .padding(horizontal = 16.dp),
            value = password,
            onValueChange = { password = it },
            label = { InfernoText(stringResource(R.string.mozac_feature_prompt_password_hint)) },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
            ),
        )
    }
}
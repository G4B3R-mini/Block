package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.Favicon
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.LoginEntry
import mozilla.components.concept.storage.LoginValidationDelegate
import mozilla.components.concept.storage.LoginValidationDelegate.Result
import mozilla.components.support.base.log.logger.Logger
import kotlin.coroutines.cancellation.CancellationException

// todo: state expanded may be necessary
// todo: finish logic & review layout (padding & margin)
// todo: add todo message to all prompt components that havent been tested
//     ex: // todo: requires testing, test with dummy data prompt objects, visible only for testing
@Composable
fun SaveLoginDialogPrompt(
    promptRequest: PromptRequest.SaveLoginPrompt,
    icon: Bitmap? = null,
    url: String?,
    onShowSnackbarAfterLoginChange: (Boolean) -> Unit,
    loginValidationDelegate: LoginValidationDelegate?,
    onCancel: () -> Unit,
    onConfirm: (LoginEntry) -> Unit,
) {
    val context = LocalContext.current
    var shouldRender by remember { mutableStateOf(false) }/*
     * If an implementation of [LoginExceptions] is hooked up to [PromptFeature], we will not
     * show this save login dialog for any origin saved as an exception.
     */
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(promptRequest) {
        CoroutineScope(IO).launch {
            if (context.components.core.loginExceptionStorage.isLoginExceptionByOrigin(
                    promptRequest.logins[0].origin
                )
            ) {
                onCancel.invoke()
            } else {
                shouldRender = true
            }
        }
    }
    if (!shouldRender) return
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginValid by remember { mutableStateOf(password.isEmpty()) }
    var validateStateUpdate by remember { mutableStateOf<Job?>(null) }
    var isUpdate by remember { mutableStateOf(false) }
    var headline by remember { mutableStateOf(context.getString(R.string.mozac_feature_prompt_login_save_headline_2)) }
    var negativeText by remember { mutableStateOf(context.getString(R.string.mozac_feature_prompt_never_save)) }
    var confirmText by remember { mutableStateOf(context.getString(R.string.mozac_feature_prompt_save_confirmation)) }

    /**
     * Check current state then update view state to match.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun update() = coroutineScope.launch(IO) {
        val entry = LoginEntry(
            origin = promptRequest.logins[0].origin,
            formActionOrigin = promptRequest.logins[0].formActionOrigin,
            httpRealm = promptRequest.logins[0].httpRealm,
            username = username,
            password = password,
        )

        try {
            validateStateUpdate?.cancelAndJoin()
        } catch (cancellationException: CancellationException) {
            Logger.error("Failed to cancel job", cancellationException)
        }

        var validateDeferred: Deferred<Result>?
        validateStateUpdate = launch validate@{
            if (!loginValid) {
                // Don't run the validation logic if we know the login is invalid
                return@validate
            }
            val validationDelegate = loginValidationDelegate ?: return@validate
            validateDeferred = validationDelegate.shouldUpdateOrCreateAsync(entry)
            val result = validateDeferred?.await()
            withContext(Main) {
                when (result) {
                    Result.CanBeCreated -> {
                        isUpdate = false
                        headline =
                            context.getString(R.string.mozac_feature_prompt_login_save_headline_2)
                        negativeText = context.getString(R.string.mozac_feature_prompt_never_save)
                        confirmText =
                            context.getString(R.string.mozac_feature_prompt_save_confirmation)
                    }

                    is Result.CanBeUpdated -> {
                        isUpdate = true
                        headline = if (result.foundLogin.username.isEmpty()) {
                            context.getString(
                                R.string.mozac_feature_prompt_login_add_username_headline_2,
                            )
                        } else {
                            context.getString(R.string.mozac_feature_prompt_login_update_headline_2)
                        }
                        negativeText =
                            context.getString(R.string.mozac_feature_prompt_dont_update_2)
                        confirmText =
                            context.getString(R.string.mozac_feature_prompt_update_confirmation)
                    }

                    else -> {
                        // no-op
                    }
                }
            }
            validateStateUpdate?.invokeOnCompletion {
                if (it is CancellationException) {
                    validateDeferred?.cancel()
                }
            }
        }
    }

    LaunchedEffect(null) {
        update()
    }
    PromptBottomSheetTemplate(
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
        onDismissRequest = onCancel,
        negativeAction = PromptBottomSheetTemplateAction(text = negativeText, action = onCancel),
        positiveAction = PromptBottomSheetTemplateAction(text = confirmText, action = {
            onConfirm.invoke(
                LoginEntry(
                    origin = promptRequest.logins[0].origin,
                    formActionOrigin = promptRequest.logins[0].formActionOrigin,
                    httpRealm = promptRequest.logins[0].httpRealm,
                    username = username,
                    password = password,
                )
            )
            onShowSnackbarAfterLoginChange.invoke(isUpdate)
        }),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // site icon
            when (icon) {
                null -> Favicon(
                    url = url ?: "",
                    size = 24.dp,
                    modifier = Modifier.size(24.dp),
                )

                else -> Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                )
            }

            // host name
            // todo: text color secondary
            InfernoText(
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 12.dp),
                text = promptRequest.logins[0].origin,
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                fontColor = Color.White,
            )
        }
        // save message
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // globe icon
            InfernoIcon(
                painter = painterResource(R.drawable.mozac_ic_login_24),
                contentDescription = "",
                modifier = Modifier.size(24.dp),
            )
            // todo: text color primary
            InfernoText(
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 12.dp),
                text = headline,
                textAlign = TextAlign.Start,
                fontSize = 18.sp,
                fontColor = Color.White,
            )
        }
        // username
        // todo: text color primary
        InfernoOutlinedTextField(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            value = username,
            onValueChange = {
                username = it
                update()
            },
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
                unfocusedTextColor = Color.LightGray,
                focusedContainerColor = Color.Black,
                errorContainerColor = Color.Black,
                disabledContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
            ),
            singleLine = true,
        )
        // password
        // todo: text color primary
        // todo: error font color
        // todo: error outline & container color
        // todo: toggle password visibility
        InfernoOutlinedTextField(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            value = password,
            onValueChange = { password = it },
            label = { InfernoText(stringResource(R.string.mozac_feature_prompt_password_hint)) },
            placeholder = {
                if (!loginValid) {
                    InfernoText(
                        stringResource(R.string.mozac_feature_prompt_error_empty_password_2),
                        fontColor = Color.Red
                    )
                }
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                errorTextColor = Color.Red,
                focusedContainerColor = Color.Black,
                errorContainerColor = Color.Black,
                disabledContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
            ),
            singleLine = true,
            isError = !loginValid,
        )
    }
}
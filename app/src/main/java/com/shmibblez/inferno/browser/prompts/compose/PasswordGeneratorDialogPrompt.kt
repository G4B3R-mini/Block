package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.dialog.emitGeneratedPasswordFilledFact
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Login
import mozilla.components.feature.prompts.login.PasswordGeneratorBottomSheet
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColors
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColorsProvider

@Composable
fun PasswordGeneratorDialogPrompt(
    loginData: PromptRequest.SelectLoginPrompt,
    sessionId: String,
    currentUrl: String,
    onSavedGeneratedPassword: (Boolean) -> Unit
) {
    val store = LocalContext.current.components.core.store

    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(loginData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, loginData))
        },
    ) {
        val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        val colorsProvider: PasswordGeneratorDialogColorsProvider =
            PasswordGeneratorDialogColors.defaultProvider()
        MaterialTheme(colors) {
            if (loginData.generatedPassword?.isNotEmpty() == true && currentUrl.isNotEmpty()) {
                PasswordGeneratorBottomSheet(
                    generatedStrongPassword = loginData.generatedPassword!!,
                    onUsePassword = {
                        val login = Login(
                            guid = "",
                            origin = currentUrl,
                            formActionOrigin = currentUrl,
                            httpRealm = currentUrl,
                            username = "",
                            password = loginData.generatedPassword!!,
                        )
                        onPositiveAction(loginData, login)
                        emitGeneratedPasswordFilledFact()
                        store.dispatch(
                            ContentAction.ConsumePromptRequestAction(
                                sessionId,
                                loginData
                            )
                        )

                        onSavedGeneratedPassword.invoke(false)
                    },
                    onCancelDialog = {
                        onNegativeAction(loginData)
                        store.dispatch(
                            ContentAction.ConsumePromptRequestAction(
                                sessionId,
                                loginData
                            )
                        )
                    },
                    colors = colorsProvider.provideColors(),
                )
            }
        }
    }
}
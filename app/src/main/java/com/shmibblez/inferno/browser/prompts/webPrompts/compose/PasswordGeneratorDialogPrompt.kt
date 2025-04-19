package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.onDismiss
import com.shmibblez.inferno.browser.prompts.onNegativeAction
import com.shmibblez.inferno.browser.prompts.onPositiveAction
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Login
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColors
import mozilla.components.ui.colors.PhotonColors

@Composable
fun PasswordGeneratorDialogPrompt(
    loginData: PromptRequest.SelectLoginPrompt,
    sessionId: String,
    currentUrl: String,
    onSavedGeneratedPassword: (Boolean) -> Unit,
) {
    val store = LocalContext.current.components.core.store

    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(loginData)
            store.dispatch(ContentAction.ConsumePromptRequestAction(sessionId, loginData))
        },
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(id = R.string.mozac_feature_prompts_suggest_strong_password_use_password),
            action = {
                val login = Login(
                    guid = "",
                    origin = currentUrl,
                    formActionOrigin = currentUrl,
                    httpRealm = currentUrl,
                    username = "",
                    password = loginData.generatedPassword!!,
                )
                onPositiveAction(loginData, login)
//                        emitGeneratedPasswordFilledFact()
                store.dispatch(
                    ContentAction.ConsumePromptRequestAction(
                        sessionId,
                        loginData
                    )
                )

                onSavedGeneratedPassword.invoke(false)
            },
        ),
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(id = R.string.mozac_feature_prompt_not_now),
            action = {
                onNegativeAction(loginData)
                store.dispatch(
                    ContentAction.ConsumePromptRequestAction(
                        sessionId,
                        loginData
                    )
                )
            }
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        val colors = (if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme())
            .copy(
                background = Color.Black,
                primaryContainer = Color.Black,
                primary = Color.White,
            )
//        val colorsProvider: PasswordGeneratorDialogColorsProvider =
//            PasswordGeneratorDialogColors.defaultProvider()
        MaterialTheme(colors) {
            if (loginData.generatedPassword?.isNotEmpty() == true && currentUrl.isNotEmpty()) {
                PasswordGeneratorBottomSheet(
                    generatedStrongPassword = loginData.generatedPassword!!,
//                    colors = colorsProvider.provideColors(),
                )
            }
        }
    }
}


private val FONT_SIZE = 16.sp
private val LINE_HEIGHT = 24.sp
private val LETTER_SPACING = 0.15.sp

/**
 * The password generator bottom sheet
 *
 * @param generatedStrongPassword The generated password.
// * @param onUsePassword Invoked when the user clicks on the Use Password button.
// * @param onCancelDialog Invoked when the user clicks on the Not Now button.
 * @param colors The colors of the dialog.
 */
@Composable
fun PasswordGeneratorBottomSheet(
    generatedStrongPassword: String,
//    onUsePassword: () -> Unit,
//    onCancelDialog: () -> Unit,
    colors: PasswordGeneratorDialogColors = PasswordGeneratorDialogColors.default().copy(
        title = Color.White,
        description = Color.White,
        background = Color.Black,
        cancelText = Color.White,
        confirmButton = Color.White,
        passwordBox = Color.Black,
        boxBorder = Color.White
    ),
) {
    Column(
        modifier = Modifier
            .background(colors.background)
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StrongPasswordBottomSheetTitle(colors = colors)

        StrongPasswordBottomSheetDescription(colors = colors)

        StrongPasswordBottomSheetPasswordBox(
            generatedPassword = generatedStrongPassword,
            colors = colors,
        )

//        StrongPasswordBottomSheetButtons(
//            onUsePassword = { onUsePassword() },
//            onCancelDialog = { onCancelDialog() },
//            colors = colors,
//        )
    }
}

@Composable
private fun StrongPasswordBottomSheetTitle(colors: PasswordGeneratorDialogColors) {
    Row {
        Icon(
            painter = painterResource(id = R.drawable.ic_login_24),
            contentDescription = null,
            tint = colors.title,
//            contentScale = ContentScale.FillWidth,
//            colorFilter = ColorFilter.tint(colors.title),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(20.dp),
        )

        InfernoText(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(id = R.string.mozac_feature_prompts_suggest_strong_password_title),
            style = TextStyle(
                fontSize = FONT_SIZE,
                lineHeight = LINE_HEIGHT,
                color = colors.title,
                letterSpacing = LETTER_SPACING,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun StrongPasswordBottomSheetDescription(
    modifier: Modifier = Modifier,
    colors: PasswordGeneratorDialogColors,
) {
    InfernoText(
        modifier = modifier.padding(start = 40.dp, top = 6.dp, end = 12.dp),
        text = stringResource(id = R.string.mozac_feature_prompts_suggest_strong_password_description_3),
        style = TextStyle(
            fontSize = FONT_SIZE,
            lineHeight = LINE_HEIGHT,
            color = colors.description,
            letterSpacing = LETTER_SPACING,
        ),
    )
}

// todo:
//   - make text editable
//   - add regenerate button
@Composable
private fun StrongPasswordBottomSheetPasswordBox(
    modifier: Modifier = Modifier,
    generatedPassword: String,
    colors: PasswordGeneratorDialogColors,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
            .background(colors.passwordBox)
            .border(1.dp, colors.boxBorder)
            .padding(4.dp),
    ) {
        InfernoText(
            modifier = modifier.padding(8.dp),
            text = generatedPassword,
            style = TextStyle(
                fontSize = FONT_SIZE,
                lineHeight = LINE_HEIGHT,
                color = colors.title,
                letterSpacing = LETTER_SPACING,
            ),
        )
    }
}

@Composable
private fun StrongPasswordBottomSheetButtons(
    onUsePassword: () -> Unit,
    onCancelDialog: () -> Unit,
    colors: PasswordGeneratorDialogColors,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .height(48.dp),
    ) {
        TextButton(
            onClick = { onCancelDialog() },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.background),
            modifier = Modifier.height(48.dp),
        ) {
            InfernoText(
                text = stringResource(id = R.string.mozac_feature_prompt_not_now),
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = colors.cancelText,
                    letterSpacing = 0.15.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        Button(
            onClick = { onUsePassword() },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.confirmButton),
            modifier = Modifier.height(48.dp),
        ) {
            InfernoText(
                text = stringResource(id = R.string.mozac_feature_prompts_suggest_strong_password_use_password),
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.White,
                    letterSpacing = 0.15.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
@Preview
private fun GenerateStrongPasswordDialogPreview() {
    DialogPreviewMaterialTheme {
        PasswordGeneratorBottomSheet(
            generatedStrongPassword = "StrongPassword123#",
//            onUsePassword = {},
//            onCancelDialog = {},
        )
    }
}

@Composable
internal fun DialogPreviewMaterialTheme(content: @Composable () -> Unit) {
    val colors = if (!isSystemInDarkTheme()) {
        lightColorScheme()
    } else {
        darkColorScheme(background = PhotonColors.DarkGrey30)
    }
    MaterialTheme(colorScheme = colors) {
        content()
    }
}

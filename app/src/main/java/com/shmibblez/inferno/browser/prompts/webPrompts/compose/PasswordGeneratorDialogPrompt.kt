package com.shmibblez.inferno.browser.prompts.webPrompts.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.storage.Login
import mozilla.components.feature.prompts.login.PasswordGeneratorDialogColors

@Composable
fun PasswordGeneratorDialogPrompt(
    promptRequest: PromptRequest.SelectLoginPrompt,
    askFirst: Boolean,
    currentUrl: String,
    onCancel: () -> Unit,
    onConfirm: (Login) -> Unit,
) {
    var ask by remember { mutableStateOf(askFirst) }

    PromptBottomSheetTemplate(
        onDismissRequest = onCancel,
        positiveAction = PromptBottomSheetTemplateAction(
            text = when (ask) {
                true -> stringResource(id = android.R.string.ok)
                false -> stringResource(id = R.string.mozac_feature_prompts_suggest_strong_password_use_password)
            },
            action = when (ask) {
                true -> {
                    { ask = false }
                }

                false -> {
                    {
                        val login = Login(
                            guid = "",
                            origin = currentUrl,
                            formActionOrigin = currentUrl,
                            httpRealm = currentUrl,
                            username = "",
                            password = promptRequest.generatedPassword!!,
                        )
                        onConfirm(login)
                    }
                }
            },
        ),
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(id = R.string.mozac_feature_prompt_not_now),
            action = onCancel,
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        if (ask) {
            PasswordGeneratorPrompt()
        } else {
            if (promptRequest.generatedPassword?.isNotEmpty() == true && currentUrl.isNotEmpty()) {
                PasswordGeneratorBottomSheet(
                    generatedStrongPassword = promptRequest.generatedPassword!!,
//                    colors = colorsProvider.provideColors(),
                )
            } else {
                onCancel.invoke()
            }
        }
    }
}


/**
 * The password generator prompt
 */
@Composable
private fun PasswordGeneratorPrompt() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.mozac_ic_login_24),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.White,
        )

        Spacer(Modifier.width(24.dp))

        InfernoText(
            text = stringResource(id = R.string.mozac_feature_prompts_suggest_strong_password_2),
            fontColor = Color.White,
            fontSize = 16.sp,
        )
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
        // title
        Row {
            Icon(
                painter = painterResource(id = R.drawable.ic_login_24),
                contentDescription = null,
                tint = colors.title,
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

        // description
        InfernoText(
            modifier = Modifier.padding(start = 40.dp, top = 6.dp, end = 12.dp),
            text = stringResource(id = R.string.mozac_feature_prompts_suggest_strong_password_description_3),
            style = TextStyle(
                fontSize = FONT_SIZE,
                lineHeight = LINE_HEIGHT,
                color = colors.description,
                letterSpacing = LETTER_SPACING,
            ),
        )

        // password box
        // todo:
        //   - make text editable
        //     - for this password should be saved in state, add undo button if password edited
        //       to go back to original password
        //   - add regenerate button as neutral button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
                .background(colors.passwordBox)
                .border(1.dp, colors.boxBorder)
                .padding(4.dp),
        ) {
            InfernoText(
                modifier = Modifier.padding(8.dp),
                text = generatedStrongPassword,
                style = TextStyle(
                    fontSize = FONT_SIZE,
                    lineHeight = LINE_HEIGHT,
                    color = colors.title,
                    letterSpacing = LETTER_SPACING,
                ),
            )
        }
    }
}
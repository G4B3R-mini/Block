package com.shmibblez.inferno.browser.prompts.creditcard

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.browser.prompts.InfernoPromptFeatureState
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.settings

@Composable
fun PinDialogWarning(
    webPrompterState: InfernoPromptFeatureState?,
) {
    val context = LocalContext.current

    LaunchedEffect(null) {
        context.settings().incrementSecureWarningCount()
    }

    PromptBottomSheetTemplate(
        onDismissRequest = {/* todo: set show dialog to false in credit card controller */},
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.credit_cards_warning_dialog_later),
            action = {
                // todo: hide here? not present originally
                webPrompterState?.onBiometricResult(isAuthenticated = false)
            },
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.credit_cards_warning_dialog_set_up_now),
            action = {
                webPrompterState?.creditCardDialogController?.hidePinDialogWarning()
                webPrompterState?.onBiometricResult(isAuthenticated = false)
                context.getActivity()?.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            },
        ),
    ) {
        // title
        InfernoText(
            text = stringResource(R.string.credit_cards_warning_dialog_title_2),
            fontWeight = FontWeight.Bold,
            fontColor = Color.White,
        )
        // message
        InfernoText(
            text = stringResource(R.string.credit_cards_warning_dialog_message_3),
            fontColor = Color.White,
        )
    }
    // todo: secure()
//    AlertDialog.Builder(context).apply {
//        create()
//    }.show().withCenterAlignedButtons().secure(context.getActivity())
}
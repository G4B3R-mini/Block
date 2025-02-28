package com.shmibblez.inferno.browser.prompts.compose

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import mozilla.components.concept.engine.prompt.PromptRequest

@Composable
fun SaveLoginDialogPrompt(loginData: PromptRequest.SaveLoginPrompt, sessionId: String, icon: Bitmap? = null) {
    PromptBottomSheetTemplate(
        onDismissRequest = {
            onDismiss(loginData)
        },
    ) { }
}
package com.shmibblez.inferno.browser.prompts.download.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.download.compose.sub.AppsGrid
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.feature.downloads.ui.DownloaderApp

@Composable
fun DownloadAppChooserPrompt(
    downloaderApps: List<DownloaderApp>,
    onAppSelected: (DownloaderApp) -> Unit,
    onDismiss: () -> Unit,
) {
    PromptBottomSheetTemplate(
        onDismissRequest = onDismiss,
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = onDismiss,
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.TOP,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.mozac_feature_download_ic_download),
                contentDescription = "",
                modifier = Modifier.size(32.dp),
                tint = Color.White,
            )
            InfernoText(
                text = stringResource(R.string.mozac_feature_downloads_third_party_app_chooser_dialog_title),
            )
        }
        AppsGrid(
            downloaderApps = downloaderApps,
            onAppSelected = onAppSelected,
            onDismiss = onDismiss,
        )
    }
}
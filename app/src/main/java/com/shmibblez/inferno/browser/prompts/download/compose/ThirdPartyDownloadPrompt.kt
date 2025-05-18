package com.shmibblez.inferno.browser.prompts.download.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.browser.prompts.download.compose.sub.AppsGrid
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.feature.downloads.ui.DownloaderApp

private val ICON_SIZE = 24.dp

@Composable
internal fun ThirdPartyDownloadPrompt(
    downloaderApps: List<DownloaderApp>,
    onAppSelected: (DownloaderApp) -> Unit,
    onDismiss: () -> Unit,
) {
    PromptBottomSheetTemplate(
        onDismissRequest = onDismiss,
        dismissOnSwipeDown = false,
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
            InfernoIcon(
                painter = painterResource(R.drawable.ic_download_24),
                contentDescription = "",
                modifier = Modifier.size(ICON_SIZE),
            )
            InfernoText(
                text = stringResource(R.string.mozac_feature_downloads_third_party_app_chooser_dialog_title),
                fontColor = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1F),
            )
        }
        AppsGrid(
            downloaderApps = downloaderApps,
            onAppSelected = onAppSelected,
            onDismiss = onDismiss,
        )
    }
}
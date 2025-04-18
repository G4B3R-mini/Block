package com.shmibblez.inferno.browser.prompts.download.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.browser.state.state.content.DownloadState

private val ICON_SIZE = 24.dp

@Composable
fun DownloadPrompt(
    download: DownloadState,
    onStartDownload: () -> Unit,
    onCancelDownload: () -> Unit,
) {
    PromptBottomSheetTemplate(
        onDismissRequest = onCancelDownload,
        dismissOnSwipeDown = false,
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = onCancelDownload,
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_downloads_dialog_download),
            action = onStartDownload,
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download_24),
                contentDescription = "download complete icon",
                modifier = Modifier.size(ICON_SIZE),
                tint = Color.White,
            )
            InfernoText(
                text = stringResource(R.string.mozac_feature_downloads_dialog_title2),
                fontColor = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1F),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
        Text(
            text = download.fileName ?: "",
            color = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                // padding is icon + row item spacing
                .padding(start = ICON_SIZE + 16.dp)
                .heightIn(max = 160.dp),
        )
    }

}
package com.shmibblez.inferno.browser.prompts.download.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import mozilla.components.feature.downloads.AbstractFetchDownloadService
import mozilla.components.feature.downloads.toMegabyteOrKilobyteString

private val ICON_SIZE = 24.dp

/**
 * [DynamicDownloadPrompt] is used to show a view in the current tab to the user, triggered when
 *  downloadFeature.onDownloadStopped gets invoked. When download successful, it hides when the
 *  users scrolls through a website (focus lost) as to not impede his activities.
 */
@Composable
fun DynamicDownloadPrompt(
    downloadState: DownloadState?,
    didFail: Boolean,
    tryAgain: (String) -> Unit,
    onCannotOpenFile: (DownloadState) -> Unit,
    onDismiss: () -> Unit,
) {
    if (downloadState == null) {
//        onDismiss.invoke()
        return
    }

    val context = LocalContext.current

    val title = when (didFail) {
        true -> context.getString(R.string.mozac_feature_downloads_failed_notification_text2)
        false -> context.getString(
            R.string.mozac_feature_downloads_completed_notification_text2,
        ) + " (${downloadState.contentLength?.toMegabyteOrKilobyteString()})"
    }

    val iconRes = when (didFail) {
        true -> R.drawable.mozac_feature_download_ic_download_failed
        false -> R.drawable.mozac_feature_download_ic_download_complete
    }

    val actionText = when (didFail) {
        true -> context.getString(R.string.mozac_feature_downloads_button_try_again)
        false -> context.getString(R.string.mozac_feature_downloads_button_open)
    }


    val actionOnClick = when (didFail) {
        true -> {
            {
                tryAgain(downloadState.id)
                onDismiss()
            }
        }

        false -> {
            {
                val fileWasOpened = AbstractFetchDownloadService.openFile(
                    applicationContext = context.applicationContext,
                    download = downloadState,
                )
                if (!fileWasOpened) {
                    onCannotOpenFile(downloadState)
                }
                onDismiss()
            }
        }
    }


    PromptBottomSheetTemplate(
        onDismissRequest = onDismiss,
        dismissOnSwipeDown = !didFail,
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = onDismiss,
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = actionText,
            action = actionOnClick,
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
                modifier = Modifier.size(ICON_SIZE),
                tint = Color.White,
                contentDescription = "",
                painter = painterResource(iconRes)
            )
            InfernoText(
                text = title,
                fontColor = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1F),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
        InfernoText(
            text = downloadState.fileName?: downloadState.url,
            fontColor = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                // padding is icon + row item spacing
                .padding(start = ICON_SIZE + 16.dp)
                .heightIn(max = 160.dp),
        )
    }
}
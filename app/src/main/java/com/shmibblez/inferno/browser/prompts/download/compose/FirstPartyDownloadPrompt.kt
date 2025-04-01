package com.shmibblez.inferno.browser.prompts.download.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.FirstPartyDownloadDialogData
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplate
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateAction
import com.shmibblez.inferno.browser.prompts.PromptBottomSheetTemplateButtonPosition
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.feature.downloads.Filename
import mozilla.components.feature.downloads.ContentSize
import mozilla.components.feature.downloads.NegativeActionCallback
import mozilla.components.feature.downloads.PositiveActionCallback
import mozilla.components.feature.downloads.toMegabyteOrKilobyteString


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FirstPartyDownloadPrompt(
    filename: Filename,
    contentSize: ContentSize,
    onPositiveAction: () -> Unit,
    onNegativeAction: () -> Unit,
) {
    PromptBottomSheetTemplate(
        onDismissRequest = onNegativeAction,
        negativeAction = PromptBottomSheetTemplateAction(
            text = stringResource(android.R.string.cancel),
            action = onNegativeAction,
        ),
        positiveAction = PromptBottomSheetTemplateAction(
            text = stringResource(R.string.mozac_feature_downloads_dialog_download),
            action = onPositiveAction,
        ),
        buttonPosition = PromptBottomSheetTemplateButtonPosition.BOTTOM,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                tint = Color.White,
                contentDescription = "download complete icon",
                painter = painterResource(R.drawable.mozac_feature_download_ic_download_complete)
            )
            InfernoText(
                text = contentSize.value.toMegabyteOrKilobyteString(),
                fontColor = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1F),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
        InfernoText(
            text = filename.value,
            fontColor = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1F)
                .padding(horizontal = 16.dp)
                // padding is icon + row item spacing
                .padding(start = 32.dp + 8.dp)
        )
    }
}
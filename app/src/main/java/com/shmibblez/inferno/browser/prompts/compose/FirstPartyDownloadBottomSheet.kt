package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.FirstPartyDownloadDialogData
import mozilla.components.feature.downloads.toMegabyteOrKilobyteString


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FirstPartyDownloadBottomSheet(
    firstPartyDownloadDialogData: FirstPartyDownloadDialogData,
    setFirstPartyDownloadDialogData: (FirstPartyDownloadDialogData?) -> Unit
) {
    // don't dismiss on swipe down
    val sheetState = rememberModalBottomSheetState(confirmValueChange = { sheetValue ->
        sheetValue != SheetValue.Hidden
    })

    fun dismiss() {
        setFirstPartyDownloadDialogData(null)
    }
    ModalBottomSheet(sheetState = sheetState,
        onDismissRequest = { },
        containerColor = Color.Black,
        scrimColor = Color.Black.copy(alpha = 0.1F),
        shape = RectangleShape,
        dragHandle = {
            /* no drag handle */
//            BottomSheetDefaults.DragHandle(
//                color = Color.White,
//                height = ToolbarMenuItemConstants.SHEET_HANDLE_HEIGHT,
////            shape = RectangleShape,
//            )
        }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .padding(start = 16.dp, top = 16.dp),
                tint = Color.White,
                contentDescription = "download complete icon",
                painter = painterResource(R.drawable.mozac_feature_download_ic_download_complete)
            )
            Text(
                text = firstPartyDownloadDialogData.contentSize.toMegabyteOrKilobyteString(),
                color = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 4.dp, end = 4.dp)
            )
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .clickable(onClick = {
                        firstPartyDownloadDialogData.negativeButtonAction.invoke()
                        dismiss()
                    }),
                tint = Color.White,
                contentDescription = "download complete icon",
                painter = painterResource(R.drawable.mozac_ic_cross_24)
            )
        }
        Text(
            text = firstPartyDownloadDialogData.filename,
            color = Color.White,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1F)
                .padding(start = 4.dp + 48.dp, end = 16.dp)
        )
        Button(onClick = {
            firstPartyDownloadDialogData.positiveButtonAction.invoke()
            dismiss()
        }) {
            Text(text = stringResource(R.string.mozac_feature_downloads_dialog_download))
        }
    }
}
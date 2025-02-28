package com.shmibblez.inferno.browser.prompts.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ThirdPartyDownloadDialogData
import mozilla.components.feature.downloads.ui.DownloaderApp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThirdPartyDownloadBottomSheet(
    thirdPartyDownloadDialogData: ThirdPartyDownloadDialogData,
    setThirdPartyDownloadDialogData: (ThirdPartyDownloadDialogData?) -> Unit
) {
    // don't dismiss on swipe down
    val sheetState = rememberModalBottomSheetState(confirmValueChange = { sheetValue ->
        sheetValue != SheetValue.Hidden
    })

    fun dismiss() {
        setThirdPartyDownloadDialogData(null)
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
                text = stringResource(R.string.mozac_feature_downloads_third_party_app_chooser_dialog_title),
                color = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 4.dp, end = 4.dp),
            )
            Icon(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .clickable(onClick = {
                        thirdPartyDownloadDialogData.negativeButtonAction.invoke()
                        dismiss()
                    }),
                tint = Color.White,
                contentDescription = "download complete icon",
                painter = painterResource(R.drawable.mozac_ic_cross_24),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 76.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            items(thirdPartyDownloadDialogData.downloaderApps) { downloaderApp ->
                DownloaderAppItem(
                    downloaderApp,
                    thirdPartyDownloadDialogData.onAppSelected,
                    { dismiss() },
                )
            }
        }
    }
}

@Composable
private fun DownloaderAppItem(
    downloaderApp: DownloaderApp, onAppClicked: (DownloaderApp) -> Unit, dismiss: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(76.dp)
            // height + spacers/margin
            .height(80.dp + 8.dp + 8.dp + 5.dp)
            .clickable {
                onAppClicked(downloaderApp)
                dismiss()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            modifier = Modifier.size(40.dp),
            painter = rememberDrawablePainter(downloaderApp.resolver.loadIcon(context.packageManager)),
            tint = Color.Transparent,
            contentDescription = "app image",
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = downloaderApp.name,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1F)
                .padding(start = 4.dp, end = 4.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
    }
}

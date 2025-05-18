package com.shmibblez.inferno.browser.prompts.download.compose.sub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.shmibblez.inferno.compose.base.InfernoIcon
import mozilla.components.feature.downloads.ui.DownloaderApp

@Composable
fun AppsGrid(
    downloaderApps: List<DownloaderApp>,
    onAppSelected: (DownloaderApp) -> Unit,
    onDismiss: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 76.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        items(downloaderApps) { downloaderApp ->
            DownloaderAppItem(
                downloaderApp = downloaderApp,
                onAppSelected = onAppSelected,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun DownloaderAppItem(
    downloaderApp: DownloaderApp, onAppSelected: (DownloaderApp) -> Unit, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(76.dp)
            // height + spacers/margin
            .height(80.dp + 8.dp + 8.dp + 5.dp)
            .clickable {
                onAppSelected.invoke(downloaderApp)
                onDismiss.invoke()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        InfernoIcon(
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

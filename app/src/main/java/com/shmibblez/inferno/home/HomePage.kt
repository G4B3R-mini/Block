package com.shmibblez.inferno.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.ext.components

@Composable
fun HomePage(topBarHeight: Dp = 0.dp, bottomBHeight: Dp = 0.dp) {
    val localConfig = LocalConfiguration.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(localConfig.screenHeightDp.dp - topBarHeight - bottomBHeight)
            .offset(y = topBarHeight),
    ) {

    }
}

@Composable
fun RecentSites() {
    val context = LocalContext.current
    val storage = context.components.core
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 60.dp)
    ) {
        item() {  }
    }
}

@Composable
fun RecentSite() {

}
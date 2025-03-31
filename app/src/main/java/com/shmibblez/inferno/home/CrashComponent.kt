package com.shmibblez.inferno.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.crashes.CrashContentView

/**
 * page shown when page crash
 * could show meme, we f**ed up, or something f**ed up
 * todo: check [CrashContentView]
 */
@Composable
fun CrashComponent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        // todo
        Text("this page is under construction (╥﹏╥)")
        Text(
            "page_title: " + stringResource(
                R.string.tab_crash_title_2,
                stringResource(R.string.app_name)
            )
        )
    }
}
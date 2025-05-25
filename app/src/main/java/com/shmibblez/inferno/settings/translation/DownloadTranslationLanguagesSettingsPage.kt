package com.shmibblez.inferno.settings.translation

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguagesPreferenceFragment

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DownloadTranslationLanguagesSettingsPage(goBack: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_back_button),
                        contentDescription = stringResource(R.string.browser_menu_back),
                        modifier = Modifier.clickable(onClick = goBack),
                    )
                },
                title = { InfernoText(stringResource(R.string.translation_settings_download_language)) },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {

            /**
             * todo: left off here
             *  reference [DownloadLanguagesPreferenceFragment]
             */

        }
    }
}
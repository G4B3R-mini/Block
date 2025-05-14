package com.shmibblez.inferno.settings.compose.mainSettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.SwitchPreference
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

@Composable
fun GeneralPreferences() {
    val context = LocalContext.current
    LaunchedEffect(null) {
        MainScope().launch {
            context.infernoSettingsDataStore.data.collect(
                object : FlowCollector<InfernoSettings> {
                    override suspend fun emit(value: InfernoSettings) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }
    SwitchPreference(
        text = stringResource(R.string.preferences_delete_browsing_data_on_quit),
        summary = stringResource(R.string.preference_summary_delete_browsing_data_on_quit_2),
        selected = TODO(),
        onSelectedChange = {

        }
    )
}
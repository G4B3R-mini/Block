package com.shmibblez.inferno.settings.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.getSelectedTheme
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ThemeSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())
    val defaultThemes = remember { listOf(InfernoTheme.dark(context), InfernoTheme.light(context)) }
    var showAddThemeDialog by remember { mutableStateOf(false) }

    var selectedTheme by remember { mutableStateOf(settings.getSelectedTheme(context)) }

    LaunchedEffect(settings.selectedDefaultTheme, settings.selectedCustomTheme) {
        selectedTheme = settings.getSelectedTheme(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_button),
                        contentDescription = stringResource(R.string.browser_menu_back),
                        modifier = Modifier.clickable(onClick = goBack),
                        tint = Color.White, // todo: theme
                    )
                },
                title = { InfernoText("Search Settings") }, // todo: string res
            )
        },
    ) {
        ThemeSelector(
            selectedDefault = if (selectedTheme.isDefault) selectedTheme else null,
            selectedCustom = if (selectedTheme.isCustom) selectedTheme else null,
            defaultThemes = defaultThemes,
            customThemes = settings.customThemesMap.map {
                InfernoTheme.fromSettingsObj(it.value)
            },
            onSelectTheme = { theme ->
                coroutineScope.launch {
                    context.infernoSettingsDataStore.updateData {
                        when (theme.isDefault) {
                            true -> {
                                it.toBuilder().setSelectedCustomTheme("")
                                    .setSelectedDefaultTheme(theme.defaultType).build()
                            }

                            false -> {
                                it.toBuilder().setSelectedCustomTheme(theme.name).build()
                            }
                        }
                    }
                }
            },
            onAddTheme = { showAddThemeDialog = true}
        )

        if (showAddThemeDialog) {
            EditThemeDialog(
                baseTheme = selectedTheme,
                onDismiss = { showAddThemeDialog = false},
                onSaveTheme = {
                    // todo
                    showAddThemeDialog = false
                },
            )
        }
    }
}
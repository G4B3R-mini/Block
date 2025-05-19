package com.shmibblez.inferno.settings.theme

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.getSelectedTheme
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceConstants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ThemeSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())
    val defaultThemes = remember {
        listOf(
            InfernoTheme.dark(context),
            InfernoTheme.light(context),
            InfernoTheme.incognitoDark(context),
            InfernoTheme.incognitoLight(context),
        )
    }
    var showEditThemeDialogFor by remember { mutableStateOf<InfernoTheme?>(null) }

    var selectedTheme by remember { mutableStateOf(settings.getSelectedTheme(context)) }

    fun canAddMoreThemes(): Boolean {
        return settings.customThemesMap.size < PreferenceConstants.CUSTOM_THEMES_MAX
    }

    LaunchedEffect(settings.selectedDefaultTheme, settings.selectedCustomTheme) {
        selectedTheme = settings.getSelectedTheme(context)
    }

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
                                it.toBuilder()
                                    // unselect custom theme if set
                                    .setSelectedCustomTheme("")
                                    // update selected default theme
                                    .setSelectedDefaultTheme(theme.defaultType).build()
                            }

                            false -> {
                                // if theme not default, select from custom themes
                                it.toBuilder().setSelectedCustomTheme(theme.name).build()
                            }
                        }
                    }
                }
            },
            onAddTheme = {
                if (canAddMoreThemes()) {
                    // show dialog to make new theme based on current one
                    showEditThemeDialogFor = selectedTheme
                } else {
                    Toast.makeText(
                        context,
                        "20 custom theme limit reached, please delete a theme to create a new one",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            },
            onEditTheme = {
                // edit specified theme
                showEditThemeDialogFor = it
            },
            onDeleteTheme = { theme ->
                // delete specified theme
                coroutineScope.launch {
                    context.infernoSettingsDataStore.updateData {
                        it.toBuilder().removeCustomThemes(theme.name).build()
                    }
                }
            },
        )

        if (showEditThemeDialogFor != null) {
            EditThemeDialog(
                baseTheme = selectedTheme,
                onDismiss = { showEditThemeDialogFor = null },
                onSaveTheme = { theme, oldThemeName ->
                    // generate unique theme name
                    fun uniqueThemeName(): String {
                        for (i in 0..100) {
                            val name = "Unnamed Theme $i"
                            if (!settings.customThemesMap.keys.contains(name)) return name
                        }
                        throw IllegalArgumentException("this should not happen, max number of themes is ${PreferenceConstants.CUSTOM_THEMES_MAX}")
                    }
                    coroutineScope.launch {
                        context.infernoSettingsDataStore.updateData {
                            // if theme name blank, auto-generate name
                            val name = theme.name.ifBlank { uniqueThemeName() }
                            it.toBuilder()
                                // remove old theme
                                .removeCustomThemes(oldThemeName)
                                // save updated theme
                                .putCustomThemes(name, theme.toSettingsObj()).build()
                        }
                    }
                    showEditThemeDialogFor = null
                },
                themeNames = settings.customThemesMap.map { it.key },
            )
        }
    }
}
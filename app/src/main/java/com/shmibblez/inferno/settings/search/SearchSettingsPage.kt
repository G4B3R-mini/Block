package com.shmibblez.inferno.settings.search

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.snackbar.Snackbar
import com.shmibblez.inferno.compose.snackbar.SnackbarState
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.toolbar.toPrefString
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.browser.icons.IconRequest
import mozilla.components.feature.search.ext.createSearchEngine

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsPage(
    goBack: () -> Unit,
) {
    val context = LocalContext.current
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

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
        // custom search engines stored here
        val customSearchEngines = context.components.core.store.state.search.customSearchEngines
        // selected search engine todo: check how its done in settings fragment
        PreferenceSelect(
            text = "Default search engine:", // todo: string res
            description = null,
            enabled = true,
            initiallySelectedMenuItem = settings.inAppToolbarVerticalPosition.toPrefString(context),
            menuItems = listOf(
                InfernoSettings.VerticalToolbarPosition.TOOLBAR_BOTTOM,
                InfernoSettings.VerticalToolbarPosition.TOOLBAR_TOP,
            ),
            mapToTitle = { it.toPrefString(context) },
            onSelectMenuItem = { selected ->
                MainScope().launch {
                    context.infernoSettingsDataStore.updateData {
                        it.toBuilder().setInAppToolbarVerticalPosition(selected).build()
                    }
                }
            },
            additionalMenuItems = listOf(
                {
                    DropdownMenuItem(
                        text = {
                            InfernoText(
                                text = stringResource(R.string.search_engine_add_custom_search_engine_title),
                                fontColor = Color.White, // todo: theme
                            )
                        },
                        onClick = {
                            // todo: show alert dialog with fields for search engine
//                            val newEngine = createSearchEngine(
//                                name,
//                                searchString.toSearchUrl(),
//                                requireComponents.core.icons.loadIcon(IconRequest(searchString))
//                                    .await().bitmap,
//                                suggestUrl = suggestString.toSearchUrl(),
//                                isGeneral = true,
//                            )
//
//                            context.components.useCases.searchUseCases.addSearchEngine(newEngine)
//
//                            val successMessage = if (searchEngine != null) {
//                                context.getString(R.string.search_edit_custom_engine_success_message, name)
//                            } else {
//                                context.getString(R.string.search_add_custom_engine_success_message, name)
//                            }

                            // todo: show toast instead of snackbar
//                                Snackbar.make(
//                                    snackBarParentView = it,
//                                    snackbarState = SnackbarState(
//                                        message = successMessage,
//                                    ),
//                                ).show()
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_new_24),
                                contentDescription = "",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White, // todo: theme
                            )
                        }
                    )
                }
            )
        )
    }
}
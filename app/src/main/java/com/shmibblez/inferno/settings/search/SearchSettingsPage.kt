package com.shmibblez.inferno.settings.search

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.getRootView
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.utils.allowUndo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.browser.state.state.searchEngines
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.lib.state.ext.flow

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsPage(
    goBack: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())
    val searchState by context.components.core.store.flow().map { state -> state.search }
        .collectAsState(initial = context.components.core.store.state.search)

    var showNewEngineDialog by remember { mutableStateOf(false) }
    var showEditEngineDialogFor by remember { mutableStateOf<SearchEngine?>(null) }

    fun deleteSearchEngine(
        engine: SearchEngine,
    ) {
        val selectedOrDefaultSearchEngine = searchState.selectedOrDefaultSearchEngine
        if (selectedOrDefaultSearchEngine == engine) {
            val nextSearchEngine = searchState.searchEngines.firstOrNull {
                it.id != engine.id && (it.isGeneral || it.type == SearchEngine.Type.CUSTOM)
            } ?: searchState.searchEngines.firstOrNull {
                it.id != engine.id
            }

            nextSearchEngine?.let {
                context.components.useCases.searchUseCases.selectSearchEngine(
                    nextSearchEngine,
                )
            }
        }

        context.components.useCases.searchUseCases.removeSearchEngine(engine)

        MainScope().allowUndo(
            view = context.getActivity()!!.getRootView()!!,
            message = context.getString(
                R.string.search_delete_search_engine_success_message, engine.name
            ),
            undoActionTitle = context.getString(R.string.snackbar_deleted_undo),
            onCancel = {
                context.components.useCases.searchUseCases.addSearchEngine(engine)
                if (selectedOrDefaultSearchEngine == engine) {
                    context.components.useCases.searchUseCases.selectSearchEngine(engine)
                }
            },
            operation = { },
        )
    }

    fun selectSearchEngine(engine: SearchEngine) {
        // select search engine and save to prefs
        val confirmedEngine = requireNotNull(searchState.searchEngines.find { searchEngine ->
            searchEngine.id == engine.id
        })
        context.components.useCases.searchUseCases.selectSearchEngine(confirmedEngine)
        MainScope().launch {
            context.infernoSettingsDataStore.updateData {
                it.toBuilder().setDefaultSearchEngine(confirmedEngine.id).build()
            }
        }
    }

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_search),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
        ) {

            /**
             * general search settings
             */

            // general title
            item { PreferenceTitle(stringResource(R.string.preferences_category_general)) }

            // select search engine
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preferences_default_search_engine),
                    description = null,
                    enabled = true,
                    selectedMenuItem = searchState.selectedOrDefaultSearchEngine!!,
                    menuItems = searchState.customSearchEngines,
                    mapToTitle = { it.name },
                    selectedLeadingIcon = {
                        Image(
                            bitmap = it.icon.asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    menuItemLeadingIcon = {
                        if (it.type == SearchEngine.Type.CUSTOM) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete_24),
                                contentDescription = stringResource(R.string.search_engine_delete),
                                modifier = Modifier.clickable {
                                    deleteSearchEngine(it)
                                },
                                tint = Color.White, // todo: theme
                            )
                        }
                    },
                    menuItemTrailingIcon = {
                        if (it.type == SearchEngine.Type.CUSTOM) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit_24),
                                contentDescription = stringResource(R.string.search_engine_delete),
                                modifier = Modifier.clickable {
                                    showEditEngineDialogFor = it
                                },
                                tint = Color.White, // todo: theme
                            )
                        }
                    },
                    onSelectMenuItem = ::selectSearchEngine,
                    additionalMenuItems = listOf(
                        {
                            HorizontalDivider(
                                thickness = 1.dp, color = Color.White
                            )
                        }, // todo: theme for color
                        {
                            // add engine item
                            DropdownMenuItem(
                                text = {
                                    InfernoText(
                                        text = stringResource(R.string.search_engine_add_custom_search_engine_title),
                                        fontColor = Color.White, // todo: theme
                                    )
                                },
                                onClick = {

                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_new_24),
                                        contentDescription = "",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.White, // todo: theme
                                    )
                                },
                            )
                        },
                    ),
                )
            }

            // show voice search
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_show_voice_search),
                    summary = null,
                    selected = settings.shouldShowVoiceSearch,
                    onSelectedChange = { shouldShowVoiceSearch ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setShouldShowVoiceSearch(shouldShowVoiceSearch)
                                    .build()
                            }
                        }
                    },
                )
            }


            /**
             * address bar search settings
             */


            // address bar title
            item { PreferenceTitle(stringResource(R.string.preferences_settings_address_bar)) }

            // autocomplete urls
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_enable_autocomplete_urls),
                    summary = null,
                    selected = settings.shouldAutocompleteUrls,
                    onSelectedChange = { shouldAutocompleteUrls ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setShouldAutocompleteUrls(shouldAutocompleteUrls)
                                    .build()
                            }
                        }
                    },
                )
            }

            // autocomplete urls in private mode
            item {
                PreferenceSwitch(
                    text = "Autocomplete URLs in private mode", // todo: string res
                    summary = null,
                    selected = settings.shouldAutocompleteUrlsInPrivate,
                    onSelectedChange = { shouldAutocompleteUrlsInPrivate ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder()
                                    .setShouldAutocompleteUrlsInPrivate(
                                        shouldAutocompleteUrlsInPrivate
                                    )
                                    .build()
                            }
                        }
                    },
                )
            }

            // search suggestions
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_show_search_suggestions),
                    summary = null,
                    selected = settings.shouldShowSearchSuggestions,
                    onSelectedChange = { shouldShowSearchSuggestions ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder()
                                    .setShouldShowSearchSuggestions(shouldShowSearchSuggestions)
                                    .build()
                            }
                        }
                    },
                )
            }

            // search suggestions in private mode
            item {
                PreferenceSwitch(
                    text = "Show search suggestions in private mode", // todo: string res
                    summary = null,
                    selected = settings.shouldShowSearchSuggestionsInPrivate,
                    onSelectedChange = { shouldShowSearchSuggestionsInPrivate ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setShouldShowSearchSuggestionsInPrivate(
                                    shouldShowSearchSuggestionsInPrivate
                                ).build()
                            }
                        }
                    },
                )
            }

            // history search suggestions
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_search_browsing_history),
                    summary = null,
                    selected = settings.shouldShowHistorySuggestions,
                    onSelectedChange = { shouldShowHistorySuggestions ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder()
                                    .setShouldShowHistorySuggestions(shouldShowHistorySuggestions)
                                    .build()
                            }
                        }
                    },
                )
            }

            // bookmarks search suggestions
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_search_bookmarks),
                    summary = null,
                    selected = settings.shouldShowBookmarkSuggestions,
                    onSelectedChange = { shouldShowBookmarkSuggestions ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder()
                                    .setShouldShowBookmarkSuggestions(shouldShowBookmarkSuggestions)
                                    .build()
                            }
                        }
                    },
                )
            }

            // synced tabs search suggestions
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_search_synced_tabs),
                    summary = null,
                    selected = settings.shouldShowSyncedTabsSuggestions,
                    onSelectedChange = { shouldShowSyncedTabsSuggestions ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder()
                                    .setShouldShowSyncedTabsSuggestions(
                                        shouldShowSyncedTabsSuggestions
                                    )
                                    .build()
                            }
                        }
                    },
                )
            }

            // synced tabs search suggestions
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_show_clipboard_suggestions),
                    summary = null,
                    selected = settings.shouldShowClipboardSuggestions,
                    onSelectedChange = { shouldShowClipboardSuggestions ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder()
                                    .setShouldShowClipboardSuggestions(
                                        shouldShowClipboardSuggestions
                                    )
                                    .build()
                            }
                        }
                    },
                )
            }
        }


        /**
         * dialogs
         */


        // new/edit engine dialogs
        when {

            // show add new engine dialog
            showNewEngineDialog -> {
                NewCustomEngineDialog(
                    onSave = { engine, successMessage ->
                        selectSearchEngine(engine)
                        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
                        // could also use snackbar
//                        Snackbar.make(
//                            snackBarParentView = it,
//                            snackbarState = SnackbarState(
//                                message = successMessage,
//                            ),
                        showNewEngineDialog = false
                    },
                    onDismiss = { showNewEngineDialog = false },
                    copyEngineId = null,
                )
            }

            // show edit engine dialog
            showEditEngineDialogFor != null -> {
                NewCustomEngineDialog(
                    onSave = { engine, successMessage ->
                        selectSearchEngine(engine)
                        // could also use snackbar
                        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
                        // could also use snackbar
//                        Snackbar.make(
//                            snackBarParentView = it,
//                            snackbarState = SnackbarState(
//                                message = successMessage,
//                            ),
//                        ).show()
                        showNewEngineDialog = false
                    },
                    onDismiss = { showNewEngineDialog = false },
                    copyEngineId = showEditEngineDialogFor!!.id,
                )
            }
        }
    }
}
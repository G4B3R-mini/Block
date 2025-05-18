package com.shmibblez.inferno.settings.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.SupportUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.state.search.SearchEngine
import mozilla.components.feature.search.ext.createSearchEngine

@Composable
fun NewCustomEngineDialog(
    onSave: (SearchEngine, successMessage: String) -> Unit,
    onDismiss: () -> Unit,
    copyEngineId: String? = null,
) {
    val context = LocalContext.current
    val searchEngine by remember {
        lazy {
            context.components.core.store.state.search.customSearchEngines.find { engine ->
                engine.id == copyEngineId
                        // added to make sure not editing app search engine
                        && engine.type != SearchEngine.Type.APPLICATION && engine.isGeneral
            }
        }
    }

    var name by remember { mutableStateOf(searchEngine?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var searchUrl by remember { mutableStateOf(searchEngine?.resultUrls?.getOrNull(0) ?: "") }
    var searchUrlError by remember { mutableStateOf<String?>(null) }
    var suggestUrl by remember { mutableStateOf(searchEngine?.suggestUrl ?: "") }
    var suggestUrlError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    fun checkForNameError(): String? {
        if (name.trim().isEmpty()) {
            return context.getString(R.string.search_add_custom_engine_error_empty_name)
        }
        return null
    }

    fun checkForSearchUrlError(): String? {
        if (searchUrl.isEmpty()) {
            return context.getString(R.string.search_add_custom_engine_error_empty_search_string)
        } else if (!searchUrl.contains("%s")) {
            return context.getString(R.string.search_add_custom_engine_error_missing_template)
        }
        return null
    }

    fun checkForSuggestUrlError(): String? {
        if (suggestUrl.isNotBlank() && !suggestUrl.contains("%s")) {
            return context.getString(R.string.search_add_custom_engine_error_missing_template)
        }
        return null
    }

    fun createCustomEngine() {
        job = coroutineScope.launch {
            val nameStr = name
            val searchStr = searchUrl
            val suggestStr = suggestUrl
            val searchStringResult = withContext(IO) {
                SearchStringValidator.isSearchStringValid(
                    context.components.core.client,
                    searchStr,
                )
            }
            if (searchStringResult == SearchStringValidator.Result.CannotReach) {
                searchUrlError = context.getString(
                    R.string.search_add_custom_engine_error_cannot_reach, nameStr
                )
            }

            val suggestStringResult = if (suggestStr.isBlank()) {
                SearchStringValidator.Result.Success
            } else {
                withContext(IO) {
                    SearchStringValidator.isSearchStringValid(
                        context.components.core.client,
                        suggestStr,
                    )
                }
            }
            if (suggestStringResult == SearchStringValidator.Result.CannotReach) {
                suggestUrlError = context.getString(
                    R.string.search_add_custom_engine_error_cannot_reach, nameStr
                )
            }

            if ((searchStringResult == SearchStringValidator.Result.Success) && (suggestStringResult == SearchStringValidator.Result.Success)) {
                val update = searchEngine?.copy(
                    name = nameStr,
                    resultUrls = listOf(searchStr.toSearchUrl()),
                    icon = context.components.core.icons.loadIcon(
                        IconRequest(
                            searchStr
                        )
                    ).await().bitmap,
                    suggestUrl = suggestStr.toSearchUrl(),
                ) ?: run {
                    createSearchEngine(
                        nameStr,
                        searchStr.toSearchUrl(),
                        context.components.core.icons.loadIcon(
                            IconRequest(
                                searchStr
                            )
                        ).await().bitmap,
                        suggestUrl = suggestStr.toSearchUrl(),
                        isGeneral = true,
                    )
                }

                context.components.useCases.searchUseCases.addSearchEngine(
                    update
                )

                val successMessage = if (searchEngine != null) {
                    context.getString(
                        R.string.search_edit_custom_engine_success_message, nameStr
                    )
                } else {
                    context.getString(
                        R.string.search_add_custom_engine_success_message, nameStr
                    )
                }

                onSave.invoke(update, successMessage)
            }
        }
        job!!.invokeOnCompletion {
            job = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {

                    // set engine name
                    InfernoOutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = checkForNameError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            InfernoText(text = stringResource(R.string.search_add_custom_engine_name_label))
                        },
                        placeholder = {
                            InfernoText(
                                text = stringResource(R.string.search_add_custom_engine_name_hint_2),
                                fontColor = Color.DarkGray,
                            )
                        },
                        supportingText = {
                            if (nameError != null) InfernoText(text = nameError!!)
                        },
                        isError = nameError != null,
                    )

                    // set engine url
                    InfernoOutlinedTextField(
                        value = searchUrl,
                        onValueChange = {
                            searchUrl = it
                            searchUrlError = checkForSearchUrlError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            InfernoText(text = stringResource(R.string.search_add_custom_engine_url_label))
                        },

                        placeholder = {
                            InfernoText(
                                text = stringResource(R.string.search_add_custom_engine_search_string_hint_2),
                                fontColor = Color.DarkGray,
                            )
                        },
                        supportingText = {
                            if (searchUrlError != null) InfernoText(text = searchUrlError!!)
                        },
                        isError = searchUrlError != null,
                    )

                    // engine url hint
                    InfernoText(
                        text = stringResource(R.string.search_add_custom_engine_search_string_example),
                        infernoStyle = InfernoTextStyle.Subtitle,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // learn more
                    InfernoText(
                        text = stringResource(R.string.exceptions_empty_message_learn_more_link),
                        infernoStyle = InfernoTextStyle.Subtitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                (context.getActivity() as HomeActivity).openToBrowserAndLoad(
                                    searchTermOrURL = SupportUtils.getSumoURLForTopic(
                                        context,
                                        SupportUtils.SumoTopic.CUSTOM_SEARCH_ENGINES,
                                    ),
                                    newTab = true,
                                    from = BrowserDirection.FromSaveSearchEngineFragment,
                                )
                            },
                        fontColor = Color.Red, // todo: theme
                    )

                    // set search suggestion api url
                    InfernoOutlinedTextField(
                        value = suggestUrl,
                        onValueChange = {
                            suggestUrl = it
                            suggestUrlError = checkForSuggestUrlError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            InfernoText(text = stringResource(R.string.search_add_custom_engine_suggest_url_label))
                        },
                        placeholder = {
                            InfernoText(
                                text = stringResource(R.string.search_add_custom_engine_suggest_string_hint),
                                fontColor = Color.DarkGray, // todo: theme
                            )
                        },
                        supportingText = {
                            if (suggestUrlError != null) InfernoText(text = suggestUrlError!!)
                        },
                        isError = suggestUrlError != null,
                    )

                    // api url hint
                    InfernoText(
                        text = stringResource(R.string.search_add_custom_engine_suggest_string_example_2),
                        infernoStyle = InfernoTextStyle.Subtitle,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // description learn more
                    InfernoText(
                        text = stringResource(R.string.exceptions_empty_message_learn_more_link),
                        infernoStyle = InfernoTextStyle.Subtitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                (context.getActivity() as HomeActivity).openToBrowserAndLoad(
                                    searchTermOrURL = SupportUtils.getSumoURLForTopic(
                                        context,
                                        SupportUtils.SumoTopic.CUSTOM_SEARCH_ENGINES,
                                    ),
                                    newTab = true,
                                    from = BrowserDirection.FromSaveSearchEngineFragment,
                                )
                            },
                        fontColor = Color.Red, // todo: theme
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1F),
                        ) { InfernoText(text = stringResource(android.R.string.cancel)) }
                        Button(
                            onClick = { createCustomEngine() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1F),
                        ) { InfernoText(text = stringResource(R.string.search_custom_engine_save_button)) }
                    }
                }
            }

            // if loading block touch events and show loading icon
            if (job != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.DarkGray.copy(alpha = 0.5F)
                    ),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = Color.Red, // todo: theme
                        )
                    }
                }
            }
        }
    }
}

//private fun String.toEditableUrl(): String {
//    return replace("{searchTerms}", "%s")
//}

private fun String.toSearchUrl(): String {
    return replace("%s", "{searchTerms}")
}
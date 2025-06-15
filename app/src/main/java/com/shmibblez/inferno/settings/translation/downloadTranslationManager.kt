package com.shmibblez.inferno.settings.translation

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.ConnectivityManagerCompat.RESTRICT_BACKGROUND_STATUS_ENABLED
import androidx.core.net.ConnectivityManagerCompat.RESTRICT_BACKGROUND_STATUS_WHITELISTED
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.compose.InfoCard
import com.shmibblez.inferno.compose.InfoType
import com.shmibblez.inferno.compose.LinkText
import com.shmibblez.inferno.compose.LinkTextState
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.mozillaAndroidComponents.compose.base.Divider
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.translations.DownloadIconIndicator
import com.shmibblez.inferno.translations.DownloadInProgressIndicator
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguageItemPreference
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguageItemTypePreference
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguagesPreferenceFragment
import com.shmibblez.inferno.wifi.WifiConnectionMonitor
import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.TranslationsAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.translate.LanguageModel
import mozilla.components.concept.engine.translate.ModelManagementOptions
import mozilla.components.concept.engine.translate.ModelOperation
import mozilla.components.concept.engine.translate.ModelState
import mozilla.components.concept.engine.translate.OperationLevel
import mozilla.components.concept.engine.translate.TranslationError
import mozilla.components.feature.downloads.toMegabyteOrKilobyteString
import mozilla.components.lib.state.Store
import mozilla.components.lib.state.ext.observe
import mozilla.components.support.base.feature.LifecycleAwareFeature
import java.util.Locale

private val ICON_SIZE = 18.dp

internal class DownloadTranslationMangerState(
    val context: Context,
    val browserStore: BrowserStore,
    val lifecycleOwner: LifecycleOwner,
    val coroutineScope: CoroutineScope,
    private val wifiConnectionMonitor: WifiConnectionMonitor,
    // todo: init somewhere or observe
    isDataSaverEnabledAndWifiDisabled: Boolean = false,
) : LifecycleAwareFeature {

    /**
     * connectivity vars
     */

    @VisibleForTesting
    internal var connectivityManager: ConnectivityManager? = null

    @VisibleForTesting
    internal val wifiConnectedListener: ((Boolean) -> Unit) by lazy {
        { connected: Boolean ->
            var isDataSaverEnabled = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val restrictBackgroundStatus = connectivityManager?.restrictBackgroundStatus
                if (restrictBackgroundStatus == RESTRICT_BACKGROUND_STATUS_ENABLED || restrictBackgroundStatus == RESTRICT_BACKGROUND_STATUS_WHITELISTED) {
                    isDataSaverEnabled = true
                }
            }

            if (isDataSaverEnabled && !connected) {
                this.isDataSaverEnabledAndWifiDisabled = true
            } else {
                this.isDataSaverEnabledAndWifiDisabled = false
            }
        }
    }

    private fun addWifiConnectedListener() {
        wifiConnectionMonitor.addOnWifiConnectedChangedListener(wifiConnectedListener)
    }

    private fun removeWifiConnectedListener() {
        wifiConnectionMonitor.removeOnWifiConnectedChangedListener(wifiConnectedListener)
    }

    /**
     * language model vars
     */

    private var isDataSaverEnabledAndWifiDisabled by mutableStateOf(
        isDataSaverEnabledAndWifiDisabled
    )

    val learnMoreUrl = SupportUtils.getSumoURLForTopic(
        context,
        SupportUtils.SumoTopic.TRANSLATIONS,
    )

    var observer: Store.Subscription<BrowserState, BrowserAction>? = null

    var downloadLanguagesError: TranslationError.ModelCouldNotRetrieveError? = null
    var downloadLanguageItemPreferences by mutableStateOf<List<DownloadLanguageItemPreference>>(
        emptyList()
    )
    var downloadedItems by mutableStateOf<List<DownloadLanguageItemPreference>>(
        emptyList()
    )
    var notDownloadedItems by mutableStateOf<List<DownloadLanguageItemPreference>>(
        emptyList()
    )
    var downloadInProgressItems by mutableStateOf<List<DownloadLanguageItemPreference>>(
        emptyList()
    )
    var deleteInProgressItems by mutableStateOf<List<DownloadLanguageItemPreference>>(
        emptyList()
    )
    var allLanguagesItemDownloaded by mutableStateOf<DownloadLanguageItemPreference?>(null)
    var itemsNotDownloaded by mutableStateOf<List<DownloadLanguageItemPreference>>(emptyList())
    var pivotLanguage by mutableStateOf<DownloadLanguageItemPreference?>(null)


    override fun start() {
        // todo: switch to flowScoped
        // language models observer
        observer = browserStore.observe(
            owner = lifecycleOwner,
            observer = { state ->
                // refresh language item list
                refreshDownloadLanguageItemPreferences(
                    state.translationEngine.languageModels?.toMutableList()
                )
                refreshLanguageListDependentItems()
                // refresh error
                downloadLanguagesError =
                    state.translationEngine.engineError as? TranslationError.ModelCouldNotRetrieveError
            },
        )

        // wifi / connectivity
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiConnectionMonitor.start()
        addWifiConnectedListener()
    }

    override fun stop() {
        // language models observer
        observer?.unsubscribe()

        // wifi / connectivity
        connectivityManager = null
        wifiConnectionMonitor.stop()
        removeWifiConnectedListener()
    }

    private fun refreshDownloadLanguageItemPreferences(languageModels: MutableList<LanguageModel>?) {
        val languageItemPreferenceList = mutableListOf<DownloadLanguageItemPreference>()

        languageModels?.let {
            var allLanguagesSizeDownloaded = 0L

            for (languageModel in languageModels) {
                var size = 0L
                languageModel.size?.let { size = it }

                if (languageModel.status == ModelState.DOWNLOADED) {
                    allLanguagesSizeDownloaded += size
                }
            }

            addAllLanguagesDownloaded(
                allLanguagesSizeDownloaded,
                languageItemPreferenceList,
            )

            val iterator = languageModels.iterator()
            while (iterator.hasNext()) {
                val languageModel = iterator.next()
                if (languageModel.language?.code.equals(
                        Locale.ENGLISH.language,
                    )
                ) {
                    languageItemPreferenceList.add(
                        DownloadLanguageItemPreference(
                            languageModel = languageModel,
                            type = DownloadLanguageItemTypePreference.PivotLanguage,
                            enabled = allLanguagesSizeDownloaded == 0L,
                        ),
                    )
                    iterator.remove()
                }

                if (!languageModel.language?.code.equals(Locale.ENGLISH.language)) {
                    languageItemPreferenceList.add(
                        DownloadLanguageItemPreference(
                            languageModel = languageModel,
                            type = DownloadLanguageItemTypePreference.GeneralLanguage,
                            enabled = languageModel.status != ModelState.DELETION_IN_PROGRESS,
                        ),
                    )
                }
            }
        }
        downloadLanguageItemPreferences = languageItemPreferenceList
    }

    private fun refreshLanguageListDependentItems() {
        downloadedItems = downloadLanguageItemPreferences.filter {
            (it.languageModel.status == ModelState.DOWNLOADED || it.languageModel.status == ModelState.ERROR_DELETION) && it.type != DownloadLanguageItemTypePreference.AllLanguages
        }
        notDownloadedItems = downloadLanguageItemPreferences.filter {
            (it.languageModel.status == ModelState.NOT_DOWNLOADED || it.languageModel.status == ModelState.ERROR_DOWNLOAD) && it.type != DownloadLanguageItemTypePreference.AllLanguages
        }
        downloadInProgressItems = downloadLanguageItemPreferences.filter {
            it.languageModel.status == ModelState.DOWNLOAD_IN_PROGRESS && it.type != DownloadLanguageItemTypePreference.AllLanguages
        }
        deleteInProgressItems = downloadLanguageItemPreferences.filter {
            it.languageModel.status == ModelState.DELETION_IN_PROGRESS && it.type != DownloadLanguageItemTypePreference.AllLanguages
        }
        allLanguagesItemDownloaded = null
        if (downloadLanguageItemPreferences.any {
                it.type == DownloadLanguageItemTypePreference.AllLanguages && it.languageModel.status == ModelState.DOWNLOADED
            }) {
            allLanguagesItemDownloaded = downloadLanguageItemPreferences.last {
                it.type == DownloadLanguageItemTypePreference.AllLanguages && it.languageModel.status == ModelState.DOWNLOADED
            }
        }
//        val itemsNotDownloaded = mutableListOf<DownloadLanguageItemPreference>()
//        itemsNotDownloaded.addAll(downloadInProgressItems)
//        itemsNotDownloaded.addAll(deleteInProgressItems)
//        itemsNotDownloaded.addAll(notDownloadedItems)
//        itemsNotDownloaded.sortBy { it.languageModel.language?.localizedDisplayName }
        itemsNotDownloaded = (downloadInProgressItems + deleteInProgressItems + notDownloadedItems)
            // sort by display name
            .sortedBy { it.languageModel.language?.localizedDisplayName }
        // reset pivot language
        pivotLanguage = null
        if (downloadLanguageItemPreferences.any { it.type == DownloadLanguageItemTypePreference.PivotLanguage }) {
            pivotLanguage = downloadLanguageItemPreferences.last {
                it.type == DownloadLanguageItemTypePreference.PivotLanguage
            }
        }
    }


    fun deleteOrDownloadModel(downloadLanguageItemPreference: DownloadLanguageItemPreference) {
        val options = ModelManagementOptions(
            languageToManage = downloadLanguageItemPreference.languageModel.language?.code,
            operation = if (downloadLanguageItemPreference.languageModel.status == ModelState.NOT_DOWNLOADED) {
                ModelOperation.DOWNLOAD
            } else {
                ModelOperation.DELETE
            },
            operationLevel = OperationLevel.LANGUAGE,
        )
        browserStore.dispatch(
            TranslationsAction.ManageLanguageModelsAction(
                options = options,
            ),
        )
    }

    fun openBrowserAndLoad(learnMoreUrl: String) {
        (context.getActivity() as HomeActivity).openToBrowserAndLoad(
            searchTermOrURL = learnMoreUrl,
            newTab = true,
            from = BrowserDirection.FromDownloadLanguagesPreferenceFragment,
        )
    }

    private fun addAllLanguagesDownloaded(
        allLanguagesSizeDownloaded: Long,
        languageItemPreferenceList: MutableList<DownloadLanguageItemPreference>,
    ) {
        if (allLanguagesSizeDownloaded != 0L) {
            languageItemPreferenceList.add(
                DownloadLanguageItemPreference(
                    languageModel = LanguageModel(
                        status = ModelState.DOWNLOADED,
                        size = allLanguagesSizeDownloaded,
                    ),
                    type = DownloadLanguageItemTypePreference.AllLanguages,
                ),
            )
        }
    }

    fun shouldShowPrefDownloadLanguageFileDialog(
        downloadLanguageItemPreference: DownloadLanguageItemPreference,
    ): Boolean {
        return (downloadLanguageItemPreference.languageModel.status == ModelState.NOT_DOWNLOADED && isDataSaverEnabledAndWifiDisabled && !context.settings().ignoreTranslationsDataSaverWarning)
    }

    fun shouldShowDownloadLanguagesHeader(
        itemsNotDownloaded: List<DownloadLanguageItemPreference>,
        deleteInProgressItems: List<DownloadLanguageItemPreference>,
        notDownloadedItems: List<DownloadLanguageItemPreference>,
    ): Boolean {
        return itemsNotDownloaded.isNotEmpty() || deleteInProgressItems.isNotEmpty() || notDownloadedItems.isNotEmpty()
    }
}

@Composable
internal fun rememberDownloadTranslationMangerState(): MutableState<DownloadTranslationMangerState> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val state = remember {
        mutableStateOf(
            DownloadTranslationMangerState(
                context = context,
                browserStore = context.components.core.store,
                coroutineScope = coroutineScope,
                lifecycleOwner = lifecycleOwner,
                wifiConnectionMonitor = context.components.wifiConnectionMonitor,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()

        onDispose {
            state.value.stop()
        }
    }

    return state
}

/**
 *  based off [DownloadLanguagesPreferenceFragment]
 */
internal fun LazyListScope.downloadTranslationManager(
    state: DownloadTranslationMangerState,
    onItemClick: (DownloadLanguageItemPreference) -> Unit,
) {
    /**
     * title
     */

    item {
        DownloadLanguagesTitle(learnMoreUrl = state.learnMoreUrl, onLearnMoreClicked = {
            // todo: not working
            state.openBrowserAndLoad(state.learnMoreUrl)
        })
    }
    if (state.downloadLanguagesError != null) {
        item {
            DownloadLanguagesErrorWarning(
                stringResource(id = R.string.download_languages_fetch_error_warning_text),
            )
        }
    }

    /**
     * available/downloaded languages
     */

    // available languages title
    if (state.allLanguagesItemDownloaded != null || state.pivotLanguage?.languageModel?.status == ModelState.DOWNLOADED) {
        item {
            PreferenceTitle(stringResource(id = R.string.download_languages_available_languages_preference))
        }
    }

    // all languages downloaded item
    state.allLanguagesItemDownloaded?.let {
        item {
            DownloadTranslationItem(
                item = state.allLanguagesItemDownloaded!!,
                onItemClick = onItemClick,
            )
        }
    }

    // downloaded languages
    items(state.downloadedItems) { item: DownloadLanguageItemPreference ->
        DownloadTranslationItem(
            item = item,
            onItemClick = onItemClick,
        )
    }

    /**
     * languages not downloaded yet
     */

    // download languages title
    if (state.pivotLanguage?.languageModel?.status == ModelState.NOT_DOWNLOADED || state.shouldShowDownloadLanguagesHeader(
            itemsNotDownloaded = state.itemsNotDownloaded,
            deleteInProgressItems = state.deleteInProgressItems,
            notDownloadedItems = state.notDownloadedItems,
        )
    ) {
        if (state.pivotLanguage?.languageModel?.status == ModelState.DOWNLOADED || state.allLanguagesItemDownloaded != null) {
            item {
                Divider(Modifier.padding(top = 8.dp, bottom = 8.dp))
            }
        }

        item {
            PreferenceTitle(stringResource(id = R.string.download_language_header_preference))
        }
    }

    // languages not downloaded
    items(state.itemsNotDownloaded) { item ->
        DownloadTranslationItem(
            item = item,
            onItemClick = onItemClick,
        )
    }

    // bottom spacer
    item {
        Spacer(
            modifier = Modifier.padding(bottom = PrefUiConst.PREFERENCE_VERTICAL_PADDING),
        )
    }
}

@Composable
private fun DownloadLanguagesTitle(
    learnMoreUrl: String,
    onLearnMoreClicked: () -> Unit,
) {
    val learnMoreText =
        stringResource(id = R.string.download_languages_header_learn_more_preference)
    val learnMoreState = LinkTextState(
        text = learnMoreText,
        url = learnMoreUrl,
        onClick = { onLearnMoreClicked() },
    )
    LinkText(
        text = stringResource(
            R.string.download_languages_header_preference,
            learnMoreText,
        ),
        modifier = Modifier.padding(
            horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
            vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
        ),
        linkTextStates = listOf(learnMoreState),
        linkTextDecoration = TextDecoration.Underline,
    )
}

@Composable
private fun DownloadLanguagesErrorWarning(title: String) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(start = 72.dp, end = 16.dp)
        .defaultMinSize(minHeight = 56.dp)
        .wrapContentHeight()

    InfoCard(
        description = title,
        type = InfoType.Warning,
        verticalRowAlignment = Alignment.CenterVertically,
        modifier = modifier,
    )
}

@Composable
private fun DownloadTranslationItem(
    item: DownloadLanguageItemPreference,
    onItemClick: (DownloadLanguageItemPreference) -> Unit,
) {
    val description: String =
        if (item.type == DownloadLanguageItemTypePreference.PivotLanguage && item.languageModel.status == ModelState.DOWNLOADED && !item.enabled) {
            stringResource(id = R.string.download_languages_default_system_language_require_preference)
        } else {
            var size = 0L
            item.languageModel.size?.let { size = it }
            size.toMegabyteOrKilobyteString()
        }

    val label = if (item.type == DownloadLanguageItemTypePreference.AllLanguages) {
        if (item.languageModel.status == ModelState.NOT_DOWNLOADED) {
            stringResource(id = R.string.download_language_all_languages_item_preference)
        } else {
            stringResource(id = R.string.download_language_all_languages_item_preference_to_delete)
        }
    } else {
        item.languageModel.language?.localizedDisplayName
    }

    val contentDescription = downloadLanguageItemContentDescriptionPreference(
        item = item,
        label = label,
        itemDescription = description,
    )

    label?.let {
        DownloadTranslationItemDescription(
            item = item,
            label = it,
            modifier = Modifier
                .clearAndSetSemantics {
                    role = Role.Button
                    this.contentDescription = contentDescription
                }
//                .defaultMinSize(minHeight = 56.dp)
                .wrapContentHeight(),
            description = description,
            enabled = item.enabled,
            onClick = { onItemClick(item) },
        )
    }

    // display error
    when (item.languageModel.status) {
        ModelState.ERROR_DELETION -> {
            item.languageModel.language?.localizedDisplayName?.let {
                DownloadLanguagesErrorWarning(
                    stringResource(
                        R.string.download_languages_delete_error_warning_text,
                        it,
                    ),
                )
            }
        }

        ModelState.ERROR_DOWNLOAD -> {
            item.languageModel.language?.localizedDisplayName?.let {
                DownloadLanguagesErrorWarning(
                    stringResource(
                        R.string.download_languages_error_warning_text,
                        it,
                    ),
                )
            }
        }

        ModelState.NOT_DOWNLOADED,
        ModelState.DOWNLOAD_IN_PROGRESS,
        ModelState.DELETION_IN_PROGRESS,
        ModelState.DOWNLOADED,
            -> {
        }
    }
}

@Composable
private fun DownloadTranslationItemDescription(
    item: DownloadLanguageItemPreference,
    label: String,
    modifier: Modifier = Modifier,
    description: String,
    enabled: Boolean = true,
    onClick: (() -> Unit),
) {
    val context = LocalContext.current
    val text = stringResource(
        R.string.download_languages_language_item_preference,
        label,
        description,
    )

    Row(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .fillMaxWidth()
            .padding(
                horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
            ),
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // language name and download size
        InfernoText(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = context.infernoTheme().value.primaryTextColor)) {
                    append(label)
                }
                withStyle(style = SpanStyle(color = context.infernoTheme().value.secondaryTextColor)) {
                    append(text.substringAfter(label))
                }
            },
        )

        // item icon
        when (item.languageModel.status) {
            ModelState.DOWNLOADED, ModelState.ERROR_DELETION -> {
                if (item.enabled) {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_delete_24),
                        contentDescription = null,
                        modifier = Modifier.size(ICON_SIZE),
                    )
                }
            }

            ModelState.NOT_DOWNLOADED, ModelState.ERROR_DOWNLOAD -> {
                InfernoIcon(
                    painter = painterResource(
                        id = R.drawable.ic_download_24,
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(ICON_SIZE),
                )
            }

            ModelState.DOWNLOAD_IN_PROGRESS -> {
                DownloadInProgressIndicator(modifier = Modifier.size(ICON_SIZE))
            }

            ModelState.DELETION_IN_PROGRESS -> {
                DownloadIconIndicator(
                    icon = painterResource(R.drawable.mozac_ic_sync_24),
                    modifier = Modifier.size(ICON_SIZE),
                )
            }
        }
    }
}

@Composable
private fun downloadLanguageItemContentDescriptionPreference(
    item: DownloadLanguageItemPreference,
    label: String? = null,
    itemDescription: String,
): String {
    val contentDescription: String

    when (item.languageModel.status) {
        ModelState.DOWNLOADED, ModelState.ERROR_DELETION -> {
            contentDescription = "$label $itemDescription" + stringResource(
                id = R.string.download_languages_item_content_description_downloaded_state,
            )
        }

        ModelState.NOT_DOWNLOADED, ModelState.ERROR_DOWNLOAD -> {
            contentDescription = "$label $itemDescription " + stringResource(
                id = R.string.download_languages_item_content_description_not_downloaded_state,
            )
        }

        ModelState.DELETION_IN_PROGRESS -> {
            contentDescription = "$label $itemDescription " + stringResource(
                id = R.string.download_languages_item_content_description_delete_in_progress_state,
            )
        }

        ModelState.DOWNLOAD_IN_PROGRESS -> {
            contentDescription = stringResource(
                id = R.string.download_languages_item_content_description_download_in_progress_state,
                item.languageModel.language?.localizedDisplayName ?: "",
                item.languageModel.size?.toMegabyteOrKilobyteString() ?: "0",
            )
        }
    }
    return contentDescription
}
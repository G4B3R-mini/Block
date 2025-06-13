package com.shmibblez.inferno.settings.extensions

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.shmibblez.inferno.BuildConfig
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.ext.newTab
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.translateDescription
import mozilla.components.feature.addons.ui.translateName
import com.shmibblez.inferno.R
import com.shmibblez.inferno.addons.InstalledAddonDetailsFragment
import com.shmibblez.inferno.addons.InstalledAddonDetailsFragmentDirections
import com.shmibblez.inferno.compose.StarRating
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoSwitch
import com.shmibblez.inferno.settings.SupportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.concept.engine.webextension.EnableSource
import mozilla.components.feature.addons.ui.AddonsManagerAdapterDelegate
import mozilla.components.feature.addons.ui.updatedAtDate
import mozilla.components.feature.addons.update.AddonUpdater
import mozilla.components.feature.addons.update.DefaultAddonUpdater.UpdateAttemptStorage
import mozilla.components.support.ktx.kotlin.ifNullOrEmpty
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * todo: pass addon id instead, query in launchedEffect, show loading screen if addon == null,
 *  show error screen if addon loading error, with retry and cancel buttons side by side
 *  reference [InstalledAddonDetailsFragment.bindAddon]
 */
@Composable
internal fun ExtensionPage(
    initialAddon: Addon,
    goBack: () -> Unit,
    onNavToBrowser: () -> Unit,
) {

    val context = LocalContext.current
    var updaterDialogRequested by remember { mutableStateOf(false) }
    var showUpdaterDialogFor by remember { mutableStateOf<AddonUpdater.UpdateAttempt?>(null) }
    val updateAttemptStorage by remember { lazy { UpdateAttemptStorage(context) } }
    val scope = rememberCoroutineScope()
    val addonManager = context.components.addonManager

    var showPermissionDialog by remember { mutableStateOf(false) }

    var addon by remember { mutableStateOf(initialAddon) }

    fun refreshAddon(updatedAddon: Addon) {
        addon = updatedAddon
    }

    fun refreshAddonOptionsVisible(): Boolean {
        return addon.isInstalled() // todo: check if this is how its done
    }

    var addonOptionsVisible by remember { mutableStateOf(refreshAddonOptionsVisible()) }

    fun refreshPrivateSwitchVisible(): Boolean {
        return addon.incognito != Addon.Incognito.NOT_ALLOWED
    }

    fun refreshSettingsOptionVisible(): Boolean {
        return !addon.installedState?.optionsPageUrl.isNullOrEmpty()
    }

    var privateSwitchVisible by remember { mutableStateOf(refreshPrivateSwitchVisible()) }
    var settingsOptionVisible by remember { mutableStateOf(refreshSettingsOptionVisible()) }

    // When the ad-on is blocklisted or not correctly signed, we do not want to enable the toggle switch
    // because users shouldn't be able to re-enable an add-on in this state.
    fun refreshEnableSwitchEnabled(): Boolean {
        return !addon.isDisabledAsBlocklisted() && !addon.isDisabledAsNotCorrectlySigned() && !addon.isDisabledAsIncompatible()
    }

    fun refreshPrivateSwitchEnabled(): Boolean {
        return addon.incognito != Addon.Incognito.NOT_ALLOWED && addon.isEnabled()
    }

    var enableSwitchEnabled by remember { mutableStateOf(refreshEnableSwitchEnabled()) }
    var privateSwitchEnabled by remember { mutableStateOf(refreshPrivateSwitchEnabled()) }
    var buttonsEnabled by remember { mutableStateOf(true) }

    fun disableAllOptions() {
        buttonsEnabled = false
        enableSwitchEnabled = false
        privateSwitchEnabled = false
    }

    fun refreshEnableSwitchChecked(): Boolean {
        return addon.isEnabled()
    }

    fun refreshPrivateSwitchChecked(): Boolean {
        return addon.isAllowedInPrivateBrowsing()
    }

    var enableSwitchChecked by remember { mutableStateOf(refreshEnableSwitchChecked()) }
    var privateSwitchChecked by remember { mutableStateOf(refreshPrivateSwitchChecked()) }

    fun enableAddon(
        onSuccess: (Addon) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        // If the addon is migrated from Fennec and supported in Fenix, for the addon to be enabled,
        // we need to also request the addon to be enabled as supported by the app
        if (addon.isSupported() && addon.isDisabledAsUnsupported()) {
            addonManager.enableAddon(
                addon,
                EnableSource.APP_SUPPORT,
                { enabledAddon ->
                    addonManager.enableAddon(enabledAddon, EnableSource.USER, onSuccess, onError)
                },
                onError,
            )
        } else {
            addonManager.enableAddon(addon, EnableSource.USER, onSuccess, onError)
        }
    }

    fun refreshOptions() {
        addonOptionsVisible = refreshAddonOptionsVisible()
        privateSwitchVisible = refreshPrivateSwitchVisible()
        settingsOptionVisible = refreshSettingsOptionVisible()
        // enable/disable items
        enableSwitchEnabled = refreshEnableSwitchEnabled()
        privateSwitchEnabled = refreshPrivateSwitchEnabled()
//        buttonsEnabled = // set manually on task started/completed
        // check/uncheck switches
        enableSwitchChecked = refreshEnableSwitchChecked()
        privateSwitchChecked = refreshPrivateSwitchChecked()
    }

    // update state depending on addon
    LaunchedEffect(addon) {
        refreshOptions()
    }

    LaunchedEffect(updaterDialogRequested) {
        if (updaterDialogRequested) {
            scope.launch {
                val updateAttempt = withContext(Dispatchers.IO) {
                    updateAttemptStorage.findUpdateAttemptBy(addon.id)
                }
                updateAttempt.let {
                    when (it) {
                        null -> {
                            updaterDialogRequested = false
                            showUpdaterDialogFor = null
                        }

                        else -> {
                            updaterDialogRequested = false
                            showUpdaterDialogFor = it
                        }
                    }
                }
            }
        }
    }

    InfernoSettingsPage(
        title = addon.translateName(context),
        goBack = goBack,
    ) { edgeInsets ->
        val openWebsite = { uri: Uri ->
            context.components.newTab(
                url = uri.toString(),
                selectTab = true,
            )
            onNavToBrowser.invoke()
        }

        LazyColumn(
            modifier = Modifier.padding(edgeInsets),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {

            // installed options, show if has optionsUrl
            if (addonOptionsVisible) {
                warningMessageBars(
                    addon = addon,
                    onLearnMoreLinkClicked = { link, addon ->
                        val url = when (link) {
                            AddonsManagerAdapterDelegate.LearnMoreLinks.BLOCKLISTED_ADDON -> "${BuildConfig.AMO_BASE_URL}/android/blocked-addon/${addon.id}/${addon.version}/"

                            AddonsManagerAdapterDelegate.LearnMoreLinks.ADDON_NOT_CORRECTLY_SIGNED -> SupportUtils.getSumoURLForTopic(
                                context,
                                SupportUtils.SumoTopic.UNSIGNED_ADDONS,
                            )
                        }
                        context.components.newTab(url = url)
                        onNavToBrowser.invoke()
                    },
                )

                installedAddonOptions(
                    privateSwitchVisible = privateSwitchVisible,
                    settingsOptionVisible = settingsOptionVisible,
                    enableSwitchEnabled = enableSwitchEnabled,
                    privateSwitchEnabled = privateSwitchEnabled,
                    buttonsEnabled = buttonsEnabled,
                    enableSwitchChecked = enableSwitchChecked,
                    privateSwitchChecked = privateSwitchChecked,
                    onToggleEnable = {
                        if (!enableSwitchEnabled) return@installedAddonOptions
                        // disable all options, re-enabled when task completed or when addon updated
                        disableAllOptions()
                        // enable/disable addon
                        when (enableSwitchChecked) {
                            true -> {
                                // if enabled, disable
                                addonManager.disableAddon(
                                    addon,
                                    onSuccess = {
                                        refreshAddon(it)
                                        buttonsEnabled = true
                                        Toast.makeText(
                                            context, context.getString(
                                                R.string.mozac_feature_addons_successfully_disabled,
                                                addon.translateName(context),
                                            ), Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = {
                                        buttonsEnabled = true
                                        Toast.makeText(
                                            context, context.getString(
                                                R.string.mozac_feature_addons_failed_to_disable,
                                                addon.translateName(context),
                                            ), Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                )
                            }

                            false -> {
                                // if not enabled, enable
                                enableAddon(
                                    onSuccess = {
                                        refreshAddon(it)
                                        buttonsEnabled = true
                                        Toast.makeText(
                                            context, context.getString(
                                                R.string.mozac_feature_addons_successfully_enabled,
                                                addon.translateName(context),
                                            ), Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = {
                                        buttonsEnabled = true
                                        Toast.makeText(
                                            context, context.getString(
                                                R.string.mozac_feature_addons_failed_to_enable,
                                                addon.translateName(context),
                                            ), Toast.LENGTH_SHORT
                                        ).show()
                                        refreshOptions()
                                    },
                                )
                            }
                        }
                    },
                    onTogglePrivateBrowsing = {
                        if (!privateSwitchEnabled) return@installedAddonOptions
                        // disable all options, re-enabled when task completed or when addon updated
                        disableAllOptions()
                        // enable/disable
                        // if enabled, disable
                        addonManager.setAddonAllowedInPrivateBrowsing(
                            addon,
                            !privateSwitchChecked, // set to opposite
                            onSuccess = {
                                refreshAddon(it)
                                buttonsEnabled = true
                            },
                            onError = {
                                buttonsEnabled = true
                                refreshOptions()
                            },
                        )
                    },
                    onClickSettings = {
                        if (!settingsOptionVisible) return@installedAddonOptions
                        // settings can be in web page or in settings dialog, check
                        val settingUrl =
                            addon.installedState?.optionsPageUrl ?: return@installedAddonOptions
                        if (addon.installedState?.openOptionsPageInTab == true) {
//                            val shouldCreatePrivateSession = (activity as HomeActivity).browsingModeManager.mode.isPrivate
                            // if current tab is private
                            val shouldCreatePrivateSession =
                                context.components.core.store.state.selectedTab?.content?.private
                                    ?: false
                            // If the addon settings page is already open in a tab, select that one
                            context.components.useCases.tabsUseCases.selectOrAddTab(
                                url = settingUrl,
                                private = shouldCreatePrivateSession,
                                ignoreFragment = true,
                            )
                            // send user to newly selected tab
                            onNavToBrowser.invoke()
                        } else {
                            /**
                             * based off: [InstalledAddonDetailsFragmentDirections.actionInstalledAddonFragmentToAddonInternalSettingsFragment]
                             * instead of new page with engine view and prompt/download request support, just opens new custom tab
                             */
                            // open in custom tab
                            val url = addon.installedState?.optionsPageUrl
                            if (url != null) {
                                val intent = SupportUtils.createCustomTabIntent(context, url)
                                context.startActivity(intent)
                            }
                        }
                    },
                    onClickPermissions = { showPermissionDialog = true },
                    onRemove = {
                        // todo
                    },
                    onReport = {
                        if (!buttonsEnabled) return@installedAddonOptions
//                        val shouldCreatePrivateSession = (activity as HomeActivity).browsingModeManager.mode.isPrivate
                        // if current tab is private
                        val shouldCreatePrivateSession =
                            context.components.core.store.state.selectedTab?.content?.private
                                ?: false
                        // select/add report site tab
                        context.components.useCases.tabsUseCases.selectOrAddTab(
                            url = "${BuildConfig.AMO_BASE_URL}/android/feedback/addon/${addon.id}/",
                            private = shouldCreatePrivateSession,
                            ignoreFragment = true,
                        )
                        // send user to newly selected tab
                        onNavToBrowser.invoke()
                    },
                )
            }

            // addon description
            addonDetails(
                addon,
                openWebsite = openWebsite,
            )

            // author label
            addon.author?.let {
                if (it.name.isBlank()) return@let
                label(title = context.getString(R.string.mozac_feature_addons_author)) {
                    InfernoText(
                        text = it.name,
                        modifier = Modifier.clickable { openWebsite(it.url.toUri()) },
                        fontColor = context.infernoTheme().value.primaryActionColor,
                    )
                }
            }

            // version label
            label(title = context.getString(R.string.mozac_feature_addons_version)) {
                InfernoText(
                    text = addon.installedState?.version.ifNullOrEmpty { addon.version },
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = { if (addon.isInstalled()) updaterDialogRequested = true },
                    ),
                    fontColor = context.infernoTheme().value.primaryActionColor,
                )
            }

            // last updated label
            if (addon.updatedAt.isNotBlank()) {
                label(title = context.getString(R.string.mozac_feature_addons_last_updated)) {
                    val formattedDate = DateFormat.getDateInstance().format(addon.updatedAtDate)
                    InfernoText(formattedDate)
                }
            }

            // homepage
            if (addon.homepageUrl.isNotBlank()) {
                label {
                    InfernoText(
                        text = stringResource(R.string.mozac_feature_addons_home_page),
                        modifier = Modifier.clickable { openWebsite(addon.homepageUrl.toUri()) },
                        fontColor = context.infernoTheme().value.primaryActionColor,
                    )
                }
            }

            // rating
            addon.rating?.let {
                label(context.getString(R.string.mozac_feature_addons_rating)) {
                    StarRating(it.average)
                    val reviews =
                        NumberFormat.getNumberInstance(Locale.getDefault()).format(it.reviews)
                    InfernoText(
                        text = context.getString(
                            R.string.mozac_feature_addons_user_rating_count_2,
                            reviews,
                        )
                    )
                }
            }

            // detail url
            if (addon.detailUrl.isNotBlank()) {
                label {
                    InfernoText(
                        text = stringResource(R.string.mozac_feature_addons_more_info_link_2),
                        modifier = Modifier.clickable { openWebsite(addon.detailUrl.toUri()) },
                        fontColor = context.infernoTheme().value.primaryActionColor,
                    )
                }
            }
        }

        if (showUpdaterDialogFor != null) {
            ExtensionUpdaterDialog(
                updateAttempt = showUpdaterDialogFor!!,
                onDismiss = { showUpdaterDialogFor = null },
            )
        }

        if (showPermissionDialog) {
            ExtensionPermissionsDialog(
                onDismiss = {showPermissionDialog = false},
                addon = addon,
                openWebsite = openWebsite,
            )
        }
    }
}

private fun LazyListScope.warningMessageBars(
    addon: Addon,
    onLearnMoreLinkClicked: (link: AddonsManagerAdapterDelegate.LearnMoreLinks, addon: Addon) -> Unit,
) {
    item {
        AddonMessageBars(
            addon = addon,
            onLearnMoreLinkClicked = onLearnMoreLinkClicked,
        )
    }
}

private fun LazyListScope.installedAddonOptions(
    privateSwitchVisible: Boolean,
    settingsOptionVisible: Boolean,
    enableSwitchEnabled: Boolean,
    privateSwitchEnabled: Boolean,
    buttonsEnabled: Boolean,
    enableSwitchChecked: Boolean,
    privateSwitchChecked: Boolean,
    onToggleEnable: () -> Unit,
    onTogglePrivateBrowsing: () -> Unit,
    onClickSettings: () -> Unit,
    onClickPermissions: () -> Unit,
    onRemove: () -> Unit, // todo: may be action (if disabled may be to enable, or maybe just remove, check moz implementation)
    onReport: () -> Unit,
) {
    // enabled
    installedAddonOption(
        titleResId = R.string.mozac_feature_addons_enabled,
        leadingIcon = null,
        trailingContent = {
            InfernoSwitch(
                checked = enableSwitchChecked,
                onCheckedChange = { onToggleEnable.invoke() },
                enabled = enableSwitchEnabled,
            )
        },
    )
    // run in private browsing
    installedAddonOption(
        titleResId = R.string.mozac_feature_addons_settings_run_in_private_browsing,
        leadingIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_private_browsing_24),
                contentDescription = "",
                modifier = Modifier.size(22.dp),
            )
        },
        trailingContent = {
            // show switch if available in private mode
            // if not available, show message
            when (privateSwitchVisible) {
                true -> {
                    // switch
                    InfernoSwitch(
                        checked = privateSwitchChecked,
                        onCheckedChange = { onTogglePrivateBrowsing.invoke() },
                        enabled = privateSwitchEnabled,
                    )
                }

                false -> {
                    // not allowed in private mode desc
                    InfernoText(stringResource(R.string.mozac_feature_addons_not_allowed_in_private_browsing))
                }
            }
        },
    )
    // addon settings
    if (settingsOptionVisible) {
        installedAddonOption(
            titleResId = R.string.mozac_feature_addons_settings,
            leadingIcon = {
                InfernoIcon(
                    painter = painterResource(R.drawable.ic_settings_24),
                    contentDescription = "",
                    modifier = Modifier.size(22.dp),
                )
            },
            trailingContent = null,
            onClick = onClickSettings,
        )
    }
    // addon permissions
    installedAddonOption(
        titleResId = R.string.mozac_feature_addons_permissions,
        leadingIcon = {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_permission_24),
                contentDescription = "",
                modifier = Modifier.size(22.dp),
            )
        },
        trailingContent = null,
        onClick = onClickPermissions,
    )

    // remove button
    item {
        InfernoButton(
            text = stringResource(R.string.mozac_feature_addons_remove),
            sensitive = true,
            onClick = onRemove,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            enabled = buttonsEnabled,
            shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonColors(
                containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                contentColor = LocalContext.current.infernoTheme().value.errorColor,
                disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                disabledContentColor = LocalContext.current.infernoTheme().value.errorColor,
            ),
        )
    }
    // report button
    item {
        InfernoButton(
            text = stringResource(R.string.mozac_feature_addons_report),
            onClick = onReport,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            enabled = buttonsEnabled,
            shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonColors(
                containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                contentColor = LocalContext.current.infernoTheme().value.primaryTextColor,
                disabledContainerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
                disabledContentColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
            ),
        )
    }

    divider()
}

private fun LazyListScope.installedAddonOption(
    titleResId: Int,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(44.dp)
                .clickable { onClick?.invoke() },
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // leading icon
            when (leadingIcon) {
                null -> {
                    Box(modifier = Modifier.size(22.dp))
                }

                else -> {
                    leadingIcon.invoke()
                }
            }

            // title
            InfernoText(text = stringResource(titleResId))

            // spacer
            Spacer(modifier = Modifier.weight(1F))

            // trailing content
            when (trailingContent) {
                null -> {}
                else -> {
                    trailingContent.invoke()
                }
            }
        }
    }
}

private fun LazyListScope.divider() {
    item {
        val dividerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor
        HorizontalDivider(thickness = 1.dp, color = dividerColor)
    }
}

private fun LazyListScope.addonDetails(addon: Addon, openWebsite: (url: Uri) -> Unit) {
    // todo: extension info items, add page separators and padding
    item {
        val context = LocalContext.current
        val detailsText = addon.translateDescription(context)

        val parsedText = detailsText.replace("\n", "<br/>")
        val linkStyle = InfernoTextStyle.Normal.toTextStyle().toSpanStyle().copy(
            color = LocalContext.current.infernoTheme().value.primaryActionColor,
            textDecoration = TextDecoration.Underline,
        )
        val linkifiedText = AnnotatedString.fromHtml(
            htmlString = parsedText,
            linkStyles = TextLinkStyles(
                style = linkStyle,
                focusedStyle = linkStyle,
                hoveredStyle = linkStyle,
                pressedStyle = linkStyle,
            ),
            linkInteractionListener = {
                val url = (it as LinkAnnotation.Url).url
                openWebsite.invoke(url.toUri())
            },
        )

        InfernoText(text = linkifiedText, modifier = Modifier.padding(16.dp))
    }

    divider()
}

private fun LazyListScope.label(title: String? = null, content: @Composable RowScope.() -> Unit) {
    item {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            title?.let {
                InfernoText(title)
                Spacer(modifier = Modifier.weight(1F))
            }
            content.invoke(this)
        }
    }

    divider()
}
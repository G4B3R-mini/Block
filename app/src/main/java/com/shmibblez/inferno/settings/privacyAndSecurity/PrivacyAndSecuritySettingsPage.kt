package com.shmibblez.inferno.settings.privacyAndSecurity

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoCheckbox
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PrivacyAndSecuritySettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_category_privacy_security),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // private browsing title
            item {
                PreferenceTitle(stringResource(R.string.preferences_private_browsing_options))
            }

            // open links in private tab
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_open_links_in_a_private_tab),
                    summary = null,
                    selected = settings.openLinksInPrivateTab,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setOpenLinksInPrivateTab(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // allow screenshots in private mode
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preferences_allow_screenshots_in_private_mode),
                    summary = stringResource(R.string.preferences_screenshots_in_private_mode_disclaimer),
                    selected = settings.allowScreenshotsInPrivateMode,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setAllowScreenshotsInPrivateMode(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // enhanced tracking protection title
            item {
                PreferenceTitle(stringResource(R.string.preference_enhanced_tracking_protection))
            }

            // enhanced tracking protection enabled
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preference_enhanced_tracking_protection),
                    summary = null,
                    selected = settings.isEnhancedTrackingProtectionEnabled,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setIsEnhancedTrackingProtectionEnabled(selected)
                                    .build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            item {
                PreferenceSelect(
                    text = stringResource(R.string.preference_enhanced_tracking_protection),
                    description = when (settings.selectedTrackingProtection!!) {
                        InfernoSettings.TrackingProtectionDefault.STANDARD -> context.getString(R.string.preference_enhanced_tracking_protection_standard_description_5)
                        InfernoSettings.TrackingProtectionDefault.STRICT -> context.getString(R.string.preference_enhanced_tracking_protection_strict_description_4)
                        InfernoSettings.TrackingProtectionDefault.CUSTOM -> context.getString(R.string.preference_enhanced_tracking_protection_custom_description_2)
                    },
                    enabled = true,
                    selectedMenuItem = settings.selectedTrackingProtection,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.TrackingProtectionDefault.STANDARD,
                            InfernoSettings.TrackingProtectionDefault.STRICT,
                            InfernoSettings.TrackingProtectionDefault.CUSTOM,
                        )
                    },
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setSelectedTrackingProtection(selected).build()
                            }
                        }
                    },
                )
            }

            // custom tracking protection options
            if (settings.isEnhancedTrackingProtectionEnabled && settings.selectedTrackingProtection == InfernoSettings.TrackingProtectionDefault.CUSTOM) {
                // cookies
                item {
                    CustomTrackingProtectionPreferenceCheckbox(
                        name = stringResource(R.string.preference_enhanced_tracking_protection_custom_cookies),
                        checked = settings.customTrackingProtection.blockCookies,
                        onCheckedChanged = { selected ->
                            coroutineScope.launch {
                                context.infernoSettingsDataStore.updateData {
                                    it.toBuilder().setCustomTrackingProtection(
                                        it.customTrackingProtection.toBuilder()
                                            .setBlockCookies(selected).build()
                                    ).build()
                                }
                            }
                        },
                    )
                }

                // selected cookie policy
                if (settings.customTrackingProtection.blockCookies) {
                    item {
                        CustomTrackingProtectionPreferenceSelect(
                            selected = settings.customTrackingProtection.blockCookiesPolicy,
                            items = remember {
                                listOf(
                                    InfernoSettings.CustomTrackingProtection.CookiePolicy.ISOLATE_CROSS_SITE_COOKIES,
                                    InfernoSettings.CustomTrackingProtection.CookiePolicy.CROSS_SITE_AND_SOCIAL_MEDIA_TRACKERS,
                                    InfernoSettings.CustomTrackingProtection.CookiePolicy.COOKIES_FROM_UNVISITED_SITES,
                                    InfernoSettings.CustomTrackingProtection.CookiePolicy.ALL_THIRD_PARTY_COOKIES,
                                    InfernoSettings.CustomTrackingProtection.CookiePolicy.ALL_COOKIES,
                                )
                            },
                            mapToTitle = { it.toPrefString(context) },
                            onSelectMenuItem = { selected ->
                                coroutineScope.launch {
                                    context.infernoSettingsDataStore.updateData {
                                        it.toBuilder().setCustomTrackingProtection(
                                            it.customTrackingProtection.toBuilder()
                                                .setBlockCookiesPolicy(selected).build()
                                        ).build()
                                    }
                                }
                            },
                            enabled = true,
                        )
                    }
                }

                // tracking content
                item {
                    CustomTrackingProtectionPreferenceCheckbox(
                        name = stringResource(R.string.preference_enhanced_tracking_protection_custom_tracking_content),
                        checked = settings.customTrackingProtection.blockTrackingContent,
                        onCheckedChanged = { selected ->
                            coroutineScope.launch {
                                context.infernoSettingsDataStore.updateData {
                                    it.toBuilder().setCustomTrackingProtection(
                                        it.customTrackingProtection.toBuilder()
                                            .setBlockTrackingContent(selected).build()
                                    ).build()
                                }
                            }
                        },
                    )
                }

                // selected tracking protection
                if (settings.customTrackingProtection.blockTrackingContent) {
                    item {
                        CustomTrackingProtectionPreferenceSelect(
                            selected = settings.customTrackingProtection.blockTrackingContentSelection,
                            items = remember {
                                listOf(
                                    InfernoSettings.CustomTrackingProtection.TrackingContentSelection.BLOCK_TRACKING_NORMAL_ONLY,
                                    InfernoSettings.CustomTrackingProtection.TrackingContentSelection.BLOCK_TRACKING_PRIVATE_ONLY,
                                    InfernoSettings.CustomTrackingProtection.TrackingContentSelection.BLOCK_TRACKING_NORMAL_AND_PRIVATE,
                                )
                            },
                            mapToTitle = { it.toPrefString(context) },
                            onSelectMenuItem = { selected ->
                                coroutineScope.launch {
                                    context.infernoSettingsDataStore.updateData {
                                        it.toBuilder().setCustomTrackingProtection(
                                            it.customTrackingProtection.toBuilder()
                                                .setBlockTrackingContentSelection(selected).build()
                                        ).build()
                                    }
                                }
                            },
                            enabled = true,
                        )
                    }
                }

                // cryptominers
                item {
                    CustomTrackingProtectionPreferenceCheckbox(
                        name = stringResource(R.string.preference_enhanced_tracking_protection_custom_cryptominers),
                        checked = settings.customTrackingProtection.blockCryptominers,
                        onCheckedChanged = { selected ->
                            coroutineScope.launch {
                                context.infernoSettingsDataStore.updateData {
                                    it.toBuilder().setCustomTrackingProtection(
                                        it.customTrackingProtection.toBuilder()
                                            .setBlockCryptominers(selected).build()
                                    ).build()
                                }
                            }
                        },
                    )
                }

                // known fingerprinters
                item {
                    CustomTrackingProtectionPreferenceCheckbox(
                        name = stringResource(R.string.preference_enhanced_tracking_protection_custom_known_fingerprinters),
                        checked = settings.customTrackingProtection.blockKnownFingerprinters,
                        onCheckedChanged = { selected ->
                            coroutineScope.launch {
                                context.infernoSettingsDataStore.updateData {
                                    it.toBuilder().setCustomTrackingProtection(
                                        it.customTrackingProtection.toBuilder()
                                            .setBlockKnownFingerprinters(selected).build()
                                    ).build()
                                }
                            }
                        },
                    )
                }

                // redirect trackers
                item {
                    CustomTrackingProtectionPreferenceCheckbox(
                        name = stringResource(R.string.etp_redirect_trackers_title),
                        checked = settings.customTrackingProtection.blockRedirectTrackers,
                        onCheckedChanged = { selected ->
                            coroutineScope.launch {
                                context.infernoSettingsDataStore.updateData {
                                    it.toBuilder().setCustomTrackingProtection(
                                        it.customTrackingProtection.toBuilder()
                                            .setBlockRedirectTrackers(selected).build()
                                    ).build()
                                }
                            }
                        },
                    )
                }

                // suspected fingerprinters
                item {
                    CustomTrackingProtectionPreferenceCheckbox(
                        name = stringResource(R.string.etp_suspected_fingerprinters_title),
                        checked = settings.customTrackingProtection.blockSuspectedFingerprinters,
                        onCheckedChanged = { selected ->
                            coroutineScope.launch {
                                context.infernoSettingsDataStore.updateData {
                                    it.toBuilder().setCustomTrackingProtection(
                                        it.customTrackingProtection.toBuilder()
                                            .setBlockSuspectedFingerprinters(selected).build()
                                    ).build()
                                }
                            }
                        },
                    )
                }

                // selected tracking protection
                if (settings.customTrackingProtection.blockSuspectedFingerprinters) {
                    item {
                        CustomTrackingProtectionPreferenceSelect(
                            selected = settings.customTrackingProtection.blockSuspectedFingerprintersSelection,
                            items = remember {
                                listOf(
                                    InfernoSettings.CustomTrackingProtection.SuspectedFingerprintersSelection.BLOCK_SUSPECTED_FINGERPRINTERS_NORMAL_ONLY,
                                    InfernoSettings.CustomTrackingProtection.SuspectedFingerprintersSelection.BLOCK_SUSPECTED_FINGERPRINTERS_PRIVATE_ONLY,
                                    InfernoSettings.CustomTrackingProtection.SuspectedFingerprintersSelection.BLOCK_SUSPECTED_FINGERPRINTERS_NORMAL_AND_PRIVATE,
                                )
                            },
                            mapToTitle = { it.toPrefString(context) },
                            onSelectMenuItem = { selected ->
                                coroutineScope.launch {
                                    context.infernoSettingsDataStore.updateData {
                                        it.toBuilder().setCustomTrackingProtection(
                                            it.customTrackingProtection.toBuilder()
                                                .setBlockSuspectedFingerprintersSelection(selected).build()
                                        ).build()
                                    }
                                }
                            },
                            enabled = true,
                        )
                    }
                }
            }

            // global privacy control (Tell websites not to share & sell data)
            item {
                PreferenceSwitch(
                    text = stringResource(R.string.preference_enhanced_tracking_protection_custom_global_privacy_control),
                    summary = null,
                    selected = settings.isGlobalPrivacyControlEnabled,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setIsGlobalPrivacyControlEnabled(selected).build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // enhanced tracking protection exceptions
            item {
                // todo: exceptions
            }

            item {
                PreferenceTitle(stringResource(R.string.preferences_https_only_title))
            }

            // https-only mode selection
            item {
                PreferenceSelect(
                    text = stringResource(R.string.preferences_https_only_title),
                    description = stringResource(R.string.preferences_https_only_summary),
                    enabled = true,
                    selectedMenuItem = settings.httpsOnlyMode,
                    menuItems = remember {
                        listOf(
                            InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_DISABLED,
                            InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_ENABLED,
                            InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_ENABLED_PRIVATE_ONLY,
                        )
                    },
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setHttpsOnlyMode(selected).build()
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CustomTrackingProtectionPreferenceCheckbox(
    name: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onValueChange = onCheckedChanged,
            )
            .padding(
                horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING,
            ),
        horizontalArrangement = Arrangement.spacedBy(
            PrefUiConst.PREFERENCE_HORIZONTAL_INTERNAL_PADDING,
            Alignment.Start
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InfernoCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChanged,
            interactionSource = interactionSource,
        )
        InfernoText(text = name, modifier = Modifier.weight(1F))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> CustomTrackingProtectionPreferenceSelect(
    selected: T,
    items: List<T>,
    mapToTitle: (T) -> String,
    onSelectMenuItem: (T) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    // dropdown menu
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                end = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                bottom = PrefUiConst.PREFERENCE_VERTICAL_INTERNAL_PADDING,
            ),
    ) {
        InfernoOutlinedTextField(
            value = mapToTitle.invoke(selected),
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable, enabled = enabled
                ),
            readOnly = true,
            label = null,
            trailingIcon = {
                InfernoIcon(
                    painter = when (expanded) {
                        true -> painterResource(R.drawable.ic_arrow_drop_up_24)
                        false -> painterResource(R.drawable.ic_arrow_drop_down_24)
                    },
                    contentDescription = "",
                )
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor,
        ) {
            // show menu items
            items.map { it to mapToTitle.invoke(it) }.forEach { (item, name) ->
                DropdownMenuItem(
                    text = { InfernoText(name) },
                    onClick = {
                        onSelectMenuItem.invoke(item)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun InfernoSettings.TrackingProtectionDefault.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.TrackingProtectionDefault.STANDARD -> context.getString(R.string.preference_enhanced_tracking_protection_standard_default_1)
        InfernoSettings.TrackingProtectionDefault.STRICT -> context.getString(R.string.preference_enhanced_tracking_protection_strict)
        InfernoSettings.TrackingProtectionDefault.CUSTOM -> context.getString(R.string.preference_enhanced_tracking_protection_custom)
    }
}

private fun InfernoSettings.HttpsOnlyMode.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_DISABLED -> context.getString(R.string.preferences_https_only_off)
        InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_ENABLED -> context.getString(R.string.preferences_https_only_in_all_tabs)
        InfernoSettings.HttpsOnlyMode.HTTPS_ONLY_ENABLED_PRIVATE_ONLY -> context.getString(R.string.preferences_https_only_in_private_tabs)
    }
}

private fun InfernoSettings.CustomTrackingProtection.CookiePolicy.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.CustomTrackingProtection.CookiePolicy.ISOLATE_CROSS_SITE_COOKIES -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_cookies_5,
        )

        InfernoSettings.CustomTrackingProtection.CookiePolicy.CROSS_SITE_AND_SOCIAL_MEDIA_TRACKERS -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_cookies_1,
        )

        InfernoSettings.CustomTrackingProtection.CookiePolicy.COOKIES_FROM_UNVISITED_SITES -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_cookies_2,
        )

        InfernoSettings.CustomTrackingProtection.CookiePolicy.ALL_THIRD_PARTY_COOKIES -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_cookies_3,
        )

        InfernoSettings.CustomTrackingProtection.CookiePolicy.ALL_COOKIES -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_cookies_4,
        )
    }
}

private fun InfernoSettings.CustomTrackingProtection.TrackingContentSelection.toPrefString(context: Context): String {
    return when (this) {
        InfernoSettings.CustomTrackingProtection.TrackingContentSelection.BLOCK_TRACKING_NORMAL_ONLY -> "Only in Normal tabs" // todo: string res

        InfernoSettings.CustomTrackingProtection.TrackingContentSelection.BLOCK_TRACKING_PRIVATE_ONLY -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_tracking_content_2
        )

        InfernoSettings.CustomTrackingProtection.TrackingContentSelection.BLOCK_TRACKING_NORMAL_AND_PRIVATE -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_tracking_content_1
        )
    }
}

private fun InfernoSettings.CustomTrackingProtection.SuspectedFingerprintersSelection.toPrefString(
    context: Context,
): String {
    return when (this) {
        InfernoSettings.CustomTrackingProtection.SuspectedFingerprintersSelection.BLOCK_SUSPECTED_FINGERPRINTERS_NORMAL_ONLY -> "Only in Normal tabs" // todo: string res
        InfernoSettings.CustomTrackingProtection.SuspectedFingerprintersSelection.BLOCK_SUSPECTED_FINGERPRINTERS_PRIVATE_ONLY -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_tracking_content_2
        )

        InfernoSettings.CustomTrackingProtection.SuspectedFingerprintersSelection.BLOCK_SUSPECTED_FINGERPRINTERS_NORMAL_AND_PRIVATE -> context.getString(
            R.string.preference_enhanced_tracking_protection_custom_tracking_content_1
        )
    }
}
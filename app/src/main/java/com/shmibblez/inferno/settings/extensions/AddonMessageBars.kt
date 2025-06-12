package com.shmibblez.inferno.settings.extensions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.infernoTheme
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.AddonsManagerAdapterDelegate
import mozilla.components.feature.addons.ui.translateName
import mozilla.components.support.ktx.android.content.appName
import mozilla.components.support.ktx.android.content.appVersionName

@Composable
internal fun AddonMessageBars(
    addon: Addon,
    onLearnMoreLinkClicked: (link: AddonsManagerAdapterDelegate.LearnMoreLinks, addon: Addon) -> Unit,
) {
    val context = LocalContext.current

    // show error/warning if applicable
    when {
        addon.isDisabledAsBlocklisted() -> {
            AddonMessageBar(
                leadingIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.mozac_ic_critical_fill_24),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = stringResource(R.string.mozac_feature_addons_status_blocklisted_1),
                learnMoreText = stringResource(R.string.mozac_feature_addons_status_see_details),
                onLearnMoreLinkClicked = {
                    onLearnMoreLinkClicked.invoke(
                        AddonsManagerAdapterDelegate.LearnMoreLinks.BLOCKLISTED_ADDON,
                        addon,
                    )
                },
            )
        }

        addon.isDisabledAsNotCorrectlySigned() -> {
            AddonMessageBar(
                leadingIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.mozac_ic_critical_fill_24),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = stringResource(
                    R.string.mozac_feature_addons_status_unsigned, addon.translateName(context)
                ),
                learnMoreText = stringResource(R.string.mozac_feature_addons_status_learn_more),
                onLearnMoreLinkClicked = {
                    onLearnMoreLinkClicked.invoke(
                        AddonsManagerAdapterDelegate.LearnMoreLinks.ADDON_NOT_CORRECTLY_SIGNED,
                        addon,
                    )
                },
            )
        }

        addon.isDisabledAsIncompatible() -> {
            AddonMessageBar(
                leadingIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.mozac_ic_critical_fill_24),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = stringResource(
                    R.string.mozac_feature_addons_status_incompatible,
                    addon.translateName(context),
                    context.appName,
                    context.appVersionName,
                ),
            )
        }

        addon.isSoftBlocked() -> {
            AddonMessageBar(
                leadingIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.mozac_ic_warning_fill_24),
                        contentDescription = "",
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = stringResource(R.string.mozac_feature_addons_status_softblocked_1),
                learnMoreText = stringResource(R.string.mozac_feature_addons_status_see_details),
                onLearnMoreLinkClicked = {
                    onLearnMoreLinkClicked.invoke(
                        AddonsManagerAdapterDelegate.LearnMoreLinks.BLOCKLISTED_ADDON,
                        addon,
                    )
                },
            )
        }
    }
}

@Composable
private fun AddonMessageBar(
    leadingIcon: @Composable () -> Unit,
    text: String,
    learnMoreText: String? = null,
    onLearnMoreLinkClicked: (() -> Unit)? = null,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = LocalContext.current.infernoTheme().value.secondaryTextColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(16.dp),
    ) {
        leadingIcon.invoke()
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start,
        ) {
            InfernoText(
                text = text,
                infernoStyle = InfernoTextStyle.Normal,
            )

            if (onLearnMoreLinkClicked != null) {
                InfernoText(
                    text = learnMoreText ?: "",
                    modifier = Modifier.clickable { onLearnMoreLinkClicked.invoke() },
                    style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline),
                )
            }
        }
    }
}
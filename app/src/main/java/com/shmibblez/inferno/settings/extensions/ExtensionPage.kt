package com.shmibblez.inferno.settings.extensions

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import com.shmibblez.inferno.compose.StarRating
import mozilla.components.feature.addons.ui.updatedAtDate
import mozilla.components.support.ktx.kotlin.ifNullOrEmpty
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

@Composable
internal fun ExtensionPage(
    addon: Addon,
    goBack: () -> Unit,
    onNavToBrowser: () -> Unit,
) {
    val context = LocalContext.current
    var showUpdaterDialog by remember { mutableStateOf(false) }

    InfernoSettingsPage(
        title = addon.translateName(context),
        goBack = goBack,
    ) { edgeInsets ->
        // todo: extension page
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

            // installed options
            if (addon.isInstalled()) {
                installedAddonOptions()
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
                        onLongClick = { if (addon.isInstalled()) showUpdaterDialog = true },
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
    }
}

private fun LazyListScope.installedAddonOptions() {
    item {
        // todo: item options
    }

    divider()
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
            modifier = Modifier.padding(16.dp),
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
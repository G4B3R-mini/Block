package com.shmibblez.inferno.browser.awesomebar

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.shmibblez.inferno.compose.base.InfernoText
import mozilla.components.compose.browser.awesomebar.AwesomeBarColors
import mozilla.components.compose.browser.awesomebar.AwesomeBarOrientation
//import mozilla.components.compose.browser.awesomebar.R
import mozilla.components.concept.awesomebar.AwesomeBar
//import mozilla.components.ui.icons.R as iconsR
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.infernoTheme

// We only show one row of text, covering at max screen width.
// Limit bigger texts that could cause slowdowns or even crashes.
private const val SUGGESTION_TEXT_MAX_LENGTH = 100

@Composable
internal fun Suggestion(
    suggestion: AwesomeBar.Suggestion,
    orientation: AwesomeBarOrientation,
    onSuggestionClicked: () -> Unit,
    onAutoComplete: () -> Unit,
) {
    Row(
        modifier = Modifier
//            .defaultMinSize(minHeight = 56.dp)
//            .background(Color.Black)
//            .testTag("mozac.awesomebar.suggestion")
            .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
            .clickable { onSuggestionClicked() },
    ) {
        val icon = suggestion.icon
        if (icon != null) {
            SuggestionIcon(
                icon = icon,
                indicator = suggestion.indicatorIcon,
            )
        }
        SuggestionTitleAndDescription(
            title = suggestion.title?.take(SUGGESTION_TEXT_MAX_LENGTH),
            description = suggestion.description?.take(SUGGESTION_TEXT_MAX_LENGTH),
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
        )
        if (suggestion.editSuggestion != null) {
            AutocompleteButton(
                onAutoComplete = onAutoComplete,
                orientation = orientation,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
private fun SuggestionTitleAndDescription(
    title: String?,
    description: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        InfernoText(
            text = if (title.isNullOrEmpty()) {
                description ?: ""
            } else {
                title
            },
            fontColor = LocalContext.current.infernoTheme().value.primaryTextColor,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(start = 2.dp, end = 8.dp),
        )
        if (description?.isNotEmpty() == true) {
            InfernoText(
                text = description,
                fontColor =LocalContext.current.infernoTheme().value.secondaryTextColor,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(start = 2.dp, end = 8.dp),
            )
        }
    }
}

@Composable
private fun SuggestionIcon(
    icon: Bitmap,
    indicator: Drawable?,
) {
    Box(
        modifier = Modifier
            .width(30.dp)
            .height(38.dp),
    ) {
        Image(
            icon.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(2.dp))
                .width(24.dp)
                .height(24.dp),
            contentScale = ContentScale.Crop,
        )
        if (indicator != null) {
            Image(
                indicator.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 22.dp, start = 14.dp)
                    .width(16.dp)
                    .height(16.dp),
            )
        }
    }
}

@Composable
@Suppress("MagicNumber")
private fun AutocompleteButton(
    onAutoComplete: () -> Unit,
    orientation: AwesomeBarOrientation,
    modifier: Modifier,
) {
    Image(
        painterResource(R.drawable.mozac_ic_append_up_left_24),
//        colorFilter = ColorFilter.tint(colors.autocompleteIcon),
        contentDescription = stringResource(R.string.mozac_browser_awesomebar_edit_suggestion),
        modifier = modifier
            .size(48.dp)
            .rotate(
                if (orientation == AwesomeBarOrientation.BOTTOM) {
                    270f
                } else {
                    0f
                },
            )
            .clickable { onAutoComplete() }
            .padding(12.dp),
    )
}

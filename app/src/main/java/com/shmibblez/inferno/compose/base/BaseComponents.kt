package com.shmibblez.inferno.compose.base

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.identitycredential.DialogColors

// todo: abstract these as much as possible
//  just to be prepared for changes in material api
//  examples:
//  - text styles set with InfernoTextType enum class
//    - this sets text size, color, and if bold or italic
//  - colors set directly from theme
/**
 * convenience component, all styling is set here
 */
@Composable
fun InfernoText(
    text: String,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = 24.sp,
    letterSpacing: TextUnit = 0.15.sp,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    fontSize: TextUnit = 16.sp,
    // todo: font type
    fontColor: Color = Color.White,
    style: TextStyle = LocalTextStyle.current.copy(
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = fontColor,
        letterSpacing = letterSpacing,
    )
) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.White,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        style = style,
    )
}

/**
 * convenience component, all styling is set here
 */
@Composable
fun InfernoText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = DialogColors.defaultProvider().provideColors().title,
        letterSpacing = 0.15.sp,
    )
) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.White,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        style = style,
    )
}

/**
 * convenience component, all styling is set here
 */
@Composable
fun InfernoOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        colors = OutlinedTextFieldDefaults.colors(),
    )
}
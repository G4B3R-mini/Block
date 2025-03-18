package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
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
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Text(
            text = text,
            modifier = modifier,
            color = fontColor,
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
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
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
}

/**
 * convenience component, all styling is set here
 */

/*
keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
            ),
            singleLine = true,
 */
@Composable
fun InfernoOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
//        color = DialogColors.defaultProvider().provideColors().title,
        letterSpacing = 0.15.sp,
    ),
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable() (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine:Boolean = false,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.LightGray,
        // todo: COLORS make based on background color
        focusedContainerColor = Color.Black,
        errorContainerColor = Color.Black,
        disabledContainerColor = Color.Black,
        unfocusedContainerColor = Color.Black,
    ) // OutlinedTextFieldDefaults. colors()
) {
//    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            colors = colors,
        )
//    }
}

@Composable
fun InfernoCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled
        )
    }
}
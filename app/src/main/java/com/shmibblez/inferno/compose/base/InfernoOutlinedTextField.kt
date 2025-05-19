package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.ext.infernoTheme

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
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorTextColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        cursorColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        errorCursorColor = LocalContext.current.infernoTheme().value.errorColor,
        selectionColors = TextSelectionColors(
            handleColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
            backgroundColor = LocalContext.current.infernoTheme().value.primaryOutlineColor.copy(
                alpha = 0.4F,
            ),
        ),
        focusedBorderColor = LocalContext.current.infernoTheme().value.secondaryOutlineColor,
        unfocusedBorderColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        disabledBorderColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorBorderColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedLeadingIconColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedLeadingIconColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledLeadingIconColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorLeadingIconColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedTrailingIconColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedTrailingIconColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledTrailingIconColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorTrailingIconColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedLabelColor = LocalContext.current.infernoTheme().value.secondaryOutlineColor,
        unfocusedLabelColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        disabledLabelColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorLabelColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedPlaceholderColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        unfocusedPlaceholderColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledPlaceholderColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorPlaceholderColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedSupportingTextColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedSupportingTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledSupportingTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorSupportingTextColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedPrefixColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedPrefixColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledPrefixColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorPrefixColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedSuffixColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedSuffixColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledSuffixColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorSuffixColor = LocalContext.current.infernoTheme().value.errorColor,
    ),
) {
//    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        colors = colors,
    )
//    }
}
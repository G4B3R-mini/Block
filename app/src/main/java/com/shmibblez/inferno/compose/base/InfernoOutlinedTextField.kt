package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        focusedContainerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        errorContainerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        disabledContainerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        unfocusedContainerColor = LocalContext.current.infernoTheme().value.primaryBackgroundColor,
        focusedBorderColor = LocalContext.current.infernoTheme().value.secondaryOutlineColor,
        unfocusedBorderColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        disabledBorderColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorBorderColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedLabelColor = LocalContext.current.infernoTheme().value.secondaryOutlineColor,
        unfocusedLabelColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        disabledLabelColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorLabelColor = LocalContext.current.infernoTheme().value.errorColor,
    ), // OutlinedTextFieldDefaults. colors()
) {
//    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        colors = colors,
    )
//    }
}
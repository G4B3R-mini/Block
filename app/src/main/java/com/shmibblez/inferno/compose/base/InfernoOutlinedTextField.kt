package com.shmibblez.inferno.compose.base

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.Container
import androidx.compose.material3.OutlinedTextFieldDefaults.FocusedBorderThickness
import androidx.compose.material3.OutlinedTextFieldDefaults.UnfocusedBorderThickness
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfernoOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current.copy(
        color = LocalContext.current.infernoTheme().value.primaryTextColor,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        textAlign = TextAlign.Start
    ),
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledTextColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorTextColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        cursorColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        errorCursorColor = LocalContext.current.infernoTheme().value.errorColor,
        selectionColors = TextSelectionColors(
            handleColor = LocalContext.current.infernoTheme().value.primaryActionColor,
            backgroundColor = LocalContext.current.infernoTheme().value.primaryActionColor.copy(
                alpha = 0.4F,
            ),
        ),
        focusedBorderColor = LocalContext.current.infernoTheme().value.primaryActionColor,
        unfocusedBorderColor = LocalContext.current.infernoTheme().value.primaryOutlineColor,
        disabledBorderColor = LocalContext.current.infernoTheme().value.secondaryOutlineColor,
        errorBorderColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedLeadingIconColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedLeadingIconColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledLeadingIconColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorLeadingIconColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedTrailingIconColor = LocalContext.current.infernoTheme().value.primaryTextColor,
        unfocusedTrailingIconColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        disabledTrailingIconColor = LocalContext.current.infernoTheme().value.secondaryTextColor,
        errorTrailingIconColor = LocalContext.current.infernoTheme().value.errorColor,
        focusedLabelColor = LocalContext.current.infernoTheme().value.primaryActionColor,
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
//    BasicTextField(
//        value = value,
//        onValueChange = onValueChange,
//        modifier = modifier,
//        enabled = enabled,
//        textStyle = textStyle,
//        readOnly = readOnly,
//        keyboardOptions = keyboardOptions,
//        keyboardActions = keyboardActions,
//        singleLine = singleLine,
//        minLines = minLines,
//        maxLines = maxLines,
//        decorationBox = { innerTextField ->
//            OutlinedTextFieldDefaults.DecorationBox(
//                value = value,
//                innerTextField = innerTextField,
//                enabled = enabled,
//                singleLine = singleLine,
//                visualTransformation = visualTransformation,
//                interactionSource = interactionSource,
//                isError = isError,
//                label = label,
//                placeholder = placeholder,
//                leadingIcon = leadingIcon,
//                trailingIcon = trailingIcon,
//                prefix = prefix,
//                suffix = suffix,
//                supportingText = supportingText,
//                colors = colors,
//                contentPadding = PaddingValues(0.dp),
//                container = {
//                    Container(
//                        enabled = enabled,
//                        isError = isError,
//                        interactionSource = interactionSource,
//                        modifier = Modifier,
//                        colors = colors,
//                        shape = shape,
//                        focusedBorderThickness = FocusedBorderThickness,
//                        unfocusedBorderThickness = UnfocusedBorderThickness,
//                    )
//                }
//            )
//        }
//    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        textStyle = textStyle,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
    )
//    }
}

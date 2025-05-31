package com.shmibblez.inferno.settings.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoOutlinedTextField
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import mozilla.components.support.ktx.kotlin.toHexString
import java.util.Locale

// for removing invalid characters
private val ARGB_STRING_FILTER = Regex("[^a-fA-F0-9]")

// regex for rgb strings
private val RGB_STRING_REGEX = Regex("^[a-fA-F0-9]{6}\$")

// regex for argb strings
private val ARGB_STRING_REGEX = Regex("^[a-fA-F0-9]{8}\$")

internal data class ColorEditorDialogData(
    val name: String,
    val color: Color,
    val theme: InfernoTheme,
    val onDismiss: (originalColor: Color) -> Unit,
    val onConfirm: (newColor: Color) -> Unit,
    val onChange: (newColor: Color) -> Unit,
)

fun String.isValidRgbOrArgbHex(): Boolean {
    return RGB_STRING_REGEX.matches(this) || ARGB_STRING_REGEX.matches(this)
}

@OptIn(ExperimentalStdlibApi::class)
fun String.hexToArgbInt(): Int {
    return when {
        RGB_STRING_REGEX.matches(this) -> "FF$this"
        ARGB_STRING_REGEX.matches(this) -> this
        else -> "00000000" // throw Error("improperly formatted hex provided")
    }.hexToInt(
        HexFormat {
            bytes {
                bytesPerGroup = 4
                upperCase = true
            }
        },
    )
}

fun Color.toArgbHexString(): String {
    return this.toArgb().argbIntToHexString().uppercase(Locale.getDefault())
}

fun Int.argbIntToHexString(): String {
    val a = (this shr 24).toByte()
    val r = (this shr 16).toByte()
    val g = (this shr 8).toByte()
    val b = (this).toByte()
    return byteArrayOf(a, r, g, b).toHexString()
}

@Composable
internal fun ColorEditorDialog(data: ColorEditorDialogData) {
    var hex by remember { mutableStateOf(data.color.toArgbHexString()) }
    var hexColor by remember { mutableStateOf(data.color) }
    val originalColor = remember { data.color }

    // true if color is badly formatted
    var colorBad by remember { mutableStateOf(false) }

    var aboutExpanded by remember { mutableStateOf(false) }

    fun checkForErrors() {
        colorBad = !hex.isValidRgbOrArgbHex()
    }

    InfernoDialog(
        onDismiss = { data.onDismiss.invoke(originalColor) },
        colors = CardColors(
            containerColor = data.theme.primaryBackgroundColor,
            contentColor = data.theme.primaryTextColor,
            disabledContainerColor = data.theme.secondaryBackgroundColor,
            disabledContentColor = data.theme.secondaryBackgroundColor,
        ),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING)
        ) {
            // color title & preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // color name
                    InfernoText(
                        text = data.name,
                        infernoStyle = InfernoTextStyle.Title,
                    )

                    // color preview box
                    Box(
                        modifier = Modifier
                            .background(hexColor, MaterialTheme.shapes.small)
                            .border(
                                1.dp, data.theme.primaryOutlineColor, MaterialTheme.shapes.small
                            )
                            .size(COLOR_BOX_SIZE),
                    )
                }
            }

            // color hex editor
            item {
                InfernoOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = hex,
                    onValueChange = { input ->
                        hex = ARGB_STRING_FILTER.replace(input, "")
                        // if string is valid hex, update color
                        if (hex.isValidRgbOrArgbHex()) {
                            Color(hex.hexToArgbInt()).let {
                                data.onChange(it)
                                hexColor = it
                            }
                        }
                        checkForErrors()
                    },
                    leadingIcon = {
                        InfernoText("#")
                    },
                    supportingText = {
                        if (colorBad) {
                            InfernoText(
                                text = "6 or 8 characters long, 0-9, A-Z. Click here to learn more", // todo: string res
                                infernoStyle = InfernoTextStyle.Error,
                            )
                        }
                    },
                    isError = colorBad,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = data.theme.primaryTextColor,
                        unfocusedTextColor = data.theme.secondaryTextColor,
                        disabledTextColor = data.theme.secondaryTextColor,
                        errorTextColor = data.theme.errorColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        cursorColor = data.theme.primaryOutlineColor,
                        errorCursorColor = data.theme.errorColor,
                        selectionColors = TextSelectionColors(
                            handleColor = data.theme.primaryOutlineColor,
                            backgroundColor = data.theme.primaryOutlineColor.copy(
                                alpha = 0.4F,
                            ),
                        ),
                        focusedBorderColor = data.theme.primaryActionColor,
                        unfocusedBorderColor = data.theme.primaryOutlineColor,
                        disabledBorderColor = data.theme.secondaryTextColor,
                        errorBorderColor = data.theme.errorColor,
                        focusedLeadingIconColor = data.theme.primaryTextColor,
                        unfocusedLeadingIconColor = data.theme.secondaryTextColor,
                        disabledLeadingIconColor = data.theme.secondaryTextColor,
                        errorLeadingIconColor = data.theme.errorColor,
                        focusedTrailingIconColor = data.theme.primaryTextColor,
                        unfocusedTrailingIconColor = data.theme.secondaryTextColor,
                        disabledTrailingIconColor = data.theme.secondaryTextColor,
                        errorTrailingIconColor = data.theme.errorColor,
                        focusedLabelColor = data.theme.primaryActionColor,
                        unfocusedLabelColor = data.theme.primaryOutlineColor,
                        disabledLabelColor = data.theme.secondaryTextColor,
                        errorLabelColor = data.theme.errorColor,
                        focusedPlaceholderColor = data.theme.secondaryTextColor,
                        unfocusedPlaceholderColor = data.theme.secondaryTextColor,
                        disabledPlaceholderColor = data.theme.secondaryTextColor,
                        errorPlaceholderColor = data.theme.errorColor,
                        focusedSupportingTextColor = data.theme.primaryTextColor,
                        unfocusedSupportingTextColor = data.theme.secondaryTextColor,
                        disabledSupportingTextColor = data.theme.secondaryTextColor,
                        errorSupportingTextColor = data.theme.errorColor,
                        focusedPrefixColor = data.theme.primaryTextColor,
                        unfocusedPrefixColor = data.theme.secondaryTextColor,
                        disabledPrefixColor = data.theme.secondaryTextColor,
                        errorPrefixColor = data.theme.errorColor,
                        focusedSuffixColor = data.theme.primaryTextColor,
                        unfocusedSuffixColor = data.theme.secondaryTextColor,
                        disabledSuffixColor = data.theme.secondaryTextColor,
                        errorSuffixColor = data.theme.errorColor,
                    ),
                )
            }

            // about item
            item {
                AboutItem(
                    expanded = aboutExpanded,
                    onExpandedChanged = { aboutExpanded = !aboutExpanded },
                    theme = data.theme,
                )
            }
        }

        // cancel / save buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = PrefUiConst.PREFERENCE_INTERNAL_PADDING),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            InfernoOutlinedButton(
                text = stringResource(android.R.string.cancel),
                modifier = Modifier.weight(1F),
                onClick = { data.onDismiss.invoke(originalColor) },
                border = BorderStroke(
                    width = 1.dp,
                    color = data.theme.primaryOutlineColor,
                ),
                colors = ButtonColors(
                    containerColor = data.theme.primaryBackgroundColor,
                    contentColor = data.theme.primaryOutlineColor,
                    disabledContainerColor = data.theme.secondaryBackgroundColor,
                    disabledContentColor = data.theme.secondaryOutlineColor,
                ),
            )
            InfernoButton(
                text = stringResource(R.string.browser_menu_save),
                modifier = Modifier.weight(1F),
                enabled = !colorBad,
                onClick = { data.onConfirm.invoke(hexColor) },
                colors = ButtonColors(
                    containerColor = data.theme.primaryActionColor,
                    contentColor = data.theme.primaryTextColor,
                    disabledContainerColor = data.theme.secondaryActionColor,
                    disabledContentColor = data.theme.secondaryTextColor,
                )
            )
        }
    }
}

@Composable
private fun AboutItem(
    expanded: Boolean,
    onExpandedChanged: (Boolean) -> Unit,
    theme: InfernoTheme,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
    ) {
        // about expand title
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChanged.invoke(!expanded) },
        ) {
            InfernoText(
                text = stringResource(R.string.preferences_category_about),
                infernoStyle = InfernoTextStyle.Normal,
                fontColor = theme.primaryTextColor,
            )
            InfernoIcon(
                painter = painterResource(
                    when (expanded) {
                        true -> R.drawable.ic_chevron_up_24
                        false -> R.drawable.ic_chevron_down_24
                    },
                ),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = theme.primaryIconColor
            )
        }

        // description
        if (expanded) {
            InfernoText(
                // todo: move to about section (expandable) R.string.preferences_category_about
                // todo: string res
                text = "Hex colors must be in format #RRGGBB or #AARRGGBB." +
                        // alpha
                        "\n- AA is alpha component and takes values from 00 (transparent) to FF (fully opaque)" +
                        // red
                        "\n- RR is red component and takes values from 00 to FF" +
                        // green
                        "\n- GG is green component and takes values from 00 to FF" +
                        // blue
                        "\n- BB is blue component and takes values from 00 to FF" +
                        // examples
                        "\nSome examples:" +
                        // red
                        "\n- Solid red would be #FF0000 (#FFFF0000 with alpha)" +
                        // green
                        "\n- Solid green would be #00FF00 (#FF00FF00 with alpha)" +
                        // blue
                        "\n- Solid blue would be #0000FF (#FF0000FF with alpha)" +
                        // purple
                        "\n- You can also make colors transparent, in this case half transparent purple would be #889000FF",
                infernoStyle = InfernoTextStyle.Subtitle,
                fontColor = theme.secondaryTextColor,
            )
        }
    }
}

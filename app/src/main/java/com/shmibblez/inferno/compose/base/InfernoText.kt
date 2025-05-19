package com.shmibblez.inferno.compose.base

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.mozillaAndroidComponents.feature.prompts.identitycredential.DialogColors

// Default InfernoText styles
interface InfernoTextStyle {
    val fontStyle: FontStyle?
    val fontWeight: FontWeight?
    val textAlign: TextAlign?
    val lineHeight: TextUnit
    val letterSpacing: TextUnit
    val overflow: TextOverflow
    val softWrap: Boolean
    val maxLines: Int
    val minLines: Int
    val fontSize: TextUnit
    val fontColor: Color;

    companion object {
        val Title: InfernoTextStyle
            @Composable get() = object : InfernoTextStyle {
                override val fontStyle: FontStyle = FontStyle.Normal
                override val fontWeight: FontWeight = FontWeight.Normal
                override val textAlign: TextAlign = TextAlign.Start
                override val lineHeight: TextUnit = 24.sp
                override val letterSpacing: TextUnit = 0.15.sp
                override val overflow: TextOverflow = TextOverflow.Ellipsis
                override val softWrap: Boolean = true
                override val maxLines: Int = Int.MAX_VALUE
                override val minLines: Int = 1
                override val fontSize: TextUnit = 20.sp
                override val fontColor: Color =
                    LocalContext.current.infernoTheme().value.primaryTextColor
            }
        val Normal: InfernoTextStyle
            @Composable get() = object : InfernoTextStyle {
                override val fontStyle: FontStyle = FontStyle.Normal
                override val fontWeight: FontWeight = FontWeight.Normal
                override val textAlign: TextAlign = TextAlign.Start
                override val lineHeight: TextUnit = 24.sp
                override val letterSpacing: TextUnit = 0.15.sp
                override val overflow: TextOverflow = TextOverflow.Ellipsis
                override val softWrap: Boolean = true
                override val maxLines: Int = Int.MAX_VALUE
                override val minLines: Int = 1
                override val fontSize: TextUnit = 16.sp
                override val fontColor: Color =
                    LocalContext.current.infernoTheme().value.primaryTextColor
            }
        val Subtitle: InfernoTextStyle
            @Composable get() = object : InfernoTextStyle {
                override val fontStyle: FontStyle = FontStyle.Normal
                override val fontWeight: FontWeight = FontWeight.Normal
                override val textAlign: TextAlign = TextAlign.Start
                override val lineHeight: TextUnit = 24.sp
                override val letterSpacing: TextUnit = 0.15.sp
                override val overflow: TextOverflow = TextOverflow.Ellipsis
                override val softWrap: Boolean = true
                override val maxLines: Int = Int.MAX_VALUE
                override val minLines: Int = 1
                override val fontSize: TextUnit = 12.sp
                override val fontColor: Color = LocalContext.current.infernoTheme().value.secondaryTextColor
            }
        val Error: InfernoTextStyle
            @Composable get() = object : InfernoTextStyle {
                override val fontStyle: FontStyle = FontStyle.Normal
                override val fontWeight: FontWeight = FontWeight.Normal
                override val textAlign: TextAlign = TextAlign.Start
                override val lineHeight: TextUnit = 24.sp
                override val letterSpacing: TextUnit = 0.15.sp
                override val overflow: TextOverflow = TextOverflow.Ellipsis
                override val softWrap: Boolean = true
                override val maxLines: Int = Int.MAX_VALUE
                override val minLines: Int = 1
                override val fontSize: TextUnit = 12.sp
                override val fontColor: Color = LocalContext.current.infernoTheme().value.errorColor
            }
    }
}

/**
 * convenience component, all styling is set here
 */
@Composable
fun InfernoText(
    text: String,
    infernoStyle: InfernoTextStyle = InfernoTextStyle.Normal,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = infernoStyle.fontStyle,
    fontWeight: FontWeight? = infernoStyle.fontWeight,
    textAlign: TextAlign? = infernoStyle.textAlign,
    lineHeight: TextUnit = infernoStyle.lineHeight,
    letterSpacing: TextUnit = infernoStyle.letterSpacing,
    overflow: TextOverflow = infernoStyle.overflow,
    softWrap: Boolean = infernoStyle.softWrap,
    maxLines: Int = infernoStyle.maxLines,
    minLines: Int = infernoStyle.minLines,
    fontSize: TextUnit = infernoStyle.fontSize,
    fontColor: Color = infernoStyle.fontColor,
    style: TextStyle = LocalTextStyle.current.copy(
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = fontColor,
        letterSpacing = letterSpacing,
    ),
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Text(
            text = text,
            modifier = modifier,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            textAlign = textAlign,
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
    ),
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
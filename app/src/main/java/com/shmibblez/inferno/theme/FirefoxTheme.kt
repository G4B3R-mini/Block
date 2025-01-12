/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.theme

//import mozilla.components.compose.base.theme.AcornColors
//import mozilla.components.compose.base.theme.AcornSize
//import mozilla.components.compose.base.theme.AcornSpace
//import mozilla.components.compose.base.theme.AcornTheme
//import mozilla.components.compose.base.theme.AcornTypography
//import mozilla.components.compose.base.theme.AcornWindowSize
//import mozilla.components.compose.base.theme.darkColorPalette
//import mozilla.components.compose.base.theme.lightColorPalette
//import mozilla.components.compose.base.theme.privateColorPalette
//import mozilla.components.compose.base.utils.inComposePreview
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.ext.settings

/**
 * The theme for Mozilla Firefox for Android (Fenix).
 *
 * @param theme The current [Theme] that is displayed.
 * @param content The children composables to be laid out.
 */
@Composable
fun FirefoxTheme(
    theme: Theme = Theme.getTheme(),
    content: @Composable () -> Unit,
) {
    val colors = when (theme) {
        Theme.Light -> MaterialTheme.colorScheme // lightColorPalette
        Theme.Dark -> MaterialTheme.colorScheme // darkColorPalette
        Theme.Private -> MaterialTheme.colorScheme // privateColorPalette
    }

//    AcornTheme(
//        colors = colors,
//        content = content,
//    )
}

/**
 * Indicates the theme that is displayed.
 */
enum class Theme {
    Light, Dark, Private, ;

    companion object {
        /**
         * Returns the current [Theme] that is displayed.
         *
         * @param allowPrivateTheme Boolean used to control whether [Theme.Private] is an option
         * for [FirefoxTheme] colors.
         * @return the current [Theme] that is displayed.
         */
        @Composable
        fun getTheme(allowPrivateTheme: Boolean = true) = if (allowPrivateTheme &&
//                !inComposePreview &&
            LocalContext.current.settings().lastKnownMode.isPrivate
        ) {
            Private
        } else if (isSystemInDarkTheme()) {
            Dark
        } else {
            Light
        }
    }
}


internal object PaletteTokens {
    val Black = Color(red = 0, green = 0, blue = 0)
    val Error0 = Color(red = 0, green = 0, blue = 0)
    val Error10 = Color(red = 65, green = 14, blue = 11)
    val Error100 = Color(red = 255, green = 255, blue = 255)
    val Error20 = Color(red = 96, green = 20, blue = 16)
    val Error30 = Color(red = 140, green = 29, blue = 24)
    val Error40 = Color(red = 179, green = 38, blue = 30)
    val Error50 = Color(red = 220, green = 54, blue = 46)
    val Error60 = Color(red = 228, green = 105, blue = 98)
    val Error70 = Color(red = 236, green = 146, blue = 142)
    val Error80 = Color(red = 242, green = 184, blue = 181)
    val Error90 = Color(red = 249, green = 222, blue = 220)
    val Error95 = Color(red = 252, green = 238, blue = 238)
    val Error99 = Color(red = 255, green = 251, blue = 249)
    val Neutral0 = Color(red = 0, green = 0, blue = 0)
    val Neutral10 = Color(red = 29, green = 27, blue = 32)
    val Neutral100 = Color(red = 255, green = 255, blue = 255)
    val Neutral12 = Color(red = 33, green = 31, blue = 38)
    val Neutral17 = Color(red = 43, green = 41, blue = 48)
    val Neutral20 = Color(red = 50, green = 47, blue = 53)
    val Neutral22 = Color(red = 54, green = 52, blue = 59)
    val Neutral24 = Color(red = 59, green = 56, blue = 62)
    val Neutral30 = Color(red = 72, green = 70, blue = 76)
    val Neutral4 = Color(red = 15, green = 13, blue = 19)
    val Neutral40 = Color(red = 96, green = 93, blue = 100)
    val Neutral50 = Color(red = 121, green = 118, blue = 125)
    val Neutral6 = Color(red = 20, green = 18, blue = 24)
    val Neutral60 = Color(red = 147, green = 143, blue = 150)
    val Neutral70 = Color(red = 174, green = 169, blue = 177)
    val Neutral80 = Color(red = 202, green = 197, blue = 205)
    val Neutral87 = Color(red = 222, green = 216, blue = 225)
    val Neutral90 = Color(red = 230, green = 224, blue = 233)
    val Neutral92 = Color(red = 236, green = 230, blue = 240)
    val Neutral94 = Color(red = 243, green = 237, blue = 247)
    val Neutral95 = Color(red = 245, green = 239, blue = 247)
    val Neutral96 = Color(red = 247, green = 242, blue = 250)
    val Neutral98 = Color(red = 254, green = 247, blue = 255)
    val Neutral99 = Color(red = 255, green = 251, blue = 255)
    val NeutralVariant0 = Color(red = 0, green = 0, blue = 0)
    val NeutralVariant10 = Color(red = 29, green = 26, blue = 34)
    val NeutralVariant100 = Color(red = 255, green = 255, blue = 255)
    val NeutralVariant20 = Color(red = 50, green = 47, blue = 55)
    val NeutralVariant30 = Color(red = 73, green = 69, blue = 79)
    val NeutralVariant40 = Color(red = 96, green = 93, blue = 102)
    val NeutralVariant50 = Color(red = 121, green = 116, blue = 126)
    val NeutralVariant60 = Color(red = 147, green = 143, blue = 153)
    val NeutralVariant70 = Color(red = 174, green = 169, blue = 180)
    val NeutralVariant80 = Color(red = 202, green = 196, blue = 208)
    val NeutralVariant90 = Color(red = 231, green = 224, blue = 236)
    val NeutralVariant95 = Color(red = 245, green = 238, blue = 250)
    val NeutralVariant99 = Color(red = 255, green = 251, blue = 254)
    val Primary0 = Color(red = 0, green = 0, blue = 0)
    val Primary10 = Color(red = 33, green = 0, blue = 93)
    val Primary100 = Color(red = 255, green = 255, blue = 255)
    val Primary20 = Color(red = 56, green = 30, blue = 114)
    val Primary30 = Color(red = 79, green = 55, blue = 139)
    val Primary40 = Color(red = 103, green = 80, blue = 164)
    val Primary50 = Color(red = 127, green = 103, blue = 190)
    val Primary60 = Color(red = 154, green = 130, blue = 219)
    val Primary70 = Color(red = 182, green = 157, blue = 248)
    val Primary80 = Color(red = 208, green = 188, blue = 255)
    val Primary90 = Color(red = 234, green = 221, blue = 255)
    val Primary95 = Color(red = 246, green = 237, blue = 255)
    val Primary99 = Color(red = 255, green = 251, blue = 254)
    val Secondary0 = Color(red = 0, green = 0, blue = 0)
    val Secondary10 = Color(red = 29, green = 25, blue = 43)
    val Secondary100 = Color(red = 255, green = 255, blue = 255)
    val Secondary20 = Color(red = 51, green = 45, blue = 65)
    val Secondary30 = Color(red = 74, green = 68, blue = 88)
    val Secondary40 = Color(red = 98, green = 91, blue = 113)
    val Secondary50 = Color(red = 122, green = 114, blue = 137)
    val Secondary60 = Color(red = 149, green = 141, blue = 165)
    val Secondary70 = Color(red = 176, green = 167, blue = 192)
    val Secondary80 = Color(red = 204, green = 194, blue = 220)
    val Secondary90 = Color(red = 232, green = 222, blue = 248)
    val Secondary95 = Color(red = 246, green = 237, blue = 255)
    val Secondary99 = Color(red = 255, green = 251, blue = 254)
    val Tertiary0 = Color(red = 0, green = 0, blue = 0)
    val Tertiary10 = Color(red = 49, green = 17, blue = 29)
    val Tertiary100 = Color(red = 255, green = 255, blue = 255)
    val Tertiary20 = Color(red = 73, green = 37, blue = 50)
    val Tertiary30 = Color(red = 99, green = 59, blue = 72)
    val Tertiary40 = Color(red = 125, green = 82, blue = 96)
    val Tertiary50 = Color(red = 152, green = 105, blue = 119)
    val Tertiary60 = Color(red = 181, green = 131, blue = 146)
    val Tertiary70 = Color(red = 210, green = 157, blue = 172)
    val Tertiary80 = Color(red = 239, green = 184, blue = 200)
    val Tertiary90 = Color(red = 255, green = 216, blue = 228)
    val Tertiary95 = Color(red = 255, green = 236, blue = 241)
    val Tertiary99 = Color(red = 255, green = 251, blue = 250)
    val White = Color(red = 255, green = 255, blue = 255)
}

internal object ColorLightTokens {
    val Background = PaletteTokens.Neutral98
    val Error = PaletteTokens.Error40
    val ErrorContainer = PaletteTokens.Error90
    val InverseOnSurface = PaletteTokens.Neutral95
    val InversePrimary = PaletteTokens.Primary80
    val InverseSurface = PaletteTokens.Neutral20
    val OnBackground = PaletteTokens.Neutral10
    val OnError = PaletteTokens.Error100
    val OnErrorContainer = PaletteTokens.Error10
    val OnPrimary = PaletteTokens.Primary100
    val OnPrimaryContainer = PaletteTokens.Primary10
    val OnPrimaryFixed = PaletteTokens.Primary10
    val OnPrimaryFixedVariant = PaletteTokens.Primary30
    val OnSecondary = PaletteTokens.Secondary100
    val OnSecondaryContainer = PaletteTokens.Secondary10
    val OnSecondaryFixed = PaletteTokens.Secondary10
    val OnSecondaryFixedVariant = PaletteTokens.Secondary30
    val OnSurface = PaletteTokens.Neutral10
    val OnSurfaceVariant = PaletteTokens.NeutralVariant30
    val OnTertiary = PaletteTokens.Tertiary100
    val OnTertiaryContainer = PaletteTokens.Tertiary10
    val OnTertiaryFixed = PaletteTokens.Tertiary10
    val OnTertiaryFixedVariant = PaletteTokens.Tertiary30
    val Outline = PaletteTokens.NeutralVariant50
    val OutlineVariant = PaletteTokens.NeutralVariant80
    val Primary = PaletteTokens.Primary40
    val PrimaryContainer = PaletteTokens.Primary90
    val PrimaryFixed = PaletteTokens.Primary90
    val PrimaryFixedDim = PaletteTokens.Primary80
    val Scrim = PaletteTokens.Neutral0
    val Secondary = PaletteTokens.Secondary40
    val SecondaryContainer = PaletteTokens.Secondary90
    val SecondaryFixed = PaletteTokens.Secondary90
    val SecondaryFixedDim = PaletteTokens.Secondary80
    val Surface = PaletteTokens.Neutral98
    val SurfaceBright = PaletteTokens.Neutral98
    val SurfaceContainer = PaletteTokens.Neutral94
    val SurfaceContainerHigh = PaletteTokens.Neutral92
    val SurfaceContainerHighest = PaletteTokens.Neutral90
    val SurfaceContainerLow = PaletteTokens.Neutral96
    val SurfaceContainerLowest = PaletteTokens.Neutral100
    val SurfaceDim = PaletteTokens.Neutral87
    val SurfaceTint = Primary
    val SurfaceVariant = PaletteTokens.NeutralVariant90
    val Tertiary = PaletteTokens.Tertiary40
    val TertiaryContainer = PaletteTokens.Tertiary90
    val TertiaryFixed = PaletteTokens.Tertiary90
    val TertiaryFixedDim = PaletteTokens.Tertiary80
}

class AcornColors(
    val primary: Color = ColorLightTokens.Primary,
    val onPrimary: Color = ColorLightTokens.OnPrimary,
    val primaryContainer: Color = ColorLightTokens.PrimaryContainer,
    val onPrimaryContainer: Color = ColorLightTokens.OnPrimaryContainer,
    val inversePrimary: Color = ColorLightTokens.InversePrimary,
    val secondary: Color = ColorLightTokens.Secondary,
    val onSecondary: Color = ColorLightTokens.OnSecondary,
    val secondaryContainer: Color = ColorLightTokens.SecondaryContainer,
    val onSecondaryContainer: Color = ColorLightTokens.OnSecondaryContainer,
    val tertiary: Color = ColorLightTokens.Tertiary,
    val onTertiary: Color = ColorLightTokens.OnTertiary,
    val tertiaryContainer: Color = ColorLightTokens.TertiaryContainer,
    val onTertiaryContainer: Color = ColorLightTokens.OnTertiaryContainer,
    val background: Color = ColorLightTokens.Background,
    val onBackground: Color = ColorLightTokens.OnBackground,
    val surface: Color = ColorLightTokens.Surface,
    val onSurface: Color = ColorLightTokens.OnSurface,
    val surfaceVariant: Color = ColorLightTokens.SurfaceVariant,
    val onSurfaceVariant: Color = ColorLightTokens.OnSurfaceVariant,
    val surfaceTint: Color = primary,
    val inverseSurface: Color = ColorLightTokens.InverseSurface,
    val inverseOnSurface: Color = ColorLightTokens.InverseOnSurface,
    val error: Color = ColorLightTokens.Error,
    val onError: Color = ColorLightTokens.OnError,
    val errorContainer: Color = ColorLightTokens.ErrorContainer,
    val onErrorContainer: Color = ColorLightTokens.OnErrorContainer,
    val outline: Color = ColorLightTokens.Outline,
    val outlineVariant: Color = ColorLightTokens.OutlineVariant,
    val scrim: Color = ColorLightTokens.Scrim,
    // haccs
    val layer1: Color = surface,
    val layer2: Color = surfaceVariant,
    val layer3: Color = ColorLightTokens.SurfaceDim,
    val textPrimary: Color = Color.White,
    val textSecondary: Color = Color.LightGray,
    val textOnColorPrimary: Color = Color.White,
    val iconPrimary: Color = Color.White,
    val iconSecondary: Color = Color.LightGray,
    val tabActive: Color = Color.DarkGray,
    val tabInactive: Color = Color.Black,
)

internal object TypefaceTokens {
    val Brand = FontFamily.SansSerif
    val Plain = FontFamily.SansSerif
    val WeightBold = FontWeight.Bold
    val WeightMedium = FontWeight.Medium
    val WeightRegular = FontWeight.Normal
}


internal object TypeScaleTokens {
    val BodyLargeFont = TypefaceTokens.Plain
    val BodyLargeLineHeight = 24.0.sp
    val BodyLargeSize = 16.sp
    val BodyLargeTracking = 0.5.sp
    val BodyLargeWeight = TypefaceTokens.WeightRegular
    val BodyMediumFont = TypefaceTokens.Plain
    val BodyMediumLineHeight = 20.0.sp
    val BodyMediumSize = 14.sp
    val BodyMediumTracking = 0.2.sp
    val BodyMediumWeight = TypefaceTokens.WeightRegular
    val BodySmallFont = TypefaceTokens.Plain
    val BodySmallLineHeight = 16.0.sp
    val BodySmallSize = 12.sp
    val BodySmallTracking = 0.4.sp
    val BodySmallWeight = TypefaceTokens.WeightRegular
    val DisplayLargeFont = TypefaceTokens.Brand
    val DisplayLargeLineHeight = 64.0.sp
    val DisplayLargeSize = 57.sp
    val DisplayLargeTracking = -0.2.sp
    val DisplayLargeWeight = TypefaceTokens.WeightRegular
    val DisplayMediumFont = TypefaceTokens.Brand
    val DisplayMediumLineHeight = 52.0.sp
    val DisplayMediumSize = 45.sp
    val DisplayMediumTracking = 0.0.sp
    val DisplayMediumWeight = TypefaceTokens.WeightRegular
    val DisplaySmallFont = TypefaceTokens.Brand
    val DisplaySmallLineHeight = 44.0.sp
    val DisplaySmallSize = 36.sp
    val DisplaySmallTracking = 0.0.sp
    val DisplaySmallWeight = TypefaceTokens.WeightRegular
    val HeadlineLargeFont = TypefaceTokens.Brand
    val HeadlineLargeLineHeight = 40.0.sp
    val HeadlineLargeSize = 32.sp
    val HeadlineLargeTracking = 0.0.sp
    val HeadlineLargeWeight = TypefaceTokens.WeightRegular
    val HeadlineMediumFont = TypefaceTokens.Brand
    val HeadlineMediumLineHeight = 36.0.sp
    val HeadlineMediumSize = 28.sp
    val HeadlineMediumTracking = 0.0.sp
    val HeadlineMediumWeight = TypefaceTokens.WeightRegular
    val HeadlineSmallFont = TypefaceTokens.Brand
    val HeadlineSmallLineHeight = 32.0.sp
    val HeadlineSmallSize = 24.sp
    val HeadlineSmallTracking = 0.0.sp
    val HeadlineSmallWeight = TypefaceTokens.WeightRegular
    val LabelLargeFont = TypefaceTokens.Plain
    val LabelLargeLineHeight = 20.0.sp
    val LabelLargeSize = 14.sp
    val LabelLargeTracking = 0.1.sp
    val LabelLargeWeight = TypefaceTokens.WeightMedium
    val LabelMediumFont = TypefaceTokens.Plain
    val LabelMediumLineHeight = 16.0.sp
    val LabelMediumSize = 12.sp
    val LabelMediumTracking = 0.5.sp
    val LabelMediumWeight = TypefaceTokens.WeightMedium
    val LabelSmallFont = TypefaceTokens.Plain
    val LabelSmallLineHeight = 16.0.sp
    val LabelSmallSize = 11.sp
    val LabelSmallTracking = 0.5.sp
    val LabelSmallWeight = TypefaceTokens.WeightMedium
    val TitleLargeFont = TypefaceTokens.Brand
    val TitleLargeLineHeight = 28.0.sp
    val TitleLargeSize = 22.sp
    val TitleLargeTracking = 0.0.sp
    val TitleLargeWeight = TypefaceTokens.WeightRegular
    val TitleMediumFont = TypefaceTokens.Plain
    val TitleMediumLineHeight = 24.0.sp
    val TitleMediumSize = 16.sp
    val TitleMediumTracking = 0.2.sp
    val TitleMediumWeight = TypefaceTokens.WeightMedium
    val TitleSmallFont = TypefaceTokens.Plain
    val TitleSmallLineHeight = 20.0.sp
    val TitleSmallSize = 14.sp
    val TitleSmallTracking = 0.1.sp
    val TitleSmallWeight = TypefaceTokens.WeightMedium
}

private const val DefaultIncludeFontPadding = false

@Suppress("DEPRECATION")
private val DefaultPlatformTextStyle =
    PlatformTextStyle(includeFontPadding = DefaultIncludeFontPadding)

internal fun defaultPlatformTextStyle(): PlatformTextStyle? = DefaultPlatformTextStyle

internal val DefaultLineHeightStyle =
    LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)


internal val DefaultTextStyle = TextStyle.Default.copy(
    platformStyle = defaultPlatformTextStyle(),
    lineHeightStyle = DefaultLineHeightStyle,
)

internal object TypographyTokens {
    val BodyLarge = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.BodyLargeFont,
        fontWeight = TypeScaleTokens.BodyLargeWeight,
        fontSize = TypeScaleTokens.BodyLargeSize,
        lineHeight = TypeScaleTokens.BodyLargeLineHeight,
        letterSpacing = TypeScaleTokens.BodyLargeTracking,
    )
    val BodyMedium = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.BodyMediumFont,
        fontWeight = TypeScaleTokens.BodyMediumWeight,
        fontSize = TypeScaleTokens.BodyMediumSize,
        lineHeight = TypeScaleTokens.BodyMediumLineHeight,
        letterSpacing = TypeScaleTokens.BodyMediumTracking,
    )
    val BodySmall = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.BodySmallFont,
        fontWeight = TypeScaleTokens.BodySmallWeight,
        fontSize = TypeScaleTokens.BodySmallSize,
        lineHeight = TypeScaleTokens.BodySmallLineHeight,
        letterSpacing = TypeScaleTokens.BodySmallTracking,
    )
    val DisplayLarge = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.DisplayLargeFont,
        fontWeight = TypeScaleTokens.DisplayLargeWeight,
        fontSize = TypeScaleTokens.DisplayLargeSize,
        lineHeight = TypeScaleTokens.DisplayLargeLineHeight,
        letterSpacing = TypeScaleTokens.DisplayLargeTracking,
    )
    val DisplayMedium = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.DisplayMediumFont,
        fontWeight = TypeScaleTokens.DisplayMediumWeight,
        fontSize = TypeScaleTokens.DisplayMediumSize,
        lineHeight = TypeScaleTokens.DisplayMediumLineHeight,
        letterSpacing = TypeScaleTokens.DisplayMediumTracking,
    )
    val DisplaySmall = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.DisplaySmallFont,
        fontWeight = TypeScaleTokens.DisplaySmallWeight,
        fontSize = TypeScaleTokens.DisplaySmallSize,
        lineHeight = TypeScaleTokens.DisplaySmallLineHeight,
        letterSpacing = TypeScaleTokens.DisplaySmallTracking,
    )
    val HeadlineLarge = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.HeadlineLargeFont,
        fontWeight = TypeScaleTokens.HeadlineLargeWeight,
        fontSize = TypeScaleTokens.HeadlineLargeSize,
        lineHeight = TypeScaleTokens.HeadlineLargeLineHeight,
        letterSpacing = TypeScaleTokens.HeadlineLargeTracking,
    )
    val HeadlineMedium = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.HeadlineMediumFont,
        fontWeight = TypeScaleTokens.HeadlineMediumWeight,
        fontSize = TypeScaleTokens.HeadlineMediumSize,
        lineHeight = TypeScaleTokens.HeadlineMediumLineHeight,
        letterSpacing = TypeScaleTokens.HeadlineMediumTracking,
    )
    val HeadlineSmall = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.HeadlineSmallFont,
        fontWeight = TypeScaleTokens.HeadlineSmallWeight,
        fontSize = TypeScaleTokens.HeadlineSmallSize,
        lineHeight = TypeScaleTokens.HeadlineSmallLineHeight,
        letterSpacing = TypeScaleTokens.HeadlineSmallTracking,
    )
    val LabelLarge = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.LabelLargeFont,
        fontWeight = TypeScaleTokens.LabelLargeWeight,
        fontSize = TypeScaleTokens.LabelLargeSize,
        lineHeight = TypeScaleTokens.LabelLargeLineHeight,
        letterSpacing = TypeScaleTokens.LabelLargeTracking,
    )
    val LabelMedium = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.LabelMediumFont,
        fontWeight = TypeScaleTokens.LabelMediumWeight,
        fontSize = TypeScaleTokens.LabelMediumSize,
        lineHeight = TypeScaleTokens.LabelMediumLineHeight,
        letterSpacing = TypeScaleTokens.LabelMediumTracking,
    )
    val LabelSmall = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.LabelSmallFont,
        fontWeight = TypeScaleTokens.LabelSmallWeight,
        fontSize = TypeScaleTokens.LabelSmallSize,
        lineHeight = TypeScaleTokens.LabelSmallLineHeight,
        letterSpacing = TypeScaleTokens.LabelSmallTracking,
    )
    val TitleLarge = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.TitleLargeFont,
        fontWeight = TypeScaleTokens.TitleLargeWeight,
        fontSize = TypeScaleTokens.TitleLargeSize,
        lineHeight = TypeScaleTokens.TitleLargeLineHeight,
        letterSpacing = TypeScaleTokens.TitleLargeTracking,
    )
    val TitleMedium = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.TitleMediumFont,
        fontWeight = TypeScaleTokens.TitleMediumWeight,
        fontSize = TypeScaleTokens.TitleMediumSize,
        lineHeight = TypeScaleTokens.TitleMediumLineHeight,
        letterSpacing = TypeScaleTokens.TitleMediumTracking,
    )
    val TitleSmall = DefaultTextStyle.copy(
        fontFamily = TypeScaleTokens.TitleSmallFont,
        fontWeight = TypeScaleTokens.TitleSmallWeight,
        fontSize = TypeScaleTokens.TitleSmallSize,
        lineHeight = TypeScaleTokens.TitleSmallLineHeight,
        letterSpacing = TypeScaleTokens.TitleSmallTracking,
    )
}

class AcornTypography(
    val displayLarge: TextStyle = TypographyTokens.DisplayLarge,
    val displayMedium: TextStyle = TypographyTokens.DisplayMedium,
    val displaySmall: TextStyle = TypographyTokens.DisplaySmall,
    val headlineLarge: TextStyle = TypographyTokens.HeadlineLarge,
    val headlineMedium: TextStyle = TypographyTokens.HeadlineMedium,
    val headlineSmall: TextStyle = TypographyTokens.HeadlineSmall,
    val titleLarge: TextStyle = TypographyTokens.TitleLarge,
    val titleMedium: TextStyle = TypographyTokens.TitleMedium,
    val titleSmall: TextStyle = TypographyTokens.TitleSmall,
    val bodyLarge: TextStyle = TypographyTokens.BodyLarge,
    val bodyMedium: TextStyle = TypographyTokens.BodyMedium,
    val bodySmall: TextStyle = TypographyTokens.BodySmall,
    val labelLarge: TextStyle = TypographyTokens.LabelLarge,
    val labelMedium: TextStyle = TypographyTokens.LabelMedium,
    val labelSmall: TextStyle = TypographyTokens.LabelSmall,
    // big haccs
    val subtitle1: TextStyle = labelMedium,
    val subtitle2: TextStyle = labelSmall,
    val caption: TextStyle = labelSmall,
    val body1: TextStyle = bodyMedium,
    val body2: TextStyle = bodySmall,
)

class AcornSpace(
    val baseContentEqualPadding: Dp = 8.dp,
)

/**
 * Provides access to the Firefox design system tokens.
 */
object FirefoxTheme {
    val colors: AcornColors
        @Composable get() = AcornTheme.colors

    class AcornTheme {
        companion object {
            val colors = AcornColors()
            val typography = AcornTypography()
            val space = AcornSpace()
        }
    }

    val typography: AcornTypography
        get() = AcornTheme.typography

    val size //: AcornSize
        @Composable get() = 0// AcornTheme.size

    val space: AcornSpace
        @Composable get() = AcornTheme.space

    val windowSize //: AcornWindowSize
        @Composable get() = 0 // AcornTheme.windowSize
}

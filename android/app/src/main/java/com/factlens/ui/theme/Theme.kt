package com.factlens.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FactLensColors {
    val primary = Color(0xFF00497D)
    val primaryContainer = Color(0xFF0061A4)
    val onPrimary = Color(0xFFFFFFFF)
    val onPrimaryContainer = Color(0xFFC0DBFF)
    val primaryFixed = Color(0xFFD1E4FF)
    val primaryFixedDim = Color(0xFF9FCAFF)
    val inversePrimary = Color(0xFF9FCAFF)

    val secondary = Color(0xFF006D44)
    val secondaryContainer = Color(0xFF99F2BE)
    val onSecondary = Color(0xFFFFFFFF)
    val onSecondaryContainer = Color(0xFF0A7148)

    val tertiary = Color(0xFF713700)
    val tertiaryContainer = Color(0xFF944A00)
    val onTertiary = Color(0xFFFFFFFF)
    val onTertiaryContainer = Color(0xFFFCCEAF)
    val tertiaryFixed = Color(0xFFFFDCC6)
    val tertiaryFixedDim = Color(0xFFFFB784)

    val error = Color(0xFFBA1A1A)
    val errorContainer = Color(0xFFFFDAD6)
    val onError = Color(0xFFFFFFFF)
    val onErrorContainer = Color(0xFF93000A)

    val background = Color(0xFFF8F9FF)
    val onBackground = Color(0xFF191C20)
    val surface = Color(0xFFF8F9FF)
    val onSurface = Color(0xFF191C20)
    val surfaceVariant = Color(0xFFE1E2E8)
    val onSurfaceVariant = Color(0xFF414750)
    val outline = Color(0xFF717782)
    val outlineVariant = Color(0xFFC1C7D2)
    val surfaceDim = Color(0xFFD8DAE0)
    val surfaceBright = Color(0xFFF8F9FF)
    val surfaceContainerLowest = Color(0xFFFFFFFF)
    val surfaceContainerLow = Color(0xFFF2F3FA)
    val surfaceContainer = Color(0xFFECEEF4)
    val surfaceContainerHigh = Color(0xFFE6E8EE)
    val surfaceContainerHighest = Color(0xFFE1E2E8)
    val inverseSurface = Color(0xFF2E3135)
    val inverseOnSurface = Color(0xFFEFF0F7)

    // Semantic
    val successEmerald = Color(0xFF006D44)
    val warningAmber = Color(0xFF924C00)
    val errorRed = Color(0xFFBA1A1A)
    val neutralGray = Color(0xFF74777F)
    val backgroundAlmostWhite = Color(0xFFFDFBFF)
    val surfacePureWhite = Color(0xFFFFFFFF)
}

val FactLensTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        fontWeight = FontWeight.Normal
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        fontWeight = FontWeight.Normal
    )
)

val FactLensShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

@Composable
fun FactLensTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = FactLensColors.primary,
            onPrimary = FactLensColors.onPrimary,
            primaryContainer = FactLensColors.primaryContainer,
            onPrimaryContainer = FactLensColors.onPrimaryContainer,
            secondary = FactLensColors.secondary,
            onSecondary = FactLensColors.onSecondary,
            secondaryContainer = FactLensColors.secondaryContainer,
            onSecondaryContainer = FactLensColors.onSecondaryContainer,
            tertiary = FactLensColors.tertiary,
            onTertiary = FactLensColors.onTertiary,
            tertiaryContainer = FactLensColors.tertiaryContainer,
            onTertiaryContainer = FactLensColors.onTertiaryContainer,
            error = FactLensColors.error,
            onError = FactLensColors.onError,
            errorContainer = FactLensColors.errorContainer,
            onErrorContainer = FactLensColors.onErrorContainer,
            background = FactLensColors.background,
            onBackground = FactLensColors.onBackground,
            surface = FactLensColors.surface,
            onSurface = FactLensColors.onSurface,
            surfaceVariant = FactLensColors.surfaceVariant,
            onSurfaceVariant = FactLensColors.onSurfaceVariant,
            outline = FactLensColors.outline,
            outlineVariant = FactLensColors.outlineVariant,
            inverseSurface = FactLensColors.inverseSurface,
            inverseOnSurface = FactLensColors.inverseOnSurface,
            inversePrimary = FactLensColors.inversePrimary,
            surfaceDim = FactLensColors.surfaceDim,
            surfaceBright = FactLensColors.surfaceBright,
            surfaceContainerLowest = FactLensColors.surfaceContainerLowest,
            surfaceContainerLow = FactLensColors.surfaceContainerLow,
            surfaceContainer = FactLensColors.surfaceContainer,
            surfaceContainerHigh = FactLensColors.surfaceContainerHigh,
            surfaceContainerHighest = FactLensColors.surfaceContainerHighest
        ),
        typography = FactLensTypography,
        shapes = FactLensShapes
    ) {
        content()
    }
}

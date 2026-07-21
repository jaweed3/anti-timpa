package com.factlens.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun FactLensTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
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
    ) { content() }
}

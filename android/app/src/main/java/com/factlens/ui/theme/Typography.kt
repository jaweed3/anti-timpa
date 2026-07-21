package com.factlens.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FactLensTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 57.sp, lineHeight = 64.sp,
        letterSpacing = (-0.25).sp, fontWeight = FontWeight.Normal
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 32.sp, lineHeight = 40.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 24.sp, lineHeight = 32.sp,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 22.sp, lineHeight = 28.sp,
        fontWeight = FontWeight.Medium
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 16.sp, lineHeight = 24.sp,
        letterSpacing = 0.15.sp, fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 16.sp, lineHeight = 24.sp,
        letterSpacing = 0.5.sp, fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 14.sp, lineHeight = 20.sp,
        letterSpacing = 0.25.sp, fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 14.sp, lineHeight = 20.sp,
        letterSpacing = 0.1.sp, fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default, fontSize = 12.sp, lineHeight = 16.sp,
        letterSpacing = 0.4.sp, fontWeight = FontWeight.Normal
    )
)

package com.gamelaunch.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.gamelaunch.R

val SyneFontFamily = FontFamily(
    Font(R.font.syne_bold, FontWeight.Bold),
    Font(R.font.syne_bold, FontWeight.SemiBold)
)

val KronosTypography = Typography(
    displayLarge = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 57.sp,
        letterSpacing = (-0.3).sp
    ),
    displayMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 45.sp,
        letterSpacing = (-0.3).sp
    ),
    displaySmall = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 36.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineLarge = TextStyle(
        fontFamily    = SyneFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SyneFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SyneFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp
    ),
    titleLarge = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        letterSpacing = (-0.3).sp
    ),
    titleSmall = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 14.sp,
        letterSpacing = (-0.3).sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = (1.6).em
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = (1.6).em
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp
    ),
    labelSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        letterSpacing = 0.06.sp
    ),
)

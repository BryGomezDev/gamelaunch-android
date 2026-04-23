@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.gamelaunch.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gamelaunch.R

val ManropeFamily = FontFamily(
    Font(R.font.manrope_variable, FontWeight.Light,    variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.manrope_variable, FontWeight.Normal,   variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.manrope_variable, FontWeight.Medium,   variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.manrope_variable, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.manrope_variable, FontWeight.Bold,     variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.manrope_variable, FontWeight.ExtraBold,variationSettings = FontVariation.Settings(FontVariation.weight(800))),
    Font(R.font.manrope_variable, FontWeight.Black,    variationSettings = FontVariation.Settings(FontVariation.weight(900))),
)

val KronosTypography = Typography(
    displayLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 57.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 32.sp,
        letterSpacing = (-1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Light,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = ManropeFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 10.sp,
        letterSpacing = 1.5.sp
    ),
)

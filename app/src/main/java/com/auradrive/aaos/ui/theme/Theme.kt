package com.auradrive.aaos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.auradrive.aaos.R

// ── Legacy Color Aliases (backward compatibility with existing code) ──
val AuraLime = Color(0xFFC6FF00)
val AuraRed = Color(0xFFFF453A)
val AuraGreen = Color(0xFF30D158)
val AuraDark = Color(0xFF07090E)
val AuraCard = Color(0xFF131622)
val AuraSurfaceGlass = Color(0xDC1A1D27)
val AuraGray = Color(0xFF1F222D)
val AuraGold = Color(0xFFFFD700)
val AuraOrange = Color(0xFFFF9500)
val AuraIcyBlue = Color(0xFF80D8FF)
val AuraCyan = Color(0xFF64D2FF)
val AuraPurple = Color(0xFFBF5AF2)
val AuraWarmWhite = Color(0xFFF5F5F7)
val AuraMuted = Color(0xFF8A8F9F)
val AuraPillGreenBg = Color(0x330D421F)
val AuraPillGreenBorder = Color(0xFF30D158)
val AuraPillRedBg = Color(0x33421217)
val AuraPillRedBorder = Color(0xFFFF453A)
val AuraRailBlueGlow = Color(0xFF0071E3)

// ── New AAOS Design System Colors ──
val AuraAccentBlue = Color(0xFF2997FF)
val AuraAmber = Color(0xFFFFD60A)

// ── Typography ──
val AuraOutfitFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.SemiBold),
    Font(R.font.outfit, FontWeight.Bold),
    Font(R.font.outfit, FontWeight.ExtraBold)
)
val AuraSyneFamily = FontFamily(
    Font(R.font.syne, FontWeight.Normal),
    Font(R.font.syne, FontWeight.Medium),
    Font(R.font.syne, FontWeight.SemiBold),
    Font(R.font.syne, FontWeight.Bold),
    Font(R.font.syne, FontWeight.ExtraBold)
)

// ── Fancy Script & Handwriting Fonts ──
val AuraDancingScriptFamily = FontFamily(
    Font(R.font.dancing_script, FontWeight.Normal),
    Font(R.font.dancing_script, FontWeight.Medium),
    Font(R.font.dancing_script, FontWeight.SemiBold),
    Font(R.font.dancing_script, FontWeight.Bold)
)
val AuraCaveatFamily = FontFamily(
    Font(R.font.caveat, FontWeight.Normal),
    Font(R.font.caveat, FontWeight.Bold)
)
val AuraGreatVibesFamily = FontFamily(Font(R.font.great_vibes))
val AuraPacificoFamily = FontFamily(Font(R.font.pacifico))

// Default fancy font for witty titles & badges
val AuraFancyFontFamily = AuraDancingScriptFamily

val AuraDisplayFamily = AuraOutfitFamily
val AuraBodyFamily = AuraOutfitFamily
val AuraMonoFamily = FontFamily.Monospace

val AuraTypography = Typography(
    // Display — Space Grotesk style (bold, tight tracking)
    displayLarge = TextStyle(
        fontFamily = AuraDisplayFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        letterSpacing = (-0.03).sp,
        lineHeight = 40.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AuraDisplayFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        letterSpacing = (-0.02).sp,
        lineHeight = 30.sp
    ),
    displaySmall = TextStyle(
        fontFamily = AuraDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        letterSpacing = 1.5.sp,
        lineHeight = 22.sp
    ),
    // Headlines
    headlineLarge = TextStyle(
        fontFamily = AuraDisplayFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AuraDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp,
        lineHeight = 22.sp
    ),
    // Body
    bodyLarge = TextStyle(
        fontFamily = AuraBodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AuraBodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AuraBodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    // Labels (Monospace style)
    labelLarge = TextStyle(
        fontFamily = AuraMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AuraMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AuraMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.5.sp,
        letterSpacing = 1.5.sp
    )
)

private val DarkColorScheme = darkColorScheme(
    primary = AuraAccentBlue,
    secondary = AuraAccentBlue,
    tertiary = AuraCyan,
    background = AuraDark,
    surface = AuraCard,
    surfaceVariant = Color(0xFF181C2B),
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = Color.White,
    error = AuraRed,
    outline = Color(0xFF222634),
    outlineVariant = Color(0xFF171A25)
)

private val LightColorScheme = lightColorScheme(
    primary = AuraAccentBlue,
    secondary = AuraAccentBlue,
    tertiary = AuraRed,
    background = Color.White,
    surface = Color.White,
)

@Composable
fun AuraDriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Always use dark theme for automotive cockpit display
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AuraTypography,
        content = content
    )
}

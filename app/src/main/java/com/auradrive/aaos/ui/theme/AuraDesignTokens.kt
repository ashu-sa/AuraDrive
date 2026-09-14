package com.auradrive.aaos.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * AuraDrive AAOS Design Tokens
 * Matching the 1480×700 cockpit infotainment display design system.
 */
object AuraTokens {

    // ── Core Backgrounds ──
    val Bg = Color(0xFF07090E)
    val Surface = Color(0xFF0E111A)
    val SurfaceCard = Color(0xFF131622)    // 100% opaque solid chassis
    val SurfaceWarm = Color(0xFF181C2B)
    val SurfaceGlass = Color(0xB3090B10)   // 70% opacity for frosted glass

    // ── Foreground ──
    val Fg = Color(0xFFF5F6FA)
    val Fg2 = Color(0xFF8A8F9F)
    val Muted = Color(0xFF5E6475)
    val Meta = Color(0xFF464A58)

    // ── Borders ──
    val Border = Color(0xFF222634)
    val BorderSoft = Color(0xFF171A25)

    // ── Accent Blue ──
    val Accent = Color(0xFF2997FF)
    val AccentHover = Color(0xFF0077ED)
    val AccentActive = Color(0xFF0066CC)
    val AccentOn = Color(0xFFFFFFFF)

    // ── Semantic Colors ──
    val Green = Color(0xFF30D158)
    val Amber = Color(0xFFFFD60A)
    val Red = Color(0xFFFF453A)
    val Cyan = Color(0xFF64D2FF)
    val Purple = Color(0xFFBF5AF2)

    // ── Rail ──
    val RailBg = Color(0xF50A0D14)         // rgba(10, 13, 20, 0.96)
    val HeaderBg = Color(0xB3090B10)       // rgba(9, 11, 16, 0.70)

    // ── Card Park Badge ──
    val ParkBadgeGradientStart = Color(0xFF132238)
    val ParkBadgeGradientEnd = Color(0xFF0C1524)
    val ParkBadgeActiveStart = Color(0xFF0071E3)
    val ParkBadgeActiveEnd = Color(0xFF004C99)

    // ── Radii ──
    val RadiusSm = 8.dp
    val RadiusMd = 14.dp
    val RadiusLg = 20.dp
    val RadiusCard = 28.dp
    val RadiusPill = 980.dp

    // ── Sizing ──
    val HeaderHeight = 64.dp
    val RailWidth = 86.dp
    val DrawerWidth = 440.dp
    val CardWidth = 400.dp
    val CardHeight = 400.dp
}

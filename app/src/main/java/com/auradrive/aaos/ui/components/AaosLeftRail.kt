package com.auradrive.aaos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// ── Design Tokens & Colors ──
val AuraBg = Color(0xFF07090E)
val AuraSurface = Color(0xFF0E111A)
val AuraSurfaceCard = Color(0xFF131622)
val AuraFg = Color(0xFFF5F6FA)
val AuraFg2 = Color(0xFF8A8F9F)
val AuraMutedColor = Color(0xFF5E6475)
val AuraMetaColor = Color(0xFF464A58)
val AuraBorder = Color(0xFF222634)
val AuraAccentBlue = Color(0xFF2997FF)
val AuraGreen = Color(0xFF30D158)
val AuraCyanColor = Color(0xFF64D2FF)

// Component-specific color constants
private val RailBackground = Color(0xF50A0D14) // rgba(10, 13, 20, 0.96)
private val RailBorderColor = Color(0x0FFFFFFF) // rgba(255, 255, 255, 0.06)
private val ParkBadgeGradStart = Color(0xFF132238)
private val ParkBadgeGradEnd = Color(0xFF0C1524)
private val ParkBadgeActiveStart = Color(0xFF0071E3)
private val ParkBadgeActiveEnd = Color(0xFF004C99)
private val ParkBadgeBorderNormal = Color(0x592997FF) // 1.5dp Color(0x592997FF)
private val ParkBadgeBorderActive = Color(0xFF64D2FF) // cyan border Color(0xFF64D2FF)
private val StatusPillBg = Color(0x08FFFFFF)
private val StatusPillBorder = Color(0x0FFFFFFF)

/**
 * Android Automotive OS (AAOS) Left Rail Navigation Component.
 *
 * Implements an 86.dp wide constant navigation rail fixed to the left edge of the
 * cockpit display, containing the Park telemetry toggle button, vertical status text,
 * and EV status indicator pill.
 *
 * @param isDrawerOpen Whether the telemetry drawer is currently open/active
 * @param onToggleDrawer Callback invoked when the user clicks the Park button
 * @param modifier Modifier for external layout customization
 */
@Composable
fun AaosLeftRail(
    isDrawerOpen: Boolean,
    onToggleDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(86.dp)
            .fillMaxHeight()
            .drawBehind {
                // Right border: 1px solid rgba(255, 255, 255, 0.06)
                val strokeWidth = 1.dp.toPx()
                val x = size.width - strokeWidth / 2f
                drawLine(
                    color = RailBorderColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokeWidth
                )
            },
        color = RailBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Park Button (parkConstantBtn)
            ParkConstantButton(
                isDrawerOpen = isDrawerOpen,
                onClick = onToggleDrawer
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Vertical Hint Text: "AAOS // CONSTANT"
            VerticalHintText(
                modifier = Modifier.padding(top = 12.dp)
            )

            // Flexible space pushing the status pill to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // 3. Status Pill (at bottom)
            StatusPill()
        }
    }
}

/**
 * Park telemetry trigger button (parkConstantBtn).
 * Centered column layout, 72.dp wide, rounded 20.dp, with 54x54.dp badge shell,
 * title, and telemetry subtitle.
 */
@Composable
private fun ParkConstantButton(
    isDrawerOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Smooth animated transitions between normal and active drawer states
    val badgeScale by animateFloatAsState(
        targetValue = if (isDrawerOpen) 1.06f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "parkBadgeScale"
    )
    val gradStart by animateColorAsState(
        targetValue = if (isDrawerOpen) ParkBadgeActiveStart else ParkBadgeGradStart,
        animationSpec = tween(220),
        label = "parkGradStart"
    )
    val gradEnd by animateColorAsState(
        targetValue = if (isDrawerOpen) ParkBadgeActiveEnd else ParkBadgeGradEnd,
        animationSpec = tween(220),
        label = "parkGradEnd"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDrawerOpen) ParkBadgeBorderActive else ParkBadgeBorderNormal,
        animationSpec = tween(220),
        label = "parkBorderColor"
    )
    val pTextColor by animateColorAsState(
        targetValue = if (isDrawerOpen) Color.White else AuraAccentBlue,
        animationSpec = tween(220),
        label = "parkPTextColor"
    )
    val titleColor by animateColorAsState(
        targetValue = if (isDrawerOpen) Color.White else AuraFg2,
        animationSpec = tween(220),
        label = "parkTitleColor"
    )
    val subtitleColor by animateColorAsState(
        targetValue = if (isDrawerOpen) AuraCyanColor else AuraMutedColor,
        animationSpec = tween(220),
        label = "parkSubtitleColor"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isDrawerOpen) 0.65f else 0f,
        animationSpec = tween(220),
        label = "parkGlowAlpha"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.width(72.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isDrawerOpen) Color(0x142997FF) else Color.Transparent,
        contentColor = Color.Unspecified
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Park Badge Shell: 54x54.dp, rounded 16.dp
            Box(
                modifier = Modifier
                    .scale(badgeScale)
                    .size(54.dp)
                    .drawBehind {
                        // Blue glow shadow when active
                        if (glowAlpha > 0f) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF0071E3).copy(alpha = glowAlpha),
                                        Color(0xFF0071E3).copy(alpha = glowAlpha * 0.35f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.maxDimension * 0.95f
                                )
                            )
                        }
                    }
                    .background(
                        brush = Brush.linearGradient(listOf(gradStart, gradEnd)),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        border = BorderStroke(1.5.dp, borderColor),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Letter "P" in Space Grotesk / bold sans, 26.sp, weight 800
                Text(
                    text = "P",
                    fontSize = 26.sp,
                    fontWeight = FontWeight(800),
                    fontFamily = FontFamily.SansSerif,
                    color = pTextColor,
                    textAlign = TextAlign.Center
                )

                // Green dot indicator (10x10.dp) at top-right corner with shadow glow
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(10.dp)
                        .drawBehind {
                            drawCircle(
                                color = AuraGreen.copy(alpha = 0.55f),
                                radius = size.width * 1.35f
                            )
                        }
                        .background(AuraGreen, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title: "PARK" - 11.sp, FontWeight.Bold, letterSpacing 0.1em, uppercase
            val titleShadow = if (isDrawerOpen) {
                Shadow(
                    color = Color(0xFF2997FF).copy(alpha = 0.85f),
                    offset = Offset(0f, 0f),
                    blurRadius = 8f
                )
            } else {
                Shadow.None
            }

            Text(
                text = "PARK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.em,
                color = titleColor,
                style = TextStyle(shadow = titleShadow),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle: "TELEMETRY" - 9.sp, mono font
            Text(
                text = "TELEMETRY",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.08.em,
                color = subtitleColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Extension layout modifier to swap measured width and height for vertical orientation,
 * ensuring the composable accurately occupies vertical space in layout calculations.
 */
private fun Modifier.vertical(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
        placeable.placeRelative(
            x = (placeable.height - placeable.width) / 2,
            y = (placeable.width - placeable.height) / 2
        )
    }
}

/**
 * Vertical Hint Text: "AAOS // CONSTANT"
 * Rotated -90° to read bottom-to-top in automotive rail style, 9.sp mono font,
 * opacity 0.7, uppercase with 0.18em tracking.
 */
@Composable
private fun VerticalHintText(
    modifier: Modifier = Modifier
) {
    Text(
        text = "AAOS // CONSTANT",
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        color = AuraMutedColor.copy(alpha = 0.7f),
        letterSpacing = 0.18.em,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
            .vertical()
            .rotate(-90f)
    )
}

/**
 * Status Pill anchored at the bottom of the rail.
 * 66.dp wide, 10.dp rounded corners, rgba(255,255,255,0.03) surface with
 * 6x6.dp glowing green dot and "EV OK" monospace telemetry label.
 */
@Composable
private fun StatusPill(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(66.dp),
        shape = RoundedCornerShape(10.dp),
        color = StatusPillBg,
        border = BorderStroke(1.dp, StatusPillBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Green dot: 6x6.dp with glow shadow
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .drawBehind {
                        drawCircle(
                            color = AuraGreen.copy(alpha = 0.5f),
                            radius = size.width * 1.5f
                        )
                    }
                    .background(AuraGreen, CircleShape)
            )

            // Status label: "EV OK" - 9.sp, mono, FontWeight.Bold, letterSpacing 0.1em
            Text(
                text = "EV OK",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = AuraFg2,
                letterSpacing = 0.1.em,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Previews ──
@Preview(showBackground = true, backgroundColor = 0xFF07090E, heightDp = 700)
@Composable
private fun AaosLeftRailClosedPreview() {
    Box(modifier = Modifier.fillMaxHeight()) {
        AaosLeftRail(
            isDrawerOpen = false,
            onToggleDrawer = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E, heightDp = 700)
@Composable
private fun AaosLeftRailOpenPreview() {
    Box(modifier = Modifier.fillMaxHeight()) {
        AaosLeftRail(
            isDrawerOpen = true,
            onToggleDrawer = {}
        )
    }
}

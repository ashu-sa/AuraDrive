package com.auradrive.aaos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auradrive.aaos.R
import com.auradrive.aaos.ui.theme.AuraAccentBlue
import com.auradrive.aaos.ui.theme.AuraCyan
import com.auradrive.aaos.ui.theme.AuraGreen
import com.auradrive.aaos.ui.theme.AuraMuted
import kotlinx.coroutines.delay

/**
 * Animated Car Startup Diagnostic Screen for AuraDrive AAOS Cockpit.
 * Matches aura-drive-infotainment-12.html:
 * - Ground perspective grid & speed streaks
 * - Rotating dashed HUD radar ring
 * - Vector EV Sports Coupe with spinning turbine wheels, suspension bounce, matrix headlights & underglow
 * - CAN-BUS telemetry loading progress (0% -> 100%)
 * - Smooth vehicle acceleration launch offscreen on completion
 */
@Composable
fun CarStartupOverlay(
    visible: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val progress = remember { Animatable(0f) }
    val carOffsetX = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }

    // Diagnostic feed message
    val currentPercent = (progress.value * 100).toInt()
    val feedText = when {
        currentPercent < 18 -> "INITIALIZING CAN-BUS TELEMETRY..."
        currentPercent < 42 -> "DIAGNOSING G-FORCE & BRAKE LOGS..."
        currentPercent < 68 -> "SYNCHRONIZING REGEN HYBRID SENSORS..."
        currentPercent < 88 -> "CALCULATING AURA TRIP SCORE..."
        else -> "SYSTEM READY. PREPARING CARDS..."
    }

    // Run the sequence when triggered
    LaunchedEffect(visible) {
        progress.snapTo(0f)
        carOffsetX.snapTo(0f)
        overlayAlpha.snapTo(1f)

        // Progress 0 -> 100% over 2.4 seconds
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )

        // Accelerate car off-screen
        carOffsetX.animateTo(
            targetValue = 900f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )

        // Fade out overlay
        overlayAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )

        onComplete()
    }

    // Infinite animations for wheels, suspension, radar, and speed streaks
    val infiniteTransition = rememberInfiniteTransition(label = "CarAnim")

    val wheelRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WheelSpin"
    )

    val suspensionY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Suspension"
    )

    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSpin"
    )

    val beamPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BeamPulse"
    )

    val streakProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Streaks"
    )

    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = overlayAlpha.value }
            .background(Color(0xFF06080D))
    ) {
        // ── 1. Animated Speed Streaks & Ground Lines ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 5 speed streaks across the middle-bottom
            val streakTops = listOf(0.24f, 0.36f, 0.48f, 0.58f, 0.70f)
            val streakLengths = listOf(220f, 320f, 180f, 260f, 380f)

            for (i in streakTops.indices) {
                val y = height * streakTops[i]
                val len = streakLengths[i]
                val offsetPhase = (streakProgress + i * 0.22f) % 1f
                val startX = width * (1.1f - offsetPhase * 1.3f)
                val endX = startX - len

                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AuraAccentBlue.copy(alpha = 0.75f),
                            AuraCyan.copy(alpha = 0.9f),
                            Color.Transparent
                        ),
                        startX = endX,
                        endX = startX
                    ),
                    start = Offset(endX, y),
                    end = Offset(startX, y),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }

            // Perspective Grid Plane at bottom (simulated with converging lines)
            val groundY = height * 0.72f
            val gridLineCount = 8
            for (g in 0..gridLineCount) {
                val progressOffset = ((streakProgress * 30f + g * 35f) % (height - groundY))
                val y = groundY + progressOffset
                val alpha = (progressOffset / (height - groundY)).coerceIn(0f, 0.45f)
                drawLine(
                    color = AuraAccentBlue.copy(alpha = alpha),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }
        }

        // ── 2. Top Brand Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF090B12),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraAccentBlue.copy(alpha = 0.5f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "AuraDrive",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        "AURA DRIVE",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.5.sp
                    )
                    Text(
                        "CAN-BUS TELEMETRY // EV SPORT CHASSIS",
                        color = AuraAccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Status Tag
            Surface(
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(980.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Text(
                    "CALIBRATING G-SENSORS",
                    color = AuraMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // ── 3. Central Car Model Stage ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            // HUD Radar Ring (rotating dashed circle)
            Canvas(
                modifier = Modifier
                    .size(480.dp, 260.dp)
                    .graphicsLayer { rotationZ = radarRotation }
            ) {
                drawOval(
                    color = AuraAccentBlue.copy(alpha = 0.25f),
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )
            }

            // Vector EV Coupe with Suspension and Acceleration Movement
            val density = LocalDensity.current
            val carTransY = with(density) { suspensionY.dp.toPx() }

            Canvas(
                modifier = Modifier
                    .width(480.dp)
                    .height(210.dp)
                    .graphicsLayer {
                        translationX = carOffsetX.value
                        translationY = carTransY
                    }
            ) {
                drawEvSportsCoupe(
                    wheelRotation = wheelRotation,
                    beamPulse = beamPulse
                )
            }
        }

        // ── 4. Bottom Telemetry Diagnostics Deck ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 60.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pulse dot + Diagnostic readout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer {
                            scaleX = dotPulse
                            scaleY = dotPulse
                        }
                        .background(AuraCyan, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    feedText,
                    color = AuraAccentBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar track
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0071E3),
                                    Color(0xFF2997FF),
                                    Color(0xFF64D2FF)
                                )
                            ),
                            RoundedCornerShape(999.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics line
            Row(
                modifier = Modifier.fillMaxWidth(0.65f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text("SPEED: ", color = AuraMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("112 KM/H", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Row {
                    Text("AURA SENSORS: ", color = AuraMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("ONLINE", color = AuraGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Text(
                    "$currentPercent%",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Draws the high-precision vector EV sports coupe matching aura-drive-infotainment-12.html:
 * Scale: 480 x 210 dp canvas
 */
private fun DrawScope.drawEvSportsCoupe(wheelRotation: Float, beamPulse: Float) {
    val scaleX = size.width / 600f
    val scaleY = size.height / 240f

    fun sx(x: Float) = x * scaleX
    fun sy(y: Float) = y * scaleY

    // ── 1. Headlight Glow Cone (Polygon projection) ──
    val beamPath = Path().apply {
        moveTo(sx(520f), sy(132f))
        lineTo(sx(660f), sy(70f))
        lineTo(sx(660f), sy(195f))
        lineTo(sx(520f), sy(140f))
        close()
    }
    drawPath(
        path = beamPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                AuraCyan.copy(alpha = 0.85f * beamPulse),
                AuraCyan.copy(alpha = 0.15f * beamPulse),
                Color.Transparent
            ),
            startX = sx(520f),
            endX = sx(660f)
        )
    )

    // ── 2. Neon Underglow ──
    drawOval(
        color = AuraAccentBlue.copy(alpha = 0.45f),
        topLeft = Offset(sx(80f), sy(182f)),
        size = Size(sx(440f), sy(18f))
    )

    // ── 3. EV Body Silhouette ──
    // Path: M70 160 C100 160 110 130 140 120 C180 110 240 70 340 70 C410 70 450 110 500 125 C530 130 555 145 550 162 C540 172 510 174 480 174 C475 145 440 145 435 174 C340 174 240 174 200 174 C195 145 160 145 155 174 C110 174 80 172 70 160 Z
    val bodyPath = Path().apply {
        moveTo(sx(70f), sy(160f))
        cubicTo(sx(100f), sy(160f), sx(110f), sy(130f), sx(140f), sy(120f))
        cubicTo(sx(180f), sy(110f), sx(240f), sy(70f), sx(340f), sy(70f))
        cubicTo(sx(410f), sy(70f), sx(450f), sy(110f), sx(500f), sy(125f))
        cubicTo(sx(530f), sy(130f), sx(555f), sy(145f), sx(550f), sy(162f))
        cubicTo(sx(540f), sy(172f), sx(510f), sy(174f), sx(480f), sy(174f))
        cubicTo(sx(475f), sy(145f), sx(440f), sy(145f), sx(435f), sy(174f))
        cubicTo(sx(340f), sy(174f), sx(240f), sy(174f), sx(200f), sy(174f))
        cubicTo(sx(195f), sy(145f), sx(160f), sy(145f), sx(155f), sy(174f))
        cubicTo(sx(110f), sy(174f), sx(80f), sy(172f), sx(70f), sy(160f))
        close()
    }

    // Body Gradient Fill
    drawPath(
        path = bodyPath,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF0A0E17),
                Color(0xFF162238),
                Color(0xFF1B2A47),
                Color(0xFF0E1524)
            ),
            startX = sx(70f),
            endX = sx(550f)
        )
    )
    // Body Accent Stroke
    drawPath(
        path = bodyPath,
        color = AuraAccentBlue,
        style = Stroke(width = 2.2f)
    )

    // ── 4. Aerodynamic Cabin Glass Roof ──
    val cabinPath = Path().apply {
        moveTo(sx(220f), sy(115f))
        lineTo(sx(265f), sy(80f))
        lineTo(sx(365f), sy(80f))
        lineTo(sx(440f), sy(115f))
        close()
    }
    drawPath(
        path = cabinPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                AuraAccentBlue.copy(alpha = 0.6f),
                Color(0xFF080C14).copy(alpha = 0.9f)
            ),
            startY = sy(80f),
            endY = sy(115f)
        )
    )
    drawPath(
        path = cabinPath,
        color = Color.White.copy(alpha = 0.25f),
        style = Stroke(width = 1.5f)
    )

    // B-Pillar Divider
    drawLine(
        color = Color(0xFF162238),
        start = Offset(sx(330f), sy(80f)),
        end = Offset(sx(330f), sy(115f)),
        strokeWidth = 3.5f
    )

    // ── 5. Side Aero Character Crease & Lower Skirt ──
    val creasePath = Path().apply {
        moveTo(sx(165f), sy(145f))
        quadraticTo(sx(310f), sy(140f), sx(460f), sy(142f))
    }
    drawPath(
        path = creasePath,
        color = AuraCyan.copy(alpha = 0.7f),
        style = Stroke(width = 2f)
    )

    drawLine(
        color = Color.White.copy(alpha = 0.15f),
        start = Offset(sx(90f), sy(155f)),
        end = Offset(sx(530f), sy(155f)),
        strokeWidth = 1f
    )

    // ── 6. Headlight & Taillight LEDs ──
    // Matrix LED Headlight
    val headlight = Path().apply {
        moveTo(sx(495f), sy(130f))
        lineTo(sx(535f), sy(134f))
        lineTo(sx(515f), sy(140f))
        close()
    }
    drawPath(path = headlight, color = AuraCyan)

    // Taillight OLED Strip
    val taillight = Path().apply {
        moveTo(sx(72f), sy(152f))
        lineTo(sx(95f), sy(150f))
        lineTo(sx(88f), sy(158f))
        close()
    }
    drawPath(path = taillight, color = Color(0xFFFF453A))

    // ── 7. Rear Wheel Assembly (Center: 178, 174) ──
    drawWheelTurbine(
        center = Offset(sx(178f), sy(174f)),
        radius = sx(28f),
        wheelRotation = wheelRotation
    )

    // ── 8. Front Wheel Assembly (Center: 458, 174) ──
    drawWheelTurbine(
        center = Offset(sx(458f), sy(174f)),
        radius = sx(28f),
        wheelRotation = wheelRotation
    )
}

/**
 * Draws aerodynamic turbine wheel with spinning spokes and center blue cap
 */
private fun DrawScope.drawWheelTurbine(center: Offset, radius: Float, wheelRotation: Float) {
    // Outer Tyre Rim
    drawCircle(
        color = Color(0xFF090B10),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color(0xFF222634),
        radius = radius,
        center = center,
        style = Stroke(width = 3.5f)
    )

    // Inner Rim
    drawCircle(
        color = Color(0xFF141722),
        radius = radius * 0.82f,
        center = center
    )
    drawCircle(
        color = AuraAccentBlue,
        radius = radius * 0.82f,
        center = center,
        style = Stroke(width = 1.5f)
    )

    // Spinning Aerodynamic Spokes
    rotate(wheelRotation, pivot = center) {
        val spokeLen = radius * 0.72f
        // Cross spokes (White)
        drawLine(
            color = Color(0xFFF5F6FA),
            start = Offset(center.x - spokeLen, center.y),
            end = Offset(center.x + spokeLen, center.y),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0xFFF5F6FA),
            start = Offset(center.x, center.y - spokeLen),
            end = Offset(center.x, center.y + spokeLen),
            strokeWidth = 2f
        )
        // Diagonal spokes (Cyan)
        val diag = spokeLen * 0.707f
        drawLine(
            color = AuraCyan,
            start = Offset(center.x - diag, center.y - diag),
            end = Offset(center.x + diag, center.y + diag),
            strokeWidth = 2f
        )
        drawLine(
            color = AuraCyan,
            start = Offset(center.x - diag, center.y + diag),
            end = Offset(center.x + diag, center.y - diag),
            strokeWidth = 2f
        )
    }

    // Hub Cap
    drawCircle(color = AuraAccentBlue, radius = radius * 0.28f, center = center)
    drawCircle(color = Color.White, radius = radius * 0.14f, center = center)
}

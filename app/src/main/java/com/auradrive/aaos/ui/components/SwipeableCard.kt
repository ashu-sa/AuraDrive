package com.auradrive.aaos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeableCard(
    onSwipe: () -> Unit = {},
    onSwipeLeft: () -> Unit = onSwipe,
    onSwipeRight: () -> Unit = onSwipe,
    onSnapBack: (() -> Unit)? = null,
    onDragProgress: ((dragX: Float, dragY: Float) -> Unit)? = null,
    programmaticSwipeDirection: Float? = null,
    key: Any? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val currentOnSwipeLeft by rememberUpdatedState(onSwipeLeft)
    val currentOnSwipeRight by rememberUpdatedState(onSwipeRight)
    val currentOnSnapBack by rememberUpdatedState(onSnapBack)
    val currentOnDragProgress by rememberUpdatedState(onDragProgress)

    val offsetX = remember(key) { Animatable(0f) }
    val rotation = remember(key) { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Reset animations when key changes
    LaunchedEffect(key) {
        offsetX.snapTo(0f)
        rotation.snapTo(0f)
    }

    // Programmatic swipe animation trigger (e.g. from Like / Dislike buttons)
    LaunchedEffect(programmaticSwipeDirection, key) {
        if (programmaticSwipeDirection != null && programmaticSwipeDirection != 0f) {
            val targetX = if (programmaticSwipeDirection > 0f) 1400f else -1400f
            scope.launch {
                rotation.animateTo(if (programmaticSwipeDirection > 0f) 18f else -18f, tween(260))
            }
            offsetX.animateTo(targetX, tween(260))
            if (programmaticSwipeDirection > 0f) {
                currentOnSwipeRight()
            } else {
                currentOnSwipeLeft()
            }
        }
    }

    val swipeThreshold = 180f

    // Dynamic drag factors
    val dragDistance = offsetX.value
    val normalizedRight = (dragDistance / swipeThreshold).coerceIn(0f, 1f)
    val normalizedLeft = (-dragDistance / swipeThreshold).coerceIn(0f, 1f)
    val totalNormalizedDrag = (abs(dragDistance) / swipeThreshold).coerceIn(0f, 1f)

    Box(
        modifier = modifier
    ) {
        // 1. Aerodynamic Slipstream Wind Lines (Neutral electric automotive wind tunnel effect)
        if (abs(dragDistance) > 20f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = ((abs(dragDistance) - 20f) / 160f).coerceIn(0f, 0.85f)
                    }
            ) {
                val isRight = dragDistance > 0
                val streamlineColor = Color(0xFF00D2FF)
                val lineCount = 6
                val heightStep = size.height / (lineCount + 1)

                for (i in 1..lineCount) {
                    val y = heightStep * i + ((i % 2) * 12f - 6f)
                    val progressRatio = abs(dragDistance) / 300f
                    val lineLength = (60f + (i * 24f) * progressRatio).coerceIn(40f, 220f)

                    val startX = if (isRight) {
                        size.width * 0.15f - (lineLength * 0.7f)
                    } else {
                        size.width * 0.85f + (lineLength * 0.7f)
                    }
                    val endX = if (isRight) startX + lineLength else startX - lineLength

                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = if (isRight) {
                                listOf(Color.Transparent, streamlineColor.copy(alpha = 0.6f))
                            } else {
                                listOf(streamlineColor.copy(alpha = 0.6f), Color.Transparent)
                            }
                        ),
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = (2.5f + (i % 3)),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // 2. Reactive EV Energy Halo Glow Behind Card (Neutral Aura Cyan Glow)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .graphicsLayer {
                    translationX = offsetX.value * 0.9f
                    rotationZ = rotation.value * 0.9f
                    alpha = totalNormalizedDrag * 0.45f
                }
        ) {
            val haloColor = Color(0xFF00D2FF)
            Surface(
                color = haloColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxSize()
            ) {}
        }

        // 3. Main Swiping Card Body with Automotive Active Suspension Physics
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ = rotation.value
                    // Subtle automotive G-Force compression
                    val scaleFactor = 1f - (abs(offsetX.value) / 1600f).coerceIn(0f, 0.05f)
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                .pointerInput(key) {
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetX.value > swipeThreshold) {
                                scope.launch {
                                    offsetX.animateTo(1400f, tween(260))
                                    currentOnSwipeRight()
                                }
                            } else if (offsetX.value < -swipeThreshold) {
                                scope.launch {
                                    offsetX.animateTo(-1400f, tween(260))
                                    currentOnSwipeLeft()
                                }
                            } else {
                                currentOnSnapBack?.invoke()
                                scope.launch {
                                    // Tuned luxury automotive active suspension rebound
                                    launch {
                                        offsetX.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                    launch {
                                        rotation.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                rotation.snapTo(offsetX.value / 38f)
                            }
                            currentOnDragProgress?.invoke(offsetX.value + dragAmount.x, 0f)
                        }
                    )
                }
        ) {
            content()
        }
    }
}

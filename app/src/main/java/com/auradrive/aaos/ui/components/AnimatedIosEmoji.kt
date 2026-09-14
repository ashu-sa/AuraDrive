package com.auradrive.aaos.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auradrive.aaos.R

/**
 * Maps standard Unicode emojis to local Apple / iOS high-resolution emoji drawables.
 */
fun getIosEmojiDrawable(emoji: String): Int? = when (emoji) {
    "😎" -> R.drawable.emoji_sunglasses
    "🛑" -> R.drawable.emoji_stop
    "⚡" -> R.drawable.emoji_lightning
    "❄️", "❄" -> R.drawable.emoji_snowflake
    "🔋" -> R.drawable.emoji_battery
    "🏁" -> R.drawable.emoji_flag
    "💀" -> R.drawable.emoji_skull
    "😭" -> R.drawable.emoji_crying
    "🕹️", "🕹" -> R.drawable.emoji_joystick
    "🛣️", "🛣" -> R.drawable.emoji_highway
    "🌡️", "🌡" -> R.drawable.emoji_thermometer
    "🚗" -> R.drawable.emoji_car
    "💡" -> R.drawable.emoji_lightbulb
    "🛞" -> R.drawable.emoji_tire
    "👑" -> R.drawable.emoji_crown
    "🚀" -> R.drawable.emoji_rocket
    "👀" -> R.drawable.emoji_eyes
    else -> null
}

/**
 * Animated iOS Emoji component.
 * Renders high-resolution Apple design emojis with tailored continuous micro-animations
 * (floating, breathing, rotation, pulse, shaking, or swaying).
 */
@Composable
fun AnimatedIosEmoji(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    animated: Boolean = true
) {
    val drawableRes = getIosEmojiDrawable(emoji)

    val infiniteTransition = rememberInfiniteTransition(label = "EmojiAnimation_$emoji")

    // Custom animation parameters based on emoji semantic personality
    val (rotation, scale, offsetY) = if (animated) {
        when (emoji) {
            "😎" -> {
                // Sunglasses: Smooth chilled-out sway & float
                val rot by infiniteTransition.animateFloat(
                    initialValue = -5f,
                    targetValue = 5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1.07f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val y by infiniteTransition.animateFloat(
                    initialValue = -3f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )
                Triple(rot, sc, y)
            }
            "🛑" -> {
                // Stop sign: Urgent heartbeat pulse + warning shudder
                val rot by infiniteTransition.animateFloat(
                    initialValue = -3f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(650, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                Triple(rot, sc, 0f)
            }
            "⚡" -> {
                // Lightning: Electric zap / fast jitter
                val rot by infiniteTransition.animateFloat(
                    initialValue = -7f,
                    targetValue = 7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.14f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val y by infiniteTransition.animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(450, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )
                Triple(rot, sc, y)
            }
            "❄️", "❄" -> {
                // Snowflake: Graceful 360 rotation + gentle float
                val rot by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(10000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rot"
                )
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val y by infiniteTransition.animateFloat(
                    initialValue = -3f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )
                Triple(rot, sc, y)
            }
            "🔋" -> {
                // Battery: Rhythmic breathing + bob
                val rot by infiniteTransition.animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val y by infiniteTransition.animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )
                Triple(rot, sc, y)
            }
            "🏁" -> {
                // Flag: Dynamic wind wave sway
                val rot by infiniteTransition.animateFloat(
                    initialValue = -10f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                Triple(rot, sc, 0f)
            }
            "💀" -> {
                // Skull: Floating wobble bob
                val rot by infiniteTransition.animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1300, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )
                val y by infiniteTransition.animateFloat(
                    initialValue = -4f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )
                Triple(rot, 1.02f, y)
            }
            "😭" -> {
                // Crying: Sobbing shudder and pulse
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val rot by infiniteTransition.animateFloat(
                    initialValue = -4f,
                    targetValue = 4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )
                val y by infiniteTransition.animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )
                Triple(rot, sc, y)
            }
            "👀" -> {
                // Side eye: Subtle glancing tilt, breath and side-wobble
                val rot by infiniteTransition.animateFloat(
                    initialValue = -5f,
                    targetValue = 5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "rot"
                )
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val y by infiniteTransition.animateFloat(
                    initialValue = -1.5f,
                    targetValue = 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )
                Triple(rot, sc, y)
            }
            else -> {
                // Default: Gentle ambient float + breathe
                val sc by infiniteTransition.animateFloat(
                    initialValue = 0.97f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val y by infiniteTransition.animateFloat(
                    initialValue = -2.5f,
                    targetValue = 2.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )
                Triple(0f, sc, y)
            }
        }
    } else {
        Triple(0f, 1f, 0f)
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .offset(y = offsetY.dp),
        contentAlignment = Alignment.Center
    ) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = emoji,
                modifier = Modifier.size(size)
            )
        } else {
            Text(
                text = emoji,
                fontSize = (size.value * 0.85f).sp
            )
        }
    }
}

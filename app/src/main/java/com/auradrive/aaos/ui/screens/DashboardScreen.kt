package com.auradrive.aaos.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auradrive.aaos.R
import com.auradrive.aaos.ui.MainViewModel
import com.auradrive.aaos.ui.components.AaosLeftRail
import com.auradrive.aaos.ui.components.AnimatedIosEmoji
import com.auradrive.aaos.ui.components.CarStartupOverlay
import com.auradrive.aaos.ui.components.SwipeableCard
import com.auradrive.aaos.roast.ParkedRoastState
import com.auradrive.aaos.model.DrivingSession
import com.auradrive.aaos.sound.SoundEffectsManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ══════════════════════════════════════════════════════════════════════════
// AAOS 1480 COCKPIT — EXACT REPLICA of aura-drive-infotainment-12.html
// Header 64 / LeftRail 86 / Deck 400x360 opaque / BottomDock 72
// ══════════════════════════════════════════════════════════════════════════

private val C_Bg = Color(0xFF07090E)
private val C_Card = Color(0xFF131622)
private val C_Fg2 = Color(0xFF8A8F9F)
private val C_Muted = Color(0xFF5E6475)
private val C_Accent = Color(0xFF2997FF)
private val C_Green = Color(0xFF30D158)
private val C_Amber = Color(0xFFFFD60A)
private val C_Red = Color(0xFFFF453A)
private val C_Cyan = Color(0xFF64D2FF)
private val C_Purple = Color(0xFFBF5AF2)

fun getAccentColor(name: String): Color = when (name) {
    "green" -> C_Green
    "amber" -> C_Amber
    "red" -> C_Red
    "cyan" -> C_Cyan
    "blue" -> C_Accent
    else -> C_Green
}

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val parkedState by viewModel.parkedRoastState.collectAsState()
    val roastCards by viewModel.roastCards.collectAsState()
    val allCards by viewModel.allCards.collectAsState()
    val vehicleData by viewModel.vehicleData.collectAsState()

    val context = LocalContext.current
    val soundManager = remember(context) { SoundEffectsManager(context) }

    var isDrawerOpen by remember { mutableStateOf(false) }
    var isGridViewOpen by remember { mutableStateOf(false) }
    var isAudioActive by remember { mutableStateOf(true) }
    var showStartupOverlay by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastText by remember { mutableStateOf("Trip Telemetry sent to iPhone 16 Pro!") }

    LaunchedEffect(isAudioActive) {
        soundManager.isAudioEnabled = isAudioActive
        viewModel.setAudioEnabled(isAudioActive)
    }
    DisposableEffect(soundManager) { onDispose { soundManager.release() } }
    LaunchedEffect(showToast) {
        if (showToast) { delay(3200); showToast = false }
    }

    fun toast(msg: String) { toastText = msg; showToast = true }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF020305)),
        contentAlignment = Alignment.Center
    ) {
        // Ambient cockpit glow behind the 1480 chassis
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0D111A), Color(0xFF05060A), Color(0xFF010203)),
                    radius = 1400f
                )
            )
        )

        // ── AAOS 1480 CHASSIS ──
        Surface(
            color = C_Bg,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(4.dp, Color(0xFF1B202D)),
            shadowElevation = 32.dp,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Ambient lighting layer
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF090B10).copy(alpha = 0.9f), Color(0xFF05070A))
                        )
                    )
                )
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp).background(
                        Brush.radialGradient(
                            colors = listOf(C_Accent.copy(alpha = 0.12f), Color.Transparent),
                            radius = 600f
                        )
                    )
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    ExactHeader(
                        currentIdx = (5 - roastCards.size).coerceIn(0, 4),
                        total = 5,
                        onDiagnostic = {
                            soundManager.playClick()
                            showStartupOverlay = true
                        }
                    )

                    // ── SCREEN BODY ──
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        AaosLeftRail(
                            isDrawerOpen = isDrawerOpen,
                            onToggleDrawer = {
                                soundManager.playClick()
                                isDrawerOpen = !isDrawerOpen
                            }
                        )

                        // Main stage with drawer overlay
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            // Center deck
                            when (parkedState) {
                                is ParkedRoastState.Intro -> {
                                    val s = (parkedState as? ParkedRoastState.Intro)?.session
                                    if (roastCards.isNotEmpty()) {
                                        ExactSwipeStage(
                                            cards = roastCards, viewModel = viewModel,
                                            soundManager = soundManager,
                                            onShare = { toast("Trip Card beamed to paired iPhone!") }
                                        )
                                    } else if (s != null) {
                                        IntroView(s) {
                                            soundManager.playEvStartup()
                                            viewModel.startRoast()
                                        }
                                    }
                                }
                                is ParkedRoastState.Complete -> {
                                    DeckFinishedCard(
                                        onRestart = { soundManager.playClick(); viewModel.resetReport() },
                                        onViewAllCards = { soundManager.playClick(); isGridViewOpen = true }
                                    )
                                }
                                else -> {
                                    if (roastCards.isNotEmpty()) {
                                        ExactSwipeStage(
                                            cards = roastCards, viewModel = viewModel,
                                            soundManager = soundManager,
                                            onShare = { toast("Trip Card beamed to paired iPhone!") }
                                        )
                                    } else {
                                        DeckFinishedCard(
                                            onRestart = { viewModel.resetReport() },
                                            onViewAllCards = { isGridViewOpen = true }
                                        )
                                    }
                                }
                            }

                            // Sliding parked drawer (absolute, like HTML left:86px width:440px)
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isDrawerOpen,
                                enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(380, easing = FastOutSlowInEasing)) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(380, easing = FastOutSlowInEasing)) + fadeOut(),
                                modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().width(340.dp)
                            ) {
                                AaosParkedDrawer(
                                    vehicleData = vehicleData,
                                    viewModel = viewModel,
                                    onClose = { soundManager.playClick(); isDrawerOpen = false }
                                )
                            }
                        }
                    }

                    ExactBottomDock()
                }

                // Toast
                androidx.compose.animation.AnimatedVisibility(
                    visible = showToast,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
                ) {
                    Surface(
                        color = Color(0xF2101620),
                        shape = RoundedCornerShape(980.dp),
                        border = BorderStroke(1.dp, C_Accent.copy(alpha = 0.5f)),
                        shadowElevation = 20.dp
                    ) {
                        Row(Modifier.padding(horizontal = 24.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("📱", fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(toastText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                CarStartupOverlay(visible = showStartupOverlay, onComplete = { showStartupOverlay = false })

                androidx.compose.animation.AnimatedVisibility(
                    visible = isGridViewOpen,
                    enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.96f, animationSpec = tween(250)),
                    exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.96f, animationSpec = tween(200))
                ) {
                    AllCardsGridView(
                        cards = if (allCards.isNotEmpty()) allCards else roastCards,
                        onClose = { soundManager.playClick(); isGridViewOpen = false },
                        onRestartDeck = { soundManager.playClick(); isGridViewOpen = false; viewModel.resetReport() },
                        onSelectCard = { c -> soundManager.playClick(); isGridViewOpen = false; viewModel.selectCardAndOpenDeck(c) }
                    )
                }
            }
        }
    }
}

// ── EXACT HEADER (64dp) ──
@Composable
private fun ExactHeader(currentIdx: Int, total: Int, onDiagnostic: () -> Unit) {
    var clock by remember { mutableStateOf("--:--") }
    LaunchedEffect(Unit) {
        while (true) {
            val f = SimpleDateFormat("h:mm a", Locale.US)
            clock = f.format(Date())
            delay(1000)
        }
    }
    val dateStr = remember { SimpleDateFormat("EEE, MMM d", Locale.US).format(Date()) }

    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp)
            .background(Color(0xB3090B10))
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp), color = Color(0xFF090B12),
                    border = BorderStroke(1.dp, C_Accent.copy(alpha = 0.4f)),
                    shadowElevation = 8.dp, modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo), contentDescription = "AuraDrive",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp))
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("AURA DRIVE", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text("TRIP TELEMETRY // CAN-BUS CONNECTED", color = C_Accent, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onDiagnostic, shape = RoundedCornerShape(980.dp),
                    color = C_Accent.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, C_Accent.copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("↻", color = C_Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Text("DIAGNOSTIC", color = C_Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(dateStr, color = C_Fg2, fontSize = 13.sp)
                Spacer(Modifier.width(12.dp))
                Text(clock, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.width(12.dp))
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.12f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), modifier = Modifier.size(34.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("YP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace) }
                }
            }
        }
        // Center counter + segments (absolute center like HTML)
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (currentIdx >= total) "ALL $total SWIPED" else "CARD ${currentIdx + 1} OF $total",
                color = C_Fg2, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace, letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(total) { i ->
                    val active = i == currentIdx
                    val done = i < currentIdx
                    Box(
                        Modifier.width(if (active) 44.dp else 32.dp).height(4.dp)
                            .background(
                                when { active -> Color.White; done -> Color.White.copy(alpha = 0.5f); else -> Color.White.copy(alpha = 0.15f) },
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
    }
}

// ── EXACT SWIPE STAGE ──
@Composable
fun AaosSwipeStage(
    cards: List<MainViewModel.RoastCard>,
    viewModel: MainViewModel,
    soundManager: SoundEffectsManager,
    isAudioActive: Boolean = true
) {
    ExactSwipeStage(cards, viewModel, soundManager, onShare = {})
}

@Composable
private fun ExactSwipeStage(
    cards: List<MainViewModel.RoastCard>,
    viewModel: MainViewModel,
    soundManager: SoundEffectsManager,
    onShare: () -> Unit
) {
    val density = LocalDensity.current
    var programmaticSwipeDirection by remember { mutableStateOf<Float?>(null) }
    var animatingCardId by remember { mutableStateOf<String?>(null) }
    var dragX by remember { mutableStateOf(0f) }
    val totalCards = 5
    val currentCardIndex = (totalCards - cards.size + 1).coerceIn(1, totalCards)

    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Apple Car square deck — single centered main card, no side bars
        Box(Modifier.size(440.dp), contentAlignment = Alignment.Center) {
            val visible = cards.take(5).reversed()
            visible.forEachIndexed { revIdx, card ->
                val depth = visible.size - 1 - revIdx
                val isTop = depth == 0
                val yOff = (14 * depth).dp
                val scaleV = when (depth) { 0 -> 1f; 1 -> 0.95f; 2 -> 0.90f; 3 -> 0.85f; else -> 0.80f }
                val alphaV = when (depth) { 0 -> 1f; 1 -> 0.92f; 2 -> 0.80f; 3 -> 0.65f; else -> 0.45f }
                key(card.id) {
                    SwipeableCard(
                        key = card.id,
                        programmaticSwipeDirection = if (isTop && animatingCardId == card.id) programmaticSwipeDirection else null,
                        onDragProgress = { dx, _ -> if (isTop) dragX = dx },
                        onSwipeLeft = {
                            animatingCardId = null; programmaticSwipeDirection = null; dragX = 0f
                            soundManager.playSwipeLeft(); viewModel.onCardSwiped(card)
                        },
                        onSwipeRight = {
                            animatingCardId = null; programmaticSwipeDirection = null; dragX = 0f
                            soundManager.playFahhh(); viewModel.onCardSwiped(card)
                        },
                        onSnapBack = { dragX = 0f; soundManager.playSnapBack() },
                        modifier = Modifier.size(400.dp)
                            .graphicsLayer {
                                scaleX = scaleV; scaleY = scaleV
                                translationY = with(density) { yOff.toPx() }
                            }.alpha(alphaV)
                    ) {
                        ExactCard(card, dragX = if (isTop) dragX else 0f)
                    }
                }
            }
            if (cards.isEmpty()) {
                DeckFinishedCard(onRestart = { viewModel.resetReport() }, onViewAllCards = {})
            }
        }

        // Controls dock (HTML: reject / reset / accept / share)
        Row(
            Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleSwipeBtn("💀", "reject") {
                val top = cards.firstOrNull()
                if (top != null && animatingCardId == null) {
                    animatingCardId = top.id; programmaticSwipeDirection = -1f
                    soundManager.playSwipeLeft()
                }
            }
            CircleSwipeBtn("↺", "reset") { soundManager.playSnapBack(); viewModel.resetReport() }
            CircleSwipeBtn("😭", "accept") {
                val top = cards.firstOrNull()
                if (top != null && animatingCardId == null) {
                    animatingCardId = top.id; programmaticSwipeDirection = 1f
                    soundManager.playFahhh()
                }
            }
            Surface(
                onClick = { soundManager.playClick(); onShare() },
                shape = RoundedCornerShape(980.dp),
                color = C_Accent.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, C_Accent.copy(alpha = 0.35f))
            ) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📱", fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("SHARE TO PHONE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            "⟵ SWIPE LEFT: IT AIN'T ME  |  SWIPE RIGHT: ZEEZ, YOU GOT ME (FAHHHH) ⟶",
            color = C_Muted, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp, modifier = Modifier.padding(top = 6.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CircleSwipeBtn(emoji: String, desc: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 20.sp) }
    }
}

// ── APPLE CAR SQUARE CARD 400x400 — clean type hierarchy ──
@Composable
fun AaosCardContent(card: MainViewModel.RoastCard, isCompact: Boolean = false) {
    ExactCard(card, 0f)
}

@Composable
private fun ExactCard(card: MainViewModel.RoastCard, dragX: Float) {
    val accent = getAccentColor(card.accentColorName)
    val threshold = 95f
    val prog = (kotlin.math.abs(dragX) / threshold).coerceIn(0f, 1f)

    Surface(
        color = Color(0xFF14171F), // Apple-style opaque chassis
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        shadowElevation = 28.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Apple gloss: soft top highlight + faint accent wash
            Box(
                Modifier.fillMaxWidth().height(140.dp).background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.07f), Color.Transparent)
                    )
                )
            )
            Box(
                Modifier.fillMaxWidth().height(120.dp).align(Alignment.TopCenter).background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.10f), Color.Transparent),
                        radius = 420f
                    )
                )
            )

            Column(
                Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: app-icon emoji tile + tinted score pill
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = accent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text(card.emoji, fontSize = 30.sp) }
                    }
                    val pillColor = if (card.scorePillPositive) C_Green else C_Red
                    if (card.scorePillText.isNotEmpty()) {
                        Surface(
                            color = pillColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(980.dp),
                            border = BorderStroke(1.dp, pillColor.copy(alpha = 0.35f))
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(6.dp).background(pillColor, CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    card.scorePillText, color = pillColor, fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                }

                // Hero copy — Apple tight tracking, balanced center
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        card.eyebrow.ifEmpty { "AURA TRIP" }.uppercase(), color = C_Accent, fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 2.4.sp,
                        textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        card.roast.trim().trim('"'), color = Color.White.copy(alpha = 0.96f),
                        fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, lineHeight = 32.sp,
                        letterSpacing = (-0.5).sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Metric pod — iOS grouped card
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                card.podMegaValue.ifEmpty { card.value }, color = accent,
                                fontSize = 52.sp, fontWeight = FontWeight.Bold,
                                lineHeight = 52.sp, letterSpacing = (-1.5).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                (card.podSubLabel.ifEmpty { card.label }).uppercase(), color = C_Fg2,
                                fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.6.sp,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = accent.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(980.dp),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.30f))
                            ) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(6.dp).background(accent, CircleShape))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        (card.statusText.ifEmpty { "NOMINAL" }).uppercase(),
                                        color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Box(Modifier.width(110.dp).height(7.dp).background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(4.dp))) {
                                Box(Modifier.fillMaxHeight().fillMaxWidth(card.barFillPercent.coerceIn(0.05f, 1f)).background(accent, RoundedCornerShape(4.dp)))
                            }
                        }
                    }
                }

                // Footer — quiet mono, high legibility
                Column {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            card.footerLeft.uppercase(), color = C_Fg2, fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            card.footerRight.uppercase(), color = accent, fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Stamps (HTML: left reject / right accept)
            if (dragX < -10) {
                Surface(
                    color = C_Red.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, C_Red),
                    modifier = Modifier.align(Alignment.TopEnd).graphicsLayer { rotationZ = 12f; alpha = prog }
                ) {
                    Text("💀 IT AIN'T ME", color = C_Red, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
            if (dragX > 10) {
                Surface(
                    color = C_Accent.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, C_Accent),
                    modifier = Modifier.align(Alignment.TopStart).graphicsLayer { rotationZ = -12f; alpha = prog }
                ) {
                    Text("😭 ZEEZ, YOU GOT ME", color = C_Accent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), maxLines = 1)
                }
            }
        }
    }
}

// ── EXACT BOTTOM DOCK (72dp) ──
@Composable
private fun ExactBottomDock() {
    var dTemp by remember { mutableStateOf(21.5f) }
    var pTemp by remember { mutableStateOf(22.0f) }
    Box(
        Modifier.fillMaxWidth().height(72.dp).background(Color(0xD9090B11))
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ClimateUnit(
                temp = dTemp, label = "DRIVER AUTO",
                onDown = { dTemp = (dTemp - 0.5f).coerceAtLeast(16f) },
                onUp = { dTemp = (dTemp + 0.5f).coerceAtMost(28f) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                DockIcon("⊞", false) {}
                Surface(shape = RoundedCornerShape(12.dp), color = C_Accent.copy(alpha = 0.15f), border = BorderStroke(1.dp, C_Accent), modifier = Modifier.size(44.dp)) {
                    Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "Aura", modifier = Modifier.fillMaxSize().padding(4.dp).clip(RoundedCornerShape(8.dp)))
                }
                DockIcon("🗺️", false) {}
                DockIcon("🎵", false) {}
                DockIcon("⚙️", false) {}
            }
            ClimateUnit(
                temp = pTemp, label = "PASSENGER SYNC",
                onDown = { pTemp = (pTemp - 0.5f).coerceAtLeast(16f) },
                onUp = { pTemp = (pTemp + 0.5f).coerceAtMost(28f) }
            )
        }
        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f)))
    }
}

@Composable
private fun ClimateUnit(temp: Float, label: String, onDown: () -> Unit, onUp: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(onClick = onDown, shape = CircleShape, color = Color.White.copy(alpha = 0.05f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("−", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${String.format(Locale.US, "%.1f", temp)}°", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = C_Fg2, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Surface(onClick = onUp, shape = CircleShape, color = Color.White.copy(alpha = 0.05f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun DockIcon(text: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = RoundedCornerShape(12.dp),
        color = if (active) C_Accent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, if (active) C_Accent else Color.White.copy(alpha = 0.10f)),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) { Text(text, fontSize = 19.sp, color = C_Fg2) }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// EXACT PARKED DRAWER — telemetry grid + auto slideshow (HTML parity)
// ══════════════════════════════════════════════════════════════════════════

data class TeleCard(
    val emoji: String, val label: String, val value: String, val sub: String,
    val valueColor: Color = Color.White, val accentColor: Color = Color(0xFF2997FF)
)

@Composable
fun AnimatedDrawerCloseButton(onClose: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val s by animateFloatAsState(if (entered) 1f else 0.3f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "s")
    val r by animateFloatAsState(if (entered) 0f else -180f, tween(380), label = "r")
    val ps by animateFloatAsState(if (isPressed) 0.82f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "ps")
    Surface(
        onClick = onClose, interactionSource = interactionSource, shape = CircleShape,
        color = if (isPressed) C_Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, if (isPressed) C_Red.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.16f)),
        modifier = modifier.size(36.dp).graphicsLayer { scaleX = s * ps; scaleY = s * ps; rotationZ = r + (if (isPressed) 90f else 0f) }
    ) {
        Box(contentAlignment = Alignment.Center) { Text("✕", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun AaosParkedDrawer(
    vehicleData: com.auradrive.aaos.model.VehicleData,
    viewModel: MainViewModel? = null,
    onClose: () -> Unit
) {
    var slide by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(4000); slide = (slide + 1) % 3 } }

    Surface(
        color = Color(0xFF0D1017),
        shadowElevation = 32.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF090B12), border = BorderStroke(1.dp, C_Accent.copy(alpha = 0.4f)), modifier = Modifier.size(34.dp)) {
                        Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "Aura", modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("VEHICLE STATUS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Text("PARK TELEMETRY // SYSTEM 10.4", color = C_Accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                    }
                }
                AnimatedDrawerCloseButton(onClose = onClose)
            }

            // 2-col grid exact HTML
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeleTile("Battery", "⚡", "78%", "Estimated 312 km", C_Green, C_Green, Modifier.weight(1f))
                    TeleTile("Gear", "🕹️", "Park", "Brake Locked", C_Accent, C_Accent, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeleTile("Odometer", "🛣️", "12,430", "km driven", Color.White, C_Accent, Modifier.weight(1f))
                    TeleTile("Cabin Temp", "🌡️", "${vehicleData.cabinTemperatureCelsius.toInt().takeIf { it > 0 } ?: 22}°C", "Dual-Zone Clima", Color.White, C_Accent, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeleTile("Doors", "🚗", "Closed", "All Latches Secured", C_Green, C_Green, Modifier.weight(1f))
                    TeleTile("Headlights", "💡", "Off", "Auto DRL Active", Color.White, C_Fg2, Modifier.weight(1f))
                }
                Surface(color = Color(0xFF141822), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TYRE PRESSURE  🛞", color = C_Fg2, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text("2.4 bar  (All 4 Wheels Nominal)", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("FL: 2.4 | FR: 2.4 | RL: 2.4 | RR: 2.4 bar", color = C_Accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Slideshow
            Surface(
                color = Color.Transparent, shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, C_Accent.copy(alpha = 0.25f)), modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF151A26), Color(0xFF0D121C)))).padding(14.dp)) {
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("📊 VEHICLE DIAGNOSTICS CAROUSEL", color = C_Cyan, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { i ->
                                    Box(Modifier.width(if (i == slide) 26.dp else 18.dp).height(3.dp).background(if (i == slide) C_Accent else Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        AnimatedContent(targetState = slide, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "slide") { s ->
                            when (s) {
                                0 -> SlideBody("Powertrain & Battery Health", "High voltage pack at optimal operating temp (26°C). Zero cell degradation detected over last 2,400 km.", "99.4% SOH • 180 kW Peak • Regen: 4.8 kWh", C_Green)
                                1 -> SlideBody("Chassis & Braking Systems", "Four-corner regenerative blend active. Brembo ceramic pads at 92% life remaining. Tyre tread depth 6.8 mm.", "Brakes: 92% • Alignment: 0.0° • ABS: Ready", C_Cyan)
                                else -> SlideBody("Air Filtration & Pre-Conditioning", "HEPA cabin filter efficiency at 98%. Scheduled departure pre-conditioning ready for 08:30 AM tomorrow.", "AQI: 12 (Pristine) • Ionizer: ON • Cabin 22°C", C_Purple)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeleTile(label: String, emoji: String, value: String, sub: String, valueColor: Color, accent: Color, modifier: Modifier = Modifier) {
    Surface(color = Color(0xFF141822).copy(alpha = 0.85f), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)), modifier = modifier) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label.uppercase(), color = C_Fg2, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(emoji, fontSize = 12.sp)
            }
            Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun SlideBody(title: String, desc: String, metrics: String, color: Color) {
    Column {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(desc, color = C_Fg2, fontSize = 11.sp, lineHeight = 15.sp)
        Spacer(Modifier.height(6.dp))
        Text(metrics, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

// ═══════════ Finished / Grid / Intro / Legacy ═══════════

@Composable
fun DeckFinishedCard(onRestart: () -> Unit, onViewAllCards: () -> Unit) {
    val scale = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 300f)) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            color = C_Card, shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.5.dp, C_Accent.copy(alpha = 0.3f)),
            shadowElevation = 24.dp,
            modifier = Modifier.size(400.dp).scale(scale.value)
        ) {
            Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🏁", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("TRIP TELEMETRY LOGGED", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text("All 5 telemetry moments reviewed and recorded to your AURA profile.", color = C_Fg2, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(onClick = onRestart, shape = RoundedCornerShape(980.dp), color = Color.White.copy(alpha = 0.1f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))) {
                        Text("↺ REVIEW AGAIN", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
                    }
                    Surface(onClick = onViewAllCards, shape = RoundedCornerShape(980.dp), color = C_Accent.copy(alpha = 0.12f), border = BorderStroke(1.dp, C_Accent.copy(alpha = 0.35f))) {
                        Text("📱 SHARE TO PHONE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AllCardsGridView(
    cards: List<MainViewModel.RoastCard>,
    onClose: () -> Unit, onRestartDeck: () -> Unit, onSelectCard: (MainViewModel.RoastCard) -> Unit
) {
    Box(Modifier.fillMaxSize().background(C_Bg)) {
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = onClose, shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.08f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))) {
                    Text("← BACK TO DECK", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
                Text("ALL TRIP TELEMETRY CARDS • TAP TO INSPECT", color = C_Accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Surface(onClick = onRestartDeck, shape = RoundedCornerShape(12.dp), color = C_Accent.copy(alpha = 0.18f), border = BorderStroke(1.dp, C_Accent.copy(alpha = 0.5f))) {
                    Text("↺ RESTART DECK", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                val row1 = cards.take(3)
                val row2 = cards.drop(3).take(2)
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row1.forEach { c -> CompactGridCard(card = c, onClick = { onSelectCard(c) }) }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row2.forEach { c -> CompactGridCard(card = c, onClick = { onSelectCard(c) }) }
                }
            }
        }
    }
}

@Composable
fun CompactGridCard(card: MainViewModel.RoastCard, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val accent = getAccentColor(card.accentColorName)
    Surface(
        onClick = onClick, color = C_Card, shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.35f)),
        shadowElevation = 12.dp, modifier = modifier.size(175.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(card.emoji, fontSize = 20.sp)
                if (card.scorePillText.isNotEmpty()) {
                    val pc = if (card.scorePillPositive) C_Green else C_Red
                    Surface(color = pc.copy(alpha = 0.15f), shape = RoundedCornerShape(980.dp), border = BorderStroke(1.dp, pc.copy(alpha = 0.4f))) {
                        Text(card.scorePillText.take(12), color = pc, fontSize = 7.5.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(card.eyebrow, color = C_Accent, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                Text("\"${card.roast}\"", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Surface(color = Color.White.copy(alpha = 0.04f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(7.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(card.podMegaValue.ifEmpty { card.value }, color = accent, fontSize = 15.sp, fontWeight = FontWeight.Black, lineHeight = 15.sp)
                        Text(card.podSubLabel.ifEmpty { card.label }, color = C_Muted, fontSize = 6.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Box(Modifier.width(34.dp).height(3.dp).background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(2.dp))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(card.barFillPercent.coerceIn(0.05f, 1f)).background(accent, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }
    }
}

@Composable
fun IntroView(session: DrivingSession, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "Aura", modifier = Modifier.size(120.dp).clip(RoundedCornerShape(20.dp)))
        Spacer(Modifier.height(16.dp))
        Text("I SAW\nEVERYTHING.", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = 48.sp)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Text("REVEAL RECAP", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun FinalReportView(sessions: List<DrivingSession>, viewModel: MainViewModel, soundManager: SoundEffectsManager? = null) {
    DeckFinishedCard(onRestart = { viewModel.resetReport() }, onViewAllCards = {})
}

@Composable
fun SubtleTopHeader(currentCardIndex: Int, totalCards: Int, isAudioActive: Boolean, onToggleAudio: () -> Unit, onParkedClick: () -> Unit, onShare: () -> Unit) {
}

@Composable
fun SubtleSwipingStage(cards: List<MainViewModel.RoastCard>, viewModel: MainViewModel, soundManager: SoundEffectsManager, isAudioActive: Boolean) {
    ExactSwipeStage(cards, viewModel, soundManager, onShare = {})
}

@Composable
fun SubtleCardContent(card: MainViewModel.RoastCard) {
    AaosCardContent(card)
}

data class TileData(val emoji: String, val value: String, val label: String)

@Composable
fun SubtleParkedDrawer(vehicleData: com.auradrive.aaos.model.VehicleData, onClose: () -> Unit) {
    AaosParkedDrawer(vehicleData, null, onClose)
}

@Composable
fun MemeCopilotWidget(card: MainViewModel.RoastCard?, currentIndex: Int, totalCount: Int, modifier: Modifier = Modifier) {
}

package com.auradrive.aaos.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auradrive.aaos.sound.SoundEffectsManager
import com.auradrive.aaos.ui.MainViewModel
import com.auradrive.aaos.ui.screens.SubtleCardContent
import com.auradrive.aaos.ui.screens.SubtleSwipingStage
import com.auradrive.aaos.ui.theme.*

/**
 * On-screen Post-Trip Cards Widget.
 * Automatically appears after a trip completes (vehicle parked), allowing the driver
 * to immediately view and navigate all generated trip roast and rating cards directly.
 */
@Composable
fun PostTripCardsWidget(
    cards: List<MainViewModel.RoastCard>,
    viewModel: MainViewModel,
    soundManager: SoundEffectsManager,
    isAudioActive: Boolean,
    modifier: Modifier = Modifier
) {
    var isGridView by remember { mutableStateOf(false) }
    val topCard = cards.firstOrNull()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Post-Trip Widget Bar: Trip Status, Aura Delta & View Mode Toggle
        Surface(
            color = Color(0x9913151D),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trip Status Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF30D158).copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFF30D158)),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✓", color = Color(0xFF30D158), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "TRIP RECAP CARDS",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = AuraCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(980.dp),
                                border = BorderStroke(1.dp, AuraCyan.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    "${cards.size} CARDS",
                                    color = AuraCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "your car is judging you",
                                color = AuraMuted,
                                fontFamily = AuraFancyFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AnimatedIosEmoji("👀", size = 14.dp)
                        }
                    }
                }

                // View Mode Toggle (Stack vs All-Cards Grid)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = {
                            soundManager.playClick()
                            isGridView = false
                        },
                        color = if (!isGridView) AuraCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(980.dp),
                        border = BorderStroke(
                            1.dp,
                            if (!isGridView) AuraCyan else Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🃏", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Stack",
                                color = if (!isGridView) AuraCyan else AuraMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            soundManager.playClick()
                            isGridView = true
                        },
                        color = if (isGridView) AuraCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(980.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isGridView) AuraCyan else Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🗂️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "View All Cards",
                                color = if (isGridView) AuraCyan else AuraMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Direct Card Selector Strip (Quick-jump pills across all cards)
        if (cards.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(cards, key = { it.id }) { card ->
                    val isTop = card.id == topCard?.id
                    Surface(
                        onClick = {
                            soundManager.playClick()
                            viewModel.bringCardToFront(card)
                        },
                        color = if (isTop) Color(0x3300D2FF) else Color(0xFF161922),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isTop) AuraCyan else Color.White.copy(alpha = 0.10f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(card.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    card.label,
                                    color = if (isTop) AuraCyan else AuraMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    card.value,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Main Stage Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isGridView,
                transitionSpec = {
                    fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(280))
                },
                label = "CardViewModeTransition"
            ) { gridMode ->
                if (gridMode) {
                    // Direct All-Cards Grid View: See all cards directly side-by-side!
                    DirectAllCardsGridView(
                        cards = cards,
                        viewModel = viewModel,
                        soundManager = soundManager
                    )
                } else {
                    // Interactive Stack Stage with aerodynamic effects & EV audio
                    SubtleSwipingStage(
                        cards = cards,
                        viewModel = viewModel,
                        soundManager = soundManager,
                        isAudioActive = isAudioActive
                    )
                }
            }
        }
    }
}

/**
 * Direct All-Cards Grid View:
 * Allows the user to directly see and browse all cards on screen simultaneously.
 */
@Composable
private fun DirectAllCardsGridView(
    cards: List<MainViewModel.RoastCard>,
    viewModel: MainViewModel,
    soundManager: SoundEffectsManager
) {
    if (cards.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("All cards reviewed!", color = AuraMuted, fontSize = 16.sp)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            DirectCardMiniature(
                card = card,
                onReactAgree = {
                    soundManager.playSwipeRight()
                    viewModel.onCardSwiped(card)
                },
                onReactDisagree = {
                    soundManager.playSwipeLeft()
                    viewModel.onCardSwiped(card)
                }
            )
        }
    }
}

@Composable
private fun DirectCardMiniature(
    card: MainViewModel.RoastCard,
    onReactAgree: () -> Unit,
    onReactDisagree: () -> Unit
) {
    Surface(
        color = Color(0xFF13151D),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Label Pill
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(980.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Text(
                    text = card.label,
                    color = AuraMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Metric
            Text(card.emoji, fontSize = 32.sp)
            Text(
                card.value,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Text(
                card.supportingStat,
                color = Color(0xFF30D158),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Roast Quote
            Surface(
                color = Color(0xFF1A1D27),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "\"${card.roast}\"",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direct Quick Action Reactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onReactDisagree,
                    color = Color(0x22FF453A),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, Color(0xFFFF453A).copy(alpha = 0.50f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedIosEmoji("💀", size = 20.dp)
                        Text(
                            "it aint me",
                            color = Color(0xFFFF6961),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                Surface(
                    onClick = onReactAgree,
                    color = Color(0x222997FF),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, Color(0xFF2997FF).copy(alpha = 0.50f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedIosEmoji("😭", size = 20.dp)
                        Text(
                            "zeez yo got me",
                            color = Color(0xFF64D2FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }
        }
    }
}

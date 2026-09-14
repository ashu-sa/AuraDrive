package com.auradrive.aaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auradrive.aaos.ui.MainViewModel
import com.auradrive.aaos.ui.theme.*

@Composable
fun DrivingScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val auraScore by viewModel.auraScore.collectAsState()
    val vehicleData by viewModel.vehicleData.collectAsState()

    val bgGradient = remember {
        Brush.radialGradient(
            colors = listOf(AuraCyan.copy(alpha = 0.2f), AuraDark, Color.Black),
            radius = 1200f
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(32.dp)
    ) {
        // Top AAOS Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gear Badge: Drive D
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF30D158),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("D", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("DRIVING", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            // Quick Action: End Trip & View Cards Widget
            Surface(
                onClick = { viewModel.finishTripAndPark() },
                color = Color(0x33FF453A),
                shape = RoundedCornerShape(980.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF453A).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🅿️", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "END TRIP & VIEW CARDS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Battery & Cabin Telemetry
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("🔋 ${vehicleData.batteryLevelPercent.toInt()}%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("🛋️ ${vehicleData.cabinTemperatureCelsius.toInt()}°C", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Center Hero: Dynamic Aura Display
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "⚡ ${auraScore.currentAura}",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 96.sp,
                letterSpacing = (-3).sp
            )

            Text("CURRENT AURA", color = AuraMuted, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // Trip Aura Delta Pill
            Surface(
                color = if (auraScore.tripAuraChange >= 0) AuraPillGreenBg else AuraPillRedBg,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (auraScore.tripAuraChange >= 0) AuraPillGreenBorder else AuraPillRedBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (auraScore.tripAuraChange >= 0) "🟢 LOCKED IN" else "⚠️ AURA PENALIZED",
                        color = if (auraScore.tripAuraChange >= 0) AuraGreen else AuraRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${if (auraScore.tripAuraChange >= 0) "+" else ""}${auraScore.tripAuraChange}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Bottom Telemetry Gauge (Speed)
        Row(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "${(vehicleData.speedMps * 3.6f).toInt()}",
                color = AuraCyan,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("km/h", color = AuraMuted, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

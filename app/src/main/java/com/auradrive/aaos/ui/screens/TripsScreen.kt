package com.auradrive.aaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.auradrive.aaos.ui.theme.AuraDark

@Composable
fun TripsScreen() {
    Box(modifier = Modifier.fillMaxSize().background(AuraDark), contentAlignment = Alignment.Center) {
        Text("TRIP HISTORY", color = Color.White)
    }
}

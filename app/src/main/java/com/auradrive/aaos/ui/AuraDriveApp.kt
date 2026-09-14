package com.auradrive.aaos.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.auradrive.aaos.driving.DrivingState
import com.auradrive.aaos.ui.screens.*

@Composable
fun AuraDriveApp(viewModel: MainViewModel) {
    val drivingState by viewModel.drivingState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (drivingState == DrivingState.DRIVING) {
            DrivingScreen(viewModel, modifier = Modifier.fillMaxSize())
        } else {
            // AAOS Cockpit Infotainment Display
            DashboardScreen(viewModel)
        }
    }
}

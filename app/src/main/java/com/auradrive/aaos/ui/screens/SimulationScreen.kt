package com.auradrive.aaos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.auradrive.aaos.model.VehicleGear
import com.auradrive.aaos.ui.MainViewModel
import com.auradrive.aaos.vehicle.MockVehicleDataSource

@Composable
fun SimulationScreen(
    mockSource: MockVehicleDataSource,
    viewModel: MainViewModel? = null
) {
    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "TRIP SIMULATION PRESETS",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = androidx.compose.ui.graphics.Color.White
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel?.simulateAggressiveTrip() },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFFF3B30)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("🔥 SIMULATE AGGRESSIVE TRIP (145 KM/H, 4× BRAKE, 16°C)", maxLines = 2)
            }
            Button(
                onClick = { viewModel?.simulateChillTrip() },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF30D158)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("🌱 SIMULATE CHILL TRIP (55 KM/H, 0× BRAKE, 22°C)", maxLines = 2)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "MANUAL SENSOR OVERRIDES",
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = androidx.compose.ui.graphics.Color.Gray
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel?.simulateGear(VehicleGear.PARK) ?: mockSource.updateGear(VehicleGear.PARK)
            }) { Text("GEAR: PARK") }
            Button(onClick = {
                viewModel?.simulateGear(VehicleGear.DRIVE) ?: mockSource.updateGear(VehicleGear.DRIVE)
            }) { Text("GEAR: DRIVE") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel?.simulateSpeed(60f) ?: mockSource.updateSpeed(60f / 3.6f)
            }) { Text("SPEED: 60 KM/H") }
            Button(onClick = {
                viewModel?.simulateSpeed(140f) ?: mockSource.updateSpeed(140f / 3.6f)
            }) { Text("SPEED: 140 KM/H (ROAST)") }
            Button(onClick = {
                viewModel?.simulateSpeed(0f) ?: mockSource.updateSpeed(0f)
            }) { Text("SPEED: 0") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel?.simulateHardAccel() ?: mockSource.triggerHardAcceleration()
            }) { Text("💨 ACCEL STOMP") }
            Button(onClick = {
                viewModel?.simulateHardBrake() ?: mockSource.triggerHardBraking()
            }) { Text("🛑 HARD BRAKE") }
            Button(onClick = {
                viewModel?.simulateHvacTemp(16f) ?: mockSource.updateCabinTemperature(16f)
            }) { Text("🥶 16°C SUB-ZERO") }
            Button(onClick = {
                viewModel?.simulateHvacTemp(28f) ?: mockSource.updateCabinTemperature(28f)
            }) { Text("🔥 28°C SAUNA") }
        }
    }
}

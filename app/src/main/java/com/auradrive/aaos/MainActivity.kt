package com.auradrive.aaos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.auradrive.aaos.aura.AuraScoreEngine
import com.auradrive.aaos.data.local.db.AuraDatabase
import com.auradrive.aaos.data.repository.AuraRepository
import com.auradrive.aaos.driving.DrivingEventDetector
import com.auradrive.aaos.driving.DrivingSessionManagerImpl
import com.auradrive.aaos.driving.DrivingStateManagerImpl
import com.auradrive.aaos.roast.RoastEngineImpl
import com.auradrive.aaos.roast.RoastRepository
import com.auradrive.aaos.ui.AuraDriveApp
import com.auradrive.aaos.ui.MainViewModel
import com.auradrive.aaos.ui.theme.AuraDriveTheme
import com.auradrive.aaos.vehicle.AaosVehicleDataSource
import com.auradrive.aaos.vehicle.MockVehicleDataSource
import com.auradrive.aaos.voice.VoiceRoastManagerImpl
import com.google.gson.Gson
import kotlinx.coroutines.launch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.auradrive.aaos.service.CarTelemetryService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject lateinit var tripRepository: com.auradrive.aaos.data.repository.TripRepository
    @javax.inject.Inject lateinit var tripAggregator: com.auradrive.aaos.telemetry.TripAggregator
    
    private lateinit var aaosVehicleDataSource: AaosVehicleDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.statusBarColor = android.graphics.Color.parseColor("#07090E")
        window.navigationBarColor = android.graphics.Color.parseColor("#07090E")
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Manual DI for prototype
        val db = Room.databaseBuilder(applicationContext, AuraDatabase::class.java, "aura-db")
            .fallbackToDestructiveMigration()
            .build()
        val gson = Gson()
        val auraRepository = AuraRepository(db.sessionDao(), gson)
        
        // Initialize Real AAOS Data Source
        aaosVehicleDataSource = AaosVehicleDataSource(this)
        
        // We still keep Mock for the developer SimulationScreen controls
        val mockVehicleDataSource = MockVehicleDataSource()
        
        // Set the active data source to AAOS (Real Car)
        val activeDataSource = aaosVehicleDataSource

        val drivingStateManager = DrivingStateManagerImpl(activeDataSource, lifecycleScope)
        val sessionManager = DrivingSessionManagerImpl(auraRepository)
        val thresholds = com.auradrive.aaos.driving.DrivingThresholds()
        
        // Detectors now listen to the real vehicle
        val eventDetector = DrivingEventDetector(activeDataSource, thresholds, lifecycleScope)
        
        val roastRepository = RoastRepository()
        val roastEngine = RoastEngineImpl(roastRepository)
        val voiceManager = VoiceRoastManagerImpl(this)
        val auraEngine = AuraScoreEngine()

        val factory = viewModelFactory {
            initializer {
                MainViewModel(
                    mockVehicleDataSource, // Still pass mock for the Simulation Screen to avoid crashes
                    drivingStateManager,
                    sessionManager,
                    eventDetector,
                    roastEngine,
                    voiceManager,
                    auraEngine,
                    auraRepository,
                    realVehicleDataSource = activeDataSource,
                    tripRepository = tripRepository,
                    tripAggregator = tripAggregator
                )
            }
        }

        // Start headless CarTelemetryService
        try {
            startForegroundService(Intent(this, CarTelemetryService::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Start listening to the car
        lifecycleScope.launch {
            aaosVehicleDataSource.start()
        }

        setContent {
            val viewModel: MainViewModel = viewModel(factory = factory)
            mainViewModel = viewModel
            AuraDriveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuraDriveApp(viewModel)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("com.auradrive.aaos.SIMULATE_AGGRESSIVE_TRIP")
            addAction("com.auradrive.aaos.SIMULATE_CHILL_TRIP")
        }
        ContextCompat.registerReceiver(this, simulationReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    private var mainViewModel: MainViewModel? = null

    private val simulationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.auradrive.aaos.SIMULATE_AGGRESSIVE_TRIP" -> {
                    mainViewModel?.simulateAggressiveTrip()
                }
                "com.auradrive.aaos.SIMULATE_CHILL_TRIP" -> {
                    mainViewModel?.simulateChillTrip()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(simulationReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
        // Clean up Car Service connection
        lifecycleScope.launch {
            aaosVehicleDataSource.stop()
        }
    }
}

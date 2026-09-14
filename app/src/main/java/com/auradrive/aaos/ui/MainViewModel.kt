package com.auradrive.aaos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auradrive.aaos.aura.AuraScoreEngine
import com.auradrive.aaos.data.local.entity.TripEntity
import com.auradrive.aaos.data.repository.AuraRepository
import com.auradrive.aaos.data.repository.TripRepository
import com.auradrive.aaos.driving.DrivingEventDetector
import com.auradrive.aaos.driving.DrivingSessionManager
import com.auradrive.aaos.driving.DrivingState
import com.auradrive.aaos.driving.DrivingStateManager
import com.auradrive.aaos.model.DrivingEvent
import com.auradrive.aaos.model.VehicleData
import com.auradrive.aaos.model.VehicleGear
import com.auradrive.aaos.roast.ParkedRoastState
import com.auradrive.aaos.roast.RoastEngine
import com.auradrive.aaos.telemetry.TripAggregator
import com.auradrive.aaos.vehicle.MockVehicleDataSource
import com.auradrive.aaos.vehicle.VehicleDataSource
import com.auradrive.aaos.voice.VoiceRoastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class MainViewModel(
    val mockSource: MockVehicleDataSource, 
    private val drivingStateManager: DrivingStateManager,
    private val sessionManager: DrivingSessionManager,
    private val eventDetector: DrivingEventDetector,
    private val roastEngine: RoastEngine,
    private val voiceManager: VoiceRoastManager,
    private val auraEngine: AuraScoreEngine,
    private val auraRepository: AuraRepository,
    val realVehicleDataSource: VehicleDataSource? = null,
    val tripRepository: TripRepository? = null,
    val tripAggregator: TripAggregator? = null
) : ViewModel() {

    val drivingState = drivingStateManager.currentState
    val auraScore = auraEngine.score
    val currentSession = sessionManager.currentSession
    val allSessions = auraRepository.allSessions

    // Merge real car telemetry from VHAL with mock controls so both work seamlessly
    val vehicleData: StateFlow<VehicleData> = if (realVehicleDataSource != null) {
        merge(realVehicleDataSource.vehicleData, mockSource.vehicleData).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            VehicleData()
        )
    } else {
        mockSource.vehicleData.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            VehicleData()
        )
    }

    private val _parkedRoastState = MutableStateFlow<ParkedRoastState>(ParkedRoastState.Idle)
    val parkedRoastState: StateFlow<ParkedRoastState> = _parkedRoastState.asStateFlow()

    private val _roastCards = MutableStateFlow<List<RoastCard>>(emptyList())
    val roastCards = _roastCards.asStateFlow()

    private val _allCards = MutableStateFlow<List<RoastCard>>(emptyList())
    val allCards: StateFlow<List<RoastCard>> = _allCards.asStateFlow()

    data class RoastCard(
        val id: String = UUID.randomUUID().toString(),
        val emoji: String,
        val value: String,
        val label: String,
        val roast: String,
        val supportingStat: String,
        val type: RoastCardType,
        val eyebrow: String = "",
        val podMegaValue: String = "",
        val podSubLabel: String = "",
        val statusText: String = "",
        val barFillPercent: Float = 0f,
        val accentColorName: String = "green",  // green, amber, red, cyan, blue
        val footerLeft: String = "",
        val footerRight: String = "",
        val scorePillText: String = "",
        val scorePillPositive: Boolean = true,
        val memeTitle: String = "",
        val memeQuote: String = ""
    )

    enum class RoastCardType {
        SCORE, EVENT, PERSONALITY
    }

    init {
        // Observe real persisted trips from Room
        tripRepository?.getLatestTrip()?.onEach { latestTrip ->
            if (latestTrip != null && drivingState.value != DrivingState.DRIVING) {
                val dynamicCards = buildRoastCardsFromTrip(latestTrip)
                _allCards.value = dynamicCards
                _roastCards.value = dynamicCards
                _parkedRoastState.value = ParkedRoastState.Swiping
            }
        }?.launchIn(viewModelScope)

        // Seed initial cards from live car data if no trips exist yet
        if (_roastCards.value.isEmpty()) {
            buildInitialCardsFromLiveTelemetry()
        }

        // Tie real events to recorders and roasters
        eventDetector.events.onEach { event ->
            if (drivingState.value == DrivingState.DRIVING) {
                sessionManager.recordEvent(event)
                auraEngine.onEvent(event)
                
                // Voice roast during live driving
                val roast = roastEngine.generateRoast(
                    event,
                    currentSession.value?.personality ?: com.auradrive.aaos.model.DriverPersonality.CHILL_DRIVER
                )
                roast?.let { voiceManager.speak(it) }
            }
        }.launchIn(viewModelScope)

        // Monitor state transitions
        drivingState.onEach { state ->
            when (state) {
                DrivingState.DRIVING -> {
                    sessionManager.startSession()
                    auraEngine.resetTrip()
                    _parkedRoastState.value = ParkedRoastState.Idle
                    _roastCards.value = emptyList()
                }
                DrivingState.PARKED -> {
                    val session = currentSession.value
                    if (session != null) {
                        sessionManager.endSession()
                        prepareRoastCards(session)
                        _parkedRoastState.value = ParkedRoastState.Swiping
                    }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    private fun buildInitialCardsFromLiveTelemetry() {
        val currentV = vehicleData.value
        val speedKmh = (currentV.speedMps * 3.6f).toInt()
        val tempCelsius = if (currentV.cabinTemperatureCelsius > 0f) currentV.cabinTemperatureCelsius else 21f
        val battery = currentV.batteryLevelPercent.toInt().coerceAtLeast(1)

        val cards = listOf(
            RoastCard(
                emoji = "🚗",
                value = "100",
                label = "AURA SCORE",
                roast = "Parked and ready. Put it in DRIVE to start your roasted trip.",
                supportingStat = "Fresh trip pending",
                type = RoastCardType.SCORE,
                eyebrow = "AURA TRIP MONITOR",
                podMegaValue = "100",
                podSubLabel = "INITIAL DRIVER AURA",
                statusText = "PARKED & READY",
                barFillPercent = 1.0f,
                accentColorName = "green",
                footerLeft = "GEAR: PARKED",
                footerRight = "CAN-BUS ONLINE",
                scorePillText = "READY",
                scorePillPositive = true,
                memeTitle = "Hand on Shoulder",
                memeQuote = "\"Bro...\" I'm genuinely proud of you."
            ),
            RoastCard(
                emoji = "🛑",
                value = "0×",
                label = "BRAKES",
                roast = "No hard braking recorded yet. Keep your brake pads happy.",
                supportingStat = "0 Hard Brakes",
                type = RoastCardType.EVENT,
                eyebrow = "DECELERATION MONITOR",
                podMegaValue = "0×",
                podSubLabel = "HARD BRAKE EVENTS",
                statusText = "OPTIMAL",
                barFillPercent = 0.05f,
                accentColorName = "green",
                footerLeft = "THRESHOLD: >70% PEDAL",
                footerRight = "REGEN READY",
                scorePillText = "+5 AURA",
                scorePillPositive = true,
                memeTitle = "Sgt. Doakes Stare",
                memeQuote = "\"SURPRISE, MOTHERF***ER\" I saw that brake check from orbit."
            ),
            RoastCard(
                emoji = "⚡",
                value = "0×",
                label = "ACCEL",
                roast = "Waiting for throttle input. Resist the urge to drag race off the line.",
                supportingStat = "Max speed 0 km/h",
                type = RoastCardType.EVENT,
                eyebrow = "THROTTLE TELEMETRY",
                podMegaValue = "0×",
                podSubLabel = "RAPID LAUNCHES",
                statusText = "IDLE",
                barFillPercent = 0.05f,
                accentColorName = "green",
                footerLeft = "CURRENT: $speedKmh KM/H",
                footerRight = "ECO MODE",
                scorePillText = "NOMINAL",
                scorePillPositive = true,
                memeTitle = "Max Verstappen Radio",
                memeQuote = "\"SIMPLY LOVELY\" Bro think he in Q3 in Monza."
            ),
            RoastCard(
                emoji = if (tempCelsius <= 18f) "❄️" else "🛋️",
                value = "${String.format(Locale.US, "%.1f", tempCelsius)}°C",
                label = "CABIN TEMP",
                roast = if (tempCelsius <= 17.5f) "Sub-zero climate detected. Polar expedition mode." else "Cabin set to a reasonable temperature.",
                supportingStat = "${String.format(Locale.US, "%.1f", tempCelsius)}°C Active",
                type = RoastCardType.EVENT,
                eyebrow = "HVAC & CABIN CLIMATE",
                podMegaValue = "${String.format(Locale.US, "%.1f", tempCelsius)}°C",
                podSubLabel = "CABIN TEMPERATURE",
                statusText = "AC SENSOR ACTIVE",
                barFillPercent = ((tempCelsius - 15f) / 15f).coerceIn(0.1f, 1f),
                accentColorName = if (tempCelsius <= 17.5f) "cyan" else "green",
                footerLeft = "SETTING: ${String.format(Locale.US, "%.1f", tempCelsius)}°C",
                footerRight = "VHAL SYNCED",
                scorePillText = "ACTIVE",
                scorePillPositive = true,
                memeTitle = "Frozen in Snow",
                memeQuote = "\"I'M NOT COLD\" Icicles forming on the air vents."
            ),
            RoastCard(
                emoji = "🔋",
                value = "$battery%",
                label = "BATTERY",
                roast = if (battery < 20) "Low battery warning! Go find a charger." else "Battery level nominal. Ready to drive.",
                supportingStat = "$battery% Remaining",
                type = RoastCardType.EVENT,
                eyebrow = "STATE OF CHARGE",
                podMegaValue = "$battery%",
                podSubLabel = "BATTERY LEVEL",
                statusText = if (battery < 20) "LOW BATTERY" else "NOMINAL",
                barFillPercent = (battery / 100f).coerceIn(0.1f, 1f),
                accentColorName = if (battery < 20) "red" else "green",
                footerLeft = "EV BATTERY MONITOR",
                footerRight = "NOMINAL",
                scorePillText = "LIVE",
                scorePillPositive = battery >= 20,
                memeTitle = "Sgt. Doakes Return",
                memeQuote = "\"SURPRISE, MOTHERF***ER\" 23% and you pushing this EV home."
            )
        )
        _allCards.value = cards
        _roastCards.value = cards
        _parkedRoastState.value = ParkedRoastState.Swiping
    }

    private fun buildRoastCardsFromTrip(trip: TripEntity): List<RoastCard> {
        val score = calculateScore(trip)
        val scoreChange = score - 80
        val heroEmoji = when {
            score >= 90 -> "😎"
            score >= 75 -> "😏"
            score >= 60 -> "😬"
            else -> "💀"
        }
        val heroStatus = when {
            score >= 90 -> "LOCKED IN"
            score >= 75 -> "SMOOTH FLOW"
            score >= 60 -> "CHAOTIC"
            else -> "AURA WIPED"
        }
        val heroAccent = when {
            score >= 80 -> "green"
            score >= 60 -> "amber"
            else -> "red"
        }
        val durationFormatted = formatDuration(trip.durationSeconds)

        val brakeRoast = when {
            trip.hardBrakeCount >= 3 -> "You slammed the brakes ${trip.hardBrakeCount} times! Are your brake pads paying rent?"
            trip.hardBrakeCount in 1..2 -> "A couple of panic anchors dropped. Antilock brakes doing heavy lifting."
            else -> "Butter-smooth braking. You didn't spill a single drop of coffee."
        }
        val brakeStatus = when {
            trip.hardBrakeCount >= 3 -> "HEAVY ANCHOR"
            trip.hardBrakeCount in 1..2 -> "MODERATE"
            else -> "SILKY SMOOTH"
        }
        val brakeAccent = when {
            trip.hardBrakeCount >= 3 -> "red"
            trip.hardBrakeCount in 1..2 -> "amber"
            else -> "green"
        }

        val accelRoast = when {
            trip.maxSpeedKmh > 130f -> "Did you think the accelerator was an elevator button to the moon? Max: ${trip.maxSpeedKmh.toInt()} km/h!"
            trip.rapidAccelerationCount >= 3 -> "Aggressive launch detected ${trip.rapidAccelerationCount} times. Vin Diesel called—wants his mid-life crisis back."
            trip.rapidAccelerationCount in 1..2 -> "A few enthusiastic launches onto the highway."
            else -> "Gentle, linear throttle control. Electric motor efficiency maximized."
        }
        val accelAccent = when {
            trip.maxSpeedKmh > 130f || trip.rapidAccelerationCount >= 3 -> "red"
            trip.rapidAccelerationCount in 1..2 -> "amber"
            else -> "green"
        }

        val hvacTemp = trip.averageHvacTempCelsius
        val hvacRoast = when {
            hvacTemp <= 17.5f -> "Average AC was ${String.format(Locale.US, "%.1f", hvacTemp)}°C. Are you driving or transporting fresh seafood?"
            hvacTemp >= 26.0f -> "Cabin set to ${String.format(Locale.US, "%.1f", hvacTemp)}°C. Nice sauna session on wheels."
            else -> "Cabin kept at a comfortable ${String.format(Locale.US, "%.1f", hvacTemp)}°C. Goldilocks would approve."
        }
        val hvacStatus = when {
            hvacTemp <= 17.5f -> "POLAR SUB-ZERO"
            hvacTemp >= 26.0f -> "SAUNA MODE"
            else -> "BALANCED COMFORT"
        }
        val hvacAccent = when {
            hvacTemp <= 17.5f -> "cyan"
            hvacTemp >= 26.0f -> "red"
            else -> "green"
        }

        val currentBattery = vehicleData.value.batteryLevelPercent.toInt().coerceAtLeast(1)

        return listOf(
            RoastCard(
                emoji = heroEmoji,
                value = score.toString(),
                label = "AURA SCORE",
                roast = trip.roastText ?: (if (score >= 80) "Smooth driving bro." else "Rough ride bro."),
                supportingStat = "${if (scoreChange >= 0) "+" else ""}$scoreChange this trip",
                type = RoastCardType.SCORE,
                eyebrow = "AURA TRIP SUMMARY",
                podMegaValue = score.toString(),
                podSubLabel = "AURA DRIVER SCORE",
                statusText = heroStatus,
                barFillPercent = (score / 100f).coerceIn(0.1f, 1f),
                accentColorName = heroAccent,
                footerLeft = "DURATION: $durationFormatted",
                footerRight = "MAX: ${trip.maxSpeedKmh.toInt()} KM/H",
                scorePillText = "${if (scoreChange >= 0) "+" else ""}$scoreChange THIS TRIP",
                scorePillPositive = scoreChange >= 0,
                memeTitle = "Hand on Shoulder",
                memeQuote = "\"Bro...\" I'm genuinely proud of you."
            ),
            RoastCard(
                emoji = "🛑",
                value = "${trip.hardBrakeCount}×",
                label = "BRAKES",
                roast = brakeRoast,
                supportingStat = if (trip.hardBrakeCount == 0) "Zero hard brakes" else "${trip.hardBrakeCount} hard stops",
                type = RoastCardType.EVENT,
                eyebrow = "DECELERATION LOG",
                podMegaValue = "${trip.hardBrakeCount}×",
                podSubLabel = "BRAKE DECELERATIONS",
                statusText = brakeStatus,
                barFillPercent = (trip.hardBrakeCount * 0.25f).coerceIn(0.05f, 1f),
                accentColorName = brakeAccent,
                footerLeft = "TOTAL STOPS: ${trip.hardBrakeCount}",
                footerRight = if (trip.hardBrakeCount == 0) "SMOOTH STOPPING" else "HEAVY ANCHOR",
                scorePillText = if (trip.hardBrakeCount == 0) "+5 AURA" else "-${trip.hardBrakeCount * 3} AURA",
                scorePillPositive = trip.hardBrakeCount == 0,
                memeTitle = "Sgt. Doakes Stare",
                memeQuote = "\"SURPRISE, MOTHERF***ER\" I saw that brake check from orbit."
            ),
            RoastCard(
                emoji = "⚡",
                value = "${trip.rapidAccelerationCount}×",
                label = "ACCEL",
                roast = accelRoast,
                supportingStat = "Max ${trip.maxSpeedKmh.toInt()} km/h",
                type = RoastCardType.EVENT,
                eyebrow = "THROTTLE TELEMETRY",
                podMegaValue = "${trip.rapidAccelerationCount}×",
                podSubLabel = "RAPID ACCELERATIONS",
                statusText = "PEAK ${trip.maxSpeedKmh.toInt()} KM/H",
                barFillPercent = (trip.maxSpeedKmh / 160f).coerceIn(0.1f, 1f),
                accentColorName = accelAccent,
                footerLeft = "AVG: ${trip.averageSpeedKmh.toInt()} KM/H",
                footerRight = if (trip.rapidAccelerationCount > 0) "SPORT THROTTLE" else "ECO LINEAR",
                scorePillText = if (trip.rapidAccelerationCount == 0 && trip.maxSpeedKmh <= 120f) "+5 AURA" else "-${trip.rapidAccelerationCount * 4} AURA",
                scorePillPositive = trip.rapidAccelerationCount == 0 && trip.maxSpeedKmh <= 120f,
                memeTitle = "Max Verstappen Radio",
                memeQuote = "\"SIMPLY LOVELY\" Bro think he in Q3 in Monza."
            ),
            RoastCard(
                emoji = if (hvacTemp <= 18f) "❄️" else if (hvacTemp >= 25f) "🔥" else "🛋️",
                value = "${String.format(Locale.US, "%.1f", hvacTemp)}°C",
                label = "CABIN TEMP",
                roast = hvacRoast,
                supportingStat = "${String.format(Locale.US, "%.1f", hvacTemp)}°C Avg",
                type = RoastCardType.EVENT,
                eyebrow = "HVAC & CABIN CLIMATE",
                podMegaValue = "${String.format(Locale.US, "%.1f", hvacTemp)}°C",
                podSubLabel = "AVERAGE TEMPERATURE",
                statusText = hvacStatus,
                barFillPercent = ((hvacTemp - 15f) / 15f).coerceIn(0.1f, 1f),
                accentColorName = hvacAccent,
                footerLeft = "CABIN CLIMATE: ACTIVE",
                footerRight = if (hvacTemp <= 17.5f) "POLAR SUB-ZERO" else "BALANCED",
                scorePillText = if (hvacTemp in 18f..24f) "+2 AURA" else "-2 AURA",
                scorePillPositive = hvacTemp in 18f..24f,
                memeTitle = "Frozen in Snow",
                memeQuote = "\"I'M NOT COLD\" Icicles forming on the air vents."
            ),
            RoastCard(
                emoji = "🔋",
                value = "$currentBattery%",
                label = "BATTERY",
                roast = if (currentBattery < 20) "Battery at $currentBattery%. Living dangerously, aren't we?" else "Energy reserve nominal. Ready for the next run.",
                supportingStat = "$currentBattery% Left",
                type = RoastCardType.EVENT,
                eyebrow = "STATE OF CHARGE",
                podMegaValue = "$currentBattery%",
                podSubLabel = "BATTERY LEVEL",
                statusText = if (currentBattery < 20) "RECHARGE SOON" else "NOMINAL",
                barFillPercent = (currentBattery / 100f).coerceIn(0.1f, 1f),
                accentColorName = if (currentBattery < 20) "amber" else "green",
                footerLeft = "EV POWERTRAIN: READY",
                footerRight = "BATTERY MONITORED",
                scorePillText = "NOMINAL",
                scorePillPositive = true,
                memeTitle = "Sgt. Doakes Return",
                memeQuote = "\"SURPRISE, MOTHERF***ER\" 23% and you pushing this EV home."
            )
        )
    }

    private fun calculateScore(trip: TripEntity): Int {
        var base = 100
        base -= (trip.hardBrakeCount * 5)
        base -= (trip.rapidAccelerationCount * 6)
        if (trip.maxSpeedKmh > 130f) {
            base -= 15
        } else if (trip.maxSpeedKmh > 110f) {
            base -= 5
        }
        if (trip.averageHvacTempCelsius < 17.5f || trip.averageHvacTempCelsius > 26f) {
            base -= 4
        }
        return base.coerceIn(10, 100)
    }

    private fun formatDuration(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    private fun prepareRoastCards(session: com.auradrive.aaos.model.DrivingSession) {
        val hardBrakes = session.events.count { it is DrivingEvent.HardBraking }
        val rapidAccels = session.events.count { it is DrivingEvent.HardAcceleration }
        val maxSpeed = (vehicleData.value.speedMps * 3.6f)
        val temp = if (vehicleData.value.cabinTemperatureCelsius > 0f) vehicleData.value.cabinTemperatureCelsius else 21f

        val syntheticTrip = TripEntity(
            tripId = session.id,
            startTimeEpochMs = session.startTime.toEpochMilli(),
            endTimeEpochMs = session.endTime?.toEpochMilli() ?: System.currentTimeMillis(),
            durationSeconds = ((session.endTime?.toEpochMilli() ?: System.currentTimeMillis()) - session.startTime.toEpochMilli()) / 1000L,
            maxSpeedKmh = maxSpeed,
            averageSpeedKmh = maxSpeed * 0.7f,
            hardBrakeCount = hardBrakes,
            rapidAccelerationCount = rapidAccels,
            averageHvacTempCelsius = temp,
            roastText = null,
            isRoastGenerated = false
        )

        val cards = buildRoastCardsFromTrip(syntheticTrip)
        _allCards.value = cards
        _roastCards.value = cards
        _parkedRoastState.value = ParkedRoastState.Swiping
    }

    fun startRoast() {
        val current = _parkedRoastState.value
        if (current is ParkedRoastState.Intro) {
            _parkedRoastState.value = ParkedRoastState.Swiping
        }
    }

    fun onCardSwiped(card: RoastCard? = null) {
        val currentCards = _roastCards.value.toMutableList()
        if (currentCards.isNotEmpty()) {
            val cardToRemove = card ?: currentCards.first()
            currentCards.remove(cardToRemove)
            _roastCards.value = currentCards
            
            if (currentCards.isEmpty()) {
                _parkedRoastState.value = ParkedRoastState.Complete
            }
        }
    }

    fun bringCardToFront(card: RoastCard) {
        val current = _roastCards.value.toMutableList()
        if (current.remove(card)) {
            current.add(0, card)
            _roastCards.value = current
        }
    }

    fun selectCardAndOpenDeck(card: RoastCard) {
        val current = _allCards.value.toMutableList()
        if (current.remove(card)) {
            current.add(0, card)
        }
        _roastCards.value = current
        _parkedRoastState.value = ParkedRoastState.Swiping
    }

    fun finishTripAndPark() {
        mockSource.updateGear(VehicleGear.PARK)
        viewModelScope.launch {
            tripAggregator?.finishTrip()?.let { finishedTrip ->
                val roasts = mutableListOf<String>()
                if (finishedTrip.maxSpeedKmh > 130f) {
                    roasts.add("Did you think the accelerator was an elevator button to the moon? Max speed was ${finishedTrip.maxSpeedKmh.toInt()} km/h!")
                }
                if (finishedTrip.hardBrakeCount >= 3) {
                    roasts.add("You slammed the brakes ${finishedTrip.hardBrakeCount} times. Are your brake pads paying rent, or are you trying to evict them?")
                }
                if (finishedTrip.rapidAccelerationCount >= 3) {
                    roasts.add("Aggressive launch detected ${finishedTrip.rapidAccelerationCount} times. Vin Diesel called—he wants his mid-life crisis back.")
                }
                if (finishedTrip.averageHvacTempCelsius <= 17.5f) {
                    roasts.add("Average AC temperature was ${String.format(Locale.US, "%.1f", finishedTrip.averageHvacTempCelsius)}°C. Are you driving or transporting fresh seafood?")
                } else if (finishedTrip.averageHvacTempCelsius >= 26.0f) {
                    roasts.add("Cabin set to ${String.format(Locale.US, "%.1f", finishedTrip.averageHvacTempCelsius)}°C. Nice sauna session on wheels.")
                }

                val finalText = if (roasts.isEmpty()) {
                    "Suspiciously normal driving. You drove so peacefully even your grandma would be bored."
                } else {
                    roasts.joinToString(" ")
                }
                val tone = if (roasts.size >= 2) "BRUTAL" else "PLAYFUL"

                val completedTrip = finishedTrip.copy(
                    roastText = finalText,
                    roastTone = tone,
                    isRoastGenerated = true
                )
                tripRepository?.saveTrip(completedTrip)
            }
        }
    }

    fun simulateAggressiveTrip() {
        viewModelScope.launch {
            // 1. Shift to DRIVE & start trip
            simulateGear(VehicleGear.DRIVE)
            delay(250)

            // 2. High speed burst (145 km/h)
            simulateSpeed(145f)
            delay(250)

            // 3. Rapid accelerator stomps (3 times)
            repeat(3) {
                simulateHardAccel()
                delay(200)
            }

            // 4. Hard brakes (4 times)
            repeat(4) {
                simulateHardBrake()
                delay(200)
            }

            // 5. Freeze cabin to 16°C
            simulateHvacTemp(16f)
            delay(250)

            // 6. Stop and shift to PARK
            simulateSpeed(0f)
            delay(200)
            simulateGear(VehicleGear.PARK)
        }
    }

    fun simulateChillTrip() {
        viewModelScope.launch {
            // 1. Shift to DRIVE & start trip
            simulateGear(VehicleGear.DRIVE)
            delay(250)

            // 2. Gentle cruising (55 km/h)
            simulateSpeed(55f)
            delay(250)

            // 3. Balanced climate (22°C)
            simulateHvacTemp(22f)
            delay(250)

            // 4. Stop and shift to PARK
            simulateSpeed(0f)
            delay(200)
            simulateGear(VehicleGear.PARK)
        }
    }

    fun resetReport() {
        viewModelScope.launch {
            val latest = tripRepository?.getLatestTrip()
            buildInitialCardsFromLiveTelemetry()
            _parkedRoastState.value = ParkedRoastState.Swiping
        }
    }

    fun setAudioEnabled(enabled: Boolean) {
        voiceManager.isMuted = !enabled
        if (!enabled) {
            voiceManager.stop()
        }
    }

    // Interactive simulation helpers that feed both the UI and TripAggregator
    fun simulateSpeed(speedKmh: Float) {
        val speedMps = speedKmh / 3.6f
        mockSource.updateSpeed(speedMps)
        viewModelScope.launch {
            tripAggregator?.recordSpeed(speedMps)
        }
    }

    fun simulateGear(gear: VehicleGear) {
        mockSource.updateGear(gear)
        if (gear == VehicleGear.PARK) {
            finishTripAndPark()
        } else {
            viewModelScope.launch {
                tripAggregator?.startTrip()
            }
        }
    }

    fun simulateHardBrake() {
        mockSource.triggerHardBraking()
        viewModelScope.launch {
            tripAggregator?.recordBrakePosition(0.85f)
        }
    }

    fun simulateHardAccel() {
        mockSource.triggerHardAcceleration()
        viewModelScope.launch {
            tripAggregator?.recordAcceleratorPosition(0.92f)
        }
    }

    fun simulateHvacTemp(tempCelsius: Float) {
        mockSource.updateCabinTemperature(tempCelsius)
        viewModelScope.launch {
            tripAggregator?.recordHvacTemperature(tempCelsius)
        }
    }
}

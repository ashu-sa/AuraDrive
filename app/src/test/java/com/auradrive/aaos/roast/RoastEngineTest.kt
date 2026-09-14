package com.auradrive.aaos.roast

import com.auradrive.aaos.model.DriverPersonality
import com.auradrive.aaos.model.DrivingEvent
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

class RoastEngineTest {
    private lateinit var engine: RoastEngine
    private val repository = RoastRepository()

    @Before
    fun setup() {
        engine = RoastEngineImpl(repository)
    }

    @Test
    fun `engine generates roast for event`() {
        val event = DrivingEvent.HardBraking(Instant.now(), -8f)
        val roast = engine.generateRoast(event, DriverPersonality.CHILL_DRIVER)
        assertNotNull(roast)
    }

    @Test
    fun `engine respects cooldown`() {
        val event = DrivingEvent.HardBraking(Instant.now(), -8f)
        
        // First roast should work
        val roast1 = engine.generateRoast(event, DriverPersonality.CHILL_DRIVER)
        assertNotNull(roast1)

        // Immediate second roast should be null due to global cooldown
        val roast2 = engine.generateRoast(event, DriverPersonality.CHILL_DRIVER)
        assertNull(roast2)
    }
}

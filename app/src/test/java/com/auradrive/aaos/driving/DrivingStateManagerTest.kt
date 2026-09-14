package com.auradrive.aaos.driving

import com.auradrive.aaos.model.VehicleGear
import com.auradrive.aaos.vehicle.MockVehicleDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DrivingStateManagerTest {

    private lateinit var mockVehicleDataSource: MockVehicleDataSource
    private lateinit var drivingStateManager: DrivingStateManager
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        mockVehicleDataSource = MockVehicleDataSource()
        // Using backgroundScope ensures that the coroutines started by stateIn
        // are cancelled when the test finishes.
        drivingStateManager = DrivingStateManagerImpl(mockVehicleDataSource, testScope.backgroundScope)
    }

    @Test
    fun `initial state is PARKED because MockVehicleDataSource defaults to PARK`() = testScope.runTest {
        // The manager collects the initial emission from the data source immediately
        assertEquals(DrivingState.PARKED, drivingStateManager.currentState.value)
    }

    @Test
    fun `PARK gear transitions to PARKED`() = testScope.runTest {
        mockVehicleDataSource.updateGear(VehicleGear.PARK)
        assertEquals(DrivingState.PARKED, drivingStateManager.currentState.value)
    }

    @Test
    fun `REVERSE gear transitions to REVERSE`() = testScope.runTest {
        mockVehicleDataSource.updateGear(VehicleGear.REVERSE)
        assertEquals(DrivingState.REVERSE, drivingStateManager.currentState.value)
    }

    @Test
    fun `DRIVE gear with speed transitions to DRIVING`() = testScope.runTest {
        mockVehicleDataSource.updateGear(VehicleGear.DRIVE)
        mockVehicleDataSource.updateSpeed(10f)
        assertEquals(DrivingState.DRIVING, drivingStateManager.currentState.value)
    }

    @Test
    fun `DRIVE gear with zero speed stays PARKED if coming from PARKED`() = testScope.runTest {
        mockVehicleDataSource.updateGear(VehicleGear.PARK)
        assertEquals(DrivingState.PARKED, drivingStateManager.currentState.value)
        
        mockVehicleDataSource.updateGear(VehicleGear.DRIVE)
        mockVehicleDataSource.updateSpeed(0f)
        assertEquals(DrivingState.PARKED, drivingStateManager.currentState.value)
    }

    @Test
    fun `DRIVE gear with zero speed stays DRIVING if coming from DRIVING`() = testScope.runTest {
        // Start driving
        mockVehicleDataSource.updateGear(VehicleGear.DRIVE)
        mockVehicleDataSource.updateSpeed(10f)
        assertEquals(DrivingState.DRIVING, drivingStateManager.currentState.value)
        
        // Stop at light
        mockVehicleDataSource.updateSpeed(0f)
        assertEquals(DrivingState.DRIVING, drivingStateManager.currentState.value)
    }

    @Test
    fun `Shifting to PARK during DRIVE ends DRIVING state`() = testScope.runTest {
        mockVehicleDataSource.updateGear(VehicleGear.DRIVE)
        mockVehicleDataSource.updateSpeed(10f)
        assertEquals(DrivingState.DRIVING, drivingStateManager.currentState.value)
        
        mockVehicleDataSource.updateGear(VehicleGear.PARK)
        assertEquals(DrivingState.PARKED, drivingStateManager.currentState.value)
    }
}

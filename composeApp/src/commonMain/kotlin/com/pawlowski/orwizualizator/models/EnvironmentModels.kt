package com.pawlowski.orwizualizator.models

import kotlinx.serialization.Serializable

@Serializable
enum class DeviceState {
    ON,
    OFF,
    BROKEN
}

@Serializable
enum class BlindState {
    OPEN,
    CLOSED
}

@Serializable
data class LightDevice(
    val id: String,
    val roomId: String,
    val state: DeviceState,
    val brightness: Int = 100 // 0-100
)

@Serializable
data class PrinterDevice(
    val id: String,
    val roomId: String,
    val state: DeviceState,
    val tonerLevel: Int = 100, // 0-100
    val paperLevel: Int = 100 // 0-100
)

@Serializable
data class MotionSensor(
    val id: String,
    val roomId: String,
    val motionDetected: Boolean,
    val lastMotionTime: String? = null
)

@Serializable
data class TemperatureSensor(
    val id: String,
    val roomId: String,
    val temperature: Double // w stopniach Celsjusza
)

@Serializable
data class BlindsDevice(
    val id: String,
    val roomId: String,
    val state: BlindState
)

@Serializable
data class Meeting(
    val startTime: String, // LocalDateTime jako string
    val endTime: String, // LocalDateTime jako string
    val title: String = "Spotkanie"
)

@Serializable
data class Room(
    val id: String,
    val name: String,
    val lights: List<LightDevice>,
    val printer: PrinterDevice?,
    val motionSensor: MotionSensor,
    val temperatureSensor: TemperatureSensor,
    val blinds: BlindsDevice?,
    val peopleCount: Int = 0,
    val scheduledMeetings: List<Meeting> = emptyList(),
    val illumination: Double = 0.0 // Naświetlenie pokoju w lux
)

@Serializable
data class EnvironmentState(
    val simulationTime: String, // LocalDateTime jako string
    val rooms: List<Room>,
    val externalTemperature: Double,
    val timeSpeedMultiplier: Double = 1.0,
    val powerOutage: Boolean = false,
    val externalLightLux: Double = 0.0 // Światło zewnętrzne w lux (0-10000)
)

@Serializable
data class Alert(
    val id: String,
    val type: String, // "low_toner", "low_paper", "printer_failure", etc.
    val printerId: String? = null,
    val lightId: String? = null,
    val roomId: String?,
    val roomName: String?,
    val message: String,
    val timestamp: String,
    val severity: String = "warning" // "info", "warning", "error"
)

// Komunikacja NL między agentami
@Serializable
enum class MessageType {
    REQUEST,   // Prośba o akcję
    INFORM,    // Informacja
    QUERY,     // Zapytanie
    RESPONSE   // Odpowiedź
}

@Serializable
data class AgentMessage(
    val id: String,
    val from: String,       // "heating_agent", "printer_agent_208", etc.
    val to: String,         // "heating_agent", "blinds_agent", "broadcast", etc.
    val type: MessageType,
    val content: String,    // Tekst w języku naturalnym
    val timestamp: String,
    val context: Map<String, String>? = null
)

@Serializable
data class RoomHeatingResponse(
    val roomId: String,
    val isHeating: Boolean
)

@Serializable
data class AgentMessageRequest(
    val from: String,
    val to: String,
    val type: MessageType,
    val content: String,
    val context: Map<String, String>? = null
)
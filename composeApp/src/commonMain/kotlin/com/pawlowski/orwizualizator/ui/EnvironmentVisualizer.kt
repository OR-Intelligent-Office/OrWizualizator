package com.pawlowski.orwizualizator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pawlowski.orwizualizator.api.EnvironmentApi
import com.pawlowski.orwizualizator.models.*

@Composable
fun EnvironmentVisualizer(api: EnvironmentApi) {
    var state by remember { mutableStateOf<EnvironmentState?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        api.observeEnvironmentState().collect { newState ->
            state = newState
            error = null
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "Symulator Inteligentnego Biura",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Status bar
                if (state != null) {
                    StatusBar(state!!)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ładowanie danych...")
                    }
                }

                if (error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Błąd: $error",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Rooms list
                if (state != null) {
                    RoomsList(state!!.rooms)
                }
            }
        }
    }
}

@Composable
fun StatusBar(state: EnvironmentState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Czas symulacji: ${state.simulationTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Temperatura zewnętrzna: ${(state.externalTemperature * 10).toInt() / 10.0}°C",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row {
            if (state.powerOutage) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text("Utrata zasilania", color = MaterialTheme.colorScheme.onError)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Światło dzienne: ${(state.daylightIntensity * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun RoomsList(rooms: List<Room>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        rooms.forEach { room ->
            RoomCard(room, modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
fun RoomCard(room: Room, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Room header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleLarge
                )
                if (room.peopleCount > 0) {
                    Badge {
                        Text("${room.peopleCount} osób")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Temperatura:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${(room.temperatureSensor.temperature * 10).toInt() / 10.0}°C",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        room.temperatureSensor.temperature < 18 -> MaterialTheme.colorScheme.error
                        room.temperatureSensor.temperature > 25 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Motion sensor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ruch:", style = MaterialTheme.typography.bodyMedium)
                if (room.motionSensor.motionDetected) {
                    Text("✓ Wykryto", color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("✗ Brak", color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Devices
            Text(
                text = "Urządzenia:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Lights
            room.lights.forEach { light ->
                DeviceRow(
                    name = "Światło ${light.id}",
                    state = light.state,
                    details = "Jasność: ${light.brightness}%"
                )
            }

            // Printer
            room.printer?.let { printer ->
                DeviceRow(
                    name = "Drukarka ${printer.id}",
                    state = printer.state,
                    details = "Toner: ${printer.tonerLevel}%, Papier: ${printer.paperLevel}%"
                )
            }

            // Blinds
            room.blinds?.let { blinds ->
                DeviceRow(
                    name = "Rolety ${blinds.id}",
                    state = if (blinds.state == BlindState.OPEN) DeviceState.ON else DeviceState.OFF,
                    details = if (blinds.state == BlindState.OPEN) "Otwarte" else "Zamknięte"
                )
            }
        }
    }
}

@Composable
fun DeviceRow(name: String, state: DeviceState, details: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DeviceStateBadge(state)
    }
}

@Composable
fun DeviceStateBadge(state: DeviceState) {
    val (text, color) = when (state) {
        DeviceState.ON -> "ON" to MaterialTheme.colorScheme.primary
        DeviceState.OFF -> "OFF" to MaterialTheme.colorScheme.outline
        DeviceState.BROKEN -> "BROKEN" to MaterialTheme.colorScheme.error
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}


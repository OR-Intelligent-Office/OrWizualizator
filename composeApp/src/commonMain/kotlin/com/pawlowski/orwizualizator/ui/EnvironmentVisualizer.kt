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
    var alerts by remember { mutableStateOf<List<Alert>>(emptyList()) }
    var messages by remember { mutableStateOf<List<AgentMessage>>(emptyList()) }

    LaunchedEffect(Unit) {
        api.observeEnvironmentState().collect { newState ->
            state = newState
            error = null
        }
    }

    LaunchedEffect(Unit) {
        api.observeAlerts().collect { newAlerts ->
            alerts = newAlerts
        }
    }
    
    LaunchedEffect(Unit) {
        api.observeMessages().collect { newMessages ->
            messages = newMessages
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Główny panel (70%)
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
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

                    // Alerts section
                    if (alerts.isNotEmpty()) {
                        AlertsSection(alerts, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    // Rooms list
                    if (state != null) {
                        RoomsList(state!!.rooms, api)
                    }
                }
                
                // Panel boczny z wiadomościami (30%)
                MessagesPanel(
                    messages = messages,
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                )
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
                text = "Światło zewnętrzne: ${(state.externalLightLux).toInt()} lux",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun RoomsList(rooms: List<Room>, api: EnvironmentApi) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        rooms.forEach { room ->
            RoomCard(room, api = api, modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
fun RoomCard(room: Room, api: EnvironmentApi, modifier: Modifier = Modifier) {
    var isHeating by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(room.id) {
        while (true) {
            api.getRoomHeating(room.id)?.let { response ->
                isHeating = response.isHeating
            }
            kotlinx.coroutines.delay(1000) // Aktualizuj co sekundę
        }
    }

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

            // Heating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ogrzewanie:", style = MaterialTheme.typography.bodyMedium)
                when (isHeating) {
                    true -> Text("✓ Włączone", color = MaterialTheme.colorScheme.primary)
                    false -> Text("✗ Wyłączone", color = MaterialTheme.colorScheme.outline)
                    null -> Text("...", color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Illumination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Naświetlenie:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${room.illumination.toInt()} lux",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        room.illumination < 700 -> MaterialTheme.colorScheme.error
                        room.illumination >= 700 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
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
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Devices
            Text(
                text = "Urządzenia:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Lights
            room.lights.forEach { light ->
                val lightLux = if (light.state == DeviceState.ON) {
                    // Oblicz lux na podstawie brightness (brightness% * 500 lux)
                    (light.brightness / 100.0) * 500.0
                } else {
                    0.0 // Wyłączone światło = 0 lux
                }
                DeviceRow(
                    name = "Światło ${light.id}",
                    state = light.state,
                    details = "${lightLux.toInt()} lux"
                )
            }

            // Printer
            room.printer?.let { printer ->
                PrinterDeviceRow(printer = printer)
            }

            // Blinds
            room.blinds?.let { blinds ->
                DeviceRow(
                    name = "Rolety ${blinds.id}",
                    state = if (blinds.state == BlindState.OPEN) DeviceState.ON else DeviceState.OFF,
                    details = if (blinds.state == BlindState.OPEN) "Otwarte" else "Zamknięte"
                )
            }

            // Scheduled meetings
            if (room.scheduledMeetings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Zaplanowane spotkania:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                room.scheduledMeetings.forEach { meeting ->
                    MeetingRow(meeting)
                }
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
fun PrinterDeviceRow(printer: PrinterDevice) {
    val tonerLow = printer.tonerLevel < 20
    val paperLow = printer.paperLevel < 15

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Drukarka ${printer.id}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Toner: ${printer.tonerLevel} lux",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (tonerLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    if (tonerLow) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Papier: ${printer.paperLevel} lux",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (paperLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (paperLow) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
        DeviceStateBadge(printer.state)
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

@Composable
fun AlertsSection(alerts: List<Alert>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ Alerty",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Badge(
                    containerColor = when {
                        alerts.any { it.severity == "error" } -> MaterialTheme.colorScheme.error
                        alerts.any { it.severity == "warning" } -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text("${alerts.size}", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Show only recent alerts (last 5)
            alerts.takeLast(3).forEach { alert ->
                AlertCard(alert, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun AlertCard(alert: Alert, modifier: Modifier = Modifier) {
    val (icon, color, containerColor) = when (alert.severity) {
        "error" -> Triple("🔴", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
        "warning" -> Triple("🟡", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        else -> Triple("ℹ️", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        maxLines = 1
                    )
                    if (alert.roomName != null) {
                        Text(
                            text = alert.roomName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = alert.timestamp.substring(11, 16), // HH:mm
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MeetingRow(meeting: Meeting) {
    // Parse time strings to extract readable time
    fun formatTime(timeString: String): String {
        // Format: "2024-01-01T14:30:00" -> "14:30"
        return try {
            val parts = timeString.split("T")
            if (parts.size >= 2) {
                val timePart = parts[1]
                timePart.substring(0, 5) // "HH:MM"
            } else {
                timeString
            }
        } catch (e: Exception) {
            timeString
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${formatTime(meeting.startTime)} - ${formatTime(meeting.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "30 min",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// =============== PANEL WIADOMOŚCI AGENTÓW ===============

@Composable
fun MessagesPanel(messages: List<AgentMessage>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(top = 16.dp, end = 16.dp, bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💬 Komunikacja Agentów",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (messages.isNotEmpty()) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("${messages.size}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            if (messages.isEmpty()) {
                // Pusty stan
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🤖",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Brak wiadomości",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Agenci nie wysłali jeszcze żadnych komunikatów",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            } else {
                // Lista wiadomości (od najnowszych)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    messages.reversed().forEach { message ->
                        MessageCard(message)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MessageCard(message: AgentMessage) {
    val (icon, containerColor) = when (message.type) {
        MessageType.REQUEST -> "📨" to MaterialTheme.colorScheme.primaryContainer
        MessageType.INFORM -> "ℹ️" to MaterialTheme.colorScheme.secondaryContainer
        MessageType.QUERY -> "❓" to MaterialTheme.colorScheme.tertiaryContainer
        MessageType.RESPONSE -> "✅" to MaterialTheme.colorScheme.surfaceVariant
    }
    
    fun formatAgentName(name: String): String {
        return name
            .replace("_agent", "")
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }
    
    fun formatTime(timestamp: String): String {
        return try {
            if (timestamp.length >= 16) {
                timestamp.substring(11, 16)
            } else timestamp
        } catch (e: Exception) {
            timestamp
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: from -> to, czas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatAgentName(message.from),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " → ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (message.to == "broadcast") "Wszyscy" else formatAgentName(message.to),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Treść wiadomości
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Kontekst (jeśli jest)
            if (!message.context.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    message.context.entries.take(3).forEach { (key, value) ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "$key: $value",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
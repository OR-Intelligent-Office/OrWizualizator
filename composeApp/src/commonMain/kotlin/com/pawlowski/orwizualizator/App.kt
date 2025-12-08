package com.pawlowski.orwizualizator

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.pawlowski.orwizualizator.api.EnvironmentApi
import com.pawlowski.orwizualizator.ui.EnvironmentVisualizer
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val api = remember { EnvironmentApi() }
    
    DisposableEffect(Unit) {
        onDispose {
            api.close()
        }
    }
    
    EnvironmentVisualizer(api)
}
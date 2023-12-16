package com.apps.adrcotfas.goodtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.apps.adrcotfas.goodtime.data.local.DatabaseHelper
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {

    //TODO: move to ViewModel with DI
    private val localDataSource: DatabaseHelper by inject()
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            settingsRepository.saveAutoStartBreak(true)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsRepository.settings.collect { settings ->
                    println("Settings: $settings")
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                localDataSource.selectAllLabels()
                    .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                    .collect { it.forEach { label -> println("$label") } }
            }
        }

        setContent {
            ApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GreetingView(Greeting().greet())
                }
            }
        }
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    ApplicationTheme {
        GreetingView("Hello, Android!")
    }
}

package com.apps.adrcotfas.goodtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.domain.TimerManager
import com.apps.adrcotfas.goodtime.domain.TimerType
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    //TODO: move to ViewModel with DI
    private val localDataRepo: LocalDataRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val timerManager: TimerManager = getKoin().get()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            localDataRepo.insertLabel(Label().copy(name = "dummy"))
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                localDataRepo.selectAllLabels()
//                    .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
//                    .collect { it.forEach { label -> println(label.name) } }
//            }
        }

        setContent {
            ApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    timerManager.start(TimerType.WORK)
                                }
                            }
                        ) {
                            Text("start timer")
                        }
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    timerManager.setLabelName("dummy")
                                }
                            }
                        ) {
                            Text("change label to dummy")
                        }
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    timerManager.setLabelName(null)
                                }
                            }
                        ) {
                            Text("change label to null")
                        }
                    }
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

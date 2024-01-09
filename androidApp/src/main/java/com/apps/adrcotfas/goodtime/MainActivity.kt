package com.apps.adrcotfas.goodtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.timer.TimerDataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    //TODO: move to ViewModel with DI
    private val localDataRepo: LocalDataRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val timerDataRepo: TimerDataRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        lifecycleScope.launch {
            timerDataRepo.start()
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                localDataRepo.selectAllLabels()
//                    .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
//                    .collect { it.forEach { label -> println(label.name) } }
//            }
//            localDataRepo.updateDefaultLabelTimerProfile(TimerProfile(false, 333, 333, 333))
//            delay(2000)
//            localDataRepo.updateDefaultLabelTimerProfile(TimerProfile(false, 666, 666, 666))
//            delay(2000)

//            delay(2000)
//            timerDataRepo.setLabelId(5)
//            delay(2000)
//            localDataRepo.updateLabelName("p", "p2")
//            delay(2000)
//            localDataRepo.updateDefaultLabelTimerProfile(TimerProfile(true, 111, 111, 111))
//            delay(2000)
//            timerDataRepo.setLabelId(1)
//            delay(2000)
//            timerDataRepo.setLabelId(5)
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

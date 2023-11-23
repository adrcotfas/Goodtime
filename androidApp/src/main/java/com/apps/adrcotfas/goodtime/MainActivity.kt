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
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import com.apps.adrcotfas.goodtime.data.local.LocalDataSourceImpl
import com.apps.adrcotfas.goodtime.data.local.Database
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    //TODO: move to ViewModel with DI
    private val localDataSource =
        LocalDataSourceImpl(
            Database(
                driver = DatabaseDriverFactory(this).create())
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            localDataSource.getAllLabels()
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { it.forEach { label -> println("$label") } }
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

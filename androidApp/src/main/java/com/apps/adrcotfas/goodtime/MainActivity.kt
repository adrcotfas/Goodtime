package com.apps.adrcotfas.goodtime

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.di.injectLogger
import com.apps.adrcotfas.goodtime.labels.archived.ArchivedLabelsScreen
import com.apps.adrcotfas.goodtime.labels.main.LabelsScreen
import com.apps.adrcotfas.goodtime.main.BottomNavigationBar
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.main.MainScreen
import com.apps.adrcotfas.goodtime.main.MainViewModel
import com.apps.adrcotfas.goodtime.main.bottomNavigationItems
import com.apps.adrcotfas.goodtime.settings.SettingsScreen
import com.apps.adrcotfas.goodtime.stats.StatsScreen
import com.apps.adrcotfas.goodtime.ui.ApplicationTheme
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val log: Logger by injectLogger("MainActivity")

    private val viewModel by inject<MainViewModel>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val workSessionIsInProgress by viewModel.timerState.map { it.workSessionIsInProgress() }
                .collectAsStateWithLifecycle(false)

            val dynamicColor = uiState.dynamicColor
            val darkTheme = uiState.isDarkTheme(isSystemInDarkTheme())

            toggleKeepScreenOn(workSessionIsInProgress)

            DisposableEffect(uiState.darkThemePreference) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim,
                        darkScrim,
                    ) { darkTheme },
                )
                onDispose {}
            }

            LaunchedEffect(savedInstanceState) {
                askForNotificationPermission()
            }
            ApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val isMainDestination =
                        bottomNavigationItems.find { it.route == currentRoute } != null
                    Scaffold(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                        bottomBar = {
                            AnimatedVisibility(visible = isMainDestination) {
                                BottomNavigationBar(navController = navController)
                            }
                        }) { innerPadding ->
                        NavHost(
                            modifier = Modifier
                                .padding(innerPadding),
                            navController = navController,
                            startDestination = Destination.Main.route
                        ) {
                            composable(Destination.Main.route) { MainScreen() }
                            composable(Destination.Labels.route) {
                                LabelsScreen(navController)
                            }
                            composable(Destination.Stats.route) { StatsScreen() }
                            composable(Destination.Settings.route) { SettingsScreen() }

                            composable(Destination.ArchivedLabels.route) {
                                ArchivedLabelsScreen({
                                    navController.navigate(Destination.Labels.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    //TODO: clean-up
    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, applicationContext.packageName)
            startActivity(settingsIntent)
        }
    }

    private fun toggleKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

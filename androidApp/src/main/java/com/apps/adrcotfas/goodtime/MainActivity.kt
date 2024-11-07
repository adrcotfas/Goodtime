package com.apps.adrcotfas.goodtime

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.bl.notifications.NotificationArchManager
import com.apps.adrcotfas.goodtime.di.injectLogger
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.main.MainViewModel
import com.apps.adrcotfas.goodtime.main.bottomNavigationItems
import com.apps.adrcotfas.goodtime.ui.ApplicationTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val log: Logger by injectLogger("MainActivity")

    private val viewModel by inject<MainViewModel>()
    private val notificationManager: NotificationArchManager by inject()

    private var fullScreenJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val coroutineScope = rememberCoroutineScope()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val workSessionIsInProgress by viewModel.timerUiState.map { it.workSessionIsInProgress() }
                .collectAsStateWithLifecycle(false)
            val isActive by viewModel.timerUiState.map { it.isActive }
                .collectAsStateWithLifecycle(false)

            val fullscreenMode = uiState.isMainScreen && uiState.fullscreenMode && isActive
            var hideBottomBarWhenActive by remember(fullscreenMode) {
                mutableStateOf(fullscreenMode)
            }

            val darkTheme = uiState.isDarkTheme(isSystemInDarkTheme())

            toggleKeepScreenOn(isActive)
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                if (uiState.dndDuringWork) {
                    notificationManager.toggleDndMode(workSessionIsInProgress)
                } else {
                    notificationManager.toggleDndMode(false)
                }
            }

            LaunchedEffect(fullscreenMode) {
                fullscreenMode.let {
                    toggleFullscreen(it)
                    if (!it) fullScreenJob?.cancel()
                }
            }

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

            ApplicationTheme(darkTheme = darkTheme) {
                val interactionSource = remember { MutableInteractionSource() }
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (fullscreenMode) {
                                fullScreenJob = coroutineScope.launch {
                                    toggleFullscreen(false)
                                    hideBottomBarWhenActive = false
                                    executeDelayed(3000) {
                                        toggleFullscreen(true)
                                        hideBottomBarWhenActive = true
                                    }
                                }
                            }
                        },
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val isMainDestination =
                        bottomNavigationItems.find { it.route == currentRoute } != null
                    viewModel.setIsMainScreen(currentRoute == Destination.Main.route)
                    val showNavigation = isMainDestination.xor(hideBottomBarWhenActive)

                    NavigationScaffold(
                        showNavigation = showNavigation,
                        onNavigate = navController::navigate
                    ) {
                        NavigationHost(navController)
                    }
                }
            }
        }
    }

    private fun toggleKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun toggleFullscreen(enabled: Boolean) {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        if (enabled) {
            windowInsetsController.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

private suspend fun executeDelayed(delay: Long, block: () -> Unit) {
    coroutineScope {
        delay(delay)
        block()
    }
}

private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

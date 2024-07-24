package com.apps.adrcotfas.goodtime.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.apps.adrcotfas.goodtime.ui.common.RowWithCheckbox
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val settings by viewModel.settings.collectAsState()

    Scaffold(
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(
                WindowInsets.statusBars
            ),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                scrollBehavior = topAppBarScrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            RowWithCheckbox(title = "Use Dynamic Color", checked = settings.uiSettings.useDynamicColor) {
                viewModel.setUseDynamicColor(it)
            }
            RowWithCheckbox(title = "Fullscreen mode", checked = settings.uiSettings.fullscreenMode) {

            }
            RowWithCheckbox(title = "Keep the screen on", checked = settings.uiSettings.keepScreenOn) {

            }
            RowWithCheckbox(title = "Screensaver mode", checked = settings.uiSettings.screensaverMode) {

            }
        }
    }
}
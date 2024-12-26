package com.apps.adrcotfas.goodtime.settings.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onNavigateBack: () -> Boolean,
    showTopBar: Boolean
) {
    Scaffold(
        topBar = {
            TopBar(
                isVisible = showTopBar,
                title = "Open Source licenses",
                onNavigateBack = { onNavigateBack() }
            )
        }
    ) { paddingValues ->
        LibrariesContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(MaterialTheme.colorScheme.background),
            showLicenseBadges = false,
            showAuthor = true
        )
    }
}
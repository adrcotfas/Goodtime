package com.apps.adrcotfas.goodtime.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopBar(
                text = "Open Source Licenses",
                onNavigateBack = onNavigateBack
            )
        },
        content = {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                LibrariesContainer(showLicenseBadges = false, showAuthor = true)
            }
        }
    )
}
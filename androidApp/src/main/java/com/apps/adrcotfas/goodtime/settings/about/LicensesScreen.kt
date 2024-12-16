package com.apps.adrcotfas.goodtime.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer


@Composable
fun LicensesScreen() {
    Column(Modifier.fillMaxSize()
    ) {
        LibrariesContainer(showLicenseBadges = false, showAuthor = true)
    }
}
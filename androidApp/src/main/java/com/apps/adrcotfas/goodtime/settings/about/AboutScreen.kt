/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.settings.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.apps.adrcotfas.goodtime.ui.common.IconListItem
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.BookOpen
import compose.icons.evaicons.outline.Email
import compose.icons.evaicons.outline.Github
import compose.icons.evaicons.outline.Star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateToLicenses: () -> Unit,
    isLicensesSelected: Boolean = false,
    onNavigateBack: () -> Boolean,
    showTopBar: Boolean,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBar(
                isVisible = showTopBar,
                title = "About and feedback",
                onNavigateBack = { onNavigateBack() },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
        ) {
            IconListItem(
                title = "Source code",
                icon = { Icon(EvaIcons.Outline.Github, contentDescription = "GitHub") },
                onClick = {
                    openUrl(context, REPO_URL)
                },
            )
            IconListItem(
                title = "Open Source Licenses",
                icon = {
                    Icon(
                        EvaIcons.Outline.BookOpen,
                        contentDescription = "Open Source Licenses",
                    )
                },
                onClick = {
                    onNavigateToLicenses()
                },
                isSelected = isLicensesSelected,
            )
            SubtleHorizontalDivider()
            IconListItem(
                title = "Feedback",
                icon = { Icon(EvaIcons.Outline.Email, contentDescription = "Feedback") },
                onClick = { sendFeedback(context) },
            )
            IconListItem(
                title = "Rate this app",
                icon = { Icon(EvaIcons.Outline.Star, contentDescription = "Rate this app") },
                onClick = {
                    openUrl(context, GOOGLE_PLAY_URL)
                },
            )
        }
    }
}

const val GOOGLE_PLAY_URL =
    "https://play.google.com/store/apps/details?id=com.apps.adrcotfas.goodtime"
const val REPO_URL = "https://github.com/adrcotfas/Goodtime"

@Preview
@Composable
fun AboutScreenPreview() {
    AboutScreen(onNavigateToLicenses = {}, onNavigateBack = { false }, showTopBar = true)
}

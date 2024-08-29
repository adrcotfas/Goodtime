package com.apps.adrcotfas.goodtime.settings.about

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.apps.adrcotfas.goodtime.common.getVersionName
import com.apps.adrcotfas.goodtime.ui.common.PreferenceWithIcon
import com.apps.adrcotfas.goodtime.ui.common.SubtleHorizontalDivider
import compose.icons.FeatherIcons
import compose.icons.feathericons.Book
import compose.icons.feathericons.Github
import compose.icons.feathericons.Mail
import compose.icons.feathericons.Star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit, onNavigateToLicenses: () -> Unit) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "About and feedback") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        content = {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                val appIcon: Drawable =
                    context.packageManager.getApplicationIcon(context.applicationInfo)
                PreferenceWithIcon(
                    title = "Goodtime Productivity",
                    subtitle = context.getVersionName(),
                    icon = {
                        Image(
                            bitmap = appIcon.toBitmap().asImageBitmap(),
                            contentDescription = "App icon",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                )
                PreferenceWithIcon(
                    title = "Source code",
                    icon = { Icon(FeatherIcons.Github, contentDescription = "GitHub") },
                    onClick = {
                        openUrl(context, REPO_URL)
                    }
                )
                PreferenceWithIcon(
                    title = "Open Source Licenses",
                    icon = { Icon(FeatherIcons.Book, contentDescription = "Open Source Licenses") },
                    onClick = {
                        onNavigateToLicenses()
                    })
                SubtleHorizontalDivider()
                PreferenceWithIcon(
                    title = "Feedback",
                    icon = { Icon(FeatherIcons.Mail, contentDescription = "Feedback") },
                    onClick = { sendFeedback(context) }
                )
                PreferenceWithIcon(
                    title = "Rate this app",
                    icon = { Icon(FeatherIcons.Star, contentDescription = "Rate this app") },
                    onClick = {
                        openUrl(context, GOOGLE_PLAY_URL)
                    }
                )
            }
        }
    )
}

const val GOOGLE_PLAY_URL =
    "https://play.google.com/store/apps/details?id=com.apps.adrcotfas.goodtime"
const val REPO_URL = "https://github.com/adrcotfas/Goodtime"

@Preview
@Composable
fun AboutScreenPreview() {
    AboutScreen(onNavigateBack = {}, onNavigateToLicenses = {})
}
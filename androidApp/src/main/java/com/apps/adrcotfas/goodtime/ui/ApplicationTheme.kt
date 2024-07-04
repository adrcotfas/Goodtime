package com.apps.adrcotfas.goodtime.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class CustomColorsPalette(
    val colors: List<Color> = listOf(Color.Unspecified)
)
val LocalColorsPalette = staticCompositionLocalOf { CustomColorsPalette() }

val LightColorsPalette =
    CustomColorsPalette(lightPalette.map { Color(android.graphics.Color.parseColor(it)) })
val DarkColorsPalette =
    CustomColorsPalette(darkPalette.map { Color(android.graphics.Color.parseColor(it)) })

val MaterialTheme.localColorsPalette: CustomColorsPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalColorsPalette.current

@Composable
fun ApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC5),
            tertiary = Color(0xFF3700B3)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC5),
            tertiary = Color(0xFF3700B3)
        )
    }

    val customColorsPalette =
        if (darkTheme) LightColorsPalette
        else DarkColorsPalette

    val typography = Typography(
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
    )
    CompositionLocalProvider(
        LocalColorsPalette provides customColorsPalette
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            content = content
        )
    }
}
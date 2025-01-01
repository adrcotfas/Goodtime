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
package com.apps.adrcotfas.goodtime.ui
import android.graphics.Color.parseColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xFF4DB6AC)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFF4DB6AC)
val onPrimaryContainerLight = Color(0xFFFFFFFF)
val secondaryLight = Color(0xFF222222)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFF434343)
val onSecondaryContainerLight = Color(0xFFFFFFFF)
val tertiaryLight = Color(0xFF000000)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFF262626)
val onTertiaryContainerLight = Color(0xFFFFFFFF)
val errorLight = Color(0xFF4E0002)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFF8C0009)
val onErrorContainerLight = Color(0xFFFFFFFF)
val backgroundLight = Color(0xFFF9F9F9)
val onBackgroundLight = Color(0xFF1B1B1B)
val surfaceLight = Color(0xFFF9F9F9)
val onSurfaceLight = Color(0xFF000000)
val surfaceVariantLight = Color(0xFFEBE0E1)
val onSurfaceVariantLight = Color(0xFF282224)
val outlineLight = Color(0xFF484142)
val outlineVariantLight = Color(0xFF484142)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF303030)
val inverseOnSurfaceLight = Color(0xFFFFFFFF)
val inversePrimaryLight = Color(0xFFECECEC)
val surfaceDimLight = Color(0xFFDADADA)
val surfaceBrightLight = Color(0xFFF9F9F9)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF3F3F3)
val surfaceContainerLight = Color(0xFFEEEEEE)
val surfaceContainerHighLight = Color(0xFFE8E8E8)
val surfaceContainerHighestLight = Color(0xFFE2E2E2)
val surfaceTintLight = Color(0xFF434343)

val primaryDark = Color(0xFF80CBC4)
val onPrimaryDark = Color(0xFF000000)
val primaryContainerDark = Color(0xFF80CBC4)
val onPrimaryContainerDark = Color(0xFF000000)
val secondaryDark = Color(0xFF80CBC4)
val onSecondaryDark = Color(0xFF000000)
val secondaryContainerDark = Color(0xFFCBCBCB)
val onSecondaryContainerDark = Color(0xFF000000)
val tertiaryDark = Color(0xFF80CBC4)
val onTertiaryDark = Color(0xFF000000)
val tertiaryContainerDark = Color(0xFFDEDEDE)
val onTertiaryContainerDark = Color(0xFF000000)
val errorDark = Color(0xFFFFF9F9)
val onErrorDark = Color(0xFF000000)
val errorContainerDark = Color(0xFFFFBAB1)
val onErrorContainerDark = Color(0xFF000000)
val backgroundDark = Color(0xFF000000)
val onBackgroundDark = Color(0xFFE2E2E2)
val surfaceDark = Color(0xFF000000)
val onSurfaceDark = Color(0xFFCBCBCB)
val surfaceVariantDark = Color(0xFF4C4546)
val onSurfaceVariantDark = Color(0xFFFFF9F9)
val outlineDark = Color(0xFFD3C8C9)
val outlineVariantDark = Color(0xFFB9AFB0)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE2E2E2)
val inverseOnSurfaceDark = Color(0xFF000000)
val inversePrimaryDark = Color(0xFF2A2A2A)
val surfaceDimDark = Color(0xFF131313)
val surfaceBrightDark = Color(0xFF393939)
val surfaceContainerLowestDark = Color(0xFF0E0E0E)
val surfaceContainerLowDark = Color(0xFF151515)
val surfaceContainerDark = Color(0xFF1F1F1F)
val surfaceContainerHighDark = Color(0xFF2A2A2A)
val surfaceContainerHighestDark = Color(0xFF353535)
val surfaceTintDark = Color(0x00FFFFFF)

val MaterialTheme.localColorsPalette: CustomColorsPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalColorsPalette.current

val lightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
    surfaceTint = surfaceTintLight,
)

val darkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
    surfaceTint = surfaceTintDark,
)

@Immutable
data class CustomColorsPalette(
    val colors: List<Color> = listOf(Color.Unspecified),
)

val LocalColorsPalette = staticCompositionLocalOf { CustomColorsPalette() }

val LightColorsPalette =
    CustomColorsPalette(lightPalette.map { Color(parseColor(it)) })
val DarkColorsPalette =
    CustomColorsPalette(darkPalette.map { Color(parseColor(it)) })

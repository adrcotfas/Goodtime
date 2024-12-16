@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.apps.adrcotfas.goodtime.ui.common

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator

val ThreePaneScaffoldNavigator<Any>.content: Any?
    get() = currentDestination?.content

val ThreePaneScaffoldNavigator<Any>.pane: ThreePaneScaffoldRole?
    get() = currentDestination?.pane

fun ThreePaneScaffoldNavigator<Any>.navigateToDetail(destination: Any) {
    navigateTo(
        pane = ListDetailPaneScaffoldRole.Detail,
        content = destination
    )
}

fun ThreePaneScaffoldNavigator<Any>.navigateToExtra(destination: Any) {
    navigateTo(
        pane = ListDetailPaneScaffoldRole.Extra,
        content = destination
    )
}
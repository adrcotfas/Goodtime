package com.apps.adrcotfas.goodtime.common

import android.content.res.Configuration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Configuration.isPortrait : Boolean
    get() = this.orientation == Configuration.ORIENTATION_PORTRAIT

val Configuration.screenWidth: Dp
    get() = if (isPortrait) screenWidthDp.dp else screenHeightDp.dp

val Configuration.screenHeight: Dp
    get() = if (isPortrait) screenHeightDp.dp else screenWidthDp.dp
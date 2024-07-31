package com.apps.adrcotfas.goodtime.common

import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi

tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Context.getAppLanguage(): String {
    val locale = resources.configuration.locales[0]
    val currentAppLocales: LocaleList = this
        .getSystemService(LocaleManager::class.java).getApplicationLocales(this.packageName)

    return (if (!currentAppLocales.isEmpty) {
        currentAppLocales[0].getDisplayLanguage(locale)
    } else {
        locale.displayLanguage
    }).replaceFirstChar { it.uppercase() }
}
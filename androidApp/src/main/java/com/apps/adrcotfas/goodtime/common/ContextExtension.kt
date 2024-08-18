package com.apps.adrcotfas.goodtime.common

import android.app.LocaleManager
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import java.io.File

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

fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
    else -> uri.path?.let(::File)?.name
}?.substringBeforeLast('.')

private fun Context.getContentFileName(uri: Uri): String? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()
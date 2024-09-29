package com.apps.adrcotfas.goodtime.common

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.os.PowerManager
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.PackageInfoCompat
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

fun Context.getVersionName(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return packageInfo.versionName
}

fun Context.getVersionCode(): Long {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val verCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    return verCode
}

@SuppressLint("BatteryLife")
fun Context.askForDisableBatteryOptimization() {
    val intent = Intent()
    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    intent.data = Uri.parse("package:" + this.packageName)
    this.startActivity(intent)
}

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val powerManager = this.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(this.packageName)
}

fun Context.areNotificationsEnabled(): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}
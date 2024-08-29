package com.apps.adrcotfas.goodtime.settings.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.apps.adrcotfas.goodtime.common.getVersionCode
import com.apps.adrcotfas.goodtime.common.getVersionName

fun getDeviceInfo(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val version = Build.VERSION.SDK_INT
    return "$manufacturer $model API $version"
}

fun sendFeedback(context: Context) {
    val email = Intent(Intent.ACTION_SENDTO)
    email.data = Uri.Builder().scheme("mailto").build()
    email.putExtra(Intent.EXTRA_EMAIL, arrayOf("goodtime-app@googlegroups.com"))
    email.putExtra(Intent.EXTRA_SUBJECT, "[Goodtime Productivity] Feedback")
    email.putExtra(Intent.EXTRA_TEXT,
     """
     * Pick a category:
     
     Feedback:
        - What do you like about the app?
        - What can be improved?

     Feature Request:
        - Describe the feature you would like to see.
        - How would this feature benefit you?

     Found Bug:
        - Describe the issue you encountered.
        - What are the steps to reproduce the issue? 

     Device info: ${getDeviceInfo()}
     App version: ${context.getVersionName()}(${context.getVersionCode()})
     """.trimIndent()
    )
    try {
        context.startActivity(Intent.createChooser(email, "Send feedback"))
    } catch (ex: ActivityNotFoundException) {
        Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
    }
}
package com.apps.adrcotfas.goodtime.about

import android.app.Dialog
import android.os.Bundle
import android.webkit.WebView
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.apps.adrcotfas.goodtime.BuildConfig
import com.apps.adrcotfas.goodtime.R

class OpenSourceLicensesDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val webView = WebView(requireContext())
        webView.loadUrl("file:///android_asset/open_source_licenses_${BuildConfig.FLAVOR}.html")
        webView.apply {
            settings.defaultFontSize = 10
            settings.javaScriptEnabled = true
        }
        return AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.about_open_source_licences))
            .setView(webView)
            .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .create()
    }
}
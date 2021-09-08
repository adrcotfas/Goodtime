package com.apps.adrcotfas.goodtime.main

import android.content.Intent

interface IBillingHelper {
    fun refresh()
    fun release()
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}
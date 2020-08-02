package com.apps.adrcotfas.goodtime.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.upgrade.BillingHelper

abstract class BaseActivity : AppCompatActivity() {

    //TODO: extract to Application as singleton
    private lateinit var billingHelper: BillingHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingHelper = BillingHelper(this)
    }

    override fun onDestroy() {
        billingHelper.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
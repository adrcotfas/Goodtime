package com.apps.adrcotfas.goodtime.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.upgrade.BillingHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var billingHelper: BillingHelper

    //TODO: switch to IapConnector
//    @Inject
//    lateinit var iapConnector: IapConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        iapConnector.addOnProductRestoredListener {
//
//        }
    }

    override fun onDestroy() {
        billingHelper.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingHelper.handleActivityResult(requestCode, resultCode, data!!)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
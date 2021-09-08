package com.apps.adrcotfas.goodtime.upgrade

import android.content.Context
import com.apps.adrcotfas.goodtime.main.IBillingHelper
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import android.content.Intent
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.util.Constants
import javax.inject.Inject

class BillingHelper @Inject constructor(context: Context, val preferenceHelper: PreferenceHelper) : IBillingHelper, IBillingHandler {

    private val mBillingProcessor: BillingProcessor = BillingProcessor.newBillingProcessor(
        context,
        context.getString(R.string.licence_key),
        context.getString(R.string.merchant_id), this
    )

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        // do nothing here
    }

    override fun onPurchaseHistoryRestored() {
        var found = false
        for (sku in mBillingProcessor.listOwnedProducts()) {
            if (sku == Constants.sku) {
                found = true
                break
            }
        }
        preferenceHelper.setPro(found)
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        // do nothing here
    }

    override fun onBillingInitialized() {
        mBillingProcessor.loadOwnedPurchasesFromGoogle()
        if (mBillingProcessor.isPurchased(Constants.sku)) {
            preferenceHelper.setPro(true)
        }
    }

    override fun refresh() {
        mBillingProcessor.loadOwnedPurchasesFromGoogle()
    }

    override fun release() {
        mBillingProcessor.release()
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return run {
            mBillingProcessor.loadOwnedPurchasesFromGoogle()
            if (mBillingProcessor.isPurchased(Constants.sku)) {
                preferenceHelper.setPro(true)
            }
            mBillingProcessor.handleActivityResult(requestCode, resultCode, data)
        }
    }

    init {
        mBillingProcessor.initialize()
    }
}
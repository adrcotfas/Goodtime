package com.apps.adrcotfas.goodtime.upgrade

import android.app.Activity
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.limerse.iap.DataWrappers
import com.limerse.iap.IapConnector
import com.limerse.iap.PurchaseServiceListener
import javax.inject.Inject

//TODO:
// - handle refunds
// - make IAP library errors visible to the user
// - listen for billing client ready / purchases fetched to enable the buy buttons
class BillingHelper @Inject constructor(private val iapConnector: IapConnector,
                                        private val preferenceHelper: PreferenceHelper) {

    init {
        iapConnector.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {
            }

            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                validateUpgrade(purchaseInfo)
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                validateUpgrade(purchaseInfo)
            }
        })
    }

    fun purchase(activity: Activity) {
        iapConnector.purchase(activity, SKU)
    }

    fun destroy() {
        iapConnector.destroy()
    }

    private fun validateUpgrade(it: DataWrappers.PurchaseInfo) {
        if (it.sku == SKU) {
            preferenceHelper.setPro(true)
        }
    }
}
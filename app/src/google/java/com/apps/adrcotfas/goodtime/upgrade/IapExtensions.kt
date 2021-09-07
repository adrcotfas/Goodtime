package com.apps.adrcotfas.goodtime.BL

import com.limerse.iap.DataWrappers
import com.limerse.iap.IapConnector
import com.limerse.iap.PurchaseServiceListener

fun IapConnector.addOnProductRestoredListener(onProductRestored : (purchaseInfo: DataWrappers.PurchaseInfo) -> Unit) {
    addPurchaseListener(object : PurchaseServiceListener {
        override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {}
        override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {}
        override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
            onProductRestored(purchaseInfo)
        }
    })
}

fun IapConnector.addOnProductPurchasedListener(onProductPurchased : (purchaseInfo: DataWrappers.PurchaseInfo) -> Unit) {
    addPurchaseListener(object : PurchaseServiceListener {
        override fun onPricesUpdated(iapKeyPrices: Map<String, String>) {}
        override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
            onProductPurchased(purchaseInfo)
        }
        override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {}
    })
}
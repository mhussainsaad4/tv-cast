package com.sensustech.mytvcast.utils.iap

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

interface PurchaseServiceListener : BillingServiceListener {
    override fun onPricesUpdated(iapKeyPrices: Map<String, ProductDetails>)

    fun onProductPurchased(purchaseInfo: Purchase)

    fun onProductRestored(purchaseInfo: Purchase)
}
package com.sensustech.mytvcast.utils.iap

import com.android.billingclient.api.ProductDetails

interface BillingServiceListener {
    fun onPricesUpdated(iapKeyPrices: Map<String, ProductDetails>)
}
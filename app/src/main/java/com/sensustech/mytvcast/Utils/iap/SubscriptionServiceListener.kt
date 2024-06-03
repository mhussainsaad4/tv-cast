package com.sensustech.mytvcast.utils.iap

import com.android.billingclient.api.Purchase

interface SubscriptionServiceListener : BillingServiceListener {

    fun onSubscriptionRestored(purchaseInfo: Purchase)

    fun onSubscriptionPurchased(purchaseInfo: Purchase)

    fun onSubscriptionsExpired()
}
package com.sensustech.mytvcast.utils.iap

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

abstract class IBillingService {

    private val purchaseServiceListeners: MutableList<PurchaseServiceListener> = mutableListOf()
    private val subscriptionServiceListeners: MutableList<SubscriptionServiceListener> =
        mutableListOf()
    private val billingClientConnectedListeners: MutableList<BillingClientConnectionListener> =
        mutableListOf()
    private val restoreServiceListeners: MutableList<RestoreServiceListener> = mutableListOf()

    fun addBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
        billingClientConnectedListeners.add(billingClientConnectionListener)
    }

    fun addRestoreServiceListeners(restoreServiceListener: RestoreServiceListener) {
        restoreServiceListeners.add(restoreServiceListener)
    }

    fun isBillingClientConnected(status: Boolean, responseCode: Int) {
        findUiHandler().post {
            for (billingServiceListener in billingClientConnectedListeners) {
                billingServiceListener.onConnected(status, responseCode)
            }
        }
    }

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.add(purchaseServiceListener)
    }

    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.remove(purchaseServiceListener)
    }

    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.add(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.remove(subscriptionServiceListener)
    }

    fun productOwned(purchase: Purchase, isRestore: Boolean) {
        findUiHandler().post {
            productOwnedInternal(purchase, isRestore)
        }
    }

    private fun productOwnedInternal(purchase: Purchase, isRestore: Boolean) {
        for (purchaseServiceListener in purchaseServiceListeners) {
            if (isRestore) {
                purchaseServiceListener.onProductRestored(purchase)
            } else {
                purchaseServiceListener.onProductPurchased(purchase)
            }
        }
    }

    fun productRestored(haveProductRestore: Boolean, isSilentCheck : Boolean) {
        findUiHandler().post {
            for (restoreServiceListener in restoreServiceListeners) {
                if (haveProductRestore) {
                    restoreServiceListener.productRestored()
                } else {
                    restoreServiceListener.noProductRestored(isSilentCheck)
                }
            }
        }
    }

    fun subscriptionOwned(purchase: Purchase, isRestore: Boolean) {
        findUiHandler().post {
            subscriptionOwnedInternal(purchase, isRestore)
        }
    }

    fun subscriptionsExpired() {
        findUiHandler().post {
            for (subscriptionServiceListener in subscriptionServiceListeners) {
                subscriptionServiceListener.onSubscriptionsExpired()
            }
        }
    }

    private fun subscriptionOwnedInternal(purchase: Purchase, isRestore: Boolean) {
        for (subscriptionServiceListener in subscriptionServiceListeners) {
            if (isRestore) {
                subscriptionServiceListener.onSubscriptionRestored(purchase)
            } else {
                subscriptionServiceListener.onSubscriptionPurchased(purchase)
            }
        }
    }

    fun updatePrices(iapKeyPrices: Map<String, ProductDetails>) {
        findUiHandler().post {
            updatePricesInternal(iapKeyPrices)
        }
    }

    private fun updatePricesInternal(iapKeyPrices: Map<String, ProductDetails>) {
        for (billingServiceListener in purchaseServiceListeners) {
            billingServiceListener.onPricesUpdated(iapKeyPrices)
        }
        for (billingServiceListener in subscriptionServiceListeners) {
            billingServiceListener.onPricesUpdated(iapKeyPrices)
        }
    }

    abstract fun init(key: String?)
    abstract fun buy(activity: Activity, productDetails: ProductDetails)
    abstract fun subscribe(activity: Activity, productDetails: ProductDetails)
    abstract fun enableDebugLogging(enable: Boolean)
    abstract fun restore(isRestoreFlow: Boolean = false, isSilentCheck : Boolean = false)

    @CallSuper
    open fun close() {
        subscriptionServiceListeners.clear()
        purchaseServiceListeners.clear()
    }
}

fun findUiHandler(): Handler {
    return Handler(Looper.getMainLooper())
}
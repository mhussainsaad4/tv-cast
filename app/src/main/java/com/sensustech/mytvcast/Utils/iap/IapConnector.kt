package com.sensustech.mytvcast.utils.iap

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import com.sensustech.mytvcast.utils.RemoteConfigValues
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Initialize billing service.
 *
 * @param context Application context.
 * @param consumableKeys SKU list for consumable one-time products.
 * @param subscriptionKeys SKU list for subscriptions.
 * @param key Key to verify purchase messages. Leave it empty if you want to skip verification.
 * @param enableLogging Log operations/errors to the logcat for debugging purposes.
 */
@OptIn(DelicateCoroutinesApi::class)
class IapConnector @JvmOverloads constructor(
    context: Context,
    nonConsumableKeys: List<String> = emptyList(),
    consumableKeys: List<String> = emptyList(),
    subscriptionKeys: List<String> = emptyList(),
    key: String? = null,
    enableLogging: Boolean = false
) {

    private var mBillingService: IBillingService? = null

    init {
        val contextLocal = context.applicationContext ?: context
        mBillingService =
            BillingService(contextLocal, nonConsumableKeys, consumableKeys, subscriptionKeys)
        getBillingService().init(key)
        getBillingService().enableDebugLogging(enableLogging)
    }

    fun addBillingConnectedListener(billingClientConnectionListener: BillingClientConnectionListener) {
        getBillingService().addBillingClientConnectionListener(billingClientConnectionListener)
    }

    fun addRestoreListener(restoreServiceListener: RestoreServiceListener) {
        getBillingService().addRestoreServiceListeners(restoreServiceListener)
    }

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().addPurchaseListener(purchaseServiceListener)
    }

    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().removePurchaseListener(purchaseServiceListener)
    }

    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        getBillingService().addSubscriptionListener(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        getBillingService().removeSubscriptionListener(subscriptionServiceListener)
    }

    fun purchase(activity: Activity, productDetails: ProductDetails) {
        getBillingService().buy(activity, productDetails)
    }

    fun subscribe(activity: Activity, productDetails: ProductDetails) {
        getBillingService().subscribe(activity, productDetails)
    }

    fun restore(isSilentCheck : Boolean) {
        getBillingService().restore(true, isSilentCheck)
    }

    fun getPrices() {
        getBillingService().restore()
    }

    fun destroy() {
        getBillingService().close()
    }

    private fun getBillingService(): IBillingService {
        return mBillingService ?: let {
            throw RuntimeException("Call IapConnector to initialize billing service")
        }
    }

    companion object {
        const val SUBSCRIPTION_1_MONTH_SKU = "com.sensustech.mytvcast.1month"
        const val SUBSCRIPTION_1_YEAR_SKU = "com.sensustech.mytvcast.1year"
        const val SUBSCRIPTION_1_YEAR_MORE_SKU = "com.sensustech.mytvcast.1yearmore"
        const val PURCHASE_LIFETIME_SKU = "com.sensustech.mytvcast.lifetime"
         const val weeklySku = "com.sensustech.mytvcast.1week"
         const val yearlySku = "yearlysub_standard"
        private var manager: IapConnector? = null

        fun getInstance(context: Context): IapConnector {
            if (manager == null) {
                /*val nonConsumableKeys = listOf(
                    PURCHASE_LIFETIME_SKU
                )*/
                var subscriptionKeys:List<String> =ArrayList()
                if (RemoteConfigValues.getInstance().isRemotePaymentCard){
                 subscriptionKeys = listOf(weeklySku, yearlySku)
                }
                else{
                    if (RemoteConfigValues.getInstance().isRemotePaymentNoTrial&&RemoteConfigValues.getInstance().remotePriceNewPlan=="yearly_notrial"){
                        subscriptionKeys = listOf(RemoteConfigValues.getInstance().remotePriceNewPlan)
                    }
                   else if (RemoteConfigValues.getInstance().isRemotePaymentNoTrial&&RemoteConfigValues.getInstance().remotePriceNewPlan=="weekly_notrial"){
                        subscriptionKeys = listOf(RemoteConfigValues.getInstance().remotePriceNewPlan)
                    }
                    else{
                        subscriptionKeys = listOf(weeklySku, yearlySku)
                    }
                }
                manager = IapConnector(
                    context,
                    //nonConsumableKeys = nonConsumableKeys,
                    subscriptionKeys = subscriptionKeys,
                    key = null
                )
            }
            return manager!!
        }
    }

    fun getOriginPricingPhase(productDetails: ProductDetails): ProductDetails.PricingPhase? {
        return productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()
    }
}

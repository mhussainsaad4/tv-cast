package com.sensustech.mytvcast.utils.iap

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.sensustech.mytvcast.quimera.QuimeraInit
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class BillingService(
    private val context: Context,
    private val nonConsumableKeys: List<String>,
    private val consumableKeys: List<String>,
    private val subscriptionSkuKeys: List<String>
) : IBillingService(), PurchasesUpdatedListener, BillingClientStateListener,
    AcknowledgePurchaseResponseListener {

    private lateinit var billingClient: BillingClient
    private var decodedKey: String? = null

    private var enableDebug: Boolean = false

    private val productDetails = mutableMapOf<String, ProductDetails>()

    override fun init(key: String?) {
        decodedKey = key
        billingClient =
            BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build()
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        isBillingClientConnected(billingResult.isOk(), billingResult.responseCode)
        log("onBillingSetupFinishedOkay: billingResult: $billingResult")
    }

    override fun restore(isRestoreFlow: Boolean, isSilentCheck: Boolean) {
        nonConsumableKeys.queryProductDetails(BillingClient.ProductType.INAPP) {
            consumableKeys.queryProductDetails(BillingClient.ProductType.INAPP) {
                subscriptionSkuKeys.queryProductDetails(BillingClient.ProductType.SUBS) {
                    GlobalScope.launch {
                        queryPurchases(isRestoreFlow, isSilentCheck)
                    }
                }
            }
        }
    }

    private suspend fun queryPurchases(
        isRestoreFlow: Boolean = false,
        isSilentCheck: Boolean = false
    ) {
        val inAppResult: PurchasesResult =
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        processPurchases(inAppResult.purchasesList, isRestore = true)
        val subsResult: PurchasesResult =
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        processPurchases(subsResult.purchasesList, isRestore = true)
        if (subsResult.purchasesList.isEmpty()) {
            subscriptionsExpired()
        }
        if (isRestoreFlow) {
            productRestored(
                subsResult.purchasesList.isNotEmpty()
                        || inAppResult.purchasesList.isNotEmpty(), isSilentCheck
            )
        }
    }

    override fun buy(activity: Activity, productDetails: ProductDetails) {
        if (!productDetails.productId.isProductReady()) {
            log("buy. Google billing service is not ready yet. (SKU is not ready yet -1)")
            return
        }
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build())).build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun subscribe(activity: Activity, productDetails: ProductDetails) {
        if (!productDetails.productId.isProductReady()) {
            log("buy. Google billing service is not ready yet. (SKU is not ready yet -1)")
            return
        }
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        productDetails.subscriptionOfferDetails?.getOrNull(0)?.offerToken?.let {
            productDetailsParamsBuilder.setOfferToken(it)
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build())).build()
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }

    override fun enableDebugLogging(enable: Boolean) {
        this.enableDebug = enable
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        log("onPurchasesUpdated: responseCode:$responseCode debugMessage: $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                log("onPurchasesUpdated. purchase: $purchases")
                if (purchases != null){
                    QuimeraInit.userPurchased(context,purchases)
                }
                processPurchases(purchases)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                log("onPurchasesUpdated: User canceled the purchase")
                QuimeraInit.userCancelBilling(context)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                log("onPurchasesUpdated: The user already owns this item")
                QuimeraInit.itemAlreadyOwned(context)
                //item already owned? call queryPurchases to verify and process all such items
                GlobalScope.launch {
                    queryPurchases()
                }
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.e(
                    TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                            "does not recognize the configuration. If you are just getting started, " +
                            "make sure you have configured the application correctly in the " +
                            "Google Play Console. The SKU product ID must match and the APK you " +
                            "are using must be signed with release keys."
                )
                QuimeraInit.developerError(context)
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                QuimeraInit.serviceDisconnected(context)
                Log.e("onPurchasesUpdated:", "service disconnected")
            }

            BillingClient.BillingResponseCode.ERROR -> {
                QuimeraInit.billingError(context)
                Log.e("onPurchasesUpdated:"," billing error")
            }

            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                QuimeraInit.serviceUnavailable(context)
                Log.e("onPurchasesUpdated:"," service unavailable")
            }

            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                QuimeraInit.billingUnavailable(context)
                Log.e("onPurchasesUpdated:"," billing unavailable")
            }

            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                QuimeraInit.itemUnavailable(context)
                Log.e("onPurchasesUpdated:"," item unavailable")
            }

            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                QuimeraInit.itemNotOwned(context)
                Log.e("onPurchasesUpdated:"," item not owned")
            }
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?, isRestore: Boolean = false) {
        if (!purchasesList.isNullOrEmpty()) {
            log("processPurchases: " + purchasesList.size + " purchase(s)")
            purchases@ for (purchase in purchasesList) {
                val purchaseSuccess = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                        || purchase.purchaseState == Purchase.PurchaseState.PENDING
                if (purchaseSuccess && purchase.products[0].isProductReady()) {
                    if (!isSignatureValid(purchase)) {
                        log("processPurchases. Signature is not valid for: $purchase")
                        continue@purchases
                    }
                    val productDetails = productDetails[purchase.products[0]]
                    when (productDetails?.productType) {
                        BillingClient.ProductType.INAPP -> {
                            when {
                                consumableKeys.contains(purchase.products[0]) -> {
                                    billingClient.consumeAsync(
                                        ConsumeParams.newBuilder()
                                            .setPurchaseToken(purchase.purchaseToken).build()
                                    ) { billingResult, _ ->
                                        when (billingResult.responseCode) {
                                            BillingClient.BillingResponseCode.OK -> {
                                                productOwned(purchase, false)
                                            }
                                            else -> {
                                                Log.d(
                                                    TAG,
                                                    "Handling consumables : Error during consumption attempt -> ${billingResult.debugMessage}"
                                                )
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    productOwned(purchase, isRestore)
                                }
                            }
                        }
                        BillingClient.ProductType.SUBS -> {
                            subscriptionOwned(purchase, isRestore)
                        }
                    }
                    if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken).build()
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
                    }
                } else {
                    Log.e(
                        TAG, "processPurchases failed. purchase: $purchase " +
                                "purchaseState: ${purchase.purchaseState} isSkuReady: ${purchase.products[0].isProductReady()}"
                    )
                }
            }
        } else {
            log("processPurchases: with no purchases")
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        val key = decodedKey ?: return true
        return Security.verifyPurchase(key, purchase.originalJson, purchase.signature)
    }

    private fun List<String>.queryProductDetails(type: String, done: () -> Unit) {
        if (::billingClient.isInitialized.not() || !billingClient.isReady || this.isEmpty()) {
            log("querySkuDetails. Google billing service is not ready yet.")
            done()
            return
        }
        val productList = this.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(type)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            if (billingResult.isOk()) {
                productDetailsList.forEach {
                    productDetails[it.productId] = it
                }
                updatePrices(productDetails)
            }
            done()
        }
    }

    private fun String.isProductReady(): Boolean {
        return productDetails.containsKey(this) && productDetails[this] != null
    }

    override fun onBillingServiceDisconnected() {

        try {
            log("onBillingServiceDisconnected")
            billingClient.startConnection(this)
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        log("onAcknowledgePurchaseResponse: billingResult: $billingResult")
    }

    override fun close() {
        billingClient.endConnection()
        super.close()
    }

    private fun BillingResult.isOk(): Boolean {
        return this.responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun log(message: String) {
        if (enableDebug) {
            Log.d(TAG, message)
        }
    }

    companion object {
        const val TAG = "GoogleBillingService"
    }
}
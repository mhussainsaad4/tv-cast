package com.sensustech.mytvcast.quimera

import com.android.billingclient.api.ProductDetails

class SubscriptionItem(productDetails: ProductDetails) : ProductItem(productDetails)
{
//    var subscribedItem: SubscribedItem? = null

    var offerToken = getSubscriptionOfferDetails()?.offerToken

    // get the price of base plan
    var pricingPhase: ProductDetails.PricingPhase? = getSubscriptionOfferDetails()?.pricingPhases?.pricingPhaseList?.last()

    // get first of offers if have
    private fun getSubscriptionOfferDetails(): ProductDetails.SubscriptionOfferDetails? {
        return productDetails.subscriptionOfferDetails?.first()
    }
}
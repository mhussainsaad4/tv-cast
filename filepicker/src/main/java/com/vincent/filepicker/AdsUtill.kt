package com.vincent.filepicker

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import com.amazon.device.ads.*
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener
import org.json.JSONObject


open class AdsUtill(mContext: Context) {

    var ctx: Context? = null

    private var banner: IronSourceBannerLayout? = null
    private var bannerContainer: FrameLayout? = null

    companion object {
        const val DEFAULT_INTERSTITIAL = "DefaultInterstitial"
        const val DefaultBanner = "DefaultBanner"
    }

    init {

        this.ctx = mContext
//        log("Initing Mobile Ads SDK...")
    }

    fun AddBannerToLayout(
        bannerContain: FrameLayout,
        context: Activity,
        loadCallback: AdLoadedCallback?
    ) {


        if (Util.isPremium(context)) {
            return
        }

        /*      if (!isSDKInitialized) {
                  Log.d("SDK Not Initialized.","initialized")
              }*/
        else {

            this.bannerContainer = bannerContain
            if (bannerContainer != null && bannerContainer!!.childCount > 0 &&
                bannerContainer!!.getChildAt(0) is IronSourceBannerLayout
            ) {
                IronSource.destroyBanner(bannerContainer!!.getChildAt(0) as IronSourceBannerLayout)
                bannerContainer!!.removeAllViews()
                Log.d("main_banner_failed", "destroy banner")
            }
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                try {

                    val size = ISBannerSize.BANNER
                    size.isAdaptive = true
                    banner = IronSource.createBanner(context, size)

                    val layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )

                    bannerContainer?.addView(banner, 0, layoutParams)
                    banner?.levelPlayBannerListener = object : LevelPlayBannerListener {
                        override fun onAdLoaded(adInfo: AdInfo) {
                            Log.e("banner>>>>", "Banner Ad Loaded, Adding to Layout")
                            loadCallback?.addLoaded(true)
                        }

                        override fun onAdLoadFailed(p0: IronSourceError?) {
                            Log.e("banner>>>>", "=========>${p0}")
                            bannerContainer?.removeAllViews()
                            loadCallback?.addLoaded(false)
                        }

                        override fun onAdClicked(adInfo: AdInfo) {

                        }

                        override fun onAdScreenPresented(adInfo: AdInfo) {

                        }

                        override fun onAdScreenDismissed(adInfo: AdInfo) {

                        }

                        override fun onAdLeftApplication(adInfo: AdInfo) {

                        }

                    }

                    try {
                        IronSource.loadBanner(banner, DefaultBanner)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                } catch (var3: Exception) {
                    var3.printStackTrace()
//                log("Unable to AdBannerToLayout with Error:$var3")
                }
            }, 100)


        }
    }

    fun loadAPSBanner(
        bannerContainer: FrameLayout,
        context: Activity
    ) {
        val loader = DTBAdRequest()
        loader.setSizes(DTBAdSize(320, 50, Constant.APS_Banner))
        loader.loadAd(object : DTBAdCallback {
            override fun onFailure(p0: AdError) {
                Log.e("banner>>>>", "APS_onFailure: ${p0.message}")
                Log.e("banner>>>>", "APS_onFailure: ${p0.code}")
                try {
                    AddBannerToLayout(bannerContainer, context, object : AdLoadedCallback {
                        override fun addLoaded(success: Boolean?) {}
                    })
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            override fun onSuccess(dtbAdResponse: DTBAdResponse) {
                Log.e("banner>>>>", "APS_onSuccess: $dtbAdResponse")

                // Append the APS bid parameters to ironSource mediation to add APS to the next ad request
                try {
                    val apsDataJsonBN = JSONObject()
                    apsDataJsonBN.put("bidInfo", SDKUtilities.getBidInfo(dtbAdResponse))
                    apsDataJsonBN.put(
                        "pricePointEncoded",
                        SDKUtilities.getPricePoint(dtbAdResponse)
                    )
                    apsDataJsonBN.put("uuid", Constant.APS_Banner)
                    apsDataJsonBN.put("width", dtbAdResponse.dtbAds[0].width)
                    apsDataJsonBN.put("height", dtbAdResponse.dtbAds[0].height)

                    // Define APS data per interstitial ad unit
                    val apsDataJson = JSONObject()
                    apsDataJson.put(IronSource.AD_UNIT.BANNER.toString(), apsDataJsonBN)
                    IronSource.setNetworkData("APS", apsDataJson)

                    //call banner
                    AddBannerToLayout(bannerContainer, context, object : AdLoadedCallback {
                        override fun addLoaded(success: Boolean?) {}
                    })
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

        })
    }

}
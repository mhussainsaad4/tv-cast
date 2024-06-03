package com.sensustech.mytvcast.utils.openAd;

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.sensustech.mytvcast.MainApplication
import com.sensustech.mytvcast.utils.RemoteConfigValues
import com.sensustech.mytvcast.utils.openAd.base.BaseManager
import com.sensustech.mytvcast.utils.openAd.delay.DelayType
import com.sensustech.mytvcast.utils.openAd.delay.InitialDelay
import com.sensustech.mytvcast.utils.openAd.logs.logDebug
import com.sensustech.mytvcast.utils.openAd.logs.logError

/**
 * @AppOpenManager = A class that handles all of the App Open Ad operations.
 *
 * Constructor arguments:
 * @param application Required to keep a track of App's state.
 * @param adUnitId Pass your created AdUnitId
 * @param initialDelay for Initial Delay
 *
 * @param adRequest = Pass a customised AdRequest if you have any.
 * @see AdRequest
 *
 * @param orientation Ad's Orientation, Can be PORTRAIT or LANDSCAPE (Default is Portrait)
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE
 *
 */


class AppOpenManager @JvmOverloads private constructor(
    application: Application,
    initialDelay: InitialDelay,
    var adUnitId: String,
    override var adRequest: AdRequest = AdRequest.Builder().build(),
    override var orientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT

) : BaseManager(application),
    LifecycleObserver {



    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        this.initialDelay = initialDelay
    }


    var isEnabledNoAdsBoolean = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        var appOpenManager: AppOpenManager? = null
        fun initialize(
            myApplication: Application,
            initialDelay: InitialDelay,
            adUnitId: String,
            adRequest: AdRequest = AdRequest.Builder().build(),
            orientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT
        ) {
            appOpenManager =
                AppOpenManager(myApplication, initialDelay, adUnitId, adRequest, orientation)



        }


    }


    fun isEnabledNoAds(): Boolean {
        return isEnabledNoAdsBoolean
    }

    fun setEnabledNoAds(enabledNoAds: Boolean) {
        isEnabledNoAdsBoolean = enabledNoAds
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        if (!isEnabledNoAdsBoolean) {
            if (initialDelay != InitialDelay.NONE) {
                saveInitialDelayTime()
            }
            if (RemoteConfigValues.getInstance().isRemoteAppOpenBackground) {
                if (!MainApplication.isSplash) {
                    showAdIfAvailableBackground()
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        if (!isEnabledNoAdsBoolean) {
            if (initialDelay != InitialDelay.NONE) {
                saveInitialDelayTime()
            }
            if (RemoteConfigValues.getInstance().isRemoteAppOpenBackground) {
                if (!MainApplication.isSplash) {
                    showAdIfAvailableBackground()
                }
            }
        }
    }


    /* @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
     private fun onStop() {
         appOpenAd = null
         isShowingAd = false
         fetchAd()
     }*/


    // Let's fetch the Ad
     fun fetchAd() {
        if (isAdAvailable() && !isEnabledNoAdsBoolean) return
        loadAd()
//        logDebug("A pre-cached Ad was not available, loading one.")
    }

    // Show the Ad if the conditions are met.
    fun showAdIfAvailable(onShowAdCompleteListener:OnShowAdCompleteListener) {
        if (isEnabledNoAdsBoolean) {
            onShowAdCompleteListener.onShowAdComplete()
            return
        }
        logDebug("appOpenAd: " + appOpenAd?.adUnitId)
        logDebug("isShowingAd: $isShowingAd")
        logDebug("isAdAvailable: " + isAdAvailable())
        logDebug("isInitialDelayOver: " + isInitialDelayOver())

        if (!isShowingAd &&
            isAdAvailable() &&
            isInitialDelayOver()
        ) {
            logDebug("loading.......: ")
            logDebug("isInitialDelayOver: " + isInitialDelayOver())
//            appOpenAd?.fullScreenContentCallback = getFullScreenContentCallback(onShowAdCompleteListener)
            appOpenAd?.fullScreenContentCallback =object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    fetchAd()
                    onShowAdCompleteListener.onShowAdComplete()
                }

                override fun onAdShowedFullScreenContent() {
                   isShowingAd=true
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    onShowAdCompleteListener.onShowAdComplete()
                }

            }
            MainApplication.ad_format="OpenAd"
            appOpenAd?.onPaidEventListener=MainApplication.getApplication().myCallback

            currentActivity?.let { appOpenAd?.show(it) }

        } else {
            onShowAdCompleteListener.onShowAdComplete()
            logDebug("isShowingAd: else")
            if (!isInitialDelayOver()) logDebug("The Initial Delay period is not over yet.")
            /**
             *If the next session happens after the delay period is over
             * & under 4 Hours, we can show a cached Ad.
             * However the above will only work for DelayType.HOURS.
             */
            if (initialDelay.delayPeriodType != DelayType.DAYS ||
                initialDelay.delayPeriodType == DelayType.DAYS &&
                isInitialDelayOver()
            ) fetchAd()
        }
    }
 // Show the Ad if the conditions are met.
 private fun showAdIfAvailableBackground() {
        if (isEnabledNoAdsBoolean) {
            return
        }
        logDebug("appOpenAd: " + appOpenAd?.adUnitId)
//        logDebug("isShowingAd: $isShowingAd")
//        logDebug("isAdAvailable: " + isAdAvailable())
//        logDebug("isInitialDelayOver: " + isInitialDelayOver())

        if (!isShowingAd &&
            isAdAvailable() &&
            isInitialDelayOver()
        ) {
            logDebug("loading.......: ")
            logDebug("isInitialDelayOver: " + isInitialDelayOver())
            appOpenAd?.fullScreenContentCallback = getFullScreenContentCallbackBackground()
//            appOpenAd?.fullScreenContentCallback
            MainApplication.ad_format="OpenAd"
            appOpenAd?.onPaidEventListener=MainApplication.getApplication().myCallback

            currentActivity?.let { appOpenAd?.show(it) }

        } else {
            logDebug("isShowingAd: else")
            if (!isInitialDelayOver()) logDebug("The Initial Delay period is not over yet.")
            /**
             *If the next session happens after the delay period is over
             * & under 4 Hours, we can show a cached Ad.
             * However the above will only work for DelayType.HOURS.
             */
            if (initialDelay.delayPeriodType != DelayType.DAYS ||
                initialDelay.delayPeriodType == DelayType.DAYS &&
                isInitialDelayOver()
            ) fetchAd()
        }
    }

    private fun loadAd() {
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(p0: AppOpenAd) {
                this@AppOpenManager.appOpenAd = p0
                this@AppOpenManager.loadTime = getCurrentTime()
                /*AnalyticsManager.getInstance().sendAnalytics(
                    AnalyticsManager.LOADED,
                    "open_ad"
                )*/

                logDebug("Ad Loaded")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                /* AnalyticsManager.getInstance().sendAnalytics(
                     AnalyticsManager.FAILED,
                     "open_ad"
                 )*/
                logError("Ad Failed To Load, Reason: ${p0.responseInfo}")
            }
        }
        AppOpenAd.load(
            getApplication(), adUnitId, adRequest, orientation,
            loadCallback as AppOpenAd.AppOpenAdLoadCallback
        )
    }

    // Handling the visibility of App Open Ad
    private fun getFullScreenContentCallback(onShowAdCompleteListener: OnShowAdCompleteListener): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                fetchAd()
                onShowAdCompleteListener.onShowAdComplete()
//                Log.e("Appopen", "onAdDismissedFullScreenContent.")
                /* AnalyticsManager.getInstance().sendAnalytics(
                     AnalyticsManager.DISMISSED,
                     "open_ad"
                 )*/
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                /* AnalyticsManager.getInstance().sendAnalytics(
                     AnalyticsManager.FAILED,
                     "open_ad"
                 )*/
//                logError("Ad Failed To Show Full-Screen Content: ${adError.message}")
                onShowAdCompleteListener.onShowAdComplete()
            }

            override fun onAdShowedFullScreenContent() {
                /* AnalyticsManager.getInstance().sendAnalytics(
                     AnalyticsManager.SHOWED,
                     "open_ad"
                 )*/
                isShowingAd = true
            }
        }
    }

    // Handling the visibility of App Open Ad background
    private fun getFullScreenContentCallbackBackground(): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                fetchAd()
//                Log.e("Appopen", "onAdDismissedFullScreenContent.")
                /* AnalyticsManager.getInstance().sendAnalytics(
                     AnalyticsManager.DISMISSED,
                     "open_ad"
                 )*/
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                /* AnalyticsManager.getInstance().sendAnalytics(
                     AnalyticsManager.FAILED,
                     "open_ad"
                 )*/
//                logError("Ad Failed To Show Full-Screen Content: ${adError.message}")
            }

            override fun onAdShowedFullScreenContent() {
                /* AnalyticsManager.getInstance().sendAnalytics(
                     AnalyticsManager.SHOWED,
                     "open_ad"
                 )*/
                isShowingAd = true
            }
        }
    }

    interface OnShowAdCompleteListener {

        fun onShowAdComplete()

    }
}
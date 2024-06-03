package com.sensustech.mytvcast.Utils;



import static com.sensustech.mytvcast.utils.Constant.DefaultInterstitial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.google.gson.Gson;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.my.tracker.MyTracker;
import com.sensustech.mytvcast.quimera.QuimeraInit;
import com.sensustech.mytvcast.quimera.SubscriptionItem;
import com.sensustech.mytvcast.utils.Constant;
import com.sensustech.mytvcast.utils.iap.BillingClientConnectionListener;
import com.sensustech.mytvcast.utils.iap.IapConnector;
import com.sensustech.mytvcast.utils.iap.PurchaseServiceListener;
import com.sensustech.mytvcast.utils.iap.RestoreServiceListener;
import com.sensustech.mytvcast.utils.iap.SubscriptionServiceListener;
import com.sensustech.mytvcast.utils.openAd.AppOpenManager;
import com.vincent.filepicker.AppPreferences;

import java.util.Map;

public class AdsManager implements PurchaseServiceListener, SubscriptionServiceListener, RestoreServiceListener {
    //private static final String INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712";//EJEMPLO GOOGLE
    private static final String INTERSTITIAL_AD_ID = "ca-app-pub-4665610594862277/6941937744";


    private String weeklySku = "com.sensustech.mytvcast.1week";
    private String yearlySku = "yearlysub_standard";

    private Activity context;
    private static volatile AdsManager instance;
    //private BillingProcessor bp;
    private IapConnector iapConnector;

    //private InterstitialAd mInterstitialAd;
    public boolean premiumOfferWasShown = false;
    public boolean adsIsReady = false;
    public boolean premiumOfferWasClosed = false;
    public boolean needReloadAds = true;

    public Map< String, ProductDetails > mapPrices = new ArrayMap<>();

    public static AdsManager getInstance() {
        AdsManager localInstance = instance;
        if (localInstance == null) {
            synchronized (AdsManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new AdsManager();
                }
            }
        }
        return localInstance;
    }

    public void init(Activity context) {
        this.context = context;
        /*bp = new BillingProcessor(context, null, this);
        bp.initialize();*/
        iapConnector = IapConnector.Companion.getInstance(context);
        iapConnector.addPurchaseListener(this);
        iapConnector.addSubscriptionListener(this);
        iapConnector.addRestoreListener(this);
        iapConnector.addBillingConnectedListener(new BillingClientConnectionListener() {
            @Override
            public void onConnected(boolean status, int billingResponseCode) {
                iapConnector.getPrices();
            }
        });
    }

    public void initIntersitial() {
        /*AdRequest adRequest = new AdRequest.Builder().build();
       //boolean test = adRequest.isTestDevice(context);
        InterstitialAd.load(context,INTERSTITIAL_AD_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        //Log.d("TAG", "The ad was dismissed.");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when fullscreen content failed to show.
                        //Log.d("TAG", "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null;
                        //Log.d("TAG", "The ad was shown.");
                    }
                });

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAd = null;
            }


        });*/


        /*mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId("ca-app-pub-4975991316875268/8703448269");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() { }
            @Override
            public void onAdFailedToLoad(int errorCode) { }
            @Override
            public void onAdOpened() {}
            @Override
            public void onAdClicked() {}
            @Override
            public void onAdLeftApplication() {}
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });*/
    }

    public void showAds() {
        /*if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }*/
        IronSource.setInterstitialListener(new InterstitialListener() {
            @Override
            public void onInterstitialAdReady() {
                Log.d("AdsManager", "onInterstitialAdReady: ");
                IronSource.showInterstitial();
            }

            @Override
            public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                Log.d("AdsManager", "onInterstitialAdLoadFailed: ");

            }

            @Override
            public void onInterstitialAdOpened() {
                Log.d("AdsManager", "onInterstitialAdOpened: ");

            }

            @Override
            public void onInterstitialAdClosed() {
//                IronSource.loadInterstitial();
                Log.d("AdsManager", "onInterstitialAdClosed: ");
                //initIntersitial();
            }

            @Override
            public void onInterstitialAdShowSucceeded() {
                Log.d("AdsManager", "onInterstitialAdShowSucceeded: ");

            }

            @Override
            public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                Log.d("AdsManager", "onInterstitialAdShowFailed: ");

            }

            @Override
            public void onInterstitialAdClicked() {
                Log.d("AdsManager", "onInterstitialAdClicked: ");

            }
        });
        if (IronSource.isInterstitialReady()) {
            IronSource.showInterstitial(DefaultInterstitial);
        } else {
//            IronSource.loadInterstitial();
        }
        /*if (mInterstitialAd != null) {
            mInterstitialAd.show(context);
        }*/
    }

    public void getPrices() {
        if (iapConnector != null) {
            iapConnector.getPrices();
        }
    }

    public void restore(Boolean isSilentCheck) {
        if (iapConnector != null) {
            iapConnector.restore(isSilentCheck);
        }
    }

    public boolean isPremium(Activity ctx) {
        if (iapConnector == null) {
            init(ctx);
        }
        if (AppPreferences.getInstance(ctx).getBoolean("isLifetimePremium"))
            return true;
        if (AppPreferences.getInstance(ctx).getBoolean("isSubscriptionPremium"))
            return true;
        return false;
    }
    public boolean isPremiumAdapty(Activity ctx) {
        if (AppPreferences.getInstance(ctx).getBoolean("isLifetimePremium"))
            return true;
        if (AppPreferences.getInstance(ctx).getBoolean("isSubscriptionPremium"))
            return true;
        return false;
    }

    /*public BillingProcessor getBP() {
        return bp;
    }*/

    public void makeSubscription(Activity activity, String purchaseId) {
        trackSubscriptionClicked();
        if (iapConnector != null) {
            ProductDetails product = mapPrices.get(purchaseId);
            if (product != null) {
                iapConnector.subscribe(activity, product);
                QuimeraInit.INSTANCE.setSingleCall(true);
                QuimeraInit.INSTANCE.sendSkuDetails(activity, new SubscriptionItem(product));
            }
        }
    }

    public void makePurchase(Activity activity, String purchaseId) {
        trackLifetimeClicked();
        if (iapConnector != null) {
            ProductDetails product = mapPrices.get(purchaseId);
            if (product != null) {
                iapConnector.purchase(activity, product);
            }
        }

    }

    public void checkPremium() {
        try {
            if (context != null) {
                context.sendBroadcast(new Intent("CHECK_PREMIUM"));
            }
        } catch (Exception e) {
        }
    }

    public void closePremium() {
        try {
            if (context != null) {
                context.sendBroadcast(new Intent("CLOSE_PREMIUM"));
            }
        } catch (Exception e) {
        }
    }

    /*@Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        if(productId.equals("com.sensustech.mytvcast.lifetime")) {
            trackLifetimeCompleted();
            AppPreferences.getInstance(context).saveData("isLifetimePremium",true);
        }else {
            trackSubscriptionCompleted();
        }
        checkPremium();
        closePremium();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        checkPremium();
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {}

    @Override
    public void onBillingInitialized() {
        if (bp != null) {
            try {
                boolean isSubsUpdateSupported = bp.isSubscriptionUpdateSupported();
                if (isSubsUpdateSupported) {
                    bp.loadOwnedPurchasesFromGoogle();
                }
            }catch (Exception e){}
            // TODO: 17/6/21 eliminar necesidad de esto
            try {
                if (context != null) {
                    context.sendBroadcast(new Intent("BILLING_INIT"));
                }
            }catch (Exception e) {}
        }
    }*/

    public void trackSubscriptionClicked() {
        MyTracker.trackEvent("subscriptionSelected");
        MyTracker.flush();
    }

    public void trackSubscriptionCompleted() {
        MyTracker.trackEvent("subscriptionCompleted");
        MyTracker.flush();
    }

    public void trackLifetimeClicked() {
        MyTracker.trackEvent("lifetimeSelected");
        MyTracker.flush();
    }

    public void trackLifetimeCompleted() {
        MyTracker.trackEvent("lifetimeCompleted");
        MyTracker.flush();
    }

    @Override
    public void onPricesUpdated(@NonNull Map< String, ProductDetails > iapKeyPrices) {
        mapPrices.putAll(iapKeyPrices);
    }

    @Override
    public void onProductPurchased(@NonNull Purchase purchaseInfo) {
        trackLifetimeCompleted();
        AppPreferences.getInstance(context).saveData("isLifetimePremium", true);
        AppOpenManager appOpenManager = AppOpenManager.Companion.getAppOpenManager();

        if (appOpenManager != null) {
            appOpenManager.setEnabledNoAds(true);
        }
        checkPremium();
        closePremium();
        Constant.purchase=purchaseInfo;
    }

    @Override
    public void onProductRestored(@NonNull Purchase purchaseInfo) {
        AppPreferences.getInstance(context).saveData("isLifetimePremium", true);
        AppOpenManager appOpenManager = AppOpenManager.Companion.getAppOpenManager();
        if (appOpenManager != null) {
            appOpenManager.setEnabledNoAds(true);
        }
        checkPremium();
    }

    @Override
    public void onSubscriptionRestored(@NonNull Purchase purchaseInfo) {
        AppPreferences.getInstance(context).saveData("isSubscriptionPremium", true);
        AppOpenManager appOpenManager = AppOpenManager.Companion.getAppOpenManager();
        if (appOpenManager != null) {
            appOpenManager.setEnabledNoAds(true);
        }
        checkPremium();
        Constant.purchase=purchaseInfo;
    }

    @Override
    public void onSubscriptionPurchased(@NonNull Purchase purchaseInfo) {
        trackSubscriptionCompleted();
        AppPreferences.getInstance(context).saveData("isSubscriptionPremium", true);
        AppOpenManager appOpenManager = AppOpenManager.Companion.getAppOpenManager();
        if (appOpenManager != null) {
            appOpenManager.setEnabledNoAds(true);
        }
        checkPremium();
        closePremium();
        Constant.purchase=purchaseInfo;

    }

    @Override
    public void onSubscriptionsExpired() {
        AppPreferences.getInstance(context).saveData("isSubscriptionPremium", false);
        AppOpenManager appOpenManager = AppOpenManager.Companion.getAppOpenManager();
        if (appOpenManager != null) {
            appOpenManager.setEnabledNoAds(false);
        }
        checkPremium();
    }

    @Override
    public void productRestored() {
        Log.e("My_TV_Cast_TAG", "productRestored");
        Toast.makeText(context, "Your purchase was restored", Toast.LENGTH_LONG).show();
    }

    @Override
    public void noProductRestored(boolean isSilentCheck) {
        Log.e("My_TV_Cast_TAG", "noProductRestored");
        if (!isSilentCheck)
            Toast.makeText(context, "Your current Google Play Store account has no purchases", Toast.LENGTH_LONG).show();
    }
}

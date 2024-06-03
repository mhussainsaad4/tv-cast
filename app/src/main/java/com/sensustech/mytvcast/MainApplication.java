package com.sensustech.mytvcast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.impressionData.ImpressionData;
import com.ironsource.mediationsdk.impressionData.ImpressionDataListener;
import com.my.tracker.MyTracker;
import com.my.tracker.MyTrackerParams;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.utils.Constant;
import com.sensustech.mytvcast.utils.openAd.AppOpenManager;
import com.sensustech.mytvcast.utils.openAd.delay.InitialDelay;
import com.vincent.filepicker.AppPreferences;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.OkHttpClient;

public class MainApplication extends MultiDexApplication implements OnPaidEventListener {

    public static final String IRON_SOURCE_APP_KEY = "fc229449";
//    public static final String IRON_SOURCE_APP_KEY = "85460dcd"; // test
public static final String APS_APP_KEY = "6a5d89b3-5e57-44c3-ae25-152573497bf7";
    public static Context context;
    private static MainApplication jesusApplication;
    public static String ad_format="";
    public static Boolean  isIronSourceInit = false;
    public static Boolean  isSplash = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
//        AppPreferences.getInstance(context).saveData("isLifetimePremium", true);
        setApplication(MainApplication.this);
        MyTracker.initTracker("34473850183167233646", this);
        setUserInfo();
        setAdjust();
        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
// Adding an Network Interceptor for Debugging purpose :
//        OkHttpClient okHttpClient = new OkHttpClient() .newBuilder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .addNetworkInterceptor(new S)
//                .build();
        try {
            // Google Play will install latest OpenSSL
            ProviderInstaller.installIfNeeded(getApplicationContext());
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
                | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        try {
            // Adding an Network Interceptor for Debugging purpose :
            AndroidNetworking.initialize(getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
               /* if (BuildConfig.DEBUG) {
                    List<String> testDeviceIds = Arrays.asList("4486F8359D68B01582819D2E8E9358E2","82B79F49C48BB1AF7A285885110AA479");
                    RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
                    MobileAds.setRequestConfiguration(configuration);
                }*/
                Intent intent = new Intent("ADS_READY");
                sendBroadcast(intent);
                AdsManager.getInstance().adsIsReady = true;

            }
        });
        initializationAds();
        loadImpression();

    }

    private void setApplication(MainApplication application) {
        jesusApplication = application;
    }

    public FirebaseAnalytics mFirebaseAnalytics;

    public FirebaseAnalytics getFirebaseAnalytics() {
        try {
            if (mFirebaseAnalytics == null)
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFirebaseAnalytics;
    }

    public void setUserInfo() {
        String userId = AppPreferences.getInstance(this).getString("mtUserId");
        if (userId == null || userId.length() == 0) {
            try {
                userId = generateUserID();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AppPreferences.getInstance(this).saveData("mtUserId", userId);
        }
        MyTrackerParams trackerParams = MyTracker.getTrackerParams();
        trackerParams.setCustomUserId(userId);
    }

    private static void loadImpression() {
        IronSource.addImpressionDataListener(new ImpressionDataListener() {
            @Override
            public void onImpressionSuccess(ImpressionData impressionData) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.AD_PLATFORM, "ironSource");
                    bundle.putString(FirebaseAnalytics.Param.AD_SOURCE, impressionData.getAdNetwork());
                    bundle.putString(FirebaseAnalytics.Param.AD_FORMAT, impressionData.getAdUnit());
                    bundle.putString(FirebaseAnalytics.Param.AD_UNIT_NAME, impressionData.getInstanceName());
                    bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
                    if (impressionData.getRevenue() == null) {
                        bundle.putDouble(FirebaseAnalytics.Param.VALUE, 0.0);
                    } else {
                        bundle.putDouble(FirebaseAnalytics.Param.VALUE, impressionData.getRevenue());
                    }

//                Log.d("comet", "impressionlog->" + bundle.toString());
                    MainApplication.getApplication().getFirebaseAnalytics().logEvent(Constant.FIREBASE_LOG_KEY, bundle);
                }catch (NullPointerException e){
                    e.printStackTrace();

                }

            }
        });

    }

    public String generateUserID() throws Exception {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }


    public static MainApplication getApplication() {
        return jesusApplication;
    }

    private void setAdjust() {

        String appToken = getString(R.string.adjust_id);
        String environment;
        if (BuildConfig.DEBUG) {

            environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        } else {

            environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        }
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        Adjust.onCreate(config);
    }


    private void initializationAds() {
        AppOpenManager.Companion.initialize(this, InitialDelay.NONE, getApplicationContext().getString(R.string.admob_open_ad_id), new AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT);


    }

    @Override
    public void onPaidEvent(@NonNull AdValue adValue) {

    }

    private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }
    }

    public OnPaidEventListener myCallback = new OnPaidEventListener() {

        @Override
        public void onPaidEvent(@NonNull AdValue adValue) {
            try {
                long valueMicros = adValue.getValueMicros();
                String currencyCode = adValue.getCurrencyCode();
                int precision = adValue.getPrecisionType();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.AD_FORMAT, ad_format);
//            bundle.putString(FirebaseAnalytics.Param.AD_UNIT_NAME, adUnitId)
                bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
                bundle.putDouble(FirebaseAnalytics.Param.VALUE, Double.parseDouble(precision+""));
//            Log.d("comet", "impressionlog->"+bundle);
                getFirebaseAnalytics().logEvent(
                        Constant.FIREBASE_LOG_KEY,
                        bundle
                ) ;
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }

        }
    };
}
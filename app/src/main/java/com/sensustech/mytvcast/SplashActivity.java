package com.sensustech.mytvcast;

import static com.sensustech.mytvcast.MainApplication.APS_APP_KEY;
import static com.sensustech.mytvcast.MainApplication.IRON_SOURCE_APP_KEY;
import static com.sensustech.mytvcast.MainApplication.isSplash;
import static com.sensustech.mytvcast.Ui.ShowMyAdaptyScreenKt.accessLevel;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adapty.Adapty;
import com.amazon.device.ads.AdRegistration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.mackhartley.roundedprogressbar.RoundedProgressBar;
import com.sensustech.mytvcast.quimera.QuimeraInit;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.utils.IronSourceAdsManger;
import com.sensustech.mytvcast.utils.RemoteConfig;
import com.sensustech.mytvcast.utils.RemoteConfigValues;
import com.sensustech.mytvcast.utils.openAd.AppOpenManager;
import com.sensustech.mytvcast.interfaces.onAdfailedToLoadListner;
import com.vincent.filepicker.AppPreferences;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        IntegrationHelper.validateIntegration(this);
        // Remote Config Fetch
        Adapty.activate(this, getString(R.string.public_sdk_key),false);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(20)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        RemoteConfig.INSTANCE.getRemoteconfig();
        accessLevel(this);

        if (savedInstanceState == null) {
            QuimeraInit.INSTANCE.initQuimeraSdk(getApplicationContext());
        }
        SeekBar seekBar = findViewById(R.id.my_seekbar);
        seekBar.setMax(200); // Set the maximum progress value
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 100; i++) {
                    seekBar.setProgress(i);
                    try {
                        Thread.sleep(50);  // Update progress every 50 milliseconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        isSplash=true;
        setFullScreenActivity();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchRemoteConfig();
            }
        }, 500);

        initIronSource();
    }

    public void setFullScreenActivity() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (!task.isSuccessful()) {

                }

//                RemoteConfigValues.getInstance().setRemoteOnboarding(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_ONBOARDING_FLAG));
//                RemoteConfigValues.getInstance().setRemotePaymentCard(false);
//                RemoteConfigValues.getInstance().setRemotePricePlan(mFirebaseRemoteConfig.getString(RemoteConfigValues.REMOTE_PRICE_PLAN_FLAG));
//                RemoteConfigValues.getInstance().setRemotePriceNewPlan(RemoteConfigValues.getInstance().getRemotePriceNewPlan());
//                RemoteConfigValues.getInstance().setRemotePaymentNoTrial(false);

                RemoteConfigValues.getInstance().setRemoteOnboarding(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_ONBOARDING_FLAG));
                RemoteConfigValues.getInstance().setRemotePaymentCard(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_PAYMENT_FLAG));
                RemoteConfigValues.getInstance().setRemotePricePlan(mFirebaseRemoteConfig.getString(RemoteConfigValues.REMOTE_PRICE_PLAN_FLAG));
                RemoteConfigValues.getInstance().setRemotePriceNewPlan(mFirebaseRemoteConfig.getString(RemoteConfigValues.REMOTE_PAYWAL_NOTRIAL_PLAN));
//                RemoteConfigValues.getInstance().setRemotePriceNewPlan("weekly_notrial");
                RemoteConfigValues.getInstance().setRemotePaymentNoTrial(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_PAYWAL_NOTRIAL));
                RemoteConfigValues.getInstance().setRemoteAppopenad(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_APPOPENAD));
                RemoteConfigValues.getInstance().setRemoteAppopenadBackground(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_APPOPENAD_BACKGROUND));
//                RemoteConfigValues.getInstance().setRemoteAppopenad(true);
//                RemoteConfigValues.getInstance().setRemoteAppopenadBackground(false);

                Log.d("RemoteConfig= ", "REMOTE_ONBOARDING_FLAG: "+mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_ONBOARDING_FLAG));
                Log.d("RemoteConfig= ", "REMOTE_PAYMENT_FLAG: "+mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_PAYMENT_FLAG));
                Log.d("RemoteConfig= ", "REMOTE_PRICE_PLAN_FLAG: "+mFirebaseRemoteConfig.getString(RemoteConfigValues.REMOTE_PRICE_PLAN_FLAG));
                Log.d("RemoteConfig= ", "REMOTE_PAYWAL_NOTRIAL_PLAN: "+mFirebaseRemoteConfig.getString(RemoteConfigValues.REMOTE_PAYWAL_NOTRIAL_PLAN));
                Log.d("RemoteConfig= ", "REMOTE_PAYWAL_NOTRIAL: "+mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_PAYWAL_NOTRIAL));
                Log.d("RemoteConfig= ", "REMOTE_APPOPENAD: "+mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_APPOPENAD));
                Log.d("RemoteConfig= ", "REMOTE_APPOPENAD_BACKGROUND: "+mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_APPOPENAD_BACKGROUND));
                nextActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                RemoteConfigValues.getInstance().setRemoteOnboarding(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_ONBOARDING_FLAG));
                RemoteConfigValues.getInstance().setRemotePaymentCard(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_PAYMENT_FLAG));
                RemoteConfigValues.getInstance().setRemotePricePlan(mFirebaseRemoteConfig.getString(RemoteConfigValues.REMOTE_PRICE_PLAN_FLAG));
                RemoteConfigValues.getInstance().setRemotePriceNewPlan(mFirebaseRemoteConfig.getString(RemoteConfigValues.REMOTE_PAYWAL_NOTRIAL_PLAN));
                RemoteConfigValues.getInstance().setRemotePaymentNoTrial(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_PAYWAL_NOTRIAL));
                RemoteConfigValues.getInstance().setRemoteAppopenad(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_APPOPENAD));
                RemoteConfigValues.getInstance().setRemoteAppopenadBackground(mFirebaseRemoteConfig.getBoolean(RemoteConfigValues.REMOTE_APPOPENAD_BACKGROUND));

                nextActivity();
            }
        });
    }

    private void nextActivity() {
        AdsManager.getInstance().init(this);
        AppOpenManager appOpenManager = AppOpenManager.Companion.getAppOpenManager();

        if (!AdsManager.getInstance().isPremium(this)){
            appOpenManager.fetchAd();
        }
        new Handler().postDelayed(() -> {
            if (RemoteConfigValues.getInstance().isRemoteAppOpen()) {
                if (appOpenManager != null) {
                    appOpenManager.showAdIfAvailable(this::navigate);
                }
                else {
                    navigate();
                }
            }
            else {
                navigate();
            }

        }, 5000);




    }

    private void navigate() {
        boolean isFirstTimeOnboarding = AppPreferences.getInstance(SplashActivity.this).getBoolean("isOnboarding");
        boolean isFirstTimeSubscription = AppPreferences.getInstance(SplashActivity.this).getBoolean("isSubscription");
        if (!isFirstTimeOnboarding && RemoteConfigValues.getInstance().isRemoteOnboarding()) {
            showLoadingScreen();
            Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            hideLoadingScreen(intent);
        } else if (!isFirstTimeSubscription && RemoteConfigValues.getInstance().isRemotePaymentCard() && !AdsManager.getInstance().isPremium(SplashActivity.this)) {
            showLoadingScreen();
            Intent it = new Intent(SplashActivity.this, SubscriptionActivity.class);
            it.putExtra("fromStart", true);
            hideLoadingScreen(it);

        } else if (!isFirstTimeSubscription &&!RemoteConfigValues.getInstance().isRemotePaymentCard() && !AdsManager.getInstance().isPremium(SplashActivity.this)) {
            if (RemoteConfigValues.getInstance().isRemotePaymentNoTrial()&& Objects.equals(RemoteConfigValues.getInstance().getRemotePriceNewPlan(), "yearly_notrial")) {
                showLoadingScreen();
                Intent it = new Intent(SplashActivity.this, SubscriptionNewActivity1.class);
                it.putExtra("fromStart", true);
                hideLoadingScreen(it);
            }
            if (RemoteConfigValues.getInstance().isRemotePaymentNoTrial()&& Objects.equals(RemoteConfigValues.getInstance().getRemotePriceNewPlan(), "weekly_notrial")) {
                showLoadingScreen();
                Intent it = new Intent(SplashActivity.this, SubscriptionNewActivity2.class);
                it.putExtra("fromStart", true);
                hideLoadingScreen(it);
            }
            else {
                showLoadingScreen();
                Intent it = new Intent(SplashActivity.this, SubscriptionActivity.class);
                it.putExtra("fromStart", true);
                hideLoadingScreen(it);
            }


        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    }

    public void showLoadingScreen() {
        findViewById(R.id.logoIv).setVisibility(View.GONE);
        findViewById(R.id.loadingScreen).setVisibility(View.VISIBLE);
    }

    public void hideLoadingScreen(Intent intent) {
        // Here is the code for show loading screen
        new Handler().postDelayed(() -> {
            findViewById(R.id.loadingScreen).setVisibility(View.GONE);
            startActivity(intent);
            finish();
        }, 3500);
    }


    //  Method that loads initializes the IronSource ads.
    private void initIronSource() {

//        AdRegistration.enableTesting(true);
//        AdRegistration.enableLogging(true);

        AdRegistration.getInstance(APS_APP_KEY, this);

        IronSource.init(this, IRON_SOURCE_APP_KEY, IronSource.AD_UNIT.INTERSTITIAL, IronSource.AD_UNIT.BANNER);
        IronSource.init(this, IRON_SOURCE_APP_KEY, IronSource.AD_UNIT.REWARDED_VIDEO);

//        IronSource.loadInterstitial();
        interIronsourceAdLoad();
//        IntegrationHelper.validateIntegration(this);
    }

    private void interIronsourceAdLoad(){
        try {
            IronSourceAdsManger.initiateAd(this, new onAdfailedToLoadListner() {
                @Override
                public void onAdFailedToLoad() {
                    //     showadsAdmob();
                }

                @Override
                public void onSuccess() {
                    //   showadsAdmob();
                }

                @Override
                public void adClose() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



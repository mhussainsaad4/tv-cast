package com.sensustech.mytvcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;
import com.android.billingclient.api.ProductDetails;
import com.ironsource.mediationsdk.IronSource;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.utils.Constant;
import com.sensustech.mytvcast.utils.CustomTypefaceSpan;
import com.sensustech.mytvcast.utils.ExtensionsKt;
import com.sensustech.mytvcast.utils.IronSourceAdsManger;
import com.sensustech.mytvcast.utils.RemoteConfigValues;
import com.sensustech.mytvcast.interfaces.adShowCallBack;
import com.vincent.filepicker.AppPreferences;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionActivity extends AppCompatActivity implements View.OnClickListener {
    TextView  tvTrial;
//    Button subscriptionButton;
    private String activeSku = "";
    private Boolean fromStart = false;
    private String weeklySku = "com.sensustech.mytvcast.1week";
    private String yearlySku = "yearlysub_standard";
    HashMap<String, ProductDetails> purchaseListingDetails;
    private boolean isButtonClickable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_new);
        AppPreferences.getInstance(SubscriptionActivity.this).saveData("isSubscription", true);
        activeSku = RemoteConfigValues.getInstance().getRemotePricePlan();

        setFullScreenActivity();
        purchaseListingDetails = new HashMap<>(1);
        fromStart = getIntent().getBooleanExtra("fromStart", false);

//        tvSubsFeature = findViewById(R.id.tv_subs_feature);
        tvTrial = findViewById(R.id.tv_trail);
//        subscriptionButton = findViewById(R.id.subscription_btn);

        findViewById(R.id.tv_policy).setOnClickListener(this);
        findViewById(R.id.tv_terms).setOnClickListener(this);
        findViewById(R.id.ic_close).setOnClickListener(this);
        findViewById(R.id.tv_desc).setOnClickListener(this);
//        findViewById(R.id.subscription_btn).setOnClickListener(this);
        findViewById(R.id.trial_btn).setOnClickListener(this);
        findViewById(R.id.tv_trail).setOnClickListener(this);

//        setFeatureText();
        readPrices();
        registerReceiver(premiumBroadcast, new IntentFilter("CLOSE_PREMIUM"));

        //List<String> list = new ArrayList<String>();
        //list.add(RemoteConfigValues.getInstance().getRemotePricePlan());
        //list.add("com.sensustech.mytvcast.1week3");
//        loadImpression();
    }

    BroadcastReceiver premiumBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Return info only when purchase is confirmed
            AdjustEvent adjustEvent = new AdjustEvent(Constant.ADJUST_TOKEN_SUBSCRIBED_USERS);
            Adjust.trackEvent(adjustEvent);

            if (purchaseListingDetails != null && purchaseListingDetails.size() > 0) {

                if (Constant.currentSku.contentEquals(weeklySku)) {
                    if (purchaseListingDetails.get(Constant.currentSku).getSubscriptionOfferDetails().size() > 0) {
                        AdjustEvent adjustEvent1 = new AdjustEvent(Constant.ADJUST_TOKEN_WEKKLY_SUB);
                        long price = purchaseListingDetails.get(Constant.currentSku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros();
                        String currencyCode = purchaseListingDetails.get(Constant.currentSku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceCurrencyCode();
                        adjustEvent1.setRevenue(price, currencyCode);
                        adjustEvent1.setOrderId(Constant.purchase.getOrderId());
                        Adjust.trackEvent(adjustEvent1);
                    }
                } else if (Constant.currentSku.contentEquals(yearlySku)) {
                    if (purchaseListingDetails.get(Constant.currentSku).getSubscriptionOfferDetails().size() > 0) {
                        AdjustEvent adjustEvent2 = new AdjustEvent(Constant.ADJUST_TOKEN_Yearly_Subs);
                        long price = purchaseListingDetails.get(Constant.currentSku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros();
                        String currencyCode = purchaseListingDetails.get(Constant.currentSku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceCurrencyCode();
                        adjustEvent2.setRevenue(price, currencyCode);
                        adjustEvent2.setOrderId(Constant.purchase.getOrderId());
                        Adjust.trackEvent(adjustEvent2);
                    }
                }
                if (fromStart) {
                    startActivity(new Intent(SubscriptionActivity.this, MainActivity.class));
                } else {
                    finish();
                }
            }
        }
    };

    public void readPrices() {
        //Replacement for AsyncTask deprecation
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {


                do {
                    if (!AdsManager.getInstance().mapPrices.isEmpty()) {
                        for (String purchaseId : AdsManager.getInstance().mapPrices.keySet()) {
                            if (purchaseId.equals(weeklySku)) {
                                purchaseListingDetails.put(weeklySku, AdsManager.getInstance().mapPrices.get(purchaseId));
                            }
                            if (purchaseId.equals(yearlySku)) {
                                purchaseListingDetails.put(yearlySku, AdsManager.getInstance().mapPrices.get(purchaseId));
                            }
                        }
                    }
                    //Waits for completion
                    if (purchaseListingDetails.size() >= 1)
                        break;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        String formattedPriceWeekly = "";
                        String formattedPriceYearly = "";
                        if (purchaseListingDetails.get(weeklySku).getSubscriptionOfferDetails().size() > 0) {
                            if (purchaseListingDetails.get(weeklySku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size() == 1) {
                                formattedPriceWeekly = purchaseListingDetails.get(weeklySku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                            } else if (purchaseListingDetails.get(weeklySku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size() > 1) {
                                formattedPriceWeekly = purchaseListingDetails.get(weeklySku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(1).getFormattedPrice();
                            } else {
                                formattedPriceWeekly = "n/a";
                            }
                        }

                        if (purchaseListingDetails.get(yearlySku).getSubscriptionOfferDetails().size() > 0) {
                            if (purchaseListingDetails.get(yearlySku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size() == 1) {
                                formattedPriceYearly = purchaseListingDetails.get(yearlySku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                            } else if (purchaseListingDetails.get(yearlySku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().size() > 1) {
                                formattedPriceYearly = purchaseListingDetails.get(yearlySku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(1).getFormattedPrice();
                            } else {
                                formattedPriceYearly = "n/a";
                            }
                        }


                        //Log.d("King", activeSku + " -> " + formattedPriceWeekly);
                        if (purchaseListingDetails.get(activeSku).getProductId().contains("yearly")) {
                            //formattedPriceWeekly = purchaseListingDetails.get(activeSku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(1).getFormattedPrice();
//                            subscriptionButton.setText("SUBSCRIBE FOR " + formattedPriceWeekly + "/WEEKLY");
                            String txt = "Unlimited free access for 3 days,\nthen " + formattedPriceYearly + " per Year";

                            setWeeklyText(txt);
                        } else if (purchaseListingDetails.get(activeSku).getProductId().contains("week")) {
                            //formattedPriceWeekly = purchaseListingDetails.get(activeSku).getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(1).getFormattedPrice();
//                            subscriptionButton.setText("SUBSCRIBE FOR " + formattedPriceYearly + "/Yearly");
                            String txt = "Unlimited free access for 3 days,\nthen " + formattedPriceWeekly + " per Week";

                            setWeeklyText(txt);
                        }


                    }
                });
            }
        });


    }

    private void
    setWeeklyText(String txt) {
        String ourStr = txt;

//        SpannableString ourSpannable = new SpannableString(ourStr);
//        ourSpannable.setSpan(new CustomTypefaceSpan(ResourcesCompat.getFont(this, R.font.inter_regular)), 0, "Enjoy a ".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        ourSpannable.setSpan(new CustomTypefaceSpan(ResourcesCompat.getFont(this, R.font.inter_regular)), ourStr.indexOf("then"), ourStr.indexOf("then") + "then".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        ourSpannable.setSpan(new CustomTypefaceSpan(ResourcesCompat.getFont(this, R.font.inter_regular)), ourStr.indexOf("Cancel"), ourStr.indexOf("Cancel") + "Cancel your subscription anytime".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        tvTrial.setText(txt);
    }

    public void setFeatureText() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String ourStr = "GET FULL\n";
        SpannableString ourSpannable = new SpannableString(ourStr);
        ourSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.tt_black)), 0, ourStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(ourSpannable);

        String tosStr = "SCREEN\nMIRRORING";
        SpannableString tosSpannable = new SpannableString(tosStr);
        tosSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorSubsFeature)), 0, tosStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(tosSpannable);

//        tvSubsFeature.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public void setFullScreenActivity() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_policy:
                openURL(ExtensionsKt.privacy);
                break;
            case R.id.tv_terms:
                openURL(ExtensionsKt.terms);
                break;
            case R.id.ic_close:
//                showInterAndMoveToNext("ic_close");
                AdsManager.getInstance().premiumOfferWasClosed = true;
                onBackPressed();
                break;
           /* case R.id.subscription_btn:
                if (activeSku.contains("yearly")) {
                    Constant.currentSku = weeklySku;
                    AdsManager.getInstance().makeSubscription(SubscriptionActivity.this, weeklySku);
                } else {
                    Constant.currentSku = yearlySku;
                    AdsManager.getInstance().makeSubscription(SubscriptionActivity.this, yearlySku);
                }

                break;*/
            case R.id.trial_btn:
                btnPressDelay();
                Constant.currentSku = activeSku;
                AdsManager.getInstance().makeSubscription(SubscriptionActivity.this, activeSku);
                break;

        }
    }
    public void btnPressDelay(){
        findViewById(R.id.trial_btn).setEnabled(false);
        if (isButtonClickable) {
            isButtonClickable = false;
            findViewById(R.id.trial_btn).setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonClickable = true;
                    findViewById(R.id.trial_btn).setEnabled(true);
                }
            },3000);

        }
    }

    @Override
    public void onBackPressed() {

        showInterAndMoveToNext("onBackPressed");
//        if (AdsManager.getInstance().premiumOfferWasClosed) {
//            closeOffer();
//            if (fromStart) {
//                startActivity(new Intent(SubscriptionActivity.this, MainActivity.class));
//            }
//            super.onBackPressed();
//
//        }
    }

    public void closeOffer() {
        AdsManager.getInstance().premiumOfferWasClosed = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreenActivity();
        IronSource.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setFullScreenActivity();
        IronSource.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(premiumBroadcast);
    }

    private void openURL(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);

    }

    public void showInterIronsource(adShowCallBack adCompleted) {
        if (!AdsManager.getInstance().isPremium(this)) {

            if (IronSource.isInterstitialReady()) {

                IronSourceAdsManger.showInterstitial(this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {

                        adCompleted.adShown(true);

                    }

                    @Override
                    public void adShownSucceed(Boolean bol) {
                        adCompleted.adShownSucceed(bol);
                    }


                    @Override
                    public void adFailed(Boolean fal) {
                        //   AppOpenManager.isAdShow = false;
                        //  showingAds = false;
                        //  adCompleted.adShown(false);
                    }

                    @Override
                    public void onClosed() {

                    }
                });


            } else {
                adCompleted.adShown(true);
            }


        }
    }

    private void showInterAndMoveToNext(String next) {
        Log.e("counter", "d" + com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER);
        com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER += 1;
        if (!AdsManager.getInstance().isPremium(this) && com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER == com.sensustech.mytvcast.utils.Constant.INTER_AD_THRESH) {
            com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER = 0;
            showInterIronsource(new adShowCallBack() {
                @Override
                public void adShown(Boolean bol) {
                    moveToNextScreen(next);
                }

                @Override
                public void adShownSucceed(Boolean bol) {

                }

                @Override
                public void adFailed(Boolean fal) {
                    moveToNextScreen(next);
                }

                @Override
                public void onClosed() {

                }
            });
        } else {
            moveToNextScreen(next);
        }
    }


    private void moveToNextScreen(String next) {
//        if (next.contains("ic_close")){
//            AdsManager.getInstance().premiumOfferWasClosed = true;
//                onBackPressed();
//        }
        if (next.contains("onBackPressed")) {
            if (AdsManager.getInstance().premiumOfferWasClosed) {
                closeOffer();
                if (fromStart) {
                    startActivity(new Intent(SubscriptionActivity.this, MainActivity.class));
                }
                super.onBackPressed();

            }
        }

    }
}
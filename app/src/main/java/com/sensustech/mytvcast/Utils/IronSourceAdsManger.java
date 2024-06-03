package com.sensustech.mytvcast.utils;

import static com.sensustech.mytvcast.MainApplication.IRON_SOURCE_APP_KEY;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.DTBAdCallback;
import com.amazon.device.ads.DTBAdRequest;
import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.DTBAdSize;
import com.amazon.device.ads.SDKUtilities;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.InitializationListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.sensustech.mytvcast.interfaces.adShowCallBack;
import com.sensustech.mytvcast.interfaces.onAdfailedToLoadListner;
import com.vincent.filepicker.Constant;

import org.json.JSONException;
import org.json.JSONObject;


public class IronSourceAdsManger {
    static String LOG_TAG = "AD_MANAGER";
    private static Boolean isSdkInitialized = false;
    public static String REWARD_VIDEO_PLACEMENT = "DefaultRewardedVideo";

    public static void initSdk(Activity activity) {
        if (!isSdkInitialized) {
            IronSource.init(activity, IRON_SOURCE_APP_KEY, new InitializationListener() {
                @Override
                public void onInitializationComplete() {
                    isSdkInitialized = false;
                }
            });
        }

        IronSource.loadRewardedVideo();
        Log.e("Tag", "Ironsource sdk init");
    }

    public static void initiateAd(Context context, onAdfailedToLoadListner listener) {
        Log.d(LOG_TAG, "Initiating Ad1");
        loadAPSIntersitial(context,listener);

    }
    public static void loadAPSIntersitial(Context context, onAdfailedToLoadListner listener){
        final DTBAdRequest loader = new DTBAdRequest();
        loader.setSizes(new DTBAdSize.DTBInterstitialAdSize(Constant.APS_Intersitial));
        loader.loadAd(new DTBAdCallback() {
            @Override
            public void onFailure(@NonNull AdError adError) {
                Log.d(LOG_TAG, ""+adError.getMessage());
                loadAd1(context, listener);
            }

            @Override
            public void onSuccess(@NonNull DTBAdResponse dtbAdResponse) {
                Log.d(LOG_TAG, ""+dtbAdResponse);
                // Append the APS bid parameters to ironSource mediation to add APS to the next ad request
                JSONObject apsDataJsonIS = new JSONObject();
                try {
                    apsDataJsonIS.put("bidInfo", SDKUtilities.getBidInfo(dtbAdResponse));
                    apsDataJsonIS.put("pricePointEncoded", SDKUtilities.getPricePoint(dtbAdResponse));
                    apsDataJsonIS.put("uuid", Constant.APS_Intersitial);

                    // Define APS data per interstitial ad unit
                    JSONObject apsDataJson = new JSONObject();
                    apsDataJson.put(IronSource.AD_UNIT.INTERSTITIAL.toString(), apsDataJsonIS);
                    IronSource.setNetworkData("APS", apsDataJson);

                    loadAd1(context,listener);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }


    private static void loadAd1(Context context, onAdfailedToLoadListner listener) {

        IronSource.loadInterstitial();
        IronSource.setLevelPlayInterstitialListener(new LevelPlayInterstitialListener() {
            @Override
            public void onAdReady(AdInfo adInfo) {
                Log.d(LOG_TAG, "Ironsource ad ready to show ");
                listener.onSuccess();
            }

            @Override
            public void onAdLoadFailed(IronSourceError ironSourceError) {
                Log.d(LOG_TAG, "iron source ad failed to load ");


                listener.onAdFailedToLoad();
            }

            @Override
            public void onAdOpened(AdInfo adInfo) {
                Log.d(LOG_TAG, "ironsource ad open ");
            }

            @Override
            public void onAdShowSucceeded(AdInfo adInfo) {
                listener.adClose();
            }

            @Override
            public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {

            }

            @Override
            public void onAdClicked(AdInfo adInfo) {

            }

            @Override
            public void onAdClosed(AdInfo adInfo) {

            }

        });
    }

    public static void showInterstitial(Context context, adShowCallBack listner) {

        IronSource.setLevelPlayInterstitialListener(new LevelPlayInterstitialListener() {

            @Override
            public void onAdReady(AdInfo adInfo) {

            }

            @Override
            public void onAdLoadFailed(IronSourceError ironSourceError) {
                listner.adFailed(true);
            }

            @Override
            public void onAdOpened(AdInfo adInfo) {

            }

            @Override
            public void onAdShowSucceeded(AdInfo adInfo) {
                listner.adShownSucceed(true);
            }

            @Override
            public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {
                listner.adShown(false);
                listner.adFailed(true);
            }

            @Override
            public void onAdClicked(AdInfo adInfo) {

            }

            @Override
            public void onAdClosed(AdInfo adInfo) {
                listner.onClosed();
                listner.adShown(true);
                loadAPSIntersitial(context, new onAdfailedToLoadListner() {
                    @Override
                    public void onAdFailedToLoad() {

                    }

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void adClose() {

                    }
                });
                listner.adFailed(false);
            }


        });

        IronSource.showInterstitial();
    }

    public static void loadRewardVideo(){
        IronSource.isRewardedVideoPlacementCapped("DefaultRewardedVideo");
        IronSource.loadRewardedVideo();
    }

    public static void showRewardVideo() {
        IronSource.setRewardedVideoListener(new RewardedVideoListener() {
            /**
             * Invoked when the RewardedVideo ad view has opened.
             * Your Activity will lose focus. Please avoid performing heavy
             * tasks till the video ad will be closed.
             */
            @Override
            public void onRewardedVideoAdOpened() {
                Log.d("RewardedAdd","called onRewardedVideoAdOpened() ");
            }
            /*Invoked when the RewardedVideo ad view is about to be closed.
            Your activity will now regain its focus.*/
            @Override
            public void onRewardedVideoAdClosed() {
                Log.d("RewardedAdd","called onRewardedVideoAdClosed() ");
            }
            /**
             * Invoked when there is a change in the ad availability status.
             *
             * @param - available - value will change to true when rewarded videos are *available.
             *          You can then show the video by calling showRewardedVideo().
             *          Value will change to false when no videos are available.
             */
            @Override
            public void onRewardedVideoAvailabilityChanged(boolean available) {
                Log.d("RewardedAdd","called onRewardedVideoAvailabilityChanged() " + available);
                //Change the in-app 'Traffic Driver' state according to availability.
            }
            /**
             /**
             * Invoked when the user completed the video and should be rewarded.
             * If using server-to-server callbacks you may ignore this events and wait *for the callback from the ironSource server.
             *
             * @param - placement - the Placement the user completed a video from.
             */
            @Override
            public void onRewardedVideoAdRewarded(Placement placement) {
                Log.d("RewardedAdd","called onRewardedVideoAdRewarded() " + placement.getRewardName());
                /** here you can reward the user according to the given amount.
                 String rewardName = placement.getRewardName();
                 int rewardAmount = placement.getRewardAmount();
                 */
            }
            /* Invoked when RewardedVideo call to show a rewarded video has failed
             * IronSourceError contains the reason for the failure.
             */
            @Override
            public void onRewardedVideoAdShowFailed(IronSourceError error) {
                Log.d("RewardedAdd","called onRewardedVideoAdShowFailed() " + error.getErrorMessage());
            }
            /*Invoked when the end user clicked on the RewardedVideo ad
             */
            @Override
            public void onRewardedVideoAdClicked(Placement placement){
                Log.d("RewardedAdd","called onRewardedVideoAdClicked() ");
            }
    /* Note: the events AdStarted and AdEnded below are not available for all supported rewarded video
            ad networks. Check which events are available per ad network you choose
            to include in your build.
            We recommend only using events which register to ALL ad networks you
            include in your build.
                    * Invoked when the video ad starts playing.
    */
            @Override
            public void onRewardedVideoAdStarted(){
                Log.d("RewardedAdd","called onRewardedVideoAdStarted() ");
            }
            /* Invoked when the video ad finishes plating. */
            @Override
            public void onRewardedVideoAdEnded(){
                Log.d("RewardedAdd","called onRewardedVideoAdEnded() ");
            }
        });
//        if (IronSource.isRewardedVideoAvailable()){
            Log.d("RewardedAdd","called showRewardedVideo() ");
            IronSource.showRewardedVideo("DefaultRewardedVideo");
//        }else{
//            Log.d("RewardedAdd","called adNotAvailable() ");
//        }

    }



}


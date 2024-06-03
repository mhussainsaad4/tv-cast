package com.sensustech.mytvcast.utils;

import android.Manifest;

import com.android.billingclient.api.Purchase;

public class Constant {
    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    public static int INTER_AD_COUNTER = 0;
    public static int INTER_AD_THRESH = 4;

    public static int GPS134_interstitialStream_frequency = 120;
    public static int GPS134_interstitialStream_frequency_local = 120;

    public static int GPS134_stream_noadsTimer = 900;
    public static int GPS134_stream_noadsTimer_local = 900;

    public static boolean isRewardEarned = false;


    public static String FROM = "";
    public static String ADJUST_TOKEN_SUBSCRIBED_USERS = "wdtms1";
    public static String ADJUST_TOKEN_WEKKLY_SUB = "5bmnxy";
    public static String ADJUST_TOKEN_Yearly_Subs = "vj9jml";
    public static String FIREBASE_LOG_KEY = "AD_IMPRESSION";
    public static String DefaultInterstitial = "DefaultInterstitial";
    public static Purchase purchase = null;
    public static String currentSku = "";
}

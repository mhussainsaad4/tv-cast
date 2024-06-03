package com.sensustech.mytvcast.utils

import android.os.Build
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


object RemoteConfig{


fun getRemoteconfig(): FirebaseRemoteConfig? {



    val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(0)
        .build()
    mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
//    mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    mFirebaseRemoteConfig.let {
        it.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                Log.e("RemoteConfig"," - updated=$result")
                Constant.INTER_AD_THRESH  = it.getDouble("GPS134_int_tap_amount").toInt()
                Constant.GPS134_interstitialStream_frequency  = it.getDouble("GPS134_interstitialStream_frequency").toInt()
                Constant.GPS134_interstitialStream_frequency_local  = it.getDouble("GPS134_interstitialStream_frequency").toInt()
                Constant.GPS134_stream_noadsTimer  = it.getDouble("GPS134_stream_noadsTimer").toInt()
                Constant.GPS134_stream_noadsTimer_local  = it.getDouble("GPS134_stream_noadsTimer").toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    it.all.forEach { (t, _) ->
//                        Log.e( "RemoteConfig","All remoteConfig values = $t - ${it.getDouble(t)}")
                    }
                }
            } else {
                Log.e("RemoteConfig","RemoteConfig - ERROR fetching ..")
            }
        }
    }
    return mFirebaseRemoteConfig
}






}



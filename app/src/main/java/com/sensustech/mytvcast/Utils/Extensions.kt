package com.sensustech.mytvcast.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.sensustech.mytvcast.SubscriptionActivity
import com.sensustech.mytvcast.SubscriptionNewActivity1
import com.sensustech.mytvcast.SubscriptionNewActivity2

/**
 * Created by Hamza Chaudhary
 * Sr. Software Engineer Android
 * Created on 22 Dec,2022 11:19
 * Copyright (c) All rights reserved.
 */


const val terms="https://weewoo.com/terms-of-use/?_ga=2.142574642.1202359919.1671535451-528951195.1665861363&_gl=1*yb2pxc*_ga*NTI4OTUxMTk1LjE2NjU4NjEzNjM.*_ga_20L6DMP0ZG*MTY3MTUzNTQ1MC40LjEuMTY3MTUzNTYwNS4wLjAuMA"
const val privacy="https://weewoo.com/privacy-policy-2/?_ga=2.248865031.1202359919.1671535451-528951195.1665861363&_gl=1%2A462wdd%2A_ga%2ANTI4OTUxMTk1LjE2NjU4NjEzNjM.%2A_ga_20L6DMP0ZG%2AMTY3MTUzNTQ1MC40LjEuMTY3MTUzNTc0Mi4wLjAuMA"


fun getFirebaseToken() {
    var token: String? = null
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->

        if (!task.isSuccessful) {
            Log.w(
                "getNotificationToken",
                "Fetching FCM registration token failed",
                task.exception
            )

            return@addOnCompleteListener
        }
        // Get new FCM registration token
        token = task.result
        Log.i("My_TV_Cast_TAG", "Your device token for FCM: $token")

    }
}
fun Activity.goToSubscriptionActivity(){
    if (!RemoteConfigValues.getInstance().isRemotePaymentCard) {
        if (RemoteConfigValues.getInstance().isRemotePaymentNoTrial && RemoteConfigValues.getInstance()
                .getRemotePriceNewPlan() == "yearly_notrial") {
            val it = Intent(
                this,
                SubscriptionNewActivity1::class.java
            )
            it.putExtra("fromStart", false)
            startActivity(it)
        }
        else if (RemoteConfigValues.getInstance().isRemotePaymentNoTrial && RemoteConfigValues.getInstance()
                .getRemotePriceNewPlan() == "weekly_notrial") {
            val it = Intent(
                this,
                SubscriptionNewActivity2::class.java
            )
            it.putExtra("fromStart", false)
            startActivity(it)
        } else {
            val it = Intent(
                this,
                SubscriptionActivity::class.java
            )
            it.putExtra("fromStart", false)
            startActivity(it)
        }
    } else {
        val it = Intent(
            this,
            SubscriptionActivity::class.java
        )
        it.putExtra("fromStart", false)
        startActivity(it)
    }
}
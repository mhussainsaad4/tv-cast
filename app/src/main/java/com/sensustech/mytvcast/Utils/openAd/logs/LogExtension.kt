package com.sensustech.mytvcast.utils.openAd.logs

import android.util.Log

internal const val TAG = "AppOpenManager"

internal fun logDebug(message: String) = Log.d(TAG, message)

internal fun logError(message: String) = Log.e(TAG, message)

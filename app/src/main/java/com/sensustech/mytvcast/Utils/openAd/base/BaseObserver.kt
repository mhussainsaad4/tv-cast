package com.sensustech.mytvcast.utils.openAd.base

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Base Observer class to identify the Current visible Activity
 * @param application Required to register Activity Lifecycle Callbacks
 *
 * We only need onActivityStarted, onActivityResumed & onActivityDestroyed
 * to track the Current visible Activity & therefore it was meaningless
 * to add all those abstract methods in a single class.
 *
 * Extended ahead by:
 * @see BaseManager
 */
open class BaseObserver(application: Application) : Application.ActivityLifecycleCallbacks {
    init {
        // Cannot directly use `this`
        // Issue : Leaking 'this' in constructor of non-final class BaseObserver
        registerActivityLifecycleCallbacks(application)
    }

    protected var currentActivity: Activity? = null

    private fun registerActivityLifecycleCallbacks(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}

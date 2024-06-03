package com.sensustech.mytvcast.utils.iap

interface RestoreServiceListener {

    fun productRestored()

    fun noProductRestored(isSilentCheck : Boolean)
}
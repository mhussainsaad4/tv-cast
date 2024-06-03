package com.sensustech.mytvcast.Ui


import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adapty.Adapty
import com.adapty.models.AdaptyPaywall
import com.adapty.models.AdaptyPaywallProduct
import com.adapty.models.AdaptyViewConfiguration
import com.adapty.utils.AdaptyResult
import com.sensustech.mytvcast.Utils.AdsManager
import com.vincent.filepicker.AppPreferences


fun showMyAdaptyScreen(placementId:String, activity: AppCompatActivity){
 if (AdsManager.getInstance().isPremiumAdapty(activity)) {
     return
 }

    //    showProgressBlurDialog(activity)
        Adapty.getPaywall(placementId) { paywallResult ->
            when (paywallResult) {
                is AdaptyResult.Success -> {
                    val paywall = paywallResult.value
                    Adapty.getPaywallProducts(paywall) { productResult ->


                        when (productResult) {
                            is AdaptyResult.Success -> {
                                val products = productResult.value

                                PaywallUiFragment.isShowing=true
                                //errorView.text = ""

                                Adapty.getViewConfiguration(paywall, "en") { configResult ->

                                    when (configResult) {
                                        is AdaptyResult.Success -> {
                                            val viewConfig = configResult.value
                                            presentPaywall(paywall, products, viewConfig,activity)
                                          //  dismissProgressDialog()
                                        }
                                        is AdaptyResult.Error -> {
                                           // dismissProgressDialog()
                                        }
                                    }
                                }


                            }
                            is AdaptyResult.Error -> {
                                Log.d("dkjdhdhd", "showMyAdaptyScreen: ${productResult.error.message}")
                                if (productResult.error?.message?.contains("Google Play In-app Billing API version is less than 3") == true)
                                {
                                    Toast.makeText(activity, "Login to Google Play Store to continue!", Toast.LENGTH_SHORT).show()
                                }

                                //dismissProgressDialog()
                            }
                        }
                    }
                }
                is AdaptyResult.Error -> {
                    Log.d("${paywallResult.error}", "showMyAdaptyScreen: ")
                   // dismissProgressDialog()
                }
            }
        }
    }
    private fun presentPaywall(
        paywall: AdaptyPaywall,
        products: List<AdaptyPaywallProduct>,
        viewConfiguration: AdaptyViewConfiguration,
        activity: AppCompatActivity
    ) {
        val paywallFragment =
            PaywallUiFragment.newInstance(
                paywall,
                products,
                viewConfiguration,
            )
        activity.supportFragmentManager
            .beginTransaction()
            .addToBackStack("paywell")
            .add(android.R.id.content, paywallFragment)
            .commit()
        /*   if (!activity.supportFragmentManager.isStateSaved) {
           } else {
               Toast.makeText(activity, "Fragment is not loaging", Toast.LENGTH_SHORT).show()
           }*/



    }
fun accessLevel(splashNewActivity: Activity) {
    Adapty.getProfile { result ->
        when (result) {
            is AdaptyResult.Success -> {
                val profile = result.value

                if (profile.accessLevels["Premium"]?.isActive == true) {
                    AppPreferences.getInstance(splashNewActivity).saveData("isSubscriptionPremium", true)
                    return@getProfile

                }
                if (profile.accessLevels["premium"]?.isActive == true) {
                    // grant access to premium features
                    AppPreferences.getInstance(splashNewActivity).saveData("isSubscriptionPremium", true)
                    return@getProfile

                }



            }

            is AdaptyResult.Error -> {
                val error = result.error
                // handle the error
            }
        }
    }
}
fun logScreen(name: String,screenName:String,screenOrder:Int){
    Adapty.logShowOnboarding(name = name, screenName = screenName, screenOrder = screenOrder)
}
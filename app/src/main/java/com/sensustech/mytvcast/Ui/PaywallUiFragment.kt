package com.sensustech.mytvcast.Ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.adapty.models.AdaptyPaywall
import com.adapty.models.AdaptyPaywallProduct
import com.adapty.models.AdaptyProfile
import com.adapty.models.AdaptyViewConfiguration
import com.adapty.ui.AdaptyPaywallInsets
import com.adapty.ui.AdaptyPaywallView
import com.adapty.ui.listeners.AdaptyUiDefaultEventListener
import com.sensustech.mytvcast.R
import com.sensustech.mytvcast.onReceiveSystemBarsInsets

class PaywallUiFragment : Fragment(R.layout.fragment_paywall_ui) {

    companion object {
        var isShowing = false

        fun newInstance(
            paywall: AdaptyPaywall,
            products: List<AdaptyPaywallProduct>,
            viewConfig: AdaptyViewConfiguration,
        ) =
            PaywallUiFragment().apply {
                this.paywall = paywall
                this.products = products
                this.viewConfiguration = viewConfig
            }
    }

    private var viewConfiguration: AdaptyViewConfiguration? = null
    private var paywall: AdaptyPaywall? = null
    private var products = listOf<AdaptyPaywallProduct>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paywallView = view as? AdaptyPaywallView ?: return
        val viewConfig = viewConfiguration ?: return
        val paywall = paywall ?: return
        isShowing = true
        paywallView.setEventListener(
            object : AdaptyUiDefaultEventListener() {


                override fun onRestoreSuccess(
                    profile: AdaptyProfile,
                    view: AdaptyPaywallView,
                ) {
                    if (profile.accessLevels["premium"]?.isActive == true) {
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        )


        paywallView.onReceiveSystemBarsInsets { insets ->
            val paywallInsets = AdaptyPaywallInsets.of(insets.top, insets.bottom)
            val customTags = mapOf("USERNAME" to "John")
            paywallView.showPaywall(
                paywall,
                products,
                viewConfig,
                paywallInsets,
                tagResolver = { tag -> customTags[tag] }
            )
        }


    }

    override fun onDestroy() {
        isShowing = false
        super.onDestroy()
    }
}
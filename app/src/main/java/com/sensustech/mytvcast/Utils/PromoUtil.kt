package com.sensustech.mytvcast.utils

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.bumptech.glide.Glide
import com.sensustech.mytvcast.Model.XpromoCampaignsItem
import com.sensustech.mytvcast.R


object PromoUtil {

    fun onButtonShowPopupWindowClick(
        context: Context,
        campaign: XpromoCampaignsItem?,
        finish: () -> Unit
    ) {
        // inflate the layout of the popup window
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.promo_dialog, null)
        val img_steps = popupView.findViewById<ImageView>(R.id.img_promo)
        val reletiveLayout = popupView.findViewById<RelativeLayout>(R.id.reletiveLayout)
        val btnContinue = popupView.findViewById<Button>(R.id.btnContinue)
        Glide.with(context).load(campaign?.promoImageUrl ?: "").into(img_steps)
        btnContinue.text = campaign?.buttonText ?: ""

        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        //        View v= popupWindow.getContentView();
        dimBehind(popupWindow)
        Handler(Looper.getMainLooper()).postDelayed({
            // dismiss the popup window when touched
            popupView.setOnTouchListener { v, event ->
                popupWindow.dismiss()
                finish.invoke()
//                navigateToNext(mainScreenItemModel)
                true
            }
        }, 4000)
        reletiveLayout.setOnClickListener {
            openURL(
                context,
                campaign?.storeUrl ?: ""
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
//            if (!flag) {
            popupWindow.dismiss()
            finish.invoke()
//                navigateToNext(mainScreenItemModel)
//            }
        }, 7000)


        btnContinue.setOnClickListener {
            openURL(
                context,
                campaign?.storeUrl ?: ""
            )
        }
    }

    private fun dimBehind(popupWindow: PopupWindow) {
        val container: View = if (popupWindow.background == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                popupWindow.contentView.parent as View
            } else {
                popupWindow.contentView
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                popupWindow.contentView.parent.parent as View
            } else {
                popupWindow.contentView.parent as View
            }
        }
        val context = popupWindow.contentView.context
        val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.7f
        wm.updateViewLayout(container, p)
    }

    private fun openURL(context: Context, url: String) {
        if (url.isNotEmpty()) {
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                return;
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }
    }
}
package com.sensustech.mytvcast.Model

import com.google.gson.annotations.SerializedName

data class PromoCompaign(

	@field:SerializedName("xpromo_campaigns")
	val xpromoCampaigns: List<XpromoCampaignsItem>? = null
)

data class XpromoCampaignsItem(

	@field:SerializedName("buttonText")
	val buttonText: String? = null,

	@field:SerializedName("promoImage_url")
	val promoImageUrl: String? = null,

	@field:SerializedName("campaing_name")
	val campaingName: String? = null,

	@field:SerializedName("store_url")
	val storeUrl: String? = null,

	@field:SerializedName("campaign_name")
	val campaignName: String? = null
)

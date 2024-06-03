package com.sensustech.mytvcast.Network.Models.channel;

import androidx.annotation.NonNull;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ChannelsModelItem{

	@SerializedName("country")
	private String country;

	@SerializedName("website")
	private String website;

	@SerializedName("languages")
	private List<String> languages;

	@SerializedName("city")
	private String city;

	@SerializedName("broadcast_area")
	private List<String> broadcastArea;

	@SerializedName("alt_names")
	private List<String> altNames;

	@SerializedName("owners")
	private List<String> owners;

	@SerializedName("launched")
	private String launched;

	@SerializedName("replaced_by")
	private String replacedBy;

	@SerializedName("network")
	private String network;

	@SerializedName("subdivision")
	private String subdivision;

	@SerializedName("is_nsfw")
	private boolean isNsfw;

	@SerializedName("name")
	private String name;

	@SerializedName("closed")
	private String closed;

	@SerializedName("logo")
	private String logo;

	@SerializedName("id")
	private String id;

	@SerializedName("categories")
	private List<String> categories;

	@SerializedName("isFavourite")
	private Boolean isFavourite = false;

	public String getCountry(){
		return country;
	}

	public String getWebsite(){
		return website;
	}

	public List<String> getLanguages(){
		return languages;
	}

	public String getCity(){
		return city;
	}

	public List<String> getBroadcastArea(){
		return broadcastArea;
	}

	public List<String> getAltNames(){
		return altNames;
	}

	public List<String> getOwners(){
		return owners;
	}

	public String getLaunched(){
		return launched;
	}

	public String getReplacedBy(){
		return replacedBy;
	}

	public String getNetwork(){
		return network;
	}

	public String getSubdivision(){
		return subdivision;
	}

	public boolean isIsNsfw(){
		return isNsfw;
	}

	public String getName(){
		return name;
	}

	public String getClosed(){
		return closed;
	}

	public String getLogo(){
		return logo;
	}

	public String getId(){
		return id;
	}

	public List<String> getCategories(){
		return categories;
	}


	public Boolean getFavourite() {
		return isFavourite;
	}

	public void setFavourite(Boolean favourite) {
		isFavourite = favourite;
	}
}
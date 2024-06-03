package com.sensustech.mytvcast.Network.Models.country;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CountryModel{

	@SerializedName("CountryModel")
	private List<CountryModelItem> countryModel;

	public List<CountryModelItem> getCountryModel(){
		return countryModel;
	}
}
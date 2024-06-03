package com.sensustech.mytvcast.Network.Models.country;

import androidx.annotation.NonNull;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CountryModelItem{

	@SerializedName("code")
	private String code;

	@SerializedName("flag")
	private String flag;

	@SerializedName("languages")
	private List<String> languages;

	@SerializedName("name")
	private String name;

	public String getCode(){
		return code;
	}

	public void setCode(String mCode){
		code = mCode;
	}

	public String getFlag(){
		return flag;
	}

	public List<String> getLanguages(){
		return languages;
	}

	public String getName(){
		return name;
	}

	public void setName(String mName){
		name = mName;
	}
	@NonNull
	@Override
	public String toString() {
		return getName();
	}
}
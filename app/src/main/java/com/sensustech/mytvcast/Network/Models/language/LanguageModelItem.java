package com.sensustech.mytvcast.Network.Models.language;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class LanguageModelItem{

	@SerializedName("code")
	private String code;

	@SerializedName("name")
	private String name;

	public String getCode(){
		return code;
	}

	public String getName(){
		return name;
	}

	public void setCode(String mCode){
		code = mCode;
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
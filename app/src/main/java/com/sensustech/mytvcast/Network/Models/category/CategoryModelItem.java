package com.sensustech.mytvcast.Network.Models.category;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class CategoryModelItem{

	@SerializedName("name")
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public void setId(String id) {
		this.id = id;
	}

	@SerializedName("id")
	private String id;

	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	@NonNull
	@Override
	public String toString() {
		return getName();
	}
}
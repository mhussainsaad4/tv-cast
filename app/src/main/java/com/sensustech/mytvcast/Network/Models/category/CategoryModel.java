package com.sensustech.mytvcast.Network.Models.category;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class CategoryModel{

	@SerializedName("CategoryModel")
	private List<CategoryModelItem> categoryModel;

	public List<CategoryModelItem> getCategoryModel(){
		return categoryModel;
	}
}
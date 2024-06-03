package com.sensustech.mytvcast.Network.Models.language;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class LanguageModel{

	@SerializedName("LanguageModel")
	private List<LanguageModelItem> languageModel;

	public List<LanguageModelItem> getLanguageModel(){
		return languageModel;
	}
}
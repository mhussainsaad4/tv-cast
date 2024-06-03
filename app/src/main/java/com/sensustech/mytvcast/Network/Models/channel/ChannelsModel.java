package com.sensustech.mytvcast.Network.Models.channel;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ChannelsModel{

	@SerializedName("ChannelsModel")
	private List<ChannelsModelItem> channelsModel;

	public List<ChannelsModelItem> getChannelsModel(){
		return channelsModel;
	}
}
package com.sensustech.mytvcast.Network.Models.stream;

import com.google.gson.annotations.SerializedName;

public class StreamModel{

	@SerializedName("added_at")
	private String addedAt;

	@SerializedName("checked_at")
	private String checkedAt;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("channel")
	private String channel;

	@SerializedName("width")
	private int width;

	@SerializedName("http_referrer")
	private String httpReferrer;

	@SerializedName("bitrate")
	private int bitrate;

	@SerializedName("frame_rate")
	private Object frameRate;

	@SerializedName("url")
	private String url;

	@SerializedName("user_agent")
	private String userAgent;

	@SerializedName("status")
	private String status;

	@SerializedName("height")
	private int height;

	public String getAddedAt(){
		return addedAt;
	}

	public String getCheckedAt(){
		return checkedAt;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public String getChannel(){
		return channel;
	}

	public int getWidth(){
		return width;
	}

	public String getHttpReferrer(){
		return httpReferrer;
	}

	public int getBitrate(){
		return bitrate;
	}

	public Object getFrameRate(){
		return frameRate;
	}

	public String getUrl(){
		return url;
	}

	public String getUserAgent(){
		return userAgent;
	}

	public String getStatus(){
		return status;
	}

	public int getHeight(){
		return height;
	}
}
package com.sensustech.mytvcast.interfaces;

import com.sensustech.mytvcast.Network.Models.category.CategoryModelItem;
import com.sensustech.mytvcast.Network.Models.channel.ChannelsModelItem;
import com.sensustech.mytvcast.Network.Models.country.CountryModelItem;
import com.sensustech.mytvcast.Network.Models.language.LanguageModelItem;

public interface IPTVFilterCallBacks {

    void onFavChannelEventCall(ChannelsModelItem data, int position, boolean isFav);

    void onItemClickedEvent(ChannelsModelItem data);

}

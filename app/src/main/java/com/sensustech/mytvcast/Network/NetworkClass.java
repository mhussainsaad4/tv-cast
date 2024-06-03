package com.sensustech.mytvcast.Network;

import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.sensustech.mytvcast.MainApplication;
import com.sensustech.mytvcast.Network.Models.category.CategoryModel;
import com.sensustech.mytvcast.Network.Models.category.CategoryModelItem;
import com.sensustech.mytvcast.Network.Models.channel.ChannelsModel;
import com.sensustech.mytvcast.Network.Models.channel.ChannelsModelItem;
import com.sensustech.mytvcast.Network.Models.country.CountryModelItem;
import com.sensustech.mytvcast.Network.Models.language.LanguageModelItem;
import com.sensustech.mytvcast.Network.Models.stream.StreamModel;
import com.sensustech.mytvcast.db.ChannelDatabase;
import com.sensustech.mytvcast.db.dao.ChannelDao;
import com.sensustech.mytvcast.db.entities.ChannelModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;


public class NetworkClass extends AppCompatActivity {
    String TAG = "NetworkCAll";
    private String baseUrl = "https://iptv-org.github.io/api/";
    public boolean isFavChannelShown = false;
    public ArrayList<ChannelsModelItem> channelList = new ArrayList<>();
    public ArrayList<ChannelsModelItem> filteredChannelList = new ArrayList<>();
    //    public ArrayList<ChannelsModelItem> filteredChannelList = new ArrayList<>();
    public MutableLiveData<Boolean> isSpinnerDataLoaded = new MutableLiveData();
    public MutableLiveData<Boolean> isChannelLoaded = new MutableLiveData();
    public ArrayList<LanguageModelItem> languageList = new ArrayList<>();
    public ArrayList<CategoryModelItem> categoryList = new ArrayList<>();
    public ArrayList<CountryModelItem> countryList = new ArrayList<>();

    public ArrayList<StreamModel> streamList = new ArrayList<>();

    public String selectedLanguage = "";
    public String selectedCategory = "";
    public String selectedCountry = "";

    private ChannelDao channelDao;

    public NetworkClass() {

        ChannelDatabase database = ChannelDatabase.getInstance(MainApplication.context);
        channelDao = database.getChannelDao();
        getStreamList();
    }

    //Network call
    public void getCategoryList() {
        AndroidNetworking.get(baseUrl + "categories.json")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(CategoryModelItem.class, new ParsedRequestListener<List<CategoryModelItem>>() {
                    @Override
                    public void onResponse(List<CategoryModelItem> mCategoryList) {
                        // do anything with response
                        Log.d(TAG, "categoryList size : " + mCategoryList.size());
                        categoryList.clear();
                        CategoryModelItem data = new CategoryModelItem();
                        data.setName("Category");
                        data.setId("Category");
                        categoryList.add(data);
                        for(CategoryModelItem category: mCategoryList){
                            if(!category.getId().equals("xxx")){
                                categoryList.add(category);
                            }
                        }
                        getCountryList();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "Error : " + anError.getLocalizedMessage());
                    }
                });
    }

    private void getCountryList() {
        AndroidNetworking.get(baseUrl + "countries.json")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(CountryModelItem.class, new ParsedRequestListener<List<CountryModelItem>>() {
                    @Override
                    public void onResponse(List<CountryModelItem> mCountryList) {
                        // do anything with response
                        Log.d(TAG, "countryList size : " + mCountryList.size());
                        CountryModelItem data = new CountryModelItem();
                        data.setName("Country");
                        data.setCode("Country");
                        data.getCode();
                        countryList.clear();
                        countryList.add(data);
                        countryList.addAll(mCountryList);

                        getLanguageList();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "Error : " + anError.getLocalizedMessage());
                    }
                });
    }

    private void getLanguageList() {
        AndroidNetworking.get(baseUrl + "languages.json")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(LanguageModelItem.class, new ParsedRequestListener<List<LanguageModelItem>>() {
                    @Override
                    public void onResponse(List<LanguageModelItem> mLanguageList) {
                        languageList.clear();
                        LanguageModelItem data = new LanguageModelItem();
                        data.setName("Language");
                        data.setCode("Language");
                        languageList.add(data);
                        languageList.addAll(mLanguageList);
                        isSpinnerDataLoaded.postValue(true);
                        getChannelList();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "Error : " + anError.getLocalizedMessage());
                    }
                });
    }

    private void getChannelList() {
        AndroidNetworking.get(baseUrl + "channels.json")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(ChannelsModelItem.class, new ParsedRequestListener<List<ChannelsModelItem>>() {
                    @Override
                    public void onResponse(List<ChannelsModelItem> mChannelList) {
                        Log.d(TAG, "channelList size : " + mChannelList.size());
                        ArrayList<ChannelsModelItem> channelWithFavIds = new ArrayList<>();
                        Executors.newSingleThreadExecutor().execute(() -> {
                            List<ChannelModel> list = channelDao.getFavChannel();
                            for (ChannelsModelItem channel : mChannelList) {

                                if(!channel.getCategories().isEmpty() && channel.getCategories().get(0).equals("xxx")){
                                    continue; // if the category is xxx continue to skip
                                }

                                for (ChannelModel favChannel : list) {
                                    if (Objects.equals(favChannel.getId(), channel.getId())) {
                                        channel.setFavourite(true);
                                    }
                                }
                                channelWithFavIds.add(channel);
                            }
                            channelList.addAll(channelWithFavIds);
                            filteredChannelList.addAll(channelWithFavIds);
                            isChannelLoaded.postValue(true);
                        });

//                        getFilteredChannelList();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "Error : " + anError.getLocalizedMessage());
                    }
                });
    }

    private void getStreamList() {
        AndroidNetworking.get(baseUrl + "streams.json")
                .setTag(this)
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(StreamModel.class, new ParsedRequestListener<List<StreamModel>>() {
                    @Override
                    public void onResponse(List<StreamModel> mStreamList) {
                        Log.d(TAG, "channelList size : " + streamList.size());
                        streamList.clear();
                        streamList.addAll(mStreamList);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "Error : " + anError.getLocalizedMessage());
                    }
                });
    }

    public void getFilteredChannelList() {
        ArrayList<ChannelsModelItem> list = new ArrayList<>();
        filteredChannelList.clear();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ChannelModel> favList = channelDao.getFavChannel();
            for (ChannelsModelItem channel : channelList) {
                if (
                        (!channel.getLanguages().isEmpty() && channel.getLanguages().get(0).contains(selectedLanguage)) &&
                                (!channel.getCategories().isEmpty() && channel.getCategories().get(0).contains(selectedCategory)) &&
                                channel.getCountry().contains(selectedCountry)
                ) {

                    Log.d("WaleedHassan"," category: "+ channel.getCategories() + " == " + selectedCategory);
                    for (ChannelModel favChannel : favList) { //sync with local fav channels
                        if (Objects.equals(favChannel.getId(), channel.getId())) {
                            channel.setFavourite(true);
                        }
                    }
                    if(isFavChannelShown){ // check to show fav/unfav channels
                        if (channel.getFavourite()){
                            list.add(channel);
                        }
                    }else{
                        list.add(channel);
                    }

                }
            }
            filteredChannelList.addAll(list);
            isChannelLoaded.postValue(true);
        });
    }



    //db calls
    public void addChannelToFav(ChannelModel channel) {
        Executors.newSingleThreadExecutor().execute(() -> channelDao.insertFavChannel(channel));
    }

    public void deleteFavChannel(ChannelModel channel) {
        Executors.newSingleThreadExecutor().execute(() -> channelDao.deleteFavChannel(channel));
    }
}

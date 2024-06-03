package com.sensustech.mytvcast.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Entity(tableName = "channel")
public class ChannelModel {

    @SerializedName("name")
    private String name;

    @NonNull
    @PrimaryKey
    @SerializedName("id")
    private String id;

//    public ChannelModel(
//            String mId,
//            String mName
//    ){
//        id = mId;
//        name = mName;
//    }
//
//    public ChannelModel(){
//
//    }

    public String getName(){
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId(){
        return id;
    }

}

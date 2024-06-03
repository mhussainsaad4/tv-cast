package com.sensustech.mytvcast.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sensustech.mytvcast.db.entities.ChannelModel;

import java.util.List;

@Dao
public interface ChannelDao {
    @Insert
    void insertFavChannel(ChannelModel channel);

    @Update
    void updateFavChannel(ChannelModel channel);

    @Delete
    void deleteFavChannel(ChannelModel channel);

    @Query("DELETE FROM channel")
    void deleteAllFavChannel();

    @Query("SELECT * FROM channel")
    LiveData<List<ChannelModel>> getLiveFavChannel();

    @Query("SELECT * FROM channel")
    List<ChannelModel> getFavChannel();
}

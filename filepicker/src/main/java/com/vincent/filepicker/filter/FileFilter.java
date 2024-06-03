package com.vincent.filepicker.filter;


import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.Loader;

import com.vincent.filepicker.filter.callback.FileLoaderCallbacks;
import com.vincent.filepicker.filter.callback.FilterResultCallback;
import com.vincent.filepicker.filter.entity.AudioFile;
import com.vincent.filepicker.filter.entity.ImageFile;
import com.vincent.filepicker.filter.entity.NormalFile;
import com.vincent.filepicker.filter.entity.VideoFile;

import static com.vincent.filepicker.filter.callback.FileLoaderCallbacks.TYPE_AUDIO;
import static com.vincent.filepicker.filter.callback.FileLoaderCallbacks.TYPE_FILE;
import static com.vincent.filepicker.filter.callback.FileLoaderCallbacks.TYPE_IMAGE;
import static com.vincent.filepicker.filter.callback.FileLoaderCallbacks.TYPE_VIDEO;

/**
 * Created by Vincent Woo
 * Date: 2016/10/11
 * Time: 10:19
 */

public class FileFilter {

    private static AppCompatActivity appActivity;
    private static FileLoaderCallbacks loaderCallbacks;

    public static void getImages(AppCompatActivity activity, FilterResultCallback<ImageFile> callback){
        loaderCallbacks = new FileLoaderCallbacks(activity, callback, TYPE_IMAGE);
        appActivity = activity;
        activity.getSupportLoaderManager().initLoader(0, null, loaderCallbacks);
    }

    public static void resetImageLoader(){
        try {
            if (appActivity != null && loaderCallbacks != null) {
                appActivity.getSupportLoaderManager().restartLoader(0, null, loaderCallbacks);
            }
        }catch (Exception e) {

        }
    }

    public static void resetVideoLoader(){
        try {
            if (appActivity != null && loaderCallbacks != null) {
                appActivity.getSupportLoaderManager().restartLoader(1, null, loaderCallbacks);
            }
        }catch (Exception e) {

        }
    }

    public static void getVideos(AppCompatActivity activity, FilterResultCallback<VideoFile> callback){
        loaderCallbacks = new FileLoaderCallbacks(activity, callback, TYPE_VIDEO);
        appActivity = activity;
        activity.getSupportLoaderManager().initLoader(1, null, loaderCallbacks);
    }

    public static void getAudios(AppCompatActivity activity, FilterResultCallback<AudioFile> callback){
        activity.getSupportLoaderManager().initLoader(2, null,new FileLoaderCallbacks(activity, callback, TYPE_AUDIO));
    }

    public static void getFiles(AppCompatActivity activity,
                                FilterResultCallback<NormalFile> callback, String[] suffix){
        activity.getSupportLoaderManager().initLoader(3, null,
                new FileLoaderCallbacks(activity, callback, TYPE_FILE, suffix));
    }
}

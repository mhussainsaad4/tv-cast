package com.sensustech.mytvcast.utils;

import android.content.Context;

import com.connectsdk.core.MediaInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.command.ServiceCommandError;

public class StreamingManager {

    private static Context context;
    private static StreamingManager instance;
    private ConnectableDevice device;
    private boolean isScreenStreaming = false;

    public static StreamingManager getInstance(Context ctx) {
        context = ctx;
        if (instance == null) {
            instance = new StreamingManager();
        }
        return instance;
    }

    public ConnectableDevice getDevice() {
        return device;
    }

    public void releaseDevice() {
        if(device.isConnected())
            device.disconnect();
        device = null;
    }

    public void setDevice(ConnectableDevice device) {
        this.device = device;
    }

    public void showContent(String title, String mimeType, String url) {
        try {
            if (device != null && device.isConnected()) {
                if (device.hasCapability(MediaPlayer.Display_Image)) {
                    MediaInfo mediaInfo = new MediaInfo.Builder(url, mimeType)
                            .setTitle(title)
                            .build();
                    device.getCapability(MediaPlayer.class).displayImage(mediaInfo, new MediaPlayer.LaunchListener() {
                        @Override
                        public void onError(ServiceCommandError error) {
                        }
                        @Override
                        public void onSuccess(MediaPlayer.MediaLaunchObject object) {
                        }
                    });
                }
            }
        } catch (Exception e) {

        }
    }

    public void playMedia(String title, String mimeType, String url) {
        try {
            if (device != null && device.isConnected()) {
                if (device.hasCapability(MediaPlayer.Play_Video)) {
                    MediaInfo mediaInfo = new MediaInfo.Builder(url, mimeType)
                            .setTitle(title)
                            .build();
                    device.getCapability(MediaPlayer.class).playMedia(mediaInfo, false, new MediaPlayer.LaunchListener() {
                        @Override
                        public void onError(ServiceCommandError error) {
                        }

                        @Override
                        public void onSuccess(MediaPlayer.MediaLaunchObject object) {
                        }
                    });
                }
            }
        } catch (Exception e) {

        }
    }

    public boolean isDeviceConnected() {
        if (device != null && device.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public void disconnect() {
        device.disconnect();
        device = null;
    }

    public void playAudio(String title, String mimeType, String url) {
        if (device != null && device.isConnected()) {
            if (device.hasCapability(MediaPlayer.Play_Audio)) {
                MediaInfo mediaInfo = new MediaInfo.Builder(url, mimeType)
                        .setTitle(title)
                        .build();
                device.getCapability(MediaPlayer.class).playMedia(mediaInfo, false, new MediaPlayer.LaunchListener() {
                    @Override
                    public void onError(ServiceCommandError error) {
                    }

                    @Override
                    public void onSuccess(MediaPlayer.MediaLaunchObject object) {
                    }
                });
            }
        }
    }
}

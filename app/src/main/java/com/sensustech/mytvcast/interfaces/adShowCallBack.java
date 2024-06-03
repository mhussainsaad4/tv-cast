package com.sensustech.mytvcast.interfaces;

public interface adShowCallBack
{
    public void adShown(Boolean bol);
    public void adShownSucceed(Boolean bol);
    public void adFailed(Boolean fal);

    public void onClosed();
}

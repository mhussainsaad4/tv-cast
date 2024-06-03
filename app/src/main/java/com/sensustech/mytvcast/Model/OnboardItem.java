package com.sensustech.mytvcast.Model;

public class OnboardItem {
    String title;
    String subtitle;
    int lottieId;

    public OnboardItem() {
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setLottieId(int lottieId) {
        this.lottieId = lottieId;
    }

    public int getLottieId() {
        return lottieId;
    }
}

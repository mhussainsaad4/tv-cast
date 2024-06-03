package com.sensustech.mytvcast.utils;

public class RemoteConfigValues {
    public static final String REMOTE_ONBOARDING_FLAG = "GPS134_onboarding_flag";
    public static final String REMOTE_PAYMENT_FLAG = "GPS134_paymentcard_flag";
    public static final String REMOTE_PRICE_PLAN_FLAG = "GPS134_price_plan";
    public static final String REMOTE_PAYWAL_NOTRIAL = "GPS134_paywall_notrial";
    public static final String REMOTE_PAYWAL_NOTRIAL_PLAN = "GPS134_paywall_notrial_plan";
    public static final String REMOTE_APPOPENAD = "GPS134_appopen";
    public static final String REMOTE_APPOPENAD_BACKGROUND = "GPS134_appopen_background";

    private static RemoteConfigValues instance;
    boolean remoteOnboarding = false;
    boolean remotePaymentCard = false;
    boolean remotePaymentNoTrial = false;
    boolean remoteAppOpen = true;
    boolean remoteAppOpenBackground = true;
    String remotePricePlan = "yearlysub_standard";
    String remotePriceNewPlan = "yearly_notrial";

    private RemoteConfigValues() {
    }

    public static RemoteConfigValues getInstance() {
        if (instance == null) {
            instance = new RemoteConfigValues();
        }
        return instance;
    }

    public static void setInstance(RemoteConfigValues instance) {
        RemoteConfigValues.instance = instance;
    }

    public void setRemoteOnboarding(boolean remoteOnboarding) {
        this.remoteOnboarding = remoteOnboarding;
    }

    public void setRemotePaymentCard(boolean remotePaymentCard) {
        this.remotePaymentCard = remotePaymentCard;
    }

    public void setRemotePaymentNoTrial(boolean remotePaymentCard) {
        this.remotePaymentNoTrial = remotePaymentCard;
    }

    public void setRemotePricePlan(String remotePricePlan) {
        this.remotePricePlan = remotePricePlan;
    }

    public void setRemotePriceNewPlan(String remotePricePlan) {
        this.remotePriceNewPlan = remotePricePlan;
    }

    public boolean isRemoteOnboarding() {
        return remoteOnboarding;
    }

  public void setRemoteAppopenad(Boolean remoteAppopenad) {
        this.remoteAppOpen = remoteAppopenad;
    }

    public boolean isRemoteAppOpen() {
        return remoteAppOpen;
    }
  public void setRemoteAppopenadBackground(Boolean remoteAppOpenBackground) {
        this.remoteAppOpenBackground = remoteAppOpenBackground;
    }

    public boolean isRemoteAppOpenBackground() {
        return remoteAppOpenBackground;
    }
    public boolean isRemotePaymentCard() {
        return remotePaymentCard;
    }
    public boolean isRemotePaymentNoTrial() {
        return remotePaymentNoTrial;
    }

    public String getRemotePriceNewPlan() {
        return remotePriceNewPlan;
    }
    public String getRemotePricePlan() {
        return remotePricePlan;
    }
}

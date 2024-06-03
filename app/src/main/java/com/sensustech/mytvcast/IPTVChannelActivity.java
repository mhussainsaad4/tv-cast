package com.sensustech.mytvcast;

import static com.sensustech.mytvcast.utils.Constant.GPS134_stream_noadsTimer;
import static com.sensustech.mytvcast.utils.Constant.GPS134_stream_noadsTimer_local;
import static com.sensustech.mytvcast.utils.Constant.isRewardEarned;
import static com.sensustech.mytvcast.utils.ExtensionsKt.goToSubscriptionActivity;
import static com.vincent.filepicker.Constant.showBanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.sensustech.mytvcast.Adapters.iptv_adapter.CategoryAdapter;
import com.sensustech.mytvcast.Adapters.iptv_adapter.ChannelAdapter;
import com.sensustech.mytvcast.Adapters.iptv_adapter.CountryAdapter;
import com.sensustech.mytvcast.Adapters.iptv_adapter.LanguageAdapter;
import com.sensustech.mytvcast.Model.PromoCompaign;
import com.sensustech.mytvcast.Model.XpromoCampaignsItem;
import com.sensustech.mytvcast.Network.Models.channel.ChannelsModelItem;
import com.sensustech.mytvcast.Network.Models.stream.StreamModel;
import com.sensustech.mytvcast.Network.NetworkClass;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.db.entities.ChannelModel;
import com.sensustech.mytvcast.interfaces.IPTVFilterCallBacks;
import com.sensustech.mytvcast.utils.Constant;
import com.sensustech.mytvcast.utils.PromoUtil;
import com.sensustech.mytvcast.utils.RemoteConfig;
import com.vincent.filepicker.AdLoadedCallback;
import com.vincent.filepicker.AdsUtill;

import java.util.ArrayList;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class IPTVChannelActivity extends NetworkClass implements IPTVFilterCallBacks, RewardedVideoListener {
    RelativeLayout includedToolbar;
    RelativeLayout rl_back;
    TextView tvTitle;

    Spinner categorySpinner;
    Spinner countrySpinner;
    Spinner languageSpinner;
    ChannelAdapter channelAdapter;
    RecyclerView rvIPTVChannel;

    ImageView ivFavBtn;
    BlurView blurView;

    FrameLayout flShowFavChannelBtn;
    FrameLayout bannerContainer;

    LinearLayout dataLayout;

    ShimmerFrameLayout shimmerLayout;
    FrameLayout flRewardAdBtn;
    TextView tvRewardAdTime;
    boolean isCategoryAutoSelected = false;
    boolean isCountryAutoSelected = false;
    boolean isLanguageAutoSelected = false;
    static boolean isStreamingActive = true;
    CountDownTimer counter = null;

    private XpromoCampaignsItem xpromoCampaignsItem = null;
    String GPS124_current_campaign = "";
    PromoCompaign promoCompaignmodel;
    public static int count = 0;
    private AdsUtill adsUtill;
    public IPTVChannelActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iptvchannel);
        adsUtill=new AdsUtill(this);
        iniViews();
        initListeners();
        getCategoryList();
        initAdapter();
        initObservers();
        initIronSourceRewardedVideo();
        if (!AdsManager.getInstance().isPremium(this)) {
            if (isRewardEarned) { // check if user watched rewarded add on channel activity
                startCounter(Constant.GPS134_stream_noadsTimer_local * 1000);
//                Log.d("Skype", "test: " + GPS134_stream_noadsTimer_local);
            }
        }
        count = 0;
        try {
            GPS124_current_campaign = RemoteConfig.INSTANCE.getRemoteconfig().getString("GPS134_current_campaign").toString();
            String jsondata = RemoteConfig.INSTANCE.getRemoteconfig().getString("GPS134_xpromo_campaigns").toString();
            if (!jsondata.isEmpty()) {
                promoCompaignmodel = new Gson().fromJson(jsondata, PromoCompaign.class);
                if (!GPS124_current_campaign.isEmpty() && !promoCompaignmodel.getXpromoCampaigns().isEmpty()) {
                    for (XpromoCampaignsItem campaign : promoCompaignmodel.getXpromoCampaigns()) {
                        if (Objects.equals(campaign.getCampaignName(), GPS124_current_campaign)) {
                            xpromoCampaignsItem = campaign;
                            Log.d("Skype", xpromoCampaignsItem.toString());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isStreamingActive=true;
        if (IPTVStreamActivity.counter!=null) {
            IPTVStreamActivity.counter.cancel();
            IPTVStreamActivity.counter = null;
        }
        if (count == 3) {
            if (xpromoCampaignsItem != null) {
                Log.d("Skype ->",count+"    " +xpromoCampaignsItem.toString());
                blurView.setVisibility(View.VISIBLE);
                PromoUtil.INSTANCE.onButtonShowPopupWindowClick(this, xpromoCampaignsItem, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        count=0;
                        blurView.setVisibility(View.INVISIBLE);
                        return null;
                    }
                });
            }
        }
        if (!AdsManager.getInstance().isPremium(IPTVChannelActivity.this)) {
            bannerContainer.setVisibility(View.VISIBLE);
            adsUtill.loadAPSBanner(bannerContainer, this);
//            IronSource.loadRewardedVideo();
            IronSource.onResume(this);
        } else {
            bannerContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onDestroy() {
        removeIronSource();
        super.onDestroy();
        cancelTimer();
    }

    private void iniViews() {
        includedToolbar = findViewById(R.id.includedToolbar);
        tvTitle = includedToolbar.findViewById(R.id.tvTitle);
        rl_back = includedToolbar.findViewById(R.id.rl_back);
        tvTitle.setText("IPTV");
        tvTitle.setVisibility(View.VISIBLE);
        categorySpinner = findViewById(R.id.categorySpinner);
        countrySpinner = findViewById(R.id.countrySpinner);
        flRewardAdBtn = findViewById(R.id.flRewardAdBtn);
        languageSpinner = findViewById(R.id.languageSpinner);
        tvRewardAdTime = findViewById(R.id.tvRewardAdTime);
        rvIPTVChannel = findViewById(R.id.rvIPTVChannel);
        ivFavBtn = findViewById(R.id.ivFavBtn);
        flShowFavChannelBtn = findViewById(R.id.flShowFavChannelBtn);
        dataLayout = findViewById(R.id.dataLayout);
        bannerContainer = findViewById(R.id.bannerContainer);
        shimmerLayout = findViewById(R.id.shimmerLayout);
        blurView = findViewById(R.id.blurView);
        dataLayout.setVisibility(View.GONE);
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        tvRewardAdTime.setText(GPS134_stream_noadsTimer_local / 60 + "mins");
        if (!AdsManager.getInstance().isPremium(this)) {
            flRewardAdBtn.setVisibility(View.VISIBLE);
        } else {
            flRewardAdBtn.setVisibility(View.GONE);
        }
        float radius = 2f;
        View decorView = getWindow().getDecorView();
        // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurView.setupWith(rootView, new RenderScriptBlur(this)) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);
    }

    private void initAdapter() {
        channelAdapter = new ChannelAdapter(this, new ArrayList<>(), this);
        rvIPTVChannel.setAdapter(channelAdapter);
    }

    private void initListeners() {

        flRewardAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRewardEarned) { // if rewarded time ends or not stated yet
                    if (!IronSource.isRewardedVideoAvailable()) {
                        IronSource.loadRewardedVideo();
                        Toast.makeText(IPTVChannelActivity.this, "Rewarded video not available!", Toast.LENGTH_SHORT).show();
                    }
                    IronSource.showRewardedVideo("DefaultRewardedVideo");
                }

            }
        });

        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeIronSource();
                finish();
            }
        });

        flShowFavChannelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AdsManager.getInstance().isPremium(IPTVChannelActivity.this)) {
                    if (isFavChannelShown) {
                        isFavChannelShown = false;
                        ivFavBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav));
                    } else {
                        isFavChannelShown = true;
                        ivFavBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_fill));
                    }
                    selectedLanguage = "";
                     selectedCategory = "";
                     selectedCountry = "";
                    languageSpinner.setSelection(0);
                    countrySpinner.setSelection(0);
                    categorySpinner.setSelection(0);
                    mGetFilteredChannelList();
                } else {
                    goToSubscriptionActivity(IPTVChannelActivity.this);
                }
            }
        });

    }

    private void initObservers() {

        isChannelLoaded.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    channelAdapter.channelList.clear();
                    channelAdapter.channelList.addAll(filteredChannelList) ;
                    channelAdapter.notifyDataSetChanged();
                    dataLayout.setVisibility(View.VISIBLE);
                    shimmerLayout.setVisibility(View.GONE);
                    shimmerLayout.stopShimmer();
                }
            }
        });

        isSpinnerDataLoaded.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    initSpinner();
                    spinnerCallBacks();
                }
            }
        });

    }

    private void initSpinner() {
        CategoryAdapter adapter = new CategoryAdapter(this, categoryList, this);
        // attaching data adapter to spinner
        categorySpinner.setAdapter(adapter);

        CountryAdapter countryAdapter = new CountryAdapter(this, countryList);
        // attaching data adapter to spinner
        countrySpinner.setAdapter(countryAdapter);

        LanguageAdapter languageAdapter = new LanguageAdapter(this, languageList);
        // attaching data adapter to spinner
        languageSpinner.setAdapter(languageAdapter);
//        Log.d("WaleedHassan", "checking the data: " + categoryList.size());
    }

    private void spinnerCallBacks() {
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isCategoryAutoSelected) {
                    if (position == 0) {
                        selectedCategory = "";
                    } else {
                        selectedCategory = categoryList.get(position).getId();
                        mGetFilteredChannelList();
                    }

                } else {
                    isCategoryAutoSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isCountryAutoSelected) {
                    if (position == 0) {
                        selectedCountry = "";
                    } else {
                        selectedCountry = countryList.get(position).getCode();
                        mGetFilteredChannelList();
                    }

                } else {
                    isCountryAutoSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isLanguageAutoSelected) {
                    if (position == 0) {
                        selectedLanguage = "";
                    } else {
                        selectedLanguage = languageList.get(position).getCode();
                        mGetFilteredChannelList();
                    }

                } else {
                    isLanguageAutoSelected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void mGetFilteredChannelList() {
        getFilteredChannelList();

    }

    @Override
    public void onFavChannelEventCall(ChannelsModelItem data, int position, boolean isFav) {
        if (AdsManager.getInstance().isPremium(IPTVChannelActivity.this)) {
            ChannelModel channel = new ChannelModel();
            channel.setId(data.getId());
            channel.setName(data.getName());
            if (isFav) {
                addChannelToFav(channel);
            } else {
                deleteFavChannel(channel);
            }

            filteredChannelList.get(position).setFavourite(isFav);
            channelAdapter.notifyDataSetChanged();
        } else {
            goToSubscriptionActivity(this);
        }
    }

    @Override
    public void onItemClickedEvent(ChannelsModelItem data) {
        if (streamList.isEmpty()) return;
        boolean isStreamAvailable = false;
        for (StreamModel stream : streamList) {
            if (Objects.equals(data.getId(), stream.getChannel())) {
                moveToNextScreen(stream);
                isStreamAvailable = true;
                break;
            }
        }
        if (!isStreamAvailable)
            Toast.makeText(this, "Stream not available at the moment!", Toast.LENGTH_SHORT).show();
    }

    private void moveToNextScreen(StreamModel stream) {
        removeIronSource();
        Intent intent = new Intent(IPTVChannelActivity.this, IPTVStreamActivity.class);
        intent.putExtra("StreamUrl", stream.getUrl());
        startActivity(intent);
    }

    private void removeIronSource() {
        showBanner=true;
        if (bannerContainer != null &&
                bannerContainer.getChildCount() > 0 &&
                bannerContainer.getChildAt(0) instanceof IronSourceBannerLayout) {
            IronSource.destroyBanner((IronSourceBannerLayout) bannerContainer.getChildAt(0));
            bannerContainer.removeAllViews();
        }
    }

    //ads
    private void loadBannerAd() {
        if (bannerContainer == null || bannerContainer.getChildCount() > 0) {
            return;
        }
        IronSourceBannerLayout banner = IronSource.createBanner(this, ISBannerSize.BANNER);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        bannerContainer.addView(banner, 0, layoutParams);
        banner.setBannerListener(new BannerListener() {
            @Override
            public void onBannerAdLoaded() {
                Log.d("main_banner_failed", "banner ad loaded");
                banner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBannerAdLoadFailed(IronSourceError error) {
                Log.d("main_banner_failed", error.getErrorMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bannerContainer.removeAllViews();
                    }
                });
            }

            @Override
            public void onBannerAdClicked() {
                Log.d("main_banner_failed", "onBannerAdClicked");
            }

            @Override
            public void onBannerAdScreenPresented() {
                Log.d("main_banner_failed", "onBannerAdScreenPresented");
            }

            @Override
            public void onBannerAdScreenDismissed() {
                Log.d("main_banner_failed", "onBannerAdScreenDismissed");
            }

            @Override
            public void onBannerAdLeftApplication() {
                Log.d("main_banner_failed", "onBannerAdLeftApplication");
            }
        });
        IronSource.loadBanner(banner);
    }

    //timer
    private void startCounter(int time) {
        if (counter != null) {
            counter.cancel();
            counter = null;
        }

        counter = new CountDownTimer(time, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                Constant.GPS134_stream_noadsTimer_local = (int) (millisUntilFinished / 1000);
//                Log.d("Skype", "Rewarded Counter onTick()" + (millisUntilFinished / 1000));
            }

            public void onFinish() {
//                Log.d("Skype", "Rewarded Counter onFinish()");
                isRewardEarned = false;
                Intent intent = new Intent("reward_time_ended");
                LocalBroadcastManager.getInstance(IPTVChannelActivity.this).sendBroadcast(intent);
            }
        };
        counter.start();
    }

    private void cancelTimer() {
        if (counter != null) {
            counter.cancel();
        }
    }

    private void initIronSourceRewardedVideo() {
        IronSource.setRewardedVideoListener(this);
        IronSource.loadRewardedVideo();
    }


    // rewardVideo callbacks
    @Override
    public void onRewardedVideoAdOpened() {
        Log.d("RewardedAdd", "called onRewardedVideoAdOpened() ");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d("RewardedAdd", "called onRewardedVideoAdClosed() ");
        GPS134_stream_noadsTimer_local = GPS134_stream_noadsTimer;
        startCounter(GPS134_stream_noadsTimer_local * 1000);
        isRewardEarned = true;
    }

    @Override
    public void onRewardedVideoAvailabilityChanged(boolean available) {
        Log.d("RewardedAdd", "called onRewardedVideoAvailabilityChanged() " + available);
    }

    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        Log.d("RewardedAdd", "called onRewardedVideoAdRewarded() " + placement.getRewardName());
    }

    @Override
    public void onRewardedVideoAdShowFailed(IronSourceError error) {
        Log.d("RewardedAdd", "called onRewardedVideoAdShowFailed() " + error.getErrorMessage());
    }

    @Override
    public void onRewardedVideoAdClicked(Placement placement) {
        Log.d("RewardedAdd", "called onRewardedVideoAdClicked() ");
    }

    @Override
    public void onRewardedVideoAdStarted() {
        Log.d("RewardedAdd", "called onRewardedVideoAdStarted() ");
    }

    @Override
    public void onRewardedVideoAdEnded() {
        Log.d("RewardedAdd", "called onRewardedVideoAdEnded() ");
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
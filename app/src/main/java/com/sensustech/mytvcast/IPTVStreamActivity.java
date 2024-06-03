package com.sensustech.mytvcast;

import static com.sensustech.mytvcast.IPTVChannelActivity.count;
import static com.sensustech.mytvcast.IPTVChannelActivity.isStreamingActive;
import static com.sensustech.mytvcast.utils.Constant.isRewardEarned;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.ironsource.mediationsdk.IronSource;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.interfaces.adShowCallBack;
import com.sensustech.mytvcast.interfaces.onAdfailedToLoadListner;
import com.sensustech.mytvcast.utils.Constant;
import com.sensustech.mytvcast.utils.IronSourceAdsManger;

public class IPTVStreamActivity extends AppCompatActivity {
    // url of video which we are loading.
    String videoURL = "https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4";
    ImageView ivBackBtn;
    PlayerView pvMain;
    SimpleExoPlayer absPlayerInternal;
    static CountDownTimer counter = null;
    private boolean playWhenReady = true;
    private long playbackPosition = 0;
    private int currentWindow = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iptvstream);
        videoURL = getIntent().getStringExtra("StreamUrl");
        isStreamingActive = false;
        initViews();
        initListeners();
//        registerBroadcast();
        if (!AdsManager.getInstance().isPremium(this)) {
            if (!isRewardEarned) { // check if user watched rewarded add on channel activity

                startCounter(Constant.GPS134_interstitialStream_frequency_local * 1000);
            }
        }


    }

    private void initViews() {
        ivBackBtn = findViewById(R.id.ivBackBtn);
    }

    private void initListeners() {
        ivBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void releasePlayer() {
        if (absPlayerInternal != null) {
            playWhenReady = absPlayerInternal.getPlayWhenReady();
            playbackPosition = absPlayerInternal.getCurrentPosition();
            currentWindow = absPlayerInternal.getCurrentWindowIndex();
            absPlayerInternal.release();
            absPlayerInternal = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initializePlayer();
        hideSystemUi();
        IronSource.onResume(this);
        registerBroadcast();
    }

    @Override
    public void onPause() {
        super.onPause();
        IronSource.onPause(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
//        if (Util.SDK_INT < 24) {
        releasePlayer();
//        cancelTimer();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        cancelTimer();
//        if (Util.SDK_INT >= 24) {
        releasePlayer();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        releasePlayer();
        cancelTimer();
//        cancelTimer();
    }

    @Override
    public void onBackPressed() {
        count++;
        finish();
        releasePlayer();
        cancelTimer();

//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        pvMain.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    private void initializePlayer() {
        Log.d("VideoUrl", videoURL);
        int appNameStringRes = R.string.app_name;
        pvMain = findViewById(R.id.pv_main);

        // creating player view

        TrackSelector trackSelectorDef = new DefaultTrackSelector();
        absPlayerInternal = ExoPlayerFactory.newSimpleInstance(this, trackSelectorDef); //creating a player instance

        String userAgent = Util.getUserAgent(this, this.getString(appNameStringRes));
        DefaultDataSourceFactory defdataSourceFactory = new DefaultDataSourceFactory(this, userAgent);

        Uri uri = Uri.parse(videoURL);
        MediaSource mediaSource = buildMediaSource(uri);


        absPlayerInternal.prepare(mediaSource);
        absPlayerInternal.setPlayWhenReady(true); // start loading video and play it at the moment a chunk of it is available offline
        absPlayerInternal.seekTo(currentWindow, playbackPosition);
        pvMain.setPlayer(absPlayerInternal); // attach surface to the view


//        absPlayerInternal.setPlayWhenReady(playWhenReady);
//        absPlayerInternal.seekTo(currentWindow, playbackPosition);
//        absPlayerInternal.prepare(mediaSource, false, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }


    //ads
    private void startCounter(int time) {
        if (counter != null) {
            counter.cancel();
            counter = null;

        }
        counter = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                Constant.GPS134_interstitialStream_frequency_local = (int) (millisUntilFinished / 1000);
                Log.d("Skype", "Interstitial Counter onTick()" + (millisUntilFinished / 1000));
            }

            public void onFinish() {
                Log.d("Skype", "Interstitial Counter onFinish()");
                cancelTimer();
                if (!isStreamingActive) {
                    showInterstitialAd();
                }
            }
        };
        counter.start();
    }

    private void cancelTimer() {
        if (counter != null) {
            counter.cancel();
            counter = null;
        }
    }

    private void showInterstitialAd() {
        showInterIronsource(new adShowCallBack() {
            @Override
            public void adShown(Boolean bol) {

            }

            @Override
            public void adShownSucceed(Boolean bol) {
                cancelTimer();
            }

            @Override
            public void adFailed(Boolean fal) {
                if (fal) {
                    Constant.GPS134_interstitialStream_frequency_local = Constant.GPS134_interstitialStream_frequency;
//                    startCounter(Constant.GPS134_interstitialStream_frequency_local * 1000);
//                    Log.d("Skype", "Interstitial adFailed()");
                }
            }

            @Override
            public void onClosed() {
//                Log.d("Skype", "Interstitial onClosed()");
                Constant.GPS134_interstitialStream_frequency_local = Constant.GPS134_interstitialStream_frequency;
                startCounter(Constant.GPS134_interstitialStream_frequency_local * 1000);
            }
        });
    }

    public void showInterIronsource(adShowCallBack adCompleted) {
//        if (!AdsManager.getInstance().isPremium(this)) {

        if (IronSource.isInterstitialReady()) {
            cancelTimer();
//            Log.d("Skype", "Interstitial show()");
            IronSourceAdsManger.showInterstitial(this, new adShowCallBack() {
                @Override
                public void adShown(Boolean bol) {
                    adCompleted.adShown(bol);
//                    Log.d("Skype", "Interstitial adShown()");
                }

                @Override
                public void adShownSucceed(Boolean bol) {
                    adCompleted.adShownSucceed(bol);
                }

                @Override

                public void adFailed(Boolean fal) {
                    adCompleted.adFailed(fal);
                }

                @Override
                public void onClosed() {
                    adCompleted.onClosed();
                }
            });
        } else {
            IronSourceAdsManger.loadAPSIntersitial(this, new onAdfailedToLoadListner() {
                @Override
                public void onAdFailedToLoad() {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void adClose() {

                }
            });
//            Log.d("Skype", "Interstitial adNotReady()");
            Constant.GPS134_interstitialStream_frequency_local = Constant.GPS134_interstitialStream_frequency;
            startCounter(Constant.GPS134_interstitialStream_frequency_local * 1000);
        }
    }

    // Our handler for received Intents. This will be called whenever an Intent`
    // with an action named "custom-event-name" is broadcasted.

    private void registerBroadcast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("reward_time_ended"));
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("receiver", "Got message: ");
            if (!AdsManager.getInstance().isPremium(IPTVStreamActivity.this)) {
                showInterstitialAd();
            }
        }
    };


}

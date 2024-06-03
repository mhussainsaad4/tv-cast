package com.sensustech.mytvcast;

import static com.sensustech.mytvcast.MainApplication.isSplash;
import static com.sensustech.mytvcast.utils.ExtensionsKt.goToSubscriptionActivity;
import static com.vincent.filepicker.Constant.showBanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blure.complexview.ComplexView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.sensustech.mytvcast.Adapters.FuncAdapter;
import com.sensustech.mytvcast.interfaces.adShowCallBack;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.utils.ExtensionsKt;
import com.sensustech.mytvcast.utils.IronSourceAdsManger;
import com.sensustech.mytvcast.utils.ItemClickSupport;
import com.sensustech.mytvcast.utils.StreamingManager;
import com.sensustech.mytvcast.utils.StreamingWebServer;
import com.sensustech.mytvcast.utils.Utils;
import com.vincent.filepicker.AdsUtill;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.AudioPickActivity;
import com.vincent.filepicker.activity.ImagePickActivity;
import com.vincent.filepicker.activity.VideoPickActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ComplexView cvBannerCard;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private RelativeLayout includedLayout;
    public static NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private NestedScrollView scroll;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private FuncAdapter adapter;
    private StreamingWebServer server;
    private String myIp;
    private MenuItem castButton;
    private MaterialCardView mirroring;
    private RelativeLayout rel_ads;
    /*private UnifiedNativeAd nativeAd;
    private UnifiedNativeAd nativeAdBack;*/

    private NativeAd nativeAd;
    private NativeAd nativeAdBack;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FrameLayout bannerContainer;
    private DisplayManager dm;
    private AdsUtill adsUtill;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        includedLayout = (RelativeLayout) findViewById(R.id.includedLayout);
        adsUtill = new AdsUtill(this);
        bannerContainer = includedLayout.findViewById(R.id.bannerContainer1);
        cvBannerCard = includedLayout.findViewById(R.id.cvBannerCard);
        ExtensionsKt.getFirebaseToken();
//        initIronSource();
        // dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//         IntegrationHelper.validateIntegration(this);


        isSplash=false;
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        appBarLayout.setBackground(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        recyclerView = includedLayout.findViewById(R.id.recycler);
        scroll = includedLayout.findViewById(R.id.scroll);
        scroll.fullScroll(View.FOCUS_UP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.inflateMenu(R.menu.main);

        mirroring = includedLayout.findViewById(R.id.cvMirroring);


        mirroring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInterAndMoveToNext("MirroringActivity");
//                if (AdsManager.getInstance().isPremium(MainActivity.this)) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        startActivity(new Intent(MainActivity.this, MirroringActivity.class));
//                    } else {
//                        notSupported();
//                    }
//                } else {
//                    Intent it = new Intent(MainActivity.this, SubscriptionActivity.class);
//                    it.putExtra("fromStart", false);
//                    startActivity(it);
//                }
            }
        });

        cvBannerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInterAndMoveToNext("IPTVChannel");
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        rel_ads = includedLayout.findViewById(R.id.rel_ads);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                showInterAndMoveToNext("onDrawerOpened");
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        manager = new LinearLayoutManager(this);
        String[] titles = {"Photo", "Video", "Audio", "YouTube", "Dropbox"};
        int[] images = {R.drawable.label_01_photo, R.drawable.label_02_video, R.drawable.label_03_audio, R.drawable.label_07_yt, R.drawable.label_05_db};
        int[] shades = {R.drawable.label_01_photo_shade, R.drawable.label_02_video_shade, R.drawable.label_03_audio_shade, R.drawable.label_07_yt_shade, R.drawable.label_05_db_shade};
        adapter = new FuncAdapter(titles, images, shades, this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                if (position == 0) {
//                    showAds();
//                    removeIronSource();
//                    showImagePick();
                    showInterAndMoveToNext("showImagePick");
                } else if (position == 1) {
//                    showAds();
//                    removeIronSource();
//                    showVideoPick();
                    showInterAndMoveToNext("showVideoPick");

                } else if (position == 2) {
//                    showAds();
//                    removeIronSource();
//                    showAudioPick();
                    showInterAndMoveToNext("showAudioPick");
                } else if (position == 3) {
                    showInterAndMoveToNext("DropBoxActivity");
//                    showAds();
//                    startActivity(new Intent(MainActivity.this, DropBoxActivity.class));
                } else if (position == 4) {
                    showInterAndMoveToNext("YoutubeActivity");
//                    if (AdsManager.getInstance().isPremium(MainActivity.this)) {
//                        //removeIronSource();
//                        startActivity(new Intent(MainActivity.this, YoutubeActivity.class));
//                    } else {
//                        Intent it = new Intent(MainActivity.this, SubscriptionActivity.class);
//                        it.putExtra("fromStart", false);
//                        startActivity(it);
//                    }
                }
            }
        });
        startWebServer();
        try {
            //AdsManager.getInstance().init(this);
            AdsManager.getInstance().restore(true);
            if (AdsManager.getInstance().needReloadAds) {
                AdsManager.getInstance().needReloadAds = false;
                refreshAd();
                refreshAdBack();
            }
        } catch (Exception e) {
            Log.e("My_TV_Cast_TAG", e.getLocalizedMessage());
        }

//        if (StreamingManager.getInstance(this).getDevice() == null || !StreamingManager.getInstance(this).getDevice().isConnected())
//            startActivity(new Intent(this, ConnectActivity.class));

        registerReceiver(streamBroadcast, new IntentFilter("STREAM_NEW_CONTENT"));
        registerReceiver(adsBroadcast, new IntentFilter("ADS_READY"));
        registerReceiver(checlPremiumBroadcast, new IntentFilter("CHECK_PREMIUM"));

    }

    private void loadBannerAd() {
        if (bannerContainer == null || bannerContainer.getChildCount() > 0) {
            Log.d("main_banner_failed", "banner container null");
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

    public void showAudioPick() {
        com.sensustech.mytvcast.utils.Constant.FROM = "filePicker";
        Intent intent1 = new Intent(MainActivity.this, AudioPickActivity.class);
        intent1.putExtra("IsNeedRecorder", true);
        intent1.putExtra(Constant.MAX_NUMBER, 1);
        intent1.putExtra("isNeedFolderList", true);
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_AUDIO);
    }

    public void showVideoPick() {
        com.sensustech.mytvcast.utils.Constant.FROM = "filePicker";
        Intent intent1 = new Intent(MainActivity.this, VideoPickActivity.class);
        intent1.putExtra("IsNeedCamera", true);
        intent1.putExtra(Constant.MAX_NUMBER, 1);
        intent1.putExtra("isNeedFolderList", true);
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_VIDEO);
    }

    public void showImagePick() {
        com.sensustech.mytvcast.utils.Constant.FROM = "filePicker";
        Intent intent1 = new Intent(MainActivity.this, ImagePickActivity.class);
        intent1.putExtra("IsNeedCamera", true);
        intent1.putExtra(Constant.MAX_NUMBER, 1);
        intent1.putExtra("isNeedFolderList", true);
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(streamBroadcast);
        unregisterReceiver(adsBroadcast);
        unregisterReceiver(checlPremiumBroadcast);
    }

    BroadcastReceiver checlPremiumBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkPremium();
        }
    };
    BroadcastReceiver adsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("My_TV_Cast_TAG", "adsBroadcast->onReceive: ");
            refreshAd();
            refreshAdBack();
            AdsManager.getInstance().initIntersitial();
        }
    };
    BroadcastReceiver streamBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!StreamingManager.getInstance(MainActivity.this).isDeviceConnected()) {
                startActivity(new Intent(MainActivity.this, ConnectActivity.class));
                showInterAndMoveToNext("onItemclick");
                return;
            }
            if (intent.getExtras().getString("fileType").equals("image")) {
                showInterAndMoveToNext("onItemclick");
                String path = intent.getExtras().getString("fileURL");
                String filename = path.substring(path.lastIndexOf("/") + 1);
                server.addFileForPath(filename, path);
                StreamingManager.getInstance(MainActivity.this).showContent(intent.getExtras().getString("fileName"), server.getMimeType(path), "http://" + myIp + ":8080/" + filename);
            } else if (intent.getExtras().getString("fileType").equals("video")) {
                showInterAndMoveToNext("onItemclick");
                String path = intent.getExtras().getString("fileURL");
                String filename = path.substring(path.lastIndexOf("/") + 1);
                server.addFileForPath(filename, path);
                StreamingManager.getInstance(MainActivity.this).playMedia(intent.getExtras().getString("fileName"), server.getMimeType(path), "http://" + myIp + ":8080/" + filename);
            } else if (intent.getExtras().getString("fileType").equals("audio")) {
                showInterAndMoveToNext("onItemclick");
                String path = intent.getExtras().getString("fileURL");
                String filename = path.substring(path.lastIndexOf("/") + 1);
                server.addFileForPath(filename, path);
                StreamingManager.getInstance(MainActivity.this).playAudio(intent.getExtras().getString("fileName"), server.getMimeType(path), "http://" + myIp + ":8080/" + filename);
            }
        }
    };


    public void startWebServer() {
        try {
            myIp = Utils.getIPAddress(true);
            server = new StreamingWebServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopWebServer() {
        try {
            if (server != null && server.isAlive())
                server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        showBackDialog();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                showInterAndMoveToNext("home");
                break;
            case R.id.nav_prem:
                //AppOpenManager.getInstance(getApplication()).showAdIfAvailable(this);
                showInterAndMoveToNext("SubscriptionActivity");
//                Intent it = new Intent(MainActivity.this, SubscriptionActivity.class);
//                it.putExtra("fromStart", false);
//                startActivity(it);
                break;
            case R.id.nav_restore:
                restorePurchases();
                break;
            case R.id.nav_share:
                shareAction();
                break;
            case R.id.nav_rate:
                openURL("https://play.google.com/store/apps/details?id=com.sensustech.mytvcast");
                break;
            case R.id.nav_contact:
                openURL("mailto:support@sensustech.com");
                break;
            case R.id.nav_term_of_use:
                openURL(ExtensionsKt.terms);
                break;
            case R.id.nav_policy:
                openURL(ExtensionsKt.privacy);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openURL(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void shareAction() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My TV Cast allows you to mirror phone screen to your Chromecast. https://play.google.com/store/apps/details?id=com.sensustech.mytvcast");
        Intent intent = Intent.createChooser(shareIntent, "Share");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        castButton = menu.findItem(R.id.screen_mirroring);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.screen_mirroring:
                removeIronSource();
                startActivity(new Intent(this, ConnectActivity.class));
               /* if (StreamingManager.getInstance(this).getDevice() == null || !StreamingManager.getInstance(this).getDevice().isConnected()) {
//                    showInterAndMoveToNext("ConnectActivity");
                    startActivity(new Intent(this, ConnectActivity.class));
                } else {
                    confirmDisconnect();
                }*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        castButton = menu.findItem(R.id.screen_mirroring);
        if (StreamingManager.getInstance(this).getDevice() == null || !StreamingManager.getInstance(this).getDevice().isConnected()) {
            castButton.setIcon(R.drawable.ic_cast);
        } else {
            castButton.setIcon(R.drawable.ic_cast2);
        }

        /*if (StreamingManager.getInstance(this).getDevice() == null && dm != null && dm.getDisplays().length > 1) {
            castButton.setIcon(R.drawable.ic_cast2);
        } else {
            //castButton.setIcon(R.drawable.ic_cast);
        }*/


        return super.onPrepareOptionsMenu(menu);
    }

    public void confirmDisconnect() {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme))
                .setTitle("Disconnect")
                .setMessage("Are you sure you want to disconnect from " + StreamingManager.getInstance(this).getDevice().getFriendlyName() + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StreamingManager.getInstance(MainActivity.this).disconnect();
                        startActivity(new Intent(MainActivity.this, ConnectActivity.class));
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void notSupported() {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme))
                .setMessage("Sorry this feature isn't supported by your device.")
                .setNegativeButton("OK", null)
                .show();
    }

    public void restorePurchases() {
        AdsManager.getInstance().restore(false);
    }

    //Angel. A partir de la version 20.0 de Admob se cambian algunas clases de nombre
    // formats.UnifiedNativeAdView	-> nativead.NativeAdView
    //  formats.UnifiedNativeAd	    -> nativead.NativeAd
    /*private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }


    private void refreshAd() {
        if(!AdsManager.getInstance().isPremium(this)) {
            AdLoader.Builder builder = new AdLoader.Builder(this, "ca-app-pub-4975991316875268/2455881915");
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    if (nativeAd != null) {
                        nativeAd.destroy();
                    }
                    nativeAd = unifiedNativeAd;
                    FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
                    UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.ad_unified_main, null);
                    populateUnifiedNativeAdView(unifiedNativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                    rel_ads.setVisibility(View.VISIBLE);
                }

            });
            VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(false).build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
            builder.withNativeAdOptions(adOptions);
            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {}
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    private void refreshAdBack() {
        if(!AdsManager.getInstance().isPremium(this)) {
            AdLoader.Builder builder = new AdLoader.Builder(this, "ca-app-pub-4975991316875268/7270947090");
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    if (nativeAdBack != null) {
                        nativeAdBack.destroy();
                    }
                    nativeAdBack = unifiedNativeAd;
                }

            });
            VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(false).build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
            builder.withNativeAdOptions(adOptions);
            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {}
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    public void showBackDialog() {
        try {
            AlertDialog.Builder backDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme))
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AdsManager.getInstance().premiumOfferWasShown = false;
                            AdsManager.getInstance().premiumOfferWasClosed = false;
                            if (StreamingManager.getInstance(MainActivity.this).getDevice() != null && StreamingManager.getInstance(MainActivity.this).getDevice().isConnected()) {
                                StreamingManager.getInstance(MainActivity.this).releaseDevice();
                            }
                            stopWebServer();
                            destroyAds();
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .setCancelable(false);
            if (!AdsManager.getInstance().isPremium(this) && nativeAdBack != null) {
                RelativeLayout backDialogLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.back_dialog, null);
                FrameLayout frameLayout = backDialogLayout.findViewById(R.id.back_ads);
                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
                populateUnifiedNativeAdView(nativeAdBack, adView);
                frameLayout.addView(adView);
                backDialog.setView(backDialogLayout);
            } else {
                backDialog.setMessage("Are you sure that you want to leave the app?");
            }
            backDialog.create();
            backDialog.show();
        }catch (Exception e) { finish(); }
    }*/

    private void populateUnifiedNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    private void refreshAd() {
        Log.d("My_TV_Cast_TAG", "refreshAd: ");
        if (!AdsManager.getInstance().isPremium(this)) {
            //AdLoader.Builder builder = new AdLoader.Builder(this, "ca-app-pub-4975991316875268/2455881915");
            AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.native_ad_1));

            builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {


                @Override
                public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                    if (MainActivity.this.nativeAd != null) {
                        MainActivity.this.nativeAd.destroy();
                    }

                    MainActivity.this.nativeAd = nativeAd;
                    FrameLayout frameLayout = includedLayout.findViewById(R.id.fl_adplaceholder);
                    NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified_main, null);
                    populateUnifiedNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                    rel_ads.setVisibility(View.VISIBLE);
                    MainApplication.ad_format="NativeAd";
                    MainActivity.this.nativeAd.setOnPaidEventListener(MainApplication.getApplication().myCallback);
                }

            });
            VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(false).build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
            builder.withNativeAdOptions(adOptions);
            AdLoader adLoader = builder.withAdListener(new AdListener() {

                @Override
                public void onAdFailedToLoad(LoadAdError errorCode) {
                    Log.d("My_TV_Cast_TAG", "onAdFailedToLoad: " + errorCode);

                }
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    private void refreshAdBack() {
        if (!AdsManager.getInstance().isPremium(this)) {
            //AdLoader.Builder builder = new AdLoader.Builder(this, "ca-app-pub-4975991316875268/7270947090");
            AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.native_ad_2));
            builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                @Override
                public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                    if (nativeAdBack != null) {
                        nativeAdBack.destroy();
                    }
                    nativeAdBack = nativeAd;
                    MainApplication.ad_format="NativeAd";
                    nativeAdBack.setOnPaidEventListener(MainApplication.getApplication().myCallback);
                }
            });

            VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(false).build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
            builder.withNativeAdOptions(adOptions);
            AdLoader adLoader = builder.withAdListener(new AdListener() {
               /* @Override
                public void onAdFailedToLoad(int errorCode) {}*/
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    public void showAds() {
        if (!AdsManager.getInstance().isPremium(this)) {
            com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER +=1;
            if (com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER == com.sensustech.mytvcast.utils.Constant.INTER_AD_THRESH){
                com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER =0;
                AdsManager.getInstance().showAds();
            }

        }
    }




    public void showBackDialog() {
        try {
            AlertDialog.Builder backDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme))
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AdsManager.getInstance().premiumOfferWasShown = false;
                            AdsManager.getInstance().premiumOfferWasClosed = false;
                            if (StreamingManager.getInstance(MainActivity.this).getDevice() != null && StreamingManager.getInstance(MainActivity.this).getDevice().isConnected()) {
                                StreamingManager.getInstance(MainActivity.this).releaseDevice();
                            }
                            stopWebServer();
                            destroyAds();
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .setCancelable(false);
//            if (!AdsManager.getInstance().isPremium(this) && nativeAdBack != null) {
//                RelativeLayout backDialogLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.back_dialog, null);
//                FrameLayout frameLayout = backDialogLayout.findViewById(R.id.back_ads);
//                NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
//                populateUnifiedNativeAdView(nativeAdBack, adView);
//                frameLayout.addView(adView);
//                backDialog.setView(backDialogLayout);
//            } else {
                backDialog.setMessage("Are you sure that you want to leave the app?");
//            }
            backDialog.create();
            backDialog.show();
        } catch (Exception e) {
            finish();
        }
    }

    public void destroyAds() {
        AdsManager.getInstance().needReloadAds = true;
        try {
            if (nativeAd != null) {
                nativeAd.destroy();
            }
            if (nativeAdBack != null) {
                nativeAdBack.destroy();
            }
        } catch (Exception e) {
        }
    }

    public void checkPremium() {
        if (AdsManager.getInstance().isPremium(this)) {
            rel_ads.setVisibility(View.GONE);
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.nav_prem).setVisible(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeIronSource();
        IronSource.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();
        AdsManager.getInstance().getPrices();


//        if (com.sensustech.mytvcast.utils.Constant.FROM.contains("filePicker")){
//            com.sensustech.mytvcast.utils.Constant.FROM = "";
//            showInterAndMoveToNext("filepicker_");
//        }
        if (!AdsManager.getInstance().isPremium(MainActivity.this)) {
            bannerContainer.setVisibility(View.VISIBLE);
            if (showBanner) {
                adsUtill.loadAPSBanner(bannerContainer, this);
            }
            IronSource.onResume(this);
        } else {
            bannerContainer.setVisibility(View.GONE);
        }
    }



    private void removeIronSource() {
        if (bannerContainer != null &&
                bannerContainer.getChildCount() > 0 &&
                bannerContainer.getChildAt(0) instanceof IronSourceBannerLayout) {
            IronSource.destroyBanner((IronSourceBannerLayout) bannerContainer.getChildAt(0));
            bannerContainer.removeAllViews();
            Log.d("main_banner_failed", "destroy banner");
        }
    }

    public void showInterIronsource(adShowCallBack adCompleted) {
        if (!AdsManager.getInstance().isPremium(this)) {

            if (IronSource.isInterstitialReady()) {

                IronSourceAdsManger.showInterstitial(this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {

                        adCompleted.adShown(true);

                    }

                    @Override
                    public void adShownSucceed(Boolean bol) {
                        adCompleted.adShownSucceed(bol);
                    }


                    @Override
                    public void adFailed(Boolean fal) {
                        //   AppOpenManager.isAdShow = false;
                        //  showingAds = false;
                        //  adCompleted.adShown(false);
                    }

                    @Override
                    public void onClosed() {

                    }
                });


            } else {

                adCompleted.adShown(true);


            }


        }
    }
   private void showInterAndMoveToNext(String next){
       Log.e("counter","d"+com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER);
       com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER +=1;
       if (!AdsManager.getInstance().isPremium(this) && com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER == com.sensustech.mytvcast.utils.Constant.INTER_AD_THRESH) {
           com.sensustech.mytvcast.utils.Constant.INTER_AD_COUNTER = 0;

           showInterIronsource(new adShowCallBack() {
               @Override
               public void adShown(Boolean bol) {
                   showBanner = false;
                   moveToNextScreen(next);
               }

               @Override
               public void adShownSucceed(Boolean bol) {

               }

               @Override
               public void adFailed(Boolean fal) {
                   moveToNextScreen(next);
               }

               @Override
               public void onClosed() {

               }
           });
       }else{
           moveToNextScreen(next);
       }
    }


    private void moveToNextScreen(String next){
        removeIronSource();
        if (next.contains("showImagePick")){
            showImagePick();
        }else if (next.contains("showVideoPick")){
            showVideoPick();
        }else if (next.contains("showAudioPick")){
            showAudioPick();
        }else if (next.contains("YoutubeActivity")){
            if (AdsManager.getInstance().isPremium(MainActivity.this)) {
                //removeIronSource();
                startActivity(new Intent(MainActivity.this, YoutubeActivity.class));
            } else {
                goToSubscriptionActivity(this);

            }
        }else if (next.contains("DropBoxActivity")){
            startActivity(new Intent(MainActivity.this, DropBoxActivity.class));
        }else if (next.contains("MirroringActivity")){
            if (AdsManager.getInstance().isPremium(MainActivity.this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(MainActivity.this, MirroringActivity.class));
                } else {
                    notSupported();
                }
            } else {
                goToSubscriptionActivity(this);
            }
        }else if (next.contains("ConnectActivity")){
            startActivity(new Intent(this, ConnectActivity.class));
        }else if (next.contains("SubscriptionActivity")){
            goToSubscriptionActivity(this);
        }else if (next.contains("home")){

        }else if (next.contains("IPTVChannel")){
            if (AdsManager.getInstance().isPremium(MainActivity.this)) {
                startActivity(new Intent(MainActivity.this, IPTVChannelActivity.class));

            } else {
                goToSubscriptionActivity(this);
            }

        }

    }


}
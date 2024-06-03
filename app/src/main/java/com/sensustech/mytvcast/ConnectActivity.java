package com.sensustech.mytvcast;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.command.ServiceCommandError;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.ironsource.mediationsdk.IronSource;
import com.sensustech.mytvcast.Adapters.TVsAdapter;
import com.sensustech.mytvcast.Model.DeviceModel;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.utils.IronSourceAdsManger;
import com.sensustech.mytvcast.utils.SimpleDividerItemDecoration;
import com.sensustech.mytvcast.utils.StreamingManager;
import com.sensustech.mytvcast.interfaces.adShowCallBack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;


public class ConnectActivity extends AppCompatActivity implements DiscoveryManagerListener {

    private Toolbar toolbar;
    private TextView devicesText;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private TVsAdapter adapter;
    private ArrayList<DeviceModel> devices = new ArrayList<DeviceModel>();
    private AlertDialog pairingAlertDialog;
    private AlertDialog pairingCodeDialog;
    private RelativeLayout rel_ads;
    private NativeAd nativeAd;
    private BroadcastReceiver adsBroadcast;
    private BlurView blurView2;
    private LottieAnimationView loading;
    BroadcastReceiver checlPremiumBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkPremium();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.b_close);
        recyclerView = findViewById(R.id.recycler);
        devicesText = findViewById(R.id.tv_count);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        manager = new LinearLayoutManager(this);
        adapter = new TVsAdapter(devices, this);
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        rel_ads = findViewById(R.id.rel_ads);
        blurView2 = findViewById(R.id.blurView);
        loading = findViewById(R.id.animationView);


        float radius = 2f;
        View decorView = getWindow().getDecorView();
        // ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurView2.setupWith(rootView, new RenderScriptBlur(this)) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);

        /*ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if(position != RecyclerView.NO_POSITION && position >= 0 && position < devices.size()) {
                    ConnectableDevice mTV = (ConnectableDevice) devices.get(position).connectableDevice;
                    loading.setVisibility(View.VISIBLE);
                    blurView2.setVisibility(View.VISIBLE);
                    mTV.addListener(deviceListener);
                    mTV.setPairingType(null);
                    mTV.connect();
                }
            }
        });*/
        adapter.setOnItemListner(new TVsAdapter.MyAdapterListener() {
            @Override
            public void onContainerClick(boolean enable, int position) {
                if(enable){
                    if (StreamingManager.getInstance(ConnectActivity.this).getDevice() == null ||
                            !StreamingManager.getInstance(ConnectActivity.this).getDevice().isConnected()) {
                        if(position != RecyclerView.NO_POSITION && position >= 0 && position < devices.size()) {
                            ConnectableDevice mTV = (ConnectableDevice) devices.get(position).connectableDevice;
                            loading.setVisibility(View.VISIBLE);
                            blurView2.setVisibility(View.VISIBLE);
                            mTV.addListener(deviceListener);
                            mTV.setPairingType(null);
                            mTV.connect();
                        }
//                    startActivity(new Intent(this, ConnectActivity.class));
                    } else {
                        confirmDisconnect(true,position);
                    }
                }
                else {
                    confirmDisconnect(false, position);
                }
            }
        });
        DiscoveryManager.init(getApplicationContext());
        startSearch();
  /*      if (!AdsManager.getInstance().premiumOfferWasShown && !AdsManager.getInstance().isPremium(this)) {
            AdsManager.getInstance().premiumOfferWasShown = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppOpenManager.getInstance(getApplication()).showAdIfAvailable(ConnectActivity.this);
                }
            }, 500);
        }*/
        if(!AdsManager.getInstance().isPremium(this)) {
            refreshAd();
            adsBroadcast = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    refreshAd();
                }
            };
            registerReceiver(adsBroadcast, new IntentFilter("ADS_READY"));
        }
        registerReceiver(checlPremiumBroadcast, new IntentFilter("CHECK_PREMIUM"));
    }
    public void confirmDisconnect(boolean connect, int position) {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme))
                .setTitle("Disconnect")
                .setCancelable(false)
                .setMessage("Are you sure you want to disconnect from " + StreamingManager.getInstance(this).getDevice().getFriendlyName() + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StreamingManager.getInstance(ConnectActivity.this).disconnect();
                        if (connect) {
                            ConnectableDevice mTV = (ConnectableDevice) devices.get(position).connectableDevice;
                            loading.setVisibility(View.VISIBLE);
                            blurView2.setVisibility(View.VISIBLE);
                            mTV.addListener(deviceListener);
                            mTV.setPairingType(null);
                            mTV.connect();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.setFlag(true);

                    }
                })
                .show();
    }

    public void checkPremium() {
        if(AdsManager.getInstance().isPremium(this)) {
            rel_ads.setVisibility(View.GONE);
        }
    }

    public void checkDevices() {
        if (devices.size() == 0) {
            devicesText.setText("Searching for devices...");
        } else {
            devicesText.setText("Found (" + devices.size() + ") device(s)");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
//            showInterAndMoveToNext("home");
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showInterAndMoveToNext("onBackPressed");
//        super.onBackPressed();
//        stopSearch();
    }

    public void startSearch() {
        DiscoveryManager.getInstance().registerDefaultDeviceTypes();
        DiscoveryManager.getInstance().setPairingLevel(DiscoveryManager.PairingLevel.ON);
        DiscoveryManager.getInstance().addListener(this);
        DiscoveryManager.getInstance().start();
    }

    public void stopSearch() {
        try {
            DiscoveryManager.getInstance().stop();
            DiscoveryManager.destroy();
        }catch (Exception e){}
    }

    @Override
    public void onDeviceAdded(DiscoveryManager manager, ConnectableDevice device) {
        try {
            DeviceModel dm = new DeviceModel();
            dm.name = device.getFriendlyName();
            dm.series = device.getModelName();
            dm.deviceIp = device.getIpAddress();
            dm.connectableDevice = device;
            if (!devices.contains(dm) && (isChromecast(device) || device.getModelName().toLowerCase().equals("chromecast"))) {
                devices.add(dm);
                adapter.notifyDataSetChanged();
                checkDevices();
            }
        }catch (Exception e){}
    }

    public final boolean isChromecast(ConnectableDevice connectableDevice) {
        if (connectableDevice != null) {
            String connectedServiceNames = connectableDevice.getConnectedServiceNames();
            if (connectedServiceNames != null) {
                String lowerCase = connectedServiceNames.toLowerCase();
                return lowerCase.contains("chromecast");
            }
        }
        return false;
    }

    @Override
    public void onDeviceUpdated(DiscoveryManager manager, ConnectableDevice device) {

    }

    @Override
    public void onDeviceRemoved(DiscoveryManager manager, ConnectableDevice device) {
        for (DeviceModel dm : devices) {
            if (dm.name.equals(device.getFriendlyName())) {
                devices.remove(dm);
                adapter.notifyDataSetChanged();
                checkDevices();
                break;
            }
        }
    }

    @Override
    public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {

    }


    private ConnectableDeviceListener deviceListener = new ConnectableDeviceListener() {
        @Override
        public void onPairingRequired(final ConnectableDevice device, DeviceService service, DeviceService.PairingType pairingType) {
            Log.d("Test", "Connected to " + device.getIpAddress());
            switch (pairingType) {
                case FIRST_SCREEN:
                    Log.d("Test", "First Screen");
                    pairingAlertDialog = new AlertDialog.Builder(ConnectActivity.this)
                            .setTitle("Pairing with TV")
                            .setMessage("Please confirm the connection on your TV.")
//                            .setPositiveButton("OK", null)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    loading.setVisibility(View.VISIBLE);
//                                    blurView.setVisibility(View.VISIBLE);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    if(!isFinishing())
                        pairingAlertDialog.show();
                    break;
                case PIN_CODE:
                case MIXED:
                    Log.d("Test", "Pin Code");
                    final EditText input = new EditText(ConnectActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    pairingCodeDialog = new AlertDialog.Builder(ConnectActivity.this)
                            .setTitle("Enter Pairing Code from your TV")
                            .setView(input)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    if (device != null) {
                                        String value = input.getText().toString().trim();
                                        device.sendPairingKey(value);
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
//                                        loading.setVisibility(View.VISIBLE);
//                                        blurView.setVisibility(View.VISIBLE);
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                }
                            })
                            .create();
                    if(!isFinishing()) {
                        pairingCodeDialog.show();
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                    break;
                case NONE:
                default:
                    break;
            }
        }

        @Override
        public void onConnectionFailed(ConnectableDevice device, ServiceCommandError error) {
            Log.d("Test", "onConnectFailed");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
            Toast.makeText(ConnectActivity.this,"something went wrong please try again!",Toast.LENGTH_LONG).show();
            loading.setVisibility(View.GONE);
            blurView2.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        }

        @Override
        public void onDeviceReady(ConnectableDevice device) {
            Log.d("Test", "onPairingSuccess");

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    loading.setVisibility(View.GONE);
                    blurView2.setVisibility(View.INVISIBLE);
                    try {
                        if (pairingAlertDialog != null && pairingAlertDialog.isShowing()) {
                            pairingAlertDialog.dismiss();
                        }
                        if (pairingCodeDialog != null && pairingCodeDialog.isShowing()) {
                            pairingCodeDialog.dismiss();
                        }
                    } catch (Exception e) {
                    }
                    StreamingManager.getInstance(ConnectActivity.this).setDevice(device);
                    finish();
                    stopSearch();
                }
            }, 2000);

        }

        @Override
        public void onDeviceDisconnected(ConnectableDevice device) {
            Log.d("Test", "Device Disconnected");
            loading.setVisibility(View.GONE);
            blurView2.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCapabilityUpdated(ConnectableDevice device, List<String> added, List<String> removed) {

        }

    };

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

    private void refreshAd() {
        if(!AdsManager.getInstance().isPremium(this)) {
            AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.native_ad_1));

            builder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {


                @Override
                public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                    if (isDestroyed() || isFinishing() || isChangingConfigurations()) {
                        nativeAd.destroy();
                        return;
                    }
                    if (ConnectActivity.this.nativeAd != null) {
                        ConnectActivity.this.nativeAd.destroy();
                    }
                    ConnectActivity.this.nativeAd = nativeAd;
                    FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
                    NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.ad_unified_main, null);
                    populateUnifiedNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                    rel_ads.setVisibility(View.VISIBLE);
                    MainApplication.ad_format="NativeAd";
                    ConnectActivity.this.nativeAd.setOnPaidEventListener(MainApplication.getApplication().myCallback);

                }

            });
            VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(false).build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
            builder.withNativeAdOptions(adOptions);
            AdLoader adLoader = builder.build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

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

    @Override
    protected void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        super.onDestroy();
        if (adsBroadcast != null) {
            unregisterReceiver(adsBroadcast);
            adsBroadcast = null;
        }
        unregisterReceiver(checlPremiumBroadcast);
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
//        if (next.contains("home")){
//            onBackPressed();
//        }
        try {
            if (next.contains("onBackPressed")){
                super.onBackPressed();
                stopSearch();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
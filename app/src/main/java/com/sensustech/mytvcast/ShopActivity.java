package com.sensustech.mytvcast;

import static com.sensustech.mytvcast.utils.iap.IapConnector.PURCHASE_LIFETIME_SKU;
import static com.sensustech.mytvcast.utils.iap.IapConnector.SUBSCRIPTION_1_MONTH_SKU;
import static com.sensustech.mytvcast.utils.iap.IapConnector.SUBSCRIPTION_1_YEAR_SKU;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.ProductDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.ironsource.mediationsdk.IronSource;
import com.sensustech.mytvcast.Utils.AdsManager;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ShopActivity extends AppCompatActivity {

    private ImageButton btn_close;
    private Button btn_free;
    private Button btn_monthly;
    private Button btn_onetime;
    private TextView privacy;
    private TextView tv_price_2;
    private TextView monthPrice;
    private TextView lifetimePrice;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String actualYearlySub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        btn_close = findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOffer();
                finish();
            }
        });
        btn_free = findViewById(R.id.btn_free);
        btn_monthly = findViewById(R.id.btn_monthly);
        btn_onetime = findViewById(R.id.btn_onetime);
        privacy = findViewById(R.id.privacy);
        tv_price_2 = findViewById(R.id.tv_price_2);
        monthPrice = findViewById(R.id.monthPrice);
        lifetimePrice = findViewById(R.id.lifetimePrice);

        actualYearlySub = SUBSCRIPTION_1_YEAR_SKU;

        btn_free.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdsManager.getInstance().makeSubscription(ShopActivity.this, actualYearlySub);
            }
        });
        btn_monthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdsManager.getInstance().makeSubscription(ShopActivity.this, SUBSCRIPTION_1_MONTH_SKU);
            }
        });
        btn_onetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdsManager.getInstance().makePurchase(ShopActivity.this, PURCHASE_LIFETIME_SKU);
            }
        });
        readPrices();

        //registerReceiver(billingBroadcast, new IntentFilter("BILLING_INIT"));
        registerReceiver(premiumBroadcast, new IntentFilter("CLOSE_PREMIUM"));

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        fetchConfig();
    }

    private void fetchConfig() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            String subscriptionType = mFirebaseRemoteConfig.getString("yearly_subscription_type");
                            actualYearlySub = subscriptionType;
                            readPrices();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(billingBroadcast);
        unregisterReceiver(premiumBroadcast);
    }

    /*BroadcastReceiver billingBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            readPrices();
        }
    };*/
    BroadcastReceiver premiumBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    public void readPrices() {
        /*try {
            if (AdsManager.getInstance().getBP() != null && AdsManager.getInstance().getBP().isInitialized()) {
                new readPricesAsync().execute();
            }
        } catch (Exception e) {
        }*/

        //Replacement for AsyncTask deprecation
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //Background work here
                //final ArrayList<SkuDetails> purchaseListingDetails = new ArrayList<>();
                final HashMap<String, ProductDetails> purchaseListingDetails = new HashMap<>(3);

                do {
                    if (!AdsManager.getInstance().mapPrices.isEmpty()) {
                        for (String purchaseId :
                                AdsManager.getInstance().mapPrices.keySet()) {
                            if (purchaseId.equals(PURCHASE_LIFETIME_SKU)) {
                                purchaseListingDetails.put(PURCHASE_LIFETIME_SKU, AdsManager.getInstance().mapPrices.get(purchaseId));
                            } else if (purchaseId.equals(actualYearlySub)) {
                                purchaseListingDetails.put(actualYearlySub, AdsManager.getInstance().mapPrices.get(purchaseId));
                            } else if (purchaseId.equals(SUBSCRIPTION_1_MONTH_SKU)) {
                                purchaseListingDetails.put(SUBSCRIPTION_1_MONTH_SKU, AdsManager.getInstance().mapPrices.get(purchaseId));
                            }
                        }
                    }
                    //Waits for completion
                    if (purchaseListingDetails.size() >= 3)
                        break;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (true);

                //return purchaseListingDetails;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        // TODO: 17/6/21 continuar por aqui 
                    }
                });
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (AdsManager.getInstance().premiumOfferWasClosed) {
            super.onBackPressed();
            closeOffer();
        }
    }

    public void closeOffer() {
        AdsManager.getInstance().premiumOfferWasClosed = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if (AdsManager.getInstance().getBP() != null && !AdsManager.getInstance().getBP().handleActivityResult(requestCode, resultCode, data)) {
        super.onActivityResult(requestCode, resultCode, data);
        //}
    }

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

}
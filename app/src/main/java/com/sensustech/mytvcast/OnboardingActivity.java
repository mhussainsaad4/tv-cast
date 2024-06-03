package com.sensustech.mytvcast;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.sensustech.mytvcast.Adapters.OnboardingAdapter;
import com.sensustech.mytvcast.Model.OnboardItem;
import com.vincent.filepicker.AppPreferences;
import com.sensustech.mytvcast.Utils.AdsManager;
import com.sensustech.mytvcast.utils.RemoteConfigValues;

import java.util.ArrayList;
import java.util.Objects;

public class OnboardingActivity extends AppCompatActivity {

    ArrayList< OnboardItem > onBoardItems = new ArrayList<>();
    private ViewPager mViewPager;
    private OnboardingAdapter adapter;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        mViewPager = (ViewPager) findViewById(R.id.pager_intro);

        setFullScreenActivity();
        prepareOnboarding();

        adapter = new OnboardingAdapter(this, onBoardItems);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Button btnContinue = findViewById(R.id.continue_btn);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentIndex == 0) {
                    mViewPager.setCurrentItem(1);

                } else if(currentIndex==1){
                    mViewPager.setCurrentItem(2);
                }else if(currentIndex==2){
                    mViewPager.setCurrentItem(3);
                }else if (currentIndex == 3) {
                    if (RemoteConfigValues.getInstance().isRemotePaymentCard() && !AdsManager.getInstance().isPremium(OnboardingActivity.this)) {
                        Intent it = new Intent(OnboardingActivity.this, SubscriptionActivity.class);
                        it.putExtra("fromStart", true);
                        startActivity(it);
                    }
                    else if (!RemoteConfigValues.getInstance().isRemotePaymentCard() && !AdsManager.getInstance().isPremium(OnboardingActivity.this)) {
                        if (RemoteConfigValues.getInstance().isRemotePaymentNoTrial()&& Objects.equals(RemoteConfigValues.getInstance().getRemotePriceNewPlan(), "yearly_notrial")) {
                            Intent it = new Intent(OnboardingActivity.this, SubscriptionNewActivity1.class);
                            it.putExtra("fromStart", true);
                            startActivity(it);
                        }
                        else if (RemoteConfigValues.getInstance().isRemotePaymentNoTrial()&& Objects.equals(RemoteConfigValues.getInstance().getRemotePriceNewPlan(), "weekly_notrial")) {
                            Intent it = new Intent(OnboardingActivity.this, SubscriptionNewActivity2.class);
                            it.putExtra("fromStart", true);
                            startActivity(it);
                        }
                        else {
                            Intent it = new Intent(OnboardingActivity.this, SubscriptionActivity.class);
                            it.putExtra("fromStart", true);
                            startActivity(it);
                        }
                    } else {
                        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                    }
                    AppPreferences.getInstance(OnboardingActivity.this).saveData("isOnboarding", true);
                    finish();
                }
            }
        });

    }

    public void setFullScreenActivity() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void prepareOnboarding() {
        String[] header = {"New TV cast On your hand","Easy connect with your TV cast","You favorites channels","Let's start with your settings"};
        String[] desc = {"Manage your device with your own phone, comfortably and easy","Connect to your TV cast less than 60 to start enjoying your content", "One device, hundreds of option to choose and enjoy","Please complete the process to have a better experience"};
        int[] imageId = {R.raw.animation1,R.raw.animation2, R.raw.animation3,R.raw.animation5};

        for (int i = 0; i < imageId.length; i++) {
            OnboardItem item = new OnboardItem();
            item.setLottieId(imageId[i]);
            item.setTitle(header[i]);
            item.setSubtitle(desc[i]);

            onBoardItems.add(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreenActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setFullScreenActivity();
    }
}
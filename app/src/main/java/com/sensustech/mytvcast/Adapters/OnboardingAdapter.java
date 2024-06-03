package com.sensustech.mytvcast.Adapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.airbnb.lottie.LottieAnimationView;
import com.sensustech.mytvcast.Model.OnboardItem;
import com.sensustech.mytvcast.R;

import java.util.ArrayList;

public class OnboardingAdapter extends PagerAdapter {
    private Context mContext;
    ArrayList<OnboardItem> onBoardItems = new ArrayList<>();

    public OnboardingAdapter(Context mContext, ArrayList<OnboardItem> items) {
        this.mContext = mContext;
        this.onBoardItems = items;
    }

    @Override
    public int getCount() {
        return onBoardItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.onboarding_layout, container, false);

        OnboardItem item = onBoardItems.get(position);

        LottieAnimationView imageView = (LottieAnimationView) itemView.findViewById(R.id.animationView);
        imageView.setAnimation(item.getLottieId());

        TextView tv_title = (TextView) itemView.findViewById(R.id.tv_title);

/*        SpannableStringBuilder builder = new SpannableStringBuilder();
        String ourStr;
        if (position == 1) {

            ourStr = "\nScreen ";
            SpannableString ourSpannable = new SpannableString(ourStr);
            ourSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.tt_black)), 0, ourStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(ourSpannable);

            String tosStr = "Mirroring";
            SpannableString tosSpannable = new SpannableString(tosStr);
            tosSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorOnboardingRed)), 0, tosStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(tosSpannable);

        } else {

       *//*     ourStr = "Share your\n";
            SpannableString ourSpannable = new SpannableString(ourStr);
            ourSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.tt_black)), 0, ourStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(ourSpannable);

            String tosStr = "Content on TV";
            SpannableString tosSpannable = new SpannableString(tosStr);
            tosSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.colorOnboardingBlue)), 0, tosStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(tosSpannable);*//*

        }*/

        tv_title.setText(item.getTitle());

        TextView tv_content = (TextView) itemView.findViewById(R.id.tv_desc);
        tv_content.setText(item.getSubtitle());


        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}

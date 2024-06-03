package com.sensustech.mytvcast.Adapters.iptv_adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sensustech.mytvcast.Network.Models.category.CategoryModelItem;
import com.sensustech.mytvcast.Network.Models.country.CountryModelItem;
import com.sensustech.mytvcast.R;
import com.sensustech.mytvcast.interfaces.IPTVFilterCallBacks;

import java.util.ArrayList;
import java.util.List;

public class CountryAdapter extends ArrayAdapter<CountryModelItem> {
    public CountryAdapter(@NonNull Context context, @NonNull ArrayList<CountryModelItem> list) {
        super(context, R.layout.item_drop_down, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // convertView which is recyclable view
        View currentItemView = convertView;

        // of the recyclable view is null then inflate the custom layout for the same
        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_drop_down, parent, false);
        }

        // get the position of the view from the ArrayAdapter
        CountryModelItem data = getItem(position);
        Log.d("WaleedHassan","name: " + data.getName());

        TextView tvTitle = currentItemView.findViewById(R.id.tvTitle);
        tvTitle.setText(data.getName());

        // then return the recyclable view
        return currentItemView;
    }
}


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
import androidx.recyclerview.widget.RecyclerView;

import com.sensustech.mytvcast.Network.Models.category.CategoryModelItem;
import com.sensustech.mytvcast.R;
import com.sensustech.mytvcast.interfaces.IPTVFilterCallBacks;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends ArrayAdapter<CategoryModelItem> {
    IPTVFilterCallBacks callback;
    public CategoryAdapter(@NonNull Context context, @NonNull ArrayList<CategoryModelItem> list, IPTVFilterCallBacks mCallback) {
        super(context, R.layout.item_drop_down, list);
        callback = mCallback;
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
        CategoryModelItem data = getItem(position);

        TextView tvTitle = currentItemView.findViewById(R.id.tvTitle);
        tvTitle.setText(data.getName());

//        currentItemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                callback.onSelectCategoryEvent(data);
//                Log.d("WaleedHassan","Clicked Event in Adapter");
//            }
//        });
        // then return the recyclable view
        return currentItemView;
    }
}

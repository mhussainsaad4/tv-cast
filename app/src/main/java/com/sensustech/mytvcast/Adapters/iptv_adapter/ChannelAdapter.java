package com.sensustech.mytvcast.Adapters.iptv_adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sensustech.mytvcast.Network.Models.channel.ChannelsModelItem;
import com.sensustech.mytvcast.R;
import com.sensustech.mytvcast.interfaces.IPTVFilterCallBacks;

import java.util.ArrayList;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.MyViewHolder> {

    Context context;
    public ArrayList<ChannelsModelItem> channelList;
    public IPTVFilterCallBacks callBack;

    public ChannelAdapter(Context mContext, ArrayList<ChannelsModelItem> mChannelList, IPTVFilterCallBacks mCallBack) {
        super();
        context = mContext;
        channelList = mChannelList;
        callBack = mCallBack;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_iptv_channel, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ChannelsModelItem data = channelList.get(position);

        if (data.getFavourite()) {
            holder.ivFavBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fav_fill));
        } else {
            holder.ivFavBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fav));
        }

        if (position == 0) {
            holder.separatorView.setVisibility(View.GONE);
        } else {
            holder.separatorView.setVisibility(View.VISIBLE);
        }
        holder.tvChannelName.setText(data.getName());
        if (!data.getLanguages().isEmpty()){
            holder.tvLanguage.setText(data.getLanguages().get(0));
        }
        holder.ivFlagIcon.setText(countryCodeToEmoji(data.getCountry()));
        holder.ivFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onFavChannelEventCall(
                        data,
                        position,
                        !data.getFavourite()
                        );
            }
        });


        // itemView click listener not working due to view's layers
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onItemClickedEvent(data);
            }
        });

        holder.tvChannelName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onItemClickedEvent(data);
            }
        });

        holder.ivFlagIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onItemClickedEvent(data);
            }
        });

        holder.tvLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onItemClickedEvent(data);
            }
        });

    }



    @Override
    public int getItemCount() {
        return channelList.size();
    }

    public String countryCodeToEmoji(String code) {

        // offset between uppercase ASCII and regional indicator symbols
        int OFFSET = 127397;

        // validate code
        if (code == null || code.length() != 2) {
            return "";
        }

        //fix for uk -> gb
        if (code.equalsIgnoreCase("uk")) {
            code = "gb";
        }

        // convert code to uppercase
        code = code.toUpperCase();

        StringBuilder emojiStr = new StringBuilder();

        //loop all characters
        for (int i = 0; i < code.length(); i++) {
            emojiStr.appendCodePoint(code.charAt(i) + OFFSET);
        }

        // return emoji
        return emojiStr.toString();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFavBtn;
        TextView tvChannelName;
        TextView ivFlagIcon;
        TextView tvLanguage;

        View separatorView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFavBtn = itemView.findViewById(R.id.ivFavBtn);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
            ivFlagIcon = itemView.findViewById(R.id.ivFlagIcon);
            tvLanguage = itemView.findViewById(R.id.tvLanguage);
            separatorView = itemView.findViewById(R.id.separatorView);
        }
    }
}

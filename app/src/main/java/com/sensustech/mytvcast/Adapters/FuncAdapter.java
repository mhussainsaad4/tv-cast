package com.sensustech.mytvcast.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sensustech.mytvcast.R;

public class FuncAdapter extends RecyclerView.Adapter<FuncAdapter.BtnsViewHolder> {

    private String[] titles;
    private int[] images;
    private int[] shades;
    private Activity activity;

    public FuncAdapter(String[] titles, int[] images, int[] shades, Activity activity) {
        this.titles = titles;
        this.images = images;
        this.shades = shades;
        this.activity = activity;
    }

    @NonNull
    @Override
    public FuncAdapter.BtnsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new BtnsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuncAdapter.BtnsViewHolder holder, int position) {
        switch (position){
            case 0: {
                holder.title1.setText("Photo");
                holder.title2.setText("Broadcast Photos");
                holder.image.setImageResource(R.drawable.ic_image_icon_new);
                break;
            }
            case 1: {
                holder.title1.setText("Video");
                holder.title2.setText("Broadcast Video");
                holder.image.setImageResource(R.drawable.ic_video_icon_new);
                break;
            }
            case 2: {
                holder.title1.setText("Audio");
                holder.title2.setText("Broadcast Audio & Music");
                holder.image.setImageResource(R.drawable.ic_music_icon_new);
                break;
            }
            case 3: {
                holder.title1.setText("DropBox");
                holder.title2.setText("Broadcast DropBox Files");
                holder.image.setImageResource(R.drawable.ic_dropbox_icon_new);
                break;
            }
            case 4: {
                holder.title1.setText("YouTube");
                holder.title2.setText("Broadcast YouTube");
                holder.image.setImageResource(R.drawable.ic_youtube_icon_new);
                break;
            }
        }
//        holder.title.setText(titles[position]);
//        holder.image.setImageResource(images[position]);
//        holder.shade.setImageResource(shades[position]);
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public static class BtnsViewHolder extends RecyclerView.ViewHolder {

        private TextView title1;

        private TextView title2;
        private ImageView image;
//        private ImageView shade;

        public BtnsViewHolder(@NonNull View itemView) {
            super(itemView);

            title1 = itemView.findViewById(R.id.tvTitle1);
            title2 = itemView.findViewById(R.id.tvTitle2);
            image = itemView.findViewById(R.id.image_new);
//            shade = itemView.findViewById(R.id.shade);

        }
    }
}

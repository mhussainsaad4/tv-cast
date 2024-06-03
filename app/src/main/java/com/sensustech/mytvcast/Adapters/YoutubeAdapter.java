package com.sensustech.mytvcast.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sensustech.mytvcast.Model.VideoModel;
import com.sensustech.mytvcast.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeAdapter.YoutubeViewHolder>{

    private Activity activity;
    private ArrayList<VideoModel> videos;

    public YoutubeAdapter(ArrayList<VideoModel> videos, Activity activity) {
        this.videos = videos;
        this.activity = activity;
    }

    @NonNull
    @Override
    public YoutubeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_item, parent, false);
        return new YoutubeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YoutubeViewHolder holder, int position) {
        holder.title.setText(videos.get(position).videoName);
        holder.author.setText(videos.get(position).videoAuthor);
        Picasso.get().load(videos.get(position).videoThumb).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public static class YoutubeViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView author;
        private ImageView imageView;

        public YoutubeViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_name_video);
            author = itemView.findViewById(R.id.tv_name_autor);
            imageView = itemView.findViewById(R.id.image_preview);

        }
    }
}

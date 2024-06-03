package com.sensustech.mytvcast.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.sensustech.mytvcast.R;
import com.sensustech.mytvcast.utils.Dropbox.FileThumbnailRequestHandler;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DBAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_FOLDER = 1;
    private static final int VIEW_TYPE_FILE = 2;

    private List<Metadata> files;
    private Activity activity;
    private Picasso mPicasso;
    int selectedPosition = -1;

    public DBAdapter(List<Metadata> files, Activity activity, Picasso picasso) {
        mPicasso = picasso;
        this.files = files;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        if (files.get(position) instanceof FileMetadata) {
            return VIEW_TYPE_FILE;
        } else {
            return VIEW_TYPE_FOLDER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_FOLDER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cloud_folder_item, parent, false);
            return new FolderViewHolder(view);
        } else if (viewType == VIEW_TYPE_FILE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cloud_file_item, parent, false);
            return new FileViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_FOLDER:
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                folderViewHolder.title.setText(files.get(position).getName());
                folderViewHolder.image.setImageResource(R.drawable.ic_folder);
                break;
            case VIEW_TYPE_FILE:
                FileViewHolder fileViewHolder = (FileViewHolder) holder;
                String fileName = files.get(position).getName();
                fileViewHolder.title.setText(fileName);
                if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                    mPicasso.load(FileThumbnailRequestHandler.buildPicassoUri((FileMetadata) files.get(position))).into(fileViewHolder.image);
                }
                if (fileName.endsWith(".mov") || fileName.endsWith(".mp4")) {
                    fileViewHolder.image.setImageResource(R.drawable.i_video_file);
                }
                if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
                    fileViewHolder.image.setImageResource(R.drawable.i_audio_file);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView image;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_name_folder);
            image = itemView.findViewById(R.id.image_file);
        }
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView image;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_name_file);
            image = itemView.findViewById(R.id.image_file);

        }
    }
}

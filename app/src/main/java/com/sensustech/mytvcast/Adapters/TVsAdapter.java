package com.sensustech.mytvcast.Adapters;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sensustech.mytvcast.Model.DeviceModel;
import com.sensustech.mytvcast.R;
import com.sensustech.mytvcast.utils.StreamingManager;

import java.util.ArrayList;
import java.util.Objects;

public class TVsAdapter extends RecyclerView.Adapter<TVsAdapter.DeviceViewHolder> {

    private ArrayList<DeviceModel> devices;
    private Activity activity;
    private MyAdapterListener listener;
    private boolean flag=false;
    public TVsAdapter(ArrayList<DeviceModel> devices, Activity activity) {
        this.devices = devices;
        this.activity = activity;
    }

    @NonNull
    @Override
    public TVsAdapter.DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TVsAdapter.DeviceViewHolder holder, int position) {
        holder.title.setText(devices.get(position).name);
        holder.series.setText(devices.get(position).series);

        if (StreamingManager.getInstance(activity).getDevice() != null&&devices.get(position).connectableDevice.getId()!=null) {
            holder.toggleSwitch.setChecked(Objects.equals(devices.get(position).connectableDevice.getId(), StreamingManager.getInstance(activity).getDevice().getId()));
        }
        holder.toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!flag){
                    holder.toggleSwitch.setEnabled(false);
                listener.onContainerClick(b, position);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            holder.toggleSwitch.setEnabled(true);
                        }
                    },1000);
            }}
        });
        flag=false;
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }
    public void setFlag(boolean flag){
        this.flag=flag;
        notifyDataSetChanged();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView series;
        private SwitchCompat toggleSwitch;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_name);
            series = itemView.findViewById(R.id.tv_series);
            toggleSwitch = itemView.findViewById(R.id.toggleSwitch);

        }
    }

    public interface MyAdapterListener {
        void onContainerClick(boolean enable, int position);
    }

    public void setOnItemListner(MyAdapterListener onItemListner) {
        this.listener = onItemListner;
    }

}

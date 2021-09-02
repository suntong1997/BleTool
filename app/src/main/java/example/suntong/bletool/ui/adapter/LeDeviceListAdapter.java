package example.suntong.bletool.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import example.suntong.bletool.R;
import example.suntong.bletool.ui.activity.BleFunctionActivity;
import example.suntong.bletool.bean.DeviceItemBean;


public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.LeDeviceViewHolder> {
    private List<DeviceItemBean> mLeDevices = new ArrayList<>();//源数据
    private List<DeviceItemBean> mFilterList = new ArrayList<>();//过滤数据

    private final Context mContext;
    //    boolean isSelect = false;
    private boolean mOptional = false;

    public LeDeviceListAdapter(List<DeviceItemBean> list, Context mContext) {
        this.mContext = mContext;
        mLeDevices = list;
        mFilterList = list;
    }

    public LeDeviceListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public LeDeviceViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = (LayoutInflater.from(mContext).inflate(R.layout.listitem_device, parent, false));
        return new LeDeviceViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull LeDeviceListAdapter.LeDeviceViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (position < mFilterList.size()) {

            holder.deviceAddress.setText(mFilterList.get(position).getAddress());
            if (mFilterList.get(position).getName() != null) {
                holder.deviceName.setText(mFilterList.get(position).getName());
            } else {
                holder.deviceName.setText("N/A");
            }
            if (mFilterList.get(position).getRssi() != 0) {
                holder.deviceRssi.setText(mFilterList.get(position).getRssi() + "");
            } else {
                holder.deviceRssi.setText("0");
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                private static final String TAG = "ViewHolder";

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, BleFunctionActivity.class);
                    intent.putExtra("device_name", mFilterList.get(position).getName());
                    intent.putExtra("device_address", mFilterList.get(position).getAddress());
                    mContext.startActivity(intent);
                }
            });
        }
    }


    public void addDevice(DeviceItemBean device) {

        if (!mFilterList.contains(device)) {
            mFilterList.add(device);
        }
        notifyDataSetChanged();
    }

    public void addDevices(List<DeviceItemBean> devices) {
        for (DeviceItemBean device : devices) {
            addDevice(device);
        }
    }

    @Override
    public int getItemCount() {

        return mFilterList.size();
    }

    public void clear() {
        mLeDevices.clear();
        notifyDataSetChanged();
    }

    public void setOptional(boolean enable) {
        if (enable) {
            mOptional = true;
        } else mOptional = false;
        notifyDataSetChanged();
    }

    class LeDeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;

        public LeDeviceViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
            deviceRssi = itemView.findViewById(R.id.device_rssi);
        }
    }
}

package example.suntong.bletool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import example.suntong.bletool.bean.DeviceItemBean;
import example.suntong.bletool.service.BluetoothLeService;


public class FilterHelper {

    //过滤搜索内容
    public static List<DeviceItemBean> filter(List<DeviceItemBean> mDevices, String newText) {

        if (newText == null) return mDevices;
        List<DeviceItemBean> mFilterList = new ArrayList<>();

        String lowNewText;

        for (DeviceItemBean device : mDevices) {
            lowNewText = newText.toLowerCase();
            if (null != device.getName() && !TextUtils.isEmpty(device.getName())) {
                //实现忽略大小写搜索
                if (device.getName().contains(lowNewText) || device.getName().toLowerCase().contains(lowNewText)) {
                    mFilterList.add(device);
                }
            }
            if (null != device.getAddress() && !TextUtils.isEmpty(device.getAddress())) {
                if (device.getAddress().contains(lowNewText) || device.getAddress().toLowerCase().contains(lowNewText)) {
                    mFilterList.add(device);
                }
            }
        }
        return mFilterList;
    }

    //广播过滤
    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_STOP_RECEIVE);
        intentFilter.addAction(BluetoothLeService.CHARACTERISTIC_WRITE_FAILURE);
        intentFilter.addAction(BluetoothLeService.CHARACTERISTIC_WRITE_SUCCESS);
        return intentFilter;
    }

    //通过uri从文件管理器中读取文件
    public static Byte[] readDataFromUri(Context context, Uri uri) throws IOException {
        final int SIZE = 4096;

        InputStream inputStream =
                context.getContentResolver().openInputStream(uri);

        List<Byte> binData = new ArrayList<>();

        int len;
        byte[] buf = new byte[SIZE];
        while ((len = Objects.requireNonNull(inputStream).read(buf)) != -1) {
            for (int k = 0; k < len; k++) {
                binData.add(buf[k]);
            }
        }

        //关资源
        inputStream.close();
        return binData.toArray(new Byte[0]);
    }
}

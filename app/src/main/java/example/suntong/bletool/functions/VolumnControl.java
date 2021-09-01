package example.suntong.bletool.functions;

import android.content.Context;

import example.suntong.bletool.util.ToastUtil;
import example.suntong.bletool.service.BluetoothLeService;

public class VolumnControl {
    public static void onVolumnControl(Context context, int num, BluetoothLeService bluetoothLeService, String deviceAddress) {
        if (num < 0 || num > 100) {
            ToastUtil.showShort(context, "设置超出范围");
        } else {
            byte[] CMD = {0x00, 0xA, 0x05, 0x00, (byte) num};
            bluetoothLeService.writeCharacteristic(deviceAddress, CMD);
        }
    }
}

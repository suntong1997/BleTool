package example.suntong.bletool.functions;

import android.content.Context;
import android.widget.Button;
import android.widget.PopupMenu;

import example.suntong.bletool.BluetoothCommand;
import example.suntong.bletool.R;
import example.suntong.bletool.service.BluetoothLeService;

public class HeartRateControl {

    public static void onHeartRateControl(Context context, Button hrControl, BluetoothLeService bluetoothLeService, String deviceAddress) {
        PopupMenu popupMenu = new PopupMenu(context, hrControl);
        popupMenu.getMenuInflater().inflate(R.menu.pop_hr_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.start_hr:
                            bluetoothLeService.writeCharacteristic(
                                    deviceAddress, BluetoothCommand.START_HEART_RATE_CMD);
                            break;
                        case R.id.stop_hr:
                            bluetoothLeService.writeCharacteristic(
                                    deviceAddress, BluetoothCommand.STOP_HEART_RATE_CMD);
                            break;
                    }
                    return true;
                });
    }
}

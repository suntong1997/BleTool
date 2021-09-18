package example.suntong.bletool.functions;

import android.content.Context;
import android.widget.Button;
import android.widget.PopupMenu;

import example.suntong.bletool.BluetoothCommand;
import example.suntong.bletool.R;
import example.suntong.bletool.service.BluetoothLeService;

public class TimeFormat {
    public static void onSetTimeFormat(Context context, Button setTimeModeBtn, BluetoothLeService bluetoothLeService, String deviceAddress) {
        PopupMenu popupMenu = new PopupMenu(context, setTimeModeBtn);
        popupMenu.getMenuInflater().inflate(R.menu.pop_time_mode_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.mode_12h:
                            bluetoothLeService.writeCharacteristic(
                                    deviceAddress, BluetoothCommand.SETTIMEFORMATTO12H);

                            break;
                        case R.id.mode_24h:
                            bluetoothLeService.writeCharacteristic(
                                    deviceAddress, BluetoothCommand.SETTIMEFORMATTO24H);

                            break;
                    }
                    return true;
                });
    }
}

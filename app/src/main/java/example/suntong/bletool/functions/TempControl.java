package example.suntong.bletool.functions;

import android.content.Context;
import android.widget.Button;
import android.widget.PopupMenu;

import example.suntong.bletool.BluetoothCommand;
import example.suntong.bletool.Iview;
import example.suntong.bletool.R;
import example.suntong.bletool.service.BluetoothLeService;

public class TempControl {

    public static void onRequestLiveTempData(Context context, Iview view, Button tempControlBtn, BluetoothLeService bluetoothLeService, String deviceAddress) {
        PopupMenu popupMenu = new PopupMenu(context, tempControlBtn);
        popupMenu.getMenuInflater().inflate(R.menu.pop_temp_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.start_temp:
                            bluetoothLeService.writeCharacteristic(
                                    deviceAddress, BluetoothCommand.START_TEMP_CMD);
                            view.displayData("接收实时温度数值");

                            break;
                        case R.id.stop_temp:
                            bluetoothLeService.writeCharacteristic(
                                    deviceAddress, BluetoothCommand.STOP_TEMP_CMD);
                            view.displayData("停止接收温度数值");

                            break;
                    }
                    return true;
                });
    }
}

package example.suntong.bletool.functions;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;

import java.util.Calendar;
import java.util.Locale;

import example.suntong.bletool.DatePicker;
import example.suntong.bletool.interfaces.Iview;
import example.suntong.bletool.util.ToastUtil;
import example.suntong.bletool.service.BluetoothLeService;

public class HistoryData {
    private static Calendar calendar = Calendar.getInstance(Locale.CHINA);

    @SuppressLint("ResourceType")
    public static void onRequestDataOfDay(Context context, Iview iView, BluetoothLeService bluetoothLeService, String deviceAddress, int cmd_class, int cmd_id)  {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        // 绑定监听器(How the parent is notified that the date is set.)
        new DatePickerDialog(
                context,
                6,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // 此处得到选择的时间，可以进行你想要的操作
                    byte intervalDay =
                            DatePicker.getDayApart(year, monthOfYear, dayOfMonth, calendar, context);

                    if (intervalDay > 6) {
                        ToastUtil.showShort(context, "请选择七天内的日期");
                    } else if (intervalDay == -1) {
                        iView.displayData("获取日期失败");
                    }

                    byte[] HISTORY_DATA_CMD;
                    if (intervalDay == 0) { // 判断选择的时间是否为今天，若是则结束传入当前时间作为结束时间
                        HISTORY_DATA_CMD =
                                new byte[]{
                                        (byte) cmd_class,
                                        (byte) cmd_id,
                                        0x07,
                                        0x00,
                                        intervalDay,
                                        0x00,
                                        (byte) calendar.get(Calendar.HOUR_OF_DAY)
                                };
                    } else {
                        HISTORY_DATA_CMD = new byte[]{(byte) cmd_class, (byte) cmd_id, 0x07, 0x00, intervalDay, 0x00, 0x017};
                    }
                    bluetoothLeService.writeCharacteristic(deviceAddress, HISTORY_DATA_CMD);
                }
                // 设置初始日期
                ,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }
}

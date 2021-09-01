package example.suntong.bletool.functions;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import example.suntong.bletool.service.BluetoothLeService;

public class SyncDate {

    //同步日期到设备
    public static void onSyncDate(BluetoothLeService bluetoothLeService, String deviceAddress) {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        TimeZone aDefault = TimeZone.getDefault();
        String zone = aDefault.getDisplayName(false, TimeZone.SHORT);
        String[] split = zone.split("\\+");

        String[] time = split[1].split(":");

        // 获取gmt时间
        byte gmtHour = (byte) Integer.parseInt(String.format("%02x", Integer.parseInt(time[0])).toUpperCase());
        byte gmtMin = (byte) Integer.parseInt(String.format("%02x", Integer.parseInt(time[1])).toUpperCase());

        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        byte heartYear = (byte) Integer.parseInt(year.substring(0, 2)); // 年份前两位
        byte rearYear = (byte) Integer.parseInt(year.substring(2, 4)); // 年份后两位
        byte month = (byte) ((byte) calendar.get(Calendar.MONTH) + 1);
        byte day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        byte hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        byte min = (byte) calendar.get(Calendar.MINUTE);
        byte second = (byte) calendar.get(Calendar.SECOND);

        byte[] cmd = new byte[]{
                0x00,
                (byte) 0x87,
                0x0e,
                0x00,
                heartYear,
                rearYear,
                month,
                day,
                hour,
                min,
                second,
                0x2b,
                gmtHour,
                gmtMin
        };

        bluetoothLeService.writeCharacteristic(deviceAddress, cmd);

    }
}

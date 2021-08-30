package example.suntong.bletool.functions;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import example.suntong.bletool.Iview;
import example.suntong.bletool.service.BluetoothLeService;

public class SportNotification {
    private static final String TAG = SportNotification.class.getSimpleName();

    static public void onUpdateSoprtNotification(Iview view, BluetoothLeService bluetoothLeService, String deviceAddress,Object obj) {
        byte CMD_MULTI_PKG = 0x7F;
        int CRC;
        byte low_CRC;
        byte high_CRC;
        int PKG_NUM = 0;
        byte low_PKG_NUM = 0;
        byte high_PKG_NUM = 0;
        byte CMD_CLASS = 0x02;
        byte CMD_ID = (byte) 0x92;
        int CMD_length;//整个命令的长度
        byte low_CMD_length;
        byte high_CMD_length;
        byte total_dynamic_fields_number = 20;
        int field_length = 16;//一块动态域的长度
        byte low_field_length = (byte) (field_length & 0xFF);
        byte low_mid_field_length = (byte) ((field_length & 0xFF00) >> 0x08);
        byte high_mid_field_length = (byte) ((field_length & 0xFF0000) >> 0x10);
        byte high_field_length = (byte) ((field_length & 0xFF000000) >> 0x18);
        byte dynamic_field_id = 0;
        byte data_type = 0x05;
        int rbg565_color = 0xFFFF;
        byte low_rbg565_color = (byte) (rbg565_color & 0xFF);
        byte high_rbg565_color = (byte) ((rbg565_color & 0xFF00) >> 0x08);
        int x = 0x00;
        byte low_x = (byte) (x & 0xFF);
        byte high_x = (byte) ((x & 0xFF00) >> 0x08);
        int y = 0x00;
        byte low_y = (byte) (y & 0xFF);
        byte high_y = (byte) ((y & 0xFF00) >> 8);
        List<Byte> dataList = new ArrayList<>();
        int height = 400;
        byte low_height = (byte) (height & 0xFF);
        byte high_height = (byte) ((height & 0xFF00) >> 0x08);
        int width = 320;
        byte low_width = (byte) (width & 0xFF);
        byte high_width = (byte) ((width & 0xFF00) >> 0x08);

        dataList.add(total_dynamic_fields_number);
        //构造一组数据
        for (int i = 0; i < total_dynamic_fields_number; i++) {
            dataList.add(low_field_length);
            dataList.add(low_mid_field_length);
            dataList.add(high_mid_field_length);
            dataList.add(high_field_length);
            dataList.add(dynamic_field_id++);
            dataList.add(data_type);
            dataList.add(low_rbg565_color);
            dataList.add(high_rbg565_color);
            dataList.add(low_x);
            dataList.add(high_x);
            dataList.add(low_y);
            dataList.add(high_y);
            dataList.add(low_height);
            dataList.add(high_height);
            dataList.add(low_width);
            dataList.add(high_width);
        }


        CMD_length = dataList.size() + 13;
        low_CMD_length = (byte) (CMD_length & 0xFF);
        high_CMD_length = (byte) ((CMD_length & 0xFF) >> 0x08);

        CRC = CMD_CLASS + CMD_ID + low_CMD_length + high_CMD_length;
        for (int i = 0; i < 178; i++) {
            CRC += dataList.get(i);
        }
        low_CRC = (byte) (CRC & 0xFF);
        high_CRC = (byte) ((CRC & 0xFF00) >> 0x08);

        int field_number = 0;
        byte low_field_number = 0;
        byte high_field_number = 0;

        PKG_NUM = (dataList.size() + 8) / 178 == 0 ? (dataList.size() + 8) / 178 : (dataList.size() + 8) / 178 + 1;
        low_PKG_NUM = (byte) (PKG_NUM & 0xFF);
        high_PKG_NUM = (byte) ((PKG_NUM & 0xFF00) >> 0x08);

        dataList.add(0, CMD_MULTI_PKG);
        dataList.add(1, low_CRC);
        dataList.add(2, low_field_number);
        dataList.add(3, high_field_number);
        dataList.add(4, low_PKG_NUM);
        dataList.add(5, high_PKG_NUM);
        dataList.add(6, low_CRC);
        dataList.add(7, high_CRC);
        dataList.add(8, CMD_CLASS);
        dataList.add(9, CMD_ID);
        dataList.add(10, low_CMD_length);
        dataList.add(11, high_CMD_length);


        new Thread(new Runnable() {
            @Override
            public void run() {
                int index = 0;//记录读取到datalist第几个数据
                byte[] value = new byte[182];
                int PKG_NUM = (dataList.size() - 4) / 178 == 0 ? (dataList.size() - 4) / 178 : (dataList.size() - 4) / 178 + 1;
                int field_number = 0;
                byte low_field_number = 0;
                byte high_field_number = 0;

                synchronized (obj) {
                    for (int i = 0; i < PKG_NUM; i++) {

                        if (i == 0) {
                            for (int j = 0; j < 182; j++) {
                                value[j] = dataList.get(j);
                                index++;
                            }

                        } else if (i == (PKG_NUM - 1)) { // 最后一包需要判断数据长度不一定为182需要另外处理
                            int size = (dataList.size()) % 178;
                            if ((dataList.size()) % 178 == 0) {
                                size = 182;
                            }
                            value = new byte[size];
                            value[0] = 0x7F;
                            value[1] = low_CRC;
                            field_number = (byte) (field_number + 1);
                            low_field_number = (byte) (field_number & 0xFF);
                            high_field_number = (byte) ((field_number & 0xFF00) >> 0x08);
                            value[2] = low_field_number;
                            value[3] = high_field_number;
                            for (int j = 4; j < size; j++) {
                                value[j] = dataList.get(index);
                                index++;
                                if (dataList.size() == index) {
                                    break;
                                }
                            }

                        } else {
                            value[0] = 0x7F;
                            value[1] = low_CRC;
                            field_number = (byte) (field_number + 1);
                            low_field_number = (byte) (field_number & 0xFF);
                            high_field_number = (byte) ((field_number & 0xFF00) >> 0x08);
                            value[2] = low_field_number;
                            value[3] = high_field_number;
                            for (int j = 4; j < 182; j++) {
                                value[j] = dataList.get(index);
                                index++;
                            }
                        }

                        final StringBuilder stringBuilder = new StringBuilder(value.length);
                        for (byte byteChar : value)
                            stringBuilder.append(String.format("%02X ", byteChar));
                        Log.w(TAG, "sendMultiPkgCmd: " + stringBuilder.toString());

                        bluetoothLeService.writeCharacteristic(deviceAddress, value);
                        try {
                            obj.wait(); // 等待写入后的回调
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }
}

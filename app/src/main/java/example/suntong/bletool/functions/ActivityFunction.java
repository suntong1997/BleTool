package example.suntong.bletool.functions;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import example.suntong.bletool.R;
import example.suntong.bletool.interfaces.Iview;
import example.suntong.bletool.service.BluetoothLeService;

public class ActivityFunction {
    private static final String TAG = ActivityFunction.class.getSimpleName();
    static int index;

    public static void onSendActivityFunction(Context context, int num, Object obj, Iview view, BluetoothLeService bluetoothLeService, String deviceAddress) {
        byte CMD_MULTI_PKG = 0x7F;
        byte CMD_CLASS = 0x01;
        byte CMD_ID = (byte) 0xA1;

        int list_image_id = 10010;
        byte lowListImageId = (byte) (list_image_id & 0xFF);
        byte highListImageId = (byte) ((list_image_id & 0xFF00) >> 0x08);

        int image_id = 10000;
        byte lowImageId = (byte) (image_id & 0xFF);
        byte highImageId = (byte) ((image_id & 0xFF00) >> 0x08);

        byte category_id = 17;

        int activity_id = 262;
        byte lowActivityId;
        byte highActivityId;

        int data_type = 0x01;
        byte lowDataType = (byte) (data_type & 0xFF);
        byte highDataType = (byte) ((data_type & 0xFF00) >> 0x08);

        byte order_id = 0;

        byte[] categoryUnicodeByte = {0x00, 0x31, 0x00, 0x32, 0x00, 0x33};
        byte categoryUnicodelength = 6;

        byte[] activityUnicodeByte = {0x00, 0x34, 0x00, 0x35, 0x00, 0x36};
        byte activityUnicodelength = 6;

        byte MET_type = 1;
        byte MET_data_num = 1;
        byte paceOrper_1 = 1;
        byte MET_data_1 = 85;
        byte total_activity_number = (byte) num; // 活动总数

        List<Byte> dataList = new ArrayList<>();
        byte activity_num = 0; // 活动编号，即第几包
        // 构造多组数据
        for (int i = 0; i < total_activity_number; i++) {
            dataList.add(activity_num++);
            dataList.add(category_id);
            dataList.add(lowListImageId);
            dataList.add(highListImageId);
            dataList.add(categoryUnicodelength);

            dataList.add(categoryUnicodeByte[1]); // 添加category数据
            dataList.add(categoryUnicodeByte[0]);
            dataList.add(categoryUnicodeByte[3]);
            dataList.add(categoryUnicodeByte[2]);
            dataList.add(categoryUnicodeByte[5]);
            dataList.add(categoryUnicodeByte[4]);

            activity_id = activity_id + 1; // 每个activity编号不同
            lowActivityId = (byte) (activity_id & 0xFF);
            highActivityId = (byte) ((activity_id & 0xFF00) >> 0x08);
            dataList.add(lowActivityId);
            dataList.add(highActivityId);
            dataList.add(lowImageId);
            dataList.add(highImageId);
            dataList.add(activityUnicodelength);

            dataList.add(activityUnicodeByte[1]); // 添加activity数据
            dataList.add(activityUnicodeByte[0]);
            dataList.add(activityUnicodeByte[3]);
            dataList.add(activityUnicodeByte[2]);
            dataList.add(activityUnicodeByte[5]);
            dataList.add(activityUnicodeByte[4]);

            dataList.add(order_id);
            dataList.add(lowDataType);
            dataList.add(highDataType);
            dataList.add(MET_type);
            dataList.add(MET_data_num);
            dataList.add(paceOrper_1);
            dataList.add(MET_data_1);
        }

        if (dataList.size() < 169) {
            view.displayData(context.getString(R.string.setting_number_small));
            return;
        }
        parseActivityFuc(view, CMD_MULTI_PKG, CMD_CLASS, CMD_ID, total_activity_number, dataList, context, obj, bluetoothLeService, deviceAddress);
    }

    private static void parseActivityFuc(
            Iview view, byte CMD_MULTI_PKG,
            byte CMD_CLASS,
            byte CMD_ID,
            byte total_activity_number,
            List<Byte> dataList, Context context, Object obj, BluetoothLeService bluetoothLeService, String deviceAddress) {
        int length;
        length = dataList.size() + 13;
        byte lowLength = (byte) (length & 0xFF);
        byte highLength = (byte) ((length & 0xFF00) >> 0x08);

        new Thread(
                () -> {
                    int CRC = CMD_CLASS + CMD_ID + lowLength + highLength + total_activity_number; // 校验码
                    for (int i = 0; i < 167; i++) {
                        CRC += dataList.get(i);
                    }
                    byte lowCRC = (byte) (CRC & 0xFF);
                    byte highCRC = (byte) ((CRC & 0xFF00) >> 0x08);

                    int pkgNum; // 包的总量
                    pkgNum =
                            (dataList.size() + 9) % 178 == 0
                                    ? (dataList.size() + 9) / 178
                                    : (dataList.size() + 9) / 178 + 1;

                    byte lowPkgNum = (byte) (pkgNum & 0xFF);
                    byte highPkgNum = (byte) ((pkgNum & 0xFF00) >> 0x08);

                    byte pkg_num = 0; // 第一包的活动编号
                    byte low_pkg_num = (byte) (pkg_num & 0xFF);
                    byte high_pkg_num = (byte) ((pkg_num & 0xFF00) >> 0x08);

                    dataList.add(0, CMD_MULTI_PKG);
                    dataList.add(1, lowCRC);
                    dataList.add(2, low_pkg_num);
                    dataList.add(3, high_pkg_num);
                    dataList.add(4, lowPkgNum);
                    dataList.add(5, highPkgNum);
                    dataList.add(6, lowCRC);
                    dataList.add(7, highCRC);
                    dataList.add(8, CMD_CLASS);
                    dataList.add(9, CMD_ID);
                    dataList.add(10, lowLength);
                    dataList.add(11, highLength);
                    dataList.add(12, total_activity_number);

                    index = 0;
                    synchronized (obj) {
                        byte[] value = new byte[182];
                        for (int i = 0; i < pkgNum; i++) {
                            if (i == 0) {
                                for (int j = 0; j < 182; j++) {
                                    value[j] = dataList.get(j);
                                    index++;
                                }

                            } else if (i == (pkgNum - 1)) { // 最后一包需要判断数据长度不一定为182需要另外处理
                                int size = (dataList.size()) % 178;
                                if ((dataList.size()) % 178 == 0) {
                                    size = 182;
                                }
                                value = new byte[size];
                                value[0] = 0x7F;
                                value[1] = lowCRC;
                                pkg_num = (byte) (pkg_num + 1);
                                low_pkg_num = (byte) (pkg_num & 0xFF);
                                high_pkg_num = (byte) ((pkg_num & 0xFF00) >> 0x08);
                                value[2] = low_pkg_num;
                                value[3] = high_pkg_num;
                                for (int j = 4; j < size; j++) {
                                    value[j] = dataList.get(index);
                                    index++;
                                    if (dataList.size() == index) {
                                        break;
                                    }
                                }

                            } else {
                                value[0] = 0x7F;
                                value[1] = lowCRC;
                                pkg_num = (byte) (pkg_num + 1);
                                low_pkg_num = (byte) (pkg_num & 0xFF);
                                high_pkg_num = (byte) ((pkg_num & 0xFF00) >> 0x08);
                                value[2] = low_pkg_num;
                                value[3] = high_pkg_num;
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
                        view.displayData(context.getString(R.string.send_success));
                    }
                })
                .start();
    }
}

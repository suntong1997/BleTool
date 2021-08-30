package example.suntong.bletool;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataParser {
    Iview view;

    public DataParser(Iview view) {
        this.view = view;
    }

    //解析收到的单包结果
    public void parseSingleData(String[] dataList) {
//        String[] dataList = data.split(" "); // 把传过来的数据字符串拆分成数组

        if (dataList[0].equals("80") && dataList[1].equals("87")) {
            displaySyncDateResult(dataList);
        } else if (dataList[0].equals("80") && dataList[1].equals("06")) {
            displayDeviceDate(dataList);
        } else if (dataList[0].equals("80") && dataList[1].equals("08")) {
            displayBatteryLevel(dataList);
        } else if (dataList[0].equals("88") && dataList[1].equals("08")) {
            displayFlashId(dataList);
        } else if (dataList[0].equals("88") && dataList[1].equals("07")) {
            displayGsensor(dataList);
        } else if (dataList[0].equals("81") && dataList[1].equals("85")) {
            displayHrControl();
        } else if (dataList[0].equals("81") && dataList[1].equals("1B")) {
            displayLiveTempData(dataList);
        }
    }


    //解析收到的多包结果
    public void parseMultiData(String[][] dataList, int length) {
        List<String> values = new ArrayList<>();
        if (dataList == null || dataList.length == 0) {
            return;
        }
        //        将所有的数据都存在一个list中
        for (int i = 0; i < length; i++) {
            if (i == 0) {//第一包数据从第13个开始
                values.addAll(Arrays.asList(dataList[0]).subList(12, 182));
            } else if (i == (length - 1) && i != 0 && dataList[i] != null) {
                for (int j = 4; j < dataList[i].length; j++) {
                    values.add(dataList[length - 1][j]);
                    if (j == (dataList[i].length - 1)) {
                        break;
                    }
                }
            } else {
                if (dataList[i] != null) {
                    values.addAll(Arrays.asList(dataList[i]).subList(4, dataList[i].length));
                }
            }
        }
        StringBuilder builder = new StringBuilder(); // 用来拼接需要的数据

        // TODO: 2021/8/30 从这里开始解析每一条指令传回的数据
        if (dataList[0][8].equals("81") && dataList[0][9].equals("06")) {
            parseWalkData(values, builder);
        } else if (dataList[0][8].equals("81") && dataList[0][9].equals("02")) {
            parseHeartRateData(values, builder);
        } else if (dataList[0][8].equals("81") && dataList[0][9].equals("05")) {
            parseTempData(values, builder);
        }

        view.displayData(builder.toString());
    }

    //展示时间同步结果
    private void displaySyncDateResult(String[] dataList) {
        if (dataList[4].equals("01")) {
            view.displayData("同步成功");
        } else {
            view.displayData("同步失败");
        }
    }

    // 展示从设备获取的时间
    private void displayDeviceDate(String[] dataList) {
        int heartYear = Integer.parseInt(dataList[4], 16);
        int rearYear = Integer.parseInt(dataList[5], 16);
        int year = heartYear * 100 + rearYear;//获取年份
        int month = Integer.parseInt(dataList[6], 16);//获取月份
        String monStr = String.format("%2s", month).replaceAll(" ", "0");
        int day = Integer.parseInt(dataList[7], 16);//获取天数
        String dayStr = String.format("%2s", day).replaceAll(" ", "0");
        int hour = Integer.parseInt(dataList[8], 16);//获取小时
        String hourStr = String.format("%2s", hour).replaceAll(" ", "0");
        int min = Integer.parseInt(dataList[9], 16);//获取分钟
        String minStr = String.format("%2s", min).replaceAll(" ", "0");
        int second = Integer.parseInt(dataList[10], 16);//获取秒数
        String secStr = String.format("%2s", second).replaceAll(" ", "0");
        //获取gmt时间
        int gmtHour = Integer.parseInt(dataList[12], 16);
        int gmtMin = Integer.parseInt(dataList[13], 16);

        String recivetime =
                year + "-" + monStr + "-" + dayStr + "," + hourStr + ":" + minStr + ":" + secStr
                        + ",gmt:+" + gmtHour + ":" + gmtMin; // 展示时间格式

        view.displayData(recivetime);
    }

    // 展示电量
    private void displayBatteryLevel(String[] dataList) {
        int level = Integer.parseInt(dataList[4], 16);
        String batteryLevel = "电量:" + level + "%";
        view.displayData(batteryLevel);
    }

    //展示flashid
    private void displayFlashId(String[] dataList) {
        long id =
                Integer.valueOf(dataList[7], 16) * 0x1000000
                        + Integer.valueOf(dataList[6], 16) * 0x10000
                        + Integer.valueOf(dataList[5], 16) * 0x100
                        + Integer.valueOf(dataList[4], 16);
        view.displayData("flashID:" + id);
    }

    // 展示G-senser
    private void displayGsensor(String[] dataList) {
        String id = dataList[4];
        int x = Integer.valueOf(dataList[6], 16) * 0x100 + Integer.valueOf(dataList[5], 16);
        int y = Integer.valueOf(dataList[8], 16) * 0x100 + Integer.valueOf(dataList[7], 16);
        int z = Integer.valueOf(dataList[10], 16) * 0x100 + Integer.valueOf(dataList[9], 16);
        String data = "id:" + id + "  x:" + x + " y:" + y + " z:" + z;
        view.displayData(data);
    }

    //展示心率控制结果
    private void displayHrControl() {
        view.displayData("心率设置成功");
    }

    /**
     * 解析收到的步数数据
     *
     * @param totalData 总数据
     * @param builder   用来拼接数据
     */
    @SuppressLint("DefaultLocale")
    private void parseWalkData(List<String> totalData, StringBuilder builder) {
        int hour = 0, min = 0;
        int stepData;
        for (int i = 0; i + 1 < totalData.size(); i = i + 2) {
            stepData =
                    Integer.parseInt(totalData.get(i + 1), 16) * 0x100
                            + Integer.parseInt(totalData.get(i), 16);
            builder.append(String.format("%02d:%02d--%02d   ", hour, min, stepData));
            // 计算每个数值对应的时间
            if ((min + 5) == 60) {
                min = 0;
                hour++;
            } else {
                min = min + 5;
            }
        }
    }

    private void displayLiveTempData(String[] dataList) {
        // 存储收到的每一条回调
        int[] tempValue = new int[6];
        // 有时候接收到的温度数据为空，判断一下若为空就返回
        if (dataList.length < 6) {
            return;
        }
        // 解析日期
        tempValue[0] = Integer.parseInt(dataList[4], 16);
        tempValue[1] = Integer.parseInt(dataList[5], 16);
        tempValue[2] = Integer.parseInt(dataList[6], 16);
        tempValue[3] = Integer.parseInt(dataList[7], 16);
        long time =
                tempValue[3] * 0x1000000L + tempValue[2] * 0x10000L + tempValue[1] * 0x100L + tempValue[0];
        String date = TimeUtil.stampToDate(String.valueOf(time), "yyyy-MM-dd HH:mm:ss");
        // 解析温度
        tempValue[4] = Integer.parseInt(dataList[8], 16);
        tempValue[5] = Integer.parseInt(dataList[9], 16);
        float data = (float) ((tempValue[5] * 0x100 + tempValue[4]) / 10.0);

        String tempStr = date + "  " + data + "℃";
        view.displayData(tempStr);
    }

    /**
     * 解析收到的心率数据
     *
     * @param totalData 总数据
     * @param builder   用来拼接数据
     */
    @SuppressLint("DefaultLocale")
    private void parseHeartRateData(List<String> totalData, StringBuilder builder) {
        int hour = 0, min = 0, HR, DBP, SBP, RR;
        for (int i = 0; i + 3 < totalData.size(); i = i + 4) {
            HR = Integer.parseInt(totalData.get(i), 16);
            DBP = Integer.parseInt(totalData.get(i + 1), 16);
            SBP = Integer.parseInt(totalData.get(i + 2), 16);
            RR = Integer.parseInt(totalData.get(i + 3), 16);

            builder.append(
                    String.format(
                            "time:%02d:%02d  HR:%02d  DBP:%02d  SBP:%02d  RR:%02d                                            ",
                            hour, min, HR, DBP, SBP, RR));
            // 计算每个数值对应的时间
            if ((min + 5) == 60) {
                min = 0;
                hour++;
            } else {
                min = min + 5;
            }
        }
    }

    /**
     * 解析收到的温度数据
     *
     * @param totalData 总数据
     * @param builder   用来拼接数据
     */
    @SuppressLint("DefaultLocale")
    private void parseTempData(@NonNull List<String> totalData, StringBuilder builder) {
        int hour = 0, min = 0;
        float tempData;
        for (int i = 0; i + 1 < totalData.size(); i = i + 2) {
            tempData =
                    (float)
                            (((float)
                                    (Integer.parseInt(totalData.get(i + 1), 16) * 0x100
                                            + Integer.parseInt(totalData.get(i), 16)))
                                    / 10.0);
            builder.append(String.format("%02d:%02d--%-2.1f℃   ", hour, min, tempData));
            // 计算每个数值对应的时间
            if ((min + 5) == 60) {
                min = 0;
                hour++;
            } else {
                min = min + 5;
            }
        }
    }
}

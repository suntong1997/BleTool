package example.suntong.bletool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import example.suntong.bletool.bean.HistoryHRBean;
import example.suntong.bletool.bean.HistorySPO2Bean;
import example.suntong.bletool.interfaces.Iview;
import example.suntong.bletool.util.ExcelUtil;
import example.suntong.bletool.util.TimeUtil;

/**
 * 解析回调的数据
 */
public class DataParser {
    Iview view;
    private String FILEPATH = "/sdcard/历史数据解析/";

    public DataParser(Iview view) {
        this.view = view;
    }

    //解析收到的单包结果
    public void parseSingleData(String[] dataList) {

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
        } else if (dataList[0].equals("81") && dataList[1].equals("1B") && dataList[2].equals("05")) {
            displayLiveTempResult();
        } else if (dataList[0].equals("81") && dataList[1].equals("1B")) {
            displayLiveTempData(dataList);
        } else if (dataList[0].equals("80") && dataList[1].equals("23")) {
            displayMacAddress(dataList);
        } else if (dataList[0].equals("80") && dataList[1].equals("0A")) {
            displaySetVolumnResult();
        } else if (dataList[0].equals("80") && dataList[1].equals("82")) {
            displayTimeFormat();
        } else if (dataList[0].equals("80") && dataList[1].equals("24")) {
            displayFirmwareVersion(dataList);
        } else if (dataList[0].equals("81") && dataList[1].equals("26")) {
            displaySOP2Data(dataList);
        } else if (dataList[0].equals("80") && dataList[1].equals("81")) {
            view.displayData("写入个人信息成功");
        }
    }

    /**
     * 解析传回的多包数据
     *
     * @param dataList 需要解析的数据
     * @param length   数据的长度
     */
    public void parseMultiData(String[][] dataList, int length) {
        List<String> values = new ArrayList<>();//存储所有的数据
        if (dataList == null || dataList.length == 0) {
            return;
        }
        // TODO: 2021/9/13   将所有的数据都存在一个list中
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

        // TODO: 2021/8/30 从这里开始解析每一条指令传回的多包数据
        if (dataList[0][8].equals("81") && dataList[0][9].equals("06")) {
            parseWalkData(values, builder);
        } else if (dataList[0][8].equals("81") && dataList[0][9].equals("02")) {
            parseHeartRateData(values, builder);
        } else if (dataList[0][8].equals("81") && dataList[0][9].equals("05")) {
            parseTempData(values, builder);
        } else if (dataList[0][8].equals("81") && dataList[0][9].equals("26")) {
            parseSPO2Data(values, builder);
        }

        view.displayData(builder.toString());
    }


    //展示时间同步结果
    private void displaySyncDateResult(String[] dataList) {
        if (dataList[4].equals("01")) {
            view.displayData(view.getContext().getString(R.string.sync_success));
        } else {
            view.displayData(view.getContext().getString(R.string.sync_fialure));
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
        String batteryLevel = view.getContext().getString(R.string.battery_level) + level + "%";
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
        view.displayData(view.getContext().getString(R.string.hr_setting_success));
    }


    //展示实时的温度信息
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

    //设置音量大小的返回结果展示
    void displaySetVolumnResult() {
        view.displayData(view.getContext().getString(R.string.volume_setting_success));
    }

    //展示设备的物理地址
    private void displayMacAddress(String[] dataList) {
        view.displayData("MAC:" + dataList[4] + ":" + dataList[5] + ":" + dataList[6] + ":" + dataList[7] + ":" + dataList[8] + ":" + dataList[9]);
    }

    private void displayTimeFormat() {
        view.displayData(view.getContext().getString(R.string.date_format_setting_success));
    }

    private void displayLiveTempResult() {
        view.displayData(view.getContext().getString(R.string.temp_control_success));
    }

    private void displayFirmwareVersion(String[] dataList) {

    }

    private void displaySOP2Data(String[] dataList) {
        List<HistorySPO2Bean> spo2List = new ArrayList<>();
//        List<List<HistorySPO2Bean>> list = new ArrayList<>();
        HistorySPO2Bean bean;
        int hour = 0, min = 0;
        int stepData;
        StringBuilder builder = new StringBuilder();
        for (int i = 4; i + 1 < dataList.length; i++) {
            stepData = Integer.parseInt(dataList[i], 16);
            builder.append(String.format("%02d:%02d--%02d   ", hour, min, stepData));
            bean = new HistorySPO2Bean(hour, min, stepData);
            spo2List.add(bean);
            // 计算每个数值对应的时间
            if ((min + 5) == 60) {
                min = 0;
                hour++;
//                list.add(spo2List);
//                spo2List.clear();
            } else {
                min = min + 5;
            }
        }
        view.displayData(builder.toString());
        exportSPO2Excel(view.getContext(), spo2List);
    }

    /**
     * 解析收到的心率数据
     *
     * @param values  总数据
     * @param builder 用来拼接数据
     */
    @SuppressLint("DefaultLocale")
    private void parseHeartRateData(List<String> values, StringBuilder builder) {
        List<HistoryHRBean> beanList = new ArrayList<>();
        String sheetName = "心率数据";
        File file = new File(FILEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        int hour = 0, min = 0, HR, DBP, SBP, RR;
        for (int i = 0; i + 3 < values.size(); i = i + 4) {
            HR = Integer.parseInt(values.get(i), 16);
            DBP = Integer.parseInt(values.get(i + 1), 16);
            SBP = Integer.parseInt(values.get(i + 2), 16);
            RR = Integer.parseInt(values.get(i + 3), 16);

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
            HistoryHRBean bean = new HistoryHRBean(hour, min, HR, DBP, SBP, RR);
            beanList.add(bean);
        }
        exportExcel(view.getContext(), beanList);

    }

    /**
     * 解析收到的温度数据
     *
     * @param values  总数据
     * @param builder 用来拼接数据
     */
    @SuppressLint("DefaultLocale")
    private void parseTempData(@NonNull List<String> values, StringBuilder builder) {
        int hour = 0, min = 0;
        float tempData;
        for (int i = 0; i + 1 < values.size(); i = i + 2) {
            tempData =
                    (float)
                            (((float)
                                    (Integer.parseInt(values.get(i + 1), 16) * 0x100
                                            + Integer.parseInt(values.get(i), 16)))
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


    //展示从设备获取的sop2的值
    private void parseSPO2Data(List<String> values, StringBuilder builder) {
        List<HistorySPO2Bean> spo2List = new ArrayList<>();
        Log.w("parseSPO2Data", "parseSPO2Data: "+values.size() );
        HistorySPO2Bean bean;
        int hour = 0, min = 0;
        int stepData;
        for (int i = 0; i + 1 < values.size(); i++) {
            stepData = Integer.parseInt(values.get(i), 16);
            builder.append(String.format("%02d:%02d--%02d   ", hour, min, stepData));
            bean = new HistorySPO2Bean(hour, min, stepData);
            spo2List.add(bean);
            // 计算每个数值对应的时间
            if ((min + 5) == 60) {
                min = 0;
                hour++;
            } else {
                min = min + 5;
            }
        }

        exportSPO2Excel(view.getContext(), spo2List);
    }

    /**
     * 解析收到的步数数据
     *
     * @param values  总数据
     * @param builder 用来拼接数据
     */
    @SuppressLint("DefaultLocale")
    private void parseWalkData(List<String> values, StringBuilder builder) {
        Log.w("parseWalkData", "parseWalkData: "+values.size());
        List<HistorySPO2Bean> spo2List = new ArrayList<>();
        HistorySPO2Bean bean;
        int hour = 0, min = 0;
        int stepData;
        for (int i = 0; i + 1 < values.size(); i = i + 2) {
            stepData =
                    Integer.parseInt(values.get(i + 1), 16) * 0x100
                            + Integer.parseInt(values.get(i), 16);
            builder.append(String.format("%02d:%02d--%02d   ", hour, min, stepData));
            bean = new HistorySPO2Bean(hour, min, stepData);
            spo2List.add(bean);
            // 计算每个数值对应的时间
            if ((min + 5) == 60) {
                min = 0;
                hour++;
            } else {
                min = min + 5;
            }
        }
        exportSPO2Excel(view.getContext(), spo2List);
    }

    private <T> void exportExcel(Context context, List<T> list) {
        String filePath = FILEPATH;
        File file = new File(FILEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        String excelFileName = TimeUtil.getCurrentDate() + ".xls";
        String[] title = {"time", "HR", "DBP", "SBP", "RR"};
        String sheetName = "心率数据";

        filePath = filePath + excelFileName;
        ExcelUtil.initExcel(filePath, sheetName, title);

        ExcelUtil.writeHRListToExcel(list, filePath, context);
    }

    private void exportSPO2Excel(Context context, List<HistorySPO2Bean> list) {
        String filePath = FILEPATH;
        File file = new File(FILEPATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        String excelFileName = TimeUtil.getCurrentDate() + ".xls";
        String[] title = {"time", "0min", "5min", "10min", "15min", "20min", "25min", "30min", "35min", "40min", "45min", "50min", "55min"};
        String sheetName = "SPO2数据";

        filePath = filePath + excelFileName;
        ExcelUtil.initExcel(filePath, sheetName, title);

        ExcelUtil.writeSPO2ListToExcel(list, filePath, context);
    }
}

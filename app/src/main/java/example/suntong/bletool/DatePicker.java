package example.suntong.bletool;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import example.suntong.bletool.util.ToastUtil;

public class DatePicker {

    /**
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     * @return 返回选择的天数与当天的间隔时间
     */
    public static byte getDayApart(int year, int monthOfYear, int dayOfMonth, Calendar calendar, Context context) {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        String endDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1)
                + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        byte intervalDay = -1;
        try {
            Date start = format.parse(startDate);
            Date end = format.parse(endDate);
            long startTime = start.getTime();
            long endTime = end.getTime();

            if (startTime > endTime) {
                ToastUtil.showShort(context, context.getString(R.string.select_days_within_seven));
            }

            intervalDay = (byte) ((endTime - startTime) / 24 / 60 / 60 / 1000);//计算间隔天数

            return intervalDay;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return intervalDay;
    }

}

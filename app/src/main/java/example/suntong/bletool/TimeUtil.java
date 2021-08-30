package example.suntong.bletool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s, String format) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s, String format) {
        String res;
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        long lt = new Long(s);
//        Log.e("TimeUtil", "stampToDate: " + lt);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String sd = sdf.format(new Date(Long.parseLong(String.valueOf(lt))*1000));//java时间戳转换需在最后补三个0
//        Log.e("TimeUtil", "stampToDate: " + sd);

        return sd;
    }

}

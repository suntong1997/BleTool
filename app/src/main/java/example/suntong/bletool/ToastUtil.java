package example.suntong.bletool;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static void showLong(Context context, String message) {

        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setText(message);
        toast.show();
    }


    public static void showShort(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setText(message);
        toast.show();
    }

    public static void showLong(Context context, int resId) {

        Toast toast = Toast.makeText(context, resId, Toast.LENGTH_LONG);
        toast.setText(resId);
        toast.show();
    }


    public static void showShort(Context context, int resId) {
        Toast toast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        toast.setText(resId);
        toast.show();
    }
}

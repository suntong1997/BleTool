package example.suntong.bletool;

import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import example.suntong.bletool.activities.MainActivity;


public class PermissionUtil {

    static public void requestPermission(String[] permissions, MainActivity activity) {
        if (Build.VERSION.SDK_INT > 23) {
            if (ContextCompat.checkSelfPermission(activity,
                    permissions[0])
                    == PackageManager.PERMISSION_GRANTED) {

            } else {
                //未获得权限
                Log.i("requestPermission:", "未授权");
                activity.requestPermissions(permissions
                        , activity.REQUEST_PERMISSION_CODE);
            }
        }
    }


}

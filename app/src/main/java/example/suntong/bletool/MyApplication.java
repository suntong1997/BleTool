package example.suntong.bletool;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String language = Locale.getDefault().getLanguage();
        Log.e("MyApplication", "onCreate: "+language );
        setLanguageLocal(language);
    }

    private void setLanguageLocal(String language) {
        Locale locale = new Locale(language);
        Resources resources = getApplicationContext().getResources();
        if (resources == null)
            return;

        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();

        if (config == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        resources.updateConfiguration(config, dm);
    }
}

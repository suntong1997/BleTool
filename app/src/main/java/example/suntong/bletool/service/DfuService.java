package example.suntong.bletool.service;

import android.app.Activity;

import example.suntong.bletool.ui.activity.NotificationActivity;
import no.nordicsemi.android.dfu.DfuBaseService;

public class DfuService extends DfuBaseService {


    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return (Class<? extends Activity>) NotificationActivity.class;
    }

    @Override
    protected boolean isDebug() {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
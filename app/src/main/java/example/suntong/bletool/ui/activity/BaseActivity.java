package example.suntong.bletool.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import example.suntong.bletool.R;
import example.suntong.bletool.service.BluetoothLeService;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    BluetoothLeService bluetoothLeService;
    String deviceAddress;
    String deviceName;

    private final ServiceConnection mServiceConnection =
            new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                    if (!bluetoothLeService.initialize()) {
                        finish();
                    }
                    Log.d(TAG, "onCreate: " + bluetoothLeService.connect(deviceAddress));
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    bluetoothLeService = null;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        deviceAddress = getIntent().getStringExtra("device_address");
        deviceName = getIntent().getStringExtra("device_name");
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 蓝牙连接
        if (bluetoothLeService != null)
            Log.e(TAG, "onCreate: " + bluetoothLeService.connect(deviceAddress));

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, BluetoothLeService.class);
        unbindService(mServiceConnection);
        stopService(intent);
    }

    boolean connect(String deviceAddress) {
        return bluetoothLeService.connect(deviceAddress);
    }

    void disconnect(String deviceAddress) {
        bluetoothLeService.disconnect(deviceAddress);
    }

    void writeCharacteristic(String deviceAddress, byte[] cmd) {
        bluetoothLeService.writeCharacteristic(deviceAddress, cmd);
    }

}
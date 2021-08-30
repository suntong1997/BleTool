package example.suntong.bletool.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.suntong.bletool.FilterHelper;
import example.suntong.bletool.R;
import example.suntong.bletool.service.BluetoothLeService;

public class BleFunctionActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext = this;
    private boolean mConnected = false;
    private Intent intent;

    @BindView(R.id.ble_update)
    CardView updateCard;
    @BindView(R.id.ble_debug)
    CardView debugCard;
    @BindView(R.id.tool_bar)
    Toolbar toolbar;

    // TODO 处理接收到的广播
    private final BroadcastReceiver mGattUpdateReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) { // 处理连接广播
                        mConnected = true;
//                        ToastUtil.showShort(mContext, "已连接");
                        invalidateOptionsMenu();
                    } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { // 处理断开广播
                        mConnected = false;
//                        ToastUtil.showShort(mContext, "已断开");
                        invalidateOptionsMenu();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_function);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // 注册广播
        registerReceiver(mGattUpdateReceiver, FilterHelper.makeGattUpdateIntentFilter());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // 左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); // 设置返回键可用
        setOnClickListerner();
        initView();
    }


    private void initView() {
        toolbar.setTitle(deviceName);
    }

    private void setOnClickListerner() {
        updateCard.setOnClickListener(this);
        debugCard.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ble_update:
                intent = new Intent(this, DeviceUpdateActivity.class);
                intent.putExtra("device_address", deviceAddress);
                intent.putExtra("device_name", deviceName);
                startActivity(intent);
                break;
            case R.id.ble_debug:
                intent = new Intent(this, DebugActivity.class);
                intent.putExtra("device_address", deviceAddress);
                intent.putExtra("device_name", deviceName);
                startActivity(intent);
                break;
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.device_control, menu);
//        menu.findItem(R.id.menu_clear_log).setVisible(false);
//        if (mConnected) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }
//        return true;
//    }
//
//    @SuppressLint("NonConstantResourceId")
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_connect:
//                bluetoothLeService.connect(deviceAddress);
//                return true;
//            case R.id.menu_disconnect:
//                bluetoothLeService.disconnect(deviceAddress);
//                return true;
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
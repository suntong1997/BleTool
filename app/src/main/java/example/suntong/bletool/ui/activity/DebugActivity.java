package example.suntong.bletool.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.suntong.bletool.BluetoothCommand;
import example.suntong.bletool.DataParser;
import example.suntong.bletool.FilterHelper;
import example.suntong.bletool.util.HideSoftInputUtil;
import example.suntong.bletool.interfaces.Iview;
import example.suntong.bletool.R;
import example.suntong.bletool.util.ToastUtil;
import example.suntong.bletool.functions.ActivityFunction;
import example.suntong.bletool.functions.HeartRateControl;
import example.suntong.bletool.functions.HistoryData;
import example.suntong.bletool.functions.SportNotification;
import example.suntong.bletool.functions.SyncDate;
import example.suntong.bletool.functions.TempControl;
import example.suntong.bletool.functions.TimeFormat;
import example.suntong.bletool.functions.VolumnControl;
import example.suntong.bletool.service.BluetoothLeService;

public class DebugActivity extends BaseActivity implements View.OnClickListener, Iview {

    private boolean mConnected = false;
    private final Context mContext = this;
    private final Object obj = new Object();
    private final String TAG = DebugActivity.class.getSimpleName();
    private DataParser dataParser;
    private String[][] reciveMultiPkg = new String[10][];
    private int recivePkgNum;
    private boolean isReciveMultPkg = false;
    private int index = 0;
    private int num;

    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.connect_state)
    TextView connectionState;
    @BindView(R.id.device_address)
    TextView deviceAddressTxt;
    @BindView(R.id.sync_date)
    Button syncDateBtn;
    @BindView(R.id.read_date)
    Button readDateBtn;
    @BindView(R.id.display_edit)
    EditText displayEdit;
    @BindView(R.id.read_battery)
    Button readBatteryBtn;
    @BindView(R.id.hr_control)
    Button hrControlBtn;
    @BindView(R.id.temp_contorl)
    Button tempControlBtn;
    @BindView(R.id.update_sport_nofitication)
    Button updateSportNotification;
    @BindView(R.id.test_vibrato)
    Button testVibration;
    @BindView(R.id.flash_id)
    Button flashIdBtn;
    @BindView(R.id.g_sensor)
    Button gSensorBtn;
    @BindView(R.id.history_step)
    Button historyStepBtn;
    @BindView(R.id.history_hr)
    Button historyHeartRateBtn;
    @BindView(R.id.history_temp)
    Button historyTempBtn;
    @BindView(R.id.activity_funtion)
    Button activityFuncBtn;
    @BindView(R.id.set_number)
    Button setNumberBtn;
    @BindView(R.id.set_volume)
    Button setVolumeBtn;
    @BindView(R.id.mac_adress)
    Button macAddressBtn;
    @BindView(R.id.time_format)
    Button setTimeModeBtn;

    // TODO 处理接收到的广播
    private final BroadcastReceiver mGattUpdateReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    final String action = intent.getAction();
                    if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) { // 处理连接广播
                        mConnected = true;
                        updateConnectionState(R.string.connected);
                        invalidateOptionsMenu();
                    } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { // 处理断开广播
                        mConnected = false;
                        updateConnectionState(R.string.disconnected);
                        invalidateOptionsMenu();
                    } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {//处理接收到的数据
                        parseReciveData(intent);
                    } else if (BluetoothLeService.CHARACTERISTIC_WRITE_SUCCESS.equals(action)) {
                        synchronized (obj) {
                            obj.notify();
                        }
                    } else if (BluetoothLeService.CHARACTERISTIC_WRITE_FAILURE.equals(action)) {
                        displayData(getString(R.string.failed_write));
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        dataParser = new DataParser(this);

        // 注册广播
        registerReceiver(mGattUpdateReceiver, FilterHelper.makeGattUpdateIntentFilter());
        HideSoftInputUtil.hideSoftInputMethod(displayEdit); // 不弹出系统键盘
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // 左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); // 设置返回键可用

        initView();
        setOnClickListerner();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 1111 && resultCode == Activity.RESULT_OK) {
            num = resultData.getIntExtra("number", 1);
            displayData("设置数值：" + num);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_control, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                bluetoothLeService.connect(deviceAddress);
                return true;
            case R.id.menu_disconnect:
                bluetoothLeService.disconnect(deviceAddress);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_clear_log:
                displayEdit.setText("");//清除记录
        }
        return super.onOptionsItemSelected(item);
    }

    private void parseReciveData(Intent intent) {
        String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
        Log.w(TAG, "onReceive:" + data);
        String[] dataList = data.split(" "); // 把传过来的数据字符串拆分成数组

        // TODO: 2021/8/30 处理接收到的多包数据
        if (dataList[0].equals("7F") && dataList[2].equals("00")) {
            isReciveMultPkg = true;//设置多包接收中
            recivePkgNum = Byte.parseByte(dataList[4], 16) + Byte.parseByte(dataList[5], 16) * 0x100;
            reciveMultiPkg[0] = dataList;
            index++;
        } else if (isReciveMultPkg && index == Byte.parseByte(dataList[2])) {
            if (index < recivePkgNum) {
                reciveMultiPkg[index] = dataList;
                index++;
            }
        }
        //如果最后一包接收完成则进行重置
        if (isReciveMultPkg && index == recivePkgNum) {
            isReciveMultPkg = false;//将接收多包状态设置为false
            dataParser.parseMultiData(reciveMultiPkg, recivePkgNum);
            reciveMultiPkg = new String[10][];
            recivePkgNum = 0;
            index = 0;
        }
        //如果接收到的数据是单包则调用该方法
        if (!dataList[0].equals("7F")) {
            dataParser.parseSingleData(dataList);
        }
    }

    // 设置连接状态
    @SuppressLint("ResourceAsColor")
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(() -> connectionState.setText(resourceId));
    }

    private void initView() {
        toolbar.setTitle(deviceName);
        deviceAddressTxt.setText(deviceAddress);
    }

    @Override
    public void onClick(View v) {
        if (!mConnected) {
            ToastUtil.showShort(mContext, R.string.unconnected_device);
            return;
        }

        switch (v.getId()) {
            case R.id.sync_date:
                SyncDate.onSyncDate(bluetoothLeService, deviceAddress);
                break;
            case R.id.read_date:
                writeCharacteristic(deviceAddress, BluetoothCommand.READ_DATE_CMD);
                break;
            case R.id.read_battery:
                writeCharacteristic(deviceAddress, BluetoothCommand.READ_BATTERY_CMD);
                break;
            case R.id.update_sport_nofitication:
                SportNotification.onUpdateSoprtNotification(this, bluetoothLeService, deviceAddress, obj);
                break;
            case R.id.test_vibrato:
                writeCharacteristic(deviceAddress, BluetoothCommand.TEST_VIBRATO_CMD);
                break;
            case R.id.flash_id:
                writeCharacteristic(deviceAddress, BluetoothCommand.FLASH_ID_CMD);
                break;
            case R.id.g_sensor:
                writeCharacteristic(deviceAddress, BluetoothCommand.G_SENSOR_CMD);
                break;
            case R.id.hr_control:
                HeartRateControl.onHeartRateControl(this, hrControlBtn, bluetoothLeService, deviceAddress);
                break;
            case R.id.history_step:
                HistoryData.onRequestDataOfDay(mContext, this, bluetoothLeService, deviceAddress, 0x01, 0x06);
                break;
            case R.id.temp_contorl:
                TempControl.onRequestLiveTempData(mContext, this, tempControlBtn, bluetoothLeService, deviceAddress);
                break;
            case R.id.history_hr:
                HistoryData.onRequestDataOfDay(mContext, this, bluetoothLeService, deviceAddress, 0x01, 0x02);
                break;
            case R.id.history_temp:
                HistoryData.onRequestDataOfDay(mContext, this, bluetoothLeService, deviceAddress, 0x01, 0x05);
                break;
            case R.id.activity_funtion:
                ActivityFunction.onSendActivityFunction(mContext, num, obj, this, bluetoothLeService, deviceAddress);
                break;
            case R.id.set_number:
                Intent intent = new Intent(this, NumberPackerActivity.class);
                startActivityForResult(intent, 1111);
                break;
            case R.id.set_volume:
                VolumnControl.onVolumnControl(mContext, num, bluetoothLeService, deviceAddress);
                break;
            case R.id.mac_adress:
                writeCharacteristic(deviceAddress, BluetoothCommand.GET_MAC_ADDRESS);
                break;
            case R.id.time_format:
                TimeFormat.onSetTimeFormat(mContext, this, setTimeModeBtn, bluetoothLeService, deviceAddress);
                break;
        }
    }

    //设置控件点击事件
    void setOnClickListerner() {
        syncDateBtn.setOnClickListener(this);
        readDateBtn.setOnClickListener(this);
        readBatteryBtn.setOnClickListener(this);
        updateSportNotification.setOnClickListener(this);
        testVibration.setOnClickListener(this);
        flashIdBtn.setOnClickListener(this);
        gSensorBtn.setOnClickListener(this);
        hrControlBtn.setOnClickListener(this);
        historyStepBtn.setOnClickListener(this);
        tempControlBtn.setOnClickListener(this);
        historyHeartRateBtn.setOnClickListener(this);
        historyTempBtn.setOnClickListener(this);
        activityFuncBtn.setOnClickListener(this);
        setNumberBtn.setOnClickListener(this);
        setVolumeBtn.setOnClickListener(this);
        macAddressBtn.setOnClickListener(this);
        setTimeModeBtn.setOnClickListener(this);
        setTimeModeBtn.setOnClickListener(this);
    }

    // 展示读取到的数据到编辑框
    @Override
    public void displayData(String data) {
        runOnUiThread(
                () -> {
                    if (!data.equals("")) {
                        if (!TextUtils.isEmpty(displayEdit.getText())) {
                            displayEdit.append("\n");
                        }
                        displayEdit.append(data);
                    }
                });
    }

}
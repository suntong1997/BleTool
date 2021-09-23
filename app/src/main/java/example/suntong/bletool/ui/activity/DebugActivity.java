package example.suntong.bletool.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import example.suntong.bletool.FileUtil;
import example.suntong.bletool.FilterHelper;
import example.suntong.bletool.R;
import example.suntong.bletool.functions.ActivityFunction;
import example.suntong.bletool.functions.HeartRateControl;
import example.suntong.bletool.functions.HistoryData;
import example.suntong.bletool.functions.SportNotification;
import example.suntong.bletool.functions.SyncDate;
import example.suntong.bletool.functions.TempControl;
import example.suntong.bletool.functions.TimeFormat;
import example.suntong.bletool.functions.VolumeControl;
import example.suntong.bletool.interfaces.Iview;
import example.suntong.bletool.service.BluetoothLeService;
import example.suntong.bletool.util.HideSoftInputUtil;
import example.suntong.bletool.util.TimeUtil;
import example.suntong.bletool.util.ToastUtil;

public class DebugActivity extends BaseActivity implements View.OnClickListener, Iview {

    private boolean mConnected = false;
    private final Context mContext = this;
    private final Object obj = new Object();
    private final String TAG = DebugActivity.class.getSimpleName();
    private DataParser dataParser;
    private String[][] receiveMultiPkg = new String[10][];
    private int receivePkgNum;
    private boolean isReceiveMultiPkg = false;
    private int index = 0;
    private int num;
    boolean isDiscoveryService = false;
    private String displayTxt;
    public static String sendData = "";//保存发送的指令
    private String receiveData = "receive: ";//保存接收到的数据
    Intent intent;

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
    @BindView(R.id.firmware_version)
    Button getFirmwareVersion;
    @BindView(R.id.history_spo2)
    Button historySpo2Btn;
    @BindView(R.id.export_log)
    Button exportLogBtn;
    @BindView(R.id.export_hex)
    Button exportHexBtn;
    @BindView(R.id.persion_info)
    Button setPersionInfo;

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
                        isDiscoveryService = false;
                        updateConnectionState(R.string.disconnected);
                        invalidateOptionsMenu();
                    } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        isDiscoveryService = true;

                    } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {//处理接收到的数据
                        parseReceiveData(intent);
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
        setOnClickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isDiscoveryService = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect(deviceAddress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 1111 && resultCode == Activity.RESULT_OK) {
            num = resultData.getIntExtra("number", 1);
            displayData(getString(R.string.value_setting) + num);
        }
        if (requestCode == 1122 && resultCode == Activity.RESULT_OK) {
            byte[] cmd = resultData.getByteArrayExtra("persion_info_cmd");
            writeCharacteristic(deviceAddress, cmd);
            receiveData = "receive: ";
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

    private void parseReceiveData(Intent intent) {
        String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);

        Log.w(TAG, "onReceive:" + data);
        String[] dataList = data.split(" "); // 把传过来的数据字符串拆分成数组

        // TODO: 2021/8/30 处理接收到的多包数据
        if (dataList[0].equals("7F") && dataList[2].equals("00")) {
            isReceiveMultiPkg = true;//设置多包接收标志为true
            receivePkgNum = Byte.parseByte(dataList[4], 16) + Byte.parseByte(dataList[5], 16) * 0x100;//计算包数
            receiveMultiPkg[0] = dataList;
            receiveData = receiveData + data + "\n";
            index++;
        } else if (isReceiveMultiPkg && index == Byte.parseByte(dataList[2])) {
            if (index < receivePkgNum) {
                receiveMultiPkg[index] = dataList;
                receiveData = receiveData + data + "\n";
                index++;
            }
        }
        //如果最后一包接收完成则进行重置
        if (isReceiveMultiPkg && index == receivePkgNum) {
            isReceiveMultiPkg = false;//将接收多包状态设置为false
            dataParser.parseMultiData(receiveMultiPkg, receivePkgNum);//解析收到的数据
            receiveMultiPkg = new String[10][];
            receivePkgNum = 0;
            index = 0;
        }
        //如果接收到的数据是单包则调用该方法
        if (!dataList[0].equals("7F")) {
            dataParser.parseSingleData(dataList);
            receiveData = receiveData + data;
        }
    }

    // 设置连接状态
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(() -> {
            if (resourceId == R.string.connected) {
                connectionState.setTextColor(Color.GREEN);
            } else {
                connectionState.setTextColor(Color.RED);
            }
            connectionState.setText(resourceId);
        });
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
                SportNotification.onUpdateSoprtNotification(bluetoothLeService, deviceAddress, obj);
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
                TempControl.onRequestLiveTempData(mContext, tempControlBtn, bluetoothLeService, deviceAddress);
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
            case R.id.history_spo2:
                HistoryData.onRequestDataOfDay(mContext, this, bluetoothLeService, deviceAddress, 0x01, 0x26);
                break;
            case R.id.set_number:
                intent = new Intent(this, NumberPackerActivity.class);
                startActivityForResult(intent, 1111);
                break;
            case R.id.set_volume:
                VolumeControl.onVolumeControl(mContext, num, bluetoothLeService, deviceAddress);
                break;
            case R.id.mac_adress:
                writeCharacteristic(deviceAddress, BluetoothCommand.GET_MAC_ADDRESS);
                break;
            case R.id.time_format:
                TimeFormat.onSetTimeFormat(mContext, setTimeModeBtn, bluetoothLeService, deviceAddress);
                break;
            case R.id.firmware_version:
                writeCharacteristic(deviceAddress, BluetoothCommand.GET_FIRMWARE_VERSION);
                break;
            case R.id.export_log:
                //导出数据
                writeToFile(displayTxt, "数据解析");

            case R.id.export_hex:
                writeToFile(sendData + "\n" + receiveData, "原始数据");
                sendData = "send: ";
                receiveData = "receive: ";
                break;
            case R.id.persion_info:
                intent = new Intent(this, PersionInfoActivity.class);
                startActivityForResult(intent, 1122);
        }
    }

    //设置控件点击事件
    void setOnClickListener() {
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
        getFirmwareVersion.setOnClickListener(this);
        historySpo2Btn.setOnClickListener(this);
        exportLogBtn.setOnClickListener(this);
        exportHexBtn.setOnClickListener(this);
        setPersionInfo.setOnClickListener(this);
    }

    private void writeToFile(String displayTxt, String fileName) {
        if (displayEdit == null) {
            ToastUtil.showShort(mContext, "数据为空");
            return;
        }
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName + "/";
        fileName = TimeUtil.getCurrentDate() + ".txt";

        boolean result = FileUtil.getInstance().writeToFile(displayTxt, filePath, fileName);
        if (result && displayTxt != null) {
            ToastUtil.showShort(mContext, "导出成功");
        } else {
            ToastUtil.showShort(mContext, "导出失败");
            return;
        }
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
                        displayTxt = data;//保存一份数据用来存储到本地
                        displayEdit.append(data);
                    }
                });
    }

    @Override
    public Context getContext() {
        return mContext;
    }
}
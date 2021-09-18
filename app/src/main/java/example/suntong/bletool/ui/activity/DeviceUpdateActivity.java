package example.suntong.bletool.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.suntong.bletool.FilterHelper;
import example.suntong.bletool.R;
import example.suntong.bletool.UpdateMode;
import example.suntong.bletool.service.BluetoothLeService;
import example.suntong.bletool.util.ToastUtil;

public class DeviceUpdateActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = DeviceUpdateActivity.class.getSimpleName();
    Context mContext = this;
    private volatile boolean mConnected = false;
    private static final int READ_REQUEST_CODE = 42;
    private Byte[] mFileData;
    private final Object obj = new Object();
    private volatile boolean mWriteSuccess = true;//判断是否写入成功
    Uri fileUri;//升级文件的uri
    String filePath;
    UpdateMode updateMode;//升级模式
    volatile int i = 0;//记录进度

    @BindView(R.id.connect_state)
    TextView connectionState;
    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.device_address)
    TextView deviceAddressTxt;
    @BindView(R.id.operation_log)
    TextView operationLog;
    @BindView(R.id.mode_choose_btn)
    Button chooseModeBtn;
    @BindView(R.id.update_btn)
    Button updateBtn;
    @BindView(R.id.update_process_LinearLayout)
    LinearLayout updateProcessLyt;
    @BindView(R.id.update_process_percent)
    TextView updateProcessPercentTxt;
    @BindView(R.id.update_progress)
    ProgressBar updateProgress;

    // TODO 处理接收到的广播
    private final BroadcastReceiver mGattUpdateReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) { // 处理连接广播
                        mConnected = true;
                        //如果升级模式不为空且文件大小不为0则设置按钮可点击
                        if (fileUri != null && updateMode != null) {
                            setUpdateBtnEnabled(true);
                        }
                        updateConnectionState(R.string.connected);
                        invalidateOptionsMenu();
                    } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { // 处理断开广播
                        mConnected = false;
                        synchronized (obj) {
                            //如果设备在发送数据的过程中断开连接，
                            // 则通知正在发送数据的线程避免一直处于等待状态，并执行连接断开后的逻辑
                            obj.notify();
                        }
                        setUpdateBtnEnabled(false);//设置更新按钮不可点击
                        setUpdateProgress(false);//隐藏进度条
                        setChooseModeBtnenable(true);//设置模式选择按钮可点击
                        updateConnectionState(R.string.disconnected);
                        runOnUiThread(() -> chooseModeBtn.setText(R.string.update_mode));
                        invalidateOptionsMenu();
                    } else if (BluetoothLeService.CHARACTERISTIC_WRITE_FAILURE.equals(action)) {
                        mWriteSuccess = false;
                        displayLog(getString(R.string.failure_update));
                    } else if (BluetoothLeService.CHARACTERISTIC_WRITE_SUCCESS.equals(action)) {
                        synchronized (obj) {
                            obj.notify();
                        }
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_update);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        // 注册广播
        registerReceiver(mGattUpdateReceiver, FilterHelper.makeGattUpdateIntentFilter());
        // 左侧添加一个默认的返回图标
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true); // 设置返回键可用
        initView();
        setOnClickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                fileUri = resultData.getData();//获取文件的uri去读取文件
                displayLog(getString(R.string.file_path) + ":" + fileUri.getPath());
                if (fileUri == null) throw new AssertionError();
                filePath = fileUri.getPath();

                //如果是Dfu模式不需要直接读取数据
                if (updateMode != UpdateMode.DFU_UPDATE) new Thread(() -> {
                    try {
                        mFileData = FilterHelper.readDataFromUri(mContext, fileUri);
                        float size = mFileData.length / 1024.0f;
                        displayLog(getString(R.string.complete_read) + ":" + size + " KB");

                        if (mConnected && mFileData.length > 0 && updateMode != null) {
                            setUpdateBtnEnabled(true);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                else {
                    if (mConnected) {
                        setUpdateBtnEnabled(true);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_explorer_menu, menu);
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
            case R.id.menu_file_explorer:
                if (updateMode == null) {
                    ToastUtil.showShort(mContext, getString(R.string.choose_file_after_mode));
                    break;
                }
                openFileExplorer();
                return true;
            case R.id.menu_connect:
                bluetoothLeService.connect(deviceAddress);
                return true;
            case R.id.menu_disconnect:
                bluetoothLeService.disconnect(deviceAddress);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (!mConnected) {
            displayLog(getString(R.string.disconnected_device));
            return;
        }

        switch (v.getId()) {
            case R.id.mode_choose_btn:
                chooseUpdateMode();
                break;
            case R.id.connect_state:
                if (!mConnected) {
                    connect(deviceAddress);
                } else {
                    disconnect(deviceAddress);
                }
                break;
            case R.id.update_btn:
                if (!mConnected) {
                    ToastUtil.showShort(mContext, R.string.unconnected_device);
                }
                switch (updateMode) {
                    case UI_UPDATE:
                        parseMultiPkg(0);
                        break;
                    case FONT_UPDATE:
                        parseMultiPkg(1);
                        break;
                    case OTA_UPDATE:
                        parseMultiPkg(2);
                        break;
                    case FACE_UPDATE:
                        parseFaceCMD(new byte[]{0x01, 0x00}, (byte) 0x02, (byte) 0x8E);
                        break;
                    default:
                        ToastUtil.showShort(mContext, getString(R.string.cannot_find_mode));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    //选择更新模式
    private void chooseUpdateMode() {
        if (!mConnected) {
            ToastUtil.showShort(mContext, R.string.unconnected_device);
            return;
        }

        PopupMenu popupMenu = new PopupMenu(mContext, chooseModeBtn);
        popupMenu.getMenuInflater().inflate(R.menu.pop_update_mode_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_update_ui:
                            displayLog(getString(R.string.ui_update));
                            chooseModeBtn.setText(R.string.ui_update);
                            updateMode = UpdateMode.UI_UPDATE;
                            setUpdateBtnEnabled(mConnected && mFileData != null && mFileData.length > 0);
                            break;
                        case R.id.menu_update_font:
                            displayLog(getString(R.string.font_update));
                            chooseModeBtn.setText(R.string.font_update);
                            updateMode = UpdateMode.FONT_UPDATE;
                            setUpdateBtnEnabled(mConnected && mFileData != null && mFileData.length > 0);
                            break;
                        case R.id.menu_update_ota:
                            displayLog(getString(R.string.ota_update));
                            chooseModeBtn.setText(R.string.ota_update);
                            updateMode = UpdateMode.OTA_UPDATE;
                            setUpdateBtnEnabled(mConnected && mFileData != null && mFileData.length > 0);
                            break;
                        case R.id.menu_update_face:
                            displayLog(getString(R.string.face_update));
                            chooseModeBtn.setText(R.string.face_update);
                            updateMode = UpdateMode.FACE_UPDATE;
                            setUpdateBtnEnabled(mConnected && mFileData != null && mFileData.length > 0);
                            break;

                    }
                    //选择模式后重置状态
                    fileUri = null;
                    filePath = null;
                    mFileData = null;
                    setUpdateBtnEnabled(false);
                    updateBtn.setText(R.string.start_update);
                    return true;
                });
    }


    void parseMultiPkg(int fileType) {
        setUpdateBtnEnabled(false);
        setChooseModeBtnenable(false);
        new Thread(
                () -> {
                    synchronized (obj) {
                        long length = mFileData.length + 5; // 数据部分长度,需要加上文件类型和文件的数据长度
                        int PKG_NUM; // 包数
                        PKG_NUM =
                                ((length + 8) % 0xB2) == 0
                                        ? (int) ((length + 8) / 0xB2)
                                        : (int) ((length + 8) / 0xB2) + 1;

                        updateProgress.setMax(PKG_NUM);

                        // 包数的高低位
                        byte lowPkgNum = (byte) (PKG_NUM & 0xFF);
                        byte highPkgNum = (byte) ((PKG_NUM & 0xFF00) >> 0x08);

                        byte CMD_MULTI_PKG = 0x7F;
                        byte CMD_CLASS = 0x06;
                        byte CMD_ID = (byte) 0x85;

                        // 数据长度
                        byte lowLength = (byte) (length & 0xFF);
                        byte lowMidLength = (byte) ((length & 0xFF00) >> 0x08);
                        byte highMidLength = (byte) ((length & 0xFF0000) >> 0x10);
                        byte highLength = (byte) ((length & 0xFF000000) >> 0x18);

                        // 命令总长度
                        long CMD_LENGTH = (length + 4);
                        byte lowCmdLength = (byte) (CMD_LENGTH & 0xFF);
                        byte highCmdLength = (byte) ((CMD_LENGTH & 0xFF00) >> 0x08);

                        // 包的序号高低位
                        byte numPkgLow;
                        byte numPkgHigh;

                        // 检验码，将第一个包从检验位之后的数据全部相加得到
                        long CRC = CMD_CLASS + CMD_ID + lowCmdLength + highCmdLength + fileType;

                        for (i = 0; i < 0xA5; i++) {
                            CRC += mFileData[i];
                        }

                        // 检验码的高低位
                        byte CRC_L = (byte) (CRC & 0xFF);
                        byte CRC_H = (byte) ((CRC & 0xFF00) >> 0x08);

                        int index = 0; // 记录数据的位置
                        byte[] pkg = new byte[182];//用来记录每一包的数据

                        setUpdateProgress(true);//显示进度条

                        // TODO: 2021/8/29   处理发送逻辑
                        for (i = 0; i < PKG_NUM; i++) {
                            mWriteSuccess = true;//重置写入状态

                            //计算是第几个包
                            numPkgLow = (byte) (i & 0xFF);
                            numPkgHigh = (byte) ((i & 0xFF00) >> 0x08);

                            pkg[0] = CMD_MULTI_PKG;
                            pkg[1] = CRC_L;
                            pkg[2] = numPkgLow;
                            pkg[3] = numPkgHigh;

                            // 第一个包需要填充的数据
                            if (i == 0) {
                                pkg[4] = lowPkgNum;
                                pkg[5] = highPkgNum;
                                pkg[6] = CRC_L;
                                pkg[7] = CRC_H;
                                pkg[8] = CMD_CLASS;
                                pkg[9] = CMD_ID;
                                pkg[10] = lowCmdLength;
                                pkg[11] = highCmdLength;
                                pkg[12] = (byte) fileType;
                                pkg[13] = lowLength;
                                pkg[14] = lowMidLength;
                                pkg[15] = highMidLength;
                                pkg[16] = highLength;

                                for (int j = 17; j < 182; j++) {
                                    if (index < length) {//防止数组越界
                                        pkg[j] = mFileData[index];
                                    }
                                    index++;
                                }
                            } else if (i == (PKG_NUM - 1)) { // 处理最后一个包的数据,防止长度不够数组越界
                                int size = (int) (length % 178);
                                if (size == 0) {//如果余数为0则最后一包也是182个字节
                                    size = 182;
                                }
                                //因为重新new了一个数组所以前面的头部数据需要补上
                                pkg = new byte[size];
                                pkg[0] = CMD_MULTI_PKG;
                                pkg[1] = CRC_L;
                                pkg[2] = numPkgLow;
                                pkg[3] = numPkgHigh;
                                for (int j = 4; j < size; j++) {
                                    if (index < length) {//防止数组越界
                                        pkg[j] = mFileData[index];
                                    }
                                    index++;
                                }
                            } else {
                                for (int j = 4; j < 182; j++) {
                                    if (index < length) {//防止数组越界
                                        pkg[j] = mFileData[index];
                                    }
                                    index++;
                                }
                            }

                            Log.w(TAG, "sendMultiPkgCmd:i= " + i);

                            // 传入发送进度
                            setUpdateProgressProcess(i, PKG_NUM);

//                            showPkgData(pkg);

                            bluetoothLeService.writeCharacteristic(deviceAddress, pkg);//写入数据到设备

                            Log.e(TAG, "parseMultiPkg: 1");

                            try {
                                obj.wait();//发送数据后进入等待，直到收到写入后的回调通知
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //如果更新过程断开连接则取消发送
                            if (!mConnected) {
                                displayLog(getString(R.string.disconnected__failed_update));
                                runOnUiThread(() -> updateBtn.setText(R.string.failure_update));
                                setUpdateBtnEnabled(false);
                                setUpdateProgress(false);
                                filePath = null;
                                fileUri = null;
                                mFileData = null;
                                break;
                            }

                            //判断收到的回调是否写入成功，若失败则停止发送
                            if (!mWriteSuccess) {
                                displayLog(getString(R.string.send_failed) + "(" + i + "/" + PKG_NUM + ")");
                                runOnUiThread(
                                        () -> {
                                            setUpdateProgress(false);
                                            updateBtn.setText(R.string.failure_update);
                                            chooseModeBtn.setText(R.string.update_mode);
                                        });
                                filePath = null;
                                fileUri = null;
                                mFileData = null;
                                setChooseModeBtnenable(true);
                                break;
                            }
                        }// TODO: 2021/8/29 发送结束

                        setUpdateProgress(false);//更新结束隐藏进度条
                        //判断是否更新完成
                        if (mWriteSuccess && mConnected) {
                            runOnUiThread(() -> {
                                displayLog(getString(R.string.send_completed));
                                updateBtn.setText(R.string.complete_update);
                                chooseModeBtn.setText(R.string.update_mode);
                            });
                            filePath = null;
                            fileUri = null;
                            mFileData = null;
                            setChooseModeBtnenable(true);
                        }
                    }
                })
                .start();

    }


    private void parseFaceCMD(byte[] faceId, byte CMD_CLASS, byte CMD_ID) {
        setUpdateBtnEnabled(false);
        setChooseModeBtnenable(false);
        new Thread(
                () -> {
                    synchronized (obj) {

                        long length = mFileData.length + 6; // 数据部分长度,需要加上文件类型和文件的数据长度

                        int PKG_NUM; // 包数
                        PKG_NUM =
                                ((length + 8) % 0xB2) == 0
                                        ? (int) ((length + 8) / 0xB2)
                                        : (int) ((length + 8) / 0xB2) + 1;

                        updateProgress.setMax(PKG_NUM);

                        // 包数的高低位
                        byte lowPkgNum = (byte) (PKG_NUM & 0xFF);
                        byte highPkgNum = (byte) ((PKG_NUM & 0xFF00) >> 0x08);
                        byte CMD_MULTI_PKG = 0x7F;

                        // 数据长度
                        byte lowLength = (byte) (length & 0xFF);
                        byte lowMidLength = (byte) ((length & 0xFF00) >> 0x08);
                        byte highMidLength = (byte) ((length & 0xFF0000) >> 0x10);
                        byte highLength = (byte) ((length & 0xFF000000) >> 0x18);

                        // 命令长度
                        long CMD_LENGTH = (length + 4);
                        byte lowCmdLength = (byte) (CMD_LENGTH & 0xFF);
                        byte highCmdLength = (byte) ((CMD_LENGTH & 0xFF00) >> 0x08);

                        // 包的序号高低位
                        byte numPkgLow;
                        byte numPkgHigh;

                        // 检验码，将第一个包从检验位之后的数据全部相加得到
                        long CRC =
                                CMD_CLASS + CMD_ID + lowCmdLength + highCmdLength + faceId[0] + faceId[1];

                        // 一个包的数据长度为182，减去第一个包前面的固定字节则数据部分最大为165
                        if (mFileData.length < 0xA5) {
                            for (byte mReadBinDatum : mFileData) {
                                CRC += mReadBinDatum;
                            }
                        } else {
                            for (i = 0; i < 0xA5; i++) {
                                CRC += mFileData[i];
                            }
                        }

                        // 检验码的高低位
                        byte CRC_L = (byte) (CRC & 0xFF);
                        byte CRC_H = (byte) ((CRC & 0xFF00) >> 0x08);

                        int index = 0; // 记录数据的位置

                        setUpdateProgress(true);

                        // 处理发送逻辑
                        for (i = 0; i < PKG_NUM; i++) {
                            mWriteSuccess = true;

                            byte[] pkg = new byte[182];
                            numPkgLow = (byte) (i & 0xFF);
                            numPkgHigh = (byte) ((i & 0xFF00) >> 0x08);

                            pkg[0] = CMD_MULTI_PKG;
                            pkg[1] = CRC_L;
                            pkg[2] = numPkgLow;
                            pkg[3] = numPkgHigh;

                            // 第一个包需要填充的数据
                            if (i == 0) {
                                pkg[4] = lowPkgNum;
                                pkg[5] = highPkgNum;
                                pkg[6] = CRC_L;
                                pkg[7] = CRC_H;
                                pkg[8] = CMD_CLASS;
                                pkg[9] = CMD_ID;
                                pkg[10] = lowCmdLength;
                                pkg[11] = highCmdLength;
                                pkg[12] = faceId[0];
                                pkg[13] = faceId[1];
                                pkg[14] = lowLength;
                                pkg[15] = lowMidLength;
                                pkg[16] = highMidLength;
                                pkg[17] = highLength;

                                for (int j = 18; j < 182; j++) {
                                    if (index < length) {
                                        pkg[j] = mFileData[index];
                                    }
                                    index++;
                                }
                            } else if (i == (PKG_NUM - 1)) { // 处理最后一个包的数据,防止长度不够数组越界
                                int size = (int) (length % 178);
                                if (size == 0) {
                                    size = 182;
                                }
                                pkg = new byte[size];
                                pkg[0] = CMD_MULTI_PKG;
                                pkg[1] = CRC_L;
                                pkg[2] = numPkgLow;
                                pkg[3] = numPkgHigh;
                                for (int j = 4; j < size; j++) {
                                    if (index < length) {
                                        pkg[j] = mFileData[index];
                                    }
                                    index++;
                                }
                            } else {
                                for (int j = 4; j < 182; j++) {
                                    if (index < length) {
                                        pkg[j] = mFileData[index];
                                    }
                                    index++;
                                }
                            }

                            Log.w(TAG, "sendMultiPkgCmd:i= " + i);

                            // 传入发送进度
                            setUpdateProgressProcess(i, PKG_NUM);

//                            showPkgData(pkg);

                            Log.e(TAG, "parseFaceCMD:写入");
                            bluetoothLeService.writeCharacteristic(deviceAddress, pkg);

                            try {
                                obj.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (!mConnected) {
                                displayLog(getString(R.string.disconnected_send_failed));
                                runOnUiThread(() -> updateBtn.setText(R.string.failure_update));
                                setUpdateBtnEnabled(false);
                                setUpdateProgress(false);
                                filePath = null;
                                fileUri = null;
                                mFileData = null;
                                break;
                            }

                            if (!mWriteSuccess) {
                                displayLog(getString(R.string.failure_send) + " (" + i + "/" + PKG_NUM + ")");
                                setUpdateProgress(false);
                                runOnUiThread(() -> {
                                    updateBtn.setText(R.string.failure_update);
                                    chooseModeBtn.setText(R.string.update_mode);
                                });
                                filePath = null;
                                fileUri = null;
                                mFileData = null;
                                setChooseModeBtnenable(true);
                                return;
                            }
                        }

                        setUpdateProgress(false);
                        if (mWriteSuccess && mConnected) {
                            runOnUiThread(() -> {
                                displayLog(getString(R.string.send_completed));
                                updateBtn.setText(R.string.complete_update);
                                chooseModeBtn.setText(R.string.update_mode);
                            });
                            filePath = null;
                            fileUri = null;
                            mFileData = null;
                            setChooseModeBtnenable(true);
                        }
                        mWriteSuccess = true;
                    }
                })
                .start();
    }

    //展示收到或发送的每一包数据
    private void showPkgData(byte[] pkg) {
        final StringBuilder stringBuilder = new StringBuilder(pkg.length);
        for (byte byteChar : pkg)
            stringBuilder.append(String.format("%02X ", byteChar));
        Log.w(TAG, "sendMultiPkgCmd: " + stringBuilder.toString());
    }

    //打开文件管理器
    void openFileExplorer() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("file/");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    //初始化界面
    void initView() {
        toolbar.setTitle(deviceName);
        deviceAddressTxt.setText(deviceAddress);
    }

    //设置控件点击事件
    private void setOnClickListener() {
        updateBtn.setOnClickListener(this);
        chooseModeBtn.setOnClickListener(this);
    }

    //展示操作记录
    public void displayLog(String str) {
        runOnUiThread(
                () -> {
                    if (!str.equals("")) {
                        if (!TextUtils.isEmpty(operationLog.getText())) {
                            operationLog.append("\n");
                        }
                        operationLog.append(str);
                    }
                });
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

    //更新进度
    private void setUpdateProgressProcess(int percent, int pkgNum) {
        runOnUiThread(() -> {
            int progress = (int) ((100.0 / pkgNum) * percent);
            updateProgress.setProgress(percent);
            updateProcessPercentTxt.setText(String.valueOf(progress));
            int updatingTxt = percent % 80;
            if (updatingTxt >= 0 && updatingTxt < 20) {
                updateBtn.setText(R.string.failure_update);
            } else if (updatingTxt >= 20 && updatingTxt < 40) {
                updateBtn.setText(R.string.failure_update+".");
            } else if (updatingTxt >= 40 && updatingTxt < 60) {
                updateBtn.setText(R.string.failure_update+"..");
            } else {
                updateBtn.setText(R.string.failure_update+"...");
            }
        });
    }

    //设置进度条的展示和隐藏
    void setUpdateProgress(boolean enable) {
        runOnUiThread(() -> {
            if (enable) {
                updateProcessLyt.setVisibility(View.VISIBLE);
                updateProgress.setVisibility(View.VISIBLE);
            } else {
                updateProcessLyt.setVisibility(View.GONE);
                updateProgress.setVisibility(View.GONE);
            }
        });
    }

    //设置更新模式选择按钮的展示和隐藏
    private void setChooseModeBtnenable(boolean enable) {
        runOnUiThread(() -> chooseModeBtn.setEnabled(enable));
    }

    //设置按键可点击
    void setUpdateBtnEnabled(boolean enable) {
        runOnUiThread(() -> updateBtn.setEnabled(enable));
    }

}
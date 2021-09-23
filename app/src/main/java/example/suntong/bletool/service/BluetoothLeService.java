package example.suntong.bletool.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import example.suntong.bletool.GattAttributes;
import example.suntong.bletool.ui.activity.DebugActivity;

public class BluetoothLeService extends Service {
    private static final String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private final Map<String, BluetoothGatt> gattMap = new HashMap<>();//记录连接到的蓝牙地址

    public static final String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public static final String ACTION_STOP_RECEIVE = "ACTION_STOP_RECEIVE";
    public static final String CHARACTERISTIC_WRITE_FAILURE = "CHARACTERISTIC_WRITE_FAILURE";
    public static final String CHARACTERISTIC_WRITE_SUCCESS = "CHARACTERISTIC_WRITE_SUCCESS";

    public static final UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT);

    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) { // 收到连接回调
                        intentAction = ACTION_GATT_CONNECTED;
                        gattMap.put(gatt.getDevice().getAddress(), gatt);
                        broadcastUpdate(intentAction, gatt);
                        Log.w(TAG, "连接到设备" + gatt.getDevice().getName());
                        Log.w(TAG, "尝试发现服务:" + gatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { // 收到断开回调
                        intentAction = ACTION_GATT_DISCONNECTED;
                        gattMap.remove(gatt.getDevice().getAddress()); // 断开后将句柄从map中移除
                        Log.i(TAG, "Disconnected from GATT server." + gatt.getDevice().getName());
                        broadcastUpdate(intentAction, gatt);
                        gatt.close(); // 关闭句柄
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt);
                        setTXCharacteristicNotification(gatt, true);
                    } else {
                        Log.w(TAG, "服务查找状态: " + status);
                    }
                }

                @Override
                public void onCharacteristicRead(
                        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                @Override
                public void onCharacteristicWrite(
                        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.w(TAG, "onCharacteristicWrite: " + status);
                    broadcastUpdate("CHARACTERISTIC_WRITE_SUCCESS");
                }

                @Override
                public void onCharacteristicChanged(
                        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    // characteristic改变时发送广播
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt);
                    Log.e(TAG, "onCharacteristicChanged: " + gatt.getDevice().getAddress());
                }
            };

    private void broadcastUpdate(final String action, BluetoothGatt gatt) {
        String address = gatt.getDevice().getAddress();

        final Intent intent = new Intent(action);
        intent.putExtra("device_address", address);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // 广播获取到的数据
    private void broadcastUpdate(
            final String action, final BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
        final Intent intent = new Intent(action);

        String address = gatt.getDevice().getAddress();
        intent.putExtra("device_address", address);

        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    // 广播获取到的数据
    private void broadcastUpdate(
            final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "蓝牙管理器初始化失败");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "获取蓝牙适配器失败");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "找不到设备" + null);
            return false;
        }

        // 如果已存在则移除
        BluetoothGatt gatt = gattMap.get(address);
        if (gatt != null) {
            gatt.disconnect();
            gattMap.remove(address);
        }

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "connect: " + mBluetoothGatt);
        Log.d(TAG, "尝试创建新连接" + device.getAddress());

        return true;
    }

    public void disconnect(String address) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "蓝牙适配器未初始化");
            return;
        }
        BluetoothGatt gatt = gattMap.get(address);
        Objects.requireNonNull(gatt).disconnect();
    }

    public void close() {

        for (BluetoothGatt gatt : gattMap.values()) {
            if (gatt != null) {
                gatt.close();
            }
        }
    }

    public void writeCharacteristic(String address, byte[] data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "蓝牙适配器未初始化");
            return;
        }

        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) stringBuilder.append(String.format("%02X ", byteChar));
            DebugActivity.sendData = "send: " + stringBuilder.toString();
        }

        BluetoothGatt gatt = gattMap.get(address);
        if (gatt != null) {
            BluetoothGattService service = gatt.getService(UUID.fromString(GattAttributes.NORDIC_UART));
            if (service != null) {
                BluetoothGattCharacteristic rxCharacteristic =
                        service.getCharacteristic(UUID.fromString(GattAttributes.RX_CHARACTERISTIC));
                if (rxCharacteristic != null) {
                    rxCharacteristic.setValue(data);
                    boolean success = gatt.writeCharacteristic(rxCharacteristic);
                    Log.w(TAG, "writeCharacteristic: " + success);
                    if (!success) {
                        broadcastUpdate(BluetoothLeService.CHARACTERISTIC_WRITE_FAILURE);
                    }
                } else {
                    Log.e(TAG, "writeCharacteristic: 获取特征失败");
                }
            } else {
                Log.e(TAG, "writeCharacteristic: 获取服务失败");
            }
        } else {
            Log.e(TAG, "writeCharacteristic: 获取GATT失败");
        }
    }

    // 开启关闭通知
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "蓝牙适配器未初始化");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor =
                characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    public void setTXCharacteristicNotification(BluetoothGatt gatt, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "蓝牙适配器未初始化");
            return;
        }

        BluetoothGattService service = gatt.getService(UUID.fromString(GattAttributes.NORDIC_UART));
        BluetoothGattCharacteristic txCharacteristic =
                service.getCharacteristic(UUID.fromString(GattAttributes.TX_CHARACTERISTIC));
        setCharacteristicNotification(txCharacteristic, true);

        boolean result = mBluetoothGatt.setCharacteristicNotification(txCharacteristic, enabled);
        Log.w(TAG, "setTXCharacteristicNotification: " + result);

        BluetoothGattDescriptor descriptor =
                txCharacteristic.getDescriptor(
                        UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }
}

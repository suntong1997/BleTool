package example.suntong.bletool.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.suntong.bletool.FilterHelper;
import example.suntong.bletool.util.PermissionUtil;
import example.suntong.bletool.R;
import example.suntong.bletool.util.ToastUtil;
import example.suntong.bletool.ui.adapter.LeDeviceListAdapter;
import example.suntong.bletool.bean.DeviceItemBean;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private LeDeviceListAdapter mAdapter;
    @BindView(R.id.scan_device_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.tool_bar)
    Toolbar mToolbar;

    boolean searchViewClicked;
    Runnable mRunnable;
    private long mBackPressed;
    private AlertDialog alertDialog;
    private SearchView mSearchView;
    private final List<DeviceItemBean> mDevices = new ArrayList<>();//??????????????????????????????
    private final List<DeviceItemBean> oldDevices = new ArrayList<>();//??????????????????????????????

    List<DeviceItemBean> filterList = new ArrayList<>();//?????????????????????????????????

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    public static final int REQUEST_PERMISSION_CODE = 0;
    private static final int TIME_EXIT = 2000;

    //???????????????????????????
    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        //???????????????
        setSupportActionBar(mToolbar);

        mHandler = new Handler();

        mAdapter = new LeDeviceListAdapter(this);
        //?????????????????????
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.showShort(this, R.string.ble_not_supported);
            finish();
        }

        //??????bluetoothmanager
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();//??????bluetoothadapter

        if (mBluetoothAdapter == null) {
            ToastUtil.showShort(this, R.string.ble_not_supported);
            finish();//?????????????????????????????????
            return;
        }

        //???????????????app??????????????????
        scanLeDevice(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter = new LeDeviceListAdapter(mDevices, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        //??????????????????
        PermissionUtil.requestPermission(permissions, MainActivity.this);

        //??????????????????????????????????????????????????????????????????
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //?????????recyclerview
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

//        scanLeDevice(true);
        mAdapter.addDevices(mDevices);//????????????????????????

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        //  TODO ??????????????????????????????
        int id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) mSearchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(Color.parseColor("#CCCCCC"));
        //  TODO ????????????????????????????????????
        //?????????????????????
        mSearchView.setOnSearchClickListener(v -> {
            mHandler.removeCallbacks(mRunnable);
            for (DeviceItemBean bean : mDevices) {
                bean.setSelected(false);
            }

            ToastUtil.showShort(MainActivity.this, "??????");
            menu.findItem(R.id.menu_stop).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.findItem(R.id.menu_scan).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.findItem(R.id.menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        });

        //?????????????????????
        mSearchView.setOnCloseListener(() -> {
            scanLeDevice(false);
            Collections.sort(oldDevices, new Comparator<DeviceItemBean>() {
                @Override
                public int compare(DeviceItemBean o1, DeviceItemBean o2) {
                    return o2.getRssi() - o1.getRssi();
                }
            });
            menu.findItem(R.id.menu_stop).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.menu_scan).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            return false;
        });

        //??????????????????
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                searchViewClicked = true;
                filterList.clear();
                filterList = FilterHelper.filter(oldDevices, newText);//????????????????????????
                Collections.sort(filterList, (o1, o2) -> o2.getRssi() - o1.getRssi());
                if (filterList.size() > 0 && filterList != null) {
                    mAdapter.clear();
                    mAdapter.addDevices(filterList);

                    Log.w("setOnQueryTextListener", "onQueryTextChange: " + newText);
                }
                return true;
            }
        });

        if (mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                scanLeDevice(true);

                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                mHandler.removeCallbacks(mRunnable);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_EXIT > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            ToastUtil.showShort(this,"??????????????????");
            mBackPressed = System.currentTimeMillis();

        }
    }

    //????????????,???requestPermissions()??????????????????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
            } else {
                ToastUtil.showShort(this,"????????????");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("permission")
                        .setMessage("????????????????????????????????????")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            }
                        });

                alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //??????????????????
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    final String TAG = "onLeScan";
                    runOnUiThread(() -> {

                        //  TODO ??????javaBean????????????????????????
                        DeviceItemBean scanDevice = new DeviceItemBean();
                        scanDevice.setName(device.getName());
                        scanDevice.setAddress(device.getAddress());
                        scanDevice.setRssi(rssi);
                        //  ????????????address?????????
                        if (mDevices.size() > 0) {
                            for (DeviceItemBean bean : mDevices) {
                                if (!TextUtils.isEmpty(bean.getAddress())) {
                                    if (scanDevice.getAddress().equals(bean.getAddress())) {
                                        return;
                                    }
                                }
                            }
                        }
                        if (!searchViewClicked) {
                            if (!mDevices.contains(scanDevice)&& scanDevice.getName() != null ) {
                                mDevices.add(scanDevice);//????????????????????????
                                //  ?????????????????????????????????????????????????????????????????????
                                oldDevices.add(scanDevice);//????????????????????????
                            }
                        }
                        //  ???????????????????????????
                        Collections.sort(mDevices, (o1, o2) -> o2.getRssi() - o1.getRssi());
                        //???????????????????????????
//                            mAdapter = new LeDeviceListAdapter(mDevices, MainActivity.this);
//                            mRecyclerView.setAdapter(mAdapter);
//                            mAdapter.addDevices(mDevices);
                        mAdapter.notifyItemChanged(mDevices.indexOf(scanDevice));
                    });
                }
            };

    //????????????
    private void scanLeDevice(final boolean enable) {
        mRunnable = () -> {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            invalidateOptionsMenu();
        };
        if (enable) {
            //??????????????????
            mHandler.postDelayed(mRunnable, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            mAdapter.notifyDataSetChanged();
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //?????????????????????
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
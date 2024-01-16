package com.moko.mkgw4.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.R;
import com.moko.mkgw4.databinding.ActivityDeviceInfoMkgw4Binding;
import com.moko.mkgw4.dialog.AlertMessageDialog;
import com.moko.mkgw4.fragment.NetworkFragment;
import com.moko.mkgw4.fragment.PositionFragment;
import com.moko.mkgw4.fragment.ScannerFragment;
import com.moko.mkgw4.fragment.SettingsFragment;
import com.moko.mkgw4.utils.ToastUtils;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;
import com.moko.support.mkgw4.entity.OrderCHAR;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MkGw4DeviceInfoActivity extends MkGw4BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private ActivityDeviceInfoMkgw4Binding mBind;
    private FragmentManager fragmentManager;
    private NetworkFragment networkFragment;
    private PositionFragment posFragment;
    private ScannerFragment scannerFragment;
    private SettingsFragment settingsFragment;
    private boolean mReceiverTag;
    private int disConnectType;
    private String advName;
    private String mac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceInfoMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        fragmentManager = getSupportFragmentManager();
        advName = getIntent().getStringExtra("advName");
        mac = getIntent().getStringExtra("mac");
        initFragment();
        mBind.radioBtnNetwork.setChecked(true);
        mBind.rgOptions.setOnCheckedChangeListener(this);
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getNetworkStatus());
            orderTasks.add(OrderTaskAssembler.getMqttConnectionStatus());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    private void initFragment() {
        networkFragment = NetworkFragment.newInstance();
        posFragment = PositionFragment.newInstance();
        scannerFragment = ScannerFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, networkFragment)
                .add(R.id.frame_container, posFragment)
                .add(R.id.frame_container, scannerFragment)
                .add(R.id.frame_container, settingsFragment)
                .show(networkFragment)
                .hide(posFragment)
                .hide(scannerFragment)
                .hide(settingsFragment)
                .commit();
        mBind.tvTitle.setText(advName);
        scannerFragment.setAdvName(advName);
        settingsFragment.setMac(mac);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                if (MokoSupport.getInstance().exportDatas != null) {
                    MokoSupport.getInstance().exportDatas.clear();
                    MokoSupport.getInstance().storeString = null;
                    MokoSupport.getInstance().startTime = 0;
                    MokoSupport.getInstance().sum = 0;
                }
                showDisconnectDialog();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_DISCONNECTED_NOTIFY) {
                    final int length = value.length;
                    if (length != 5) return;
                    int header = value[0] & 0xFF;
                    int flag = value[1] & 0xFF;
                    int cmd = value[2] & 0xFF;
                    int len = value[3] & 0xFF;
                    int type = value[4] & 0xFF;
                    if (header == 0xED && flag == 0x02 && cmd == 0x01 && len == 0x01) {
                        disConnectType = type;
                    }
                }
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_SCAN_REPORT_ENABLE:
                                case KEY_POWER_LOSS_NOTIFY:
                                case KEY_DELETE_BUFFER_DATA:
                                    if (result != 1) {
                                        ToastUtils.showToast(this, "Setup failed");
                                    } else {
                                        ToastUtils.showToast(this, "Setup succeed");
                                    }
                                    break;

                                case KEY_RESET:
                                case KEY_REBOOT:
                                case KEY_CLOSE:
                                    if (result == 1) {
                                        ToastUtils.showToast(this, "Setup succeed");
                                        MokoSupport.getInstance().disConnectBle();
                                    } else {
                                        ToastUtils.showToast(this, "Setup failed");
                                    }
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_NETWORK_STATUS:
                                    if (length == 1) {
                                        int networkStatus = value[4] & 0xFF;
                                        networkFragment.setNetworkStatus(networkStatus);
                                    }
                                    break;
                                case KEY_MQTT_CONNECT_STATUS:
                                    if (length == 1) {
                                        int status = value[4] & 0xFF;
                                        networkFragment.setMqttConnectionStatus(status);
                                    }
                                    break;

                                case KEY_SCAN_REPORT_ENABLE:
                                    if (length == 1) {
                                        scannerFragment.setModeSwitch(value[4] & 0xff);
                                    }
                                    break;

                                case KEY_BATTERY_POWER:
                                    if (length > 0) {
                                        int battery = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                        settingsFragment.setBattery(battery);
                                    }
                                    break;

                                case KEY_POWER_LOSS_NOTIFY:
                                    if (length > 0) {
                                        settingsFragment.setPowerLossNotify(value[4] & 0xff);
                                    }
                                    break;

                                case KEY_AUTO_POWER_ON_ENABLE:
                                    if (length > 0) {
                                        settingsFragment.setPowerChargeNotify(value[4] & 0xff);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void showAlertDialog(String title, String msg, String confirm) {
        AlertMessageDialog dialog = new AlertMessageDialog();
        if (!TextUtils.isEmpty(title)) dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setConfirm(confirm);
        dialog.setCancelGone();
        dialog.setOnAlertConfirmListener(() -> {
            setResult(RESULT_OK);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }

    private void showDisconnectDialog() {
        if (disConnectType == 3) {
            showAlertDialog("Change Password", "Password changed successfully!Please reconnect the device.", "OK");
        } else if (disConnectType == 2) {
            showAlertDialog(null, "No data communication for 10 minutes, the device is disconnected.", "OK");
        } else if (disConnectType == 1) {
            showAlertDialog(null, "The device is disconnected!", "OK");
        } else {
            if (MokoSupport.getInstance().isBluetoothOpen()) {
                showAlertDialog("Dismiss", "The device disconnected!", "Exit");
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        dismissSyncProgressDialog();
                        AlertDialog.Builder builder = new AlertDialog.Builder(MkGw4DeviceInfoActivity.this);
                        builder.setTitle("Dismiss");
                        builder.setCancelable(false);
                        builder.setMessage("The current system of bluetooth is not available!");
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            MkGw4DeviceInfoActivity.this.setResult(RESULT_OK);
                            finish();
                        });
                        builder.show();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        if (isWindowLocked()) return;
        back();
    }

    private void back() {
        MokoSupport.getInstance().disConnectBle();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.radioBtnNetwork) {
            showNetworkAndGetData();
        } else if (checkedId == R.id.radioBtn_position) {
            showPosAndGetData();
        } else if (checkedId == R.id.radioBtn_scanner) {
            showScannerAndGetData();
        } else if (checkedId == R.id.radioBtn_setting) {
            showSettingAndGetData();
        }
    }

    private void showSettingAndGetData() {
        mBind.tvTitle.setText("Settings");
        fragmentManager.beginTransaction()
                .hide(networkFragment)
                .hide(posFragment)
                .hide(scannerFragment)
                .show(settingsFragment)
                .commit();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        // device
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getPowerLossNotify());
        orderTasks.add(OrderTaskAssembler.getAutoPowerOn());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void showScannerAndGetData() {
        mBind.tvTitle.setText("General Settings");
        fragmentManager.beginTransaction()
                .hide(networkFragment)
                .hide(posFragment)
                .show(scannerFragment)
                .hide(settingsFragment)
                .commit();
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getScanReportEnable());
    }

    private void showPosAndGetData() {
        mBind.tvTitle.setText("Position");
        fragmentManager.beginTransaction()
                .hide(networkFragment)
                .show(posFragment)
                .hide(scannerFragment)
                .hide(settingsFragment)
                .commit();
    }

    private void showNetworkAndGetData() {
        mBind.tvTitle.setText(advName);
        fragmentManager.beginTransaction()
                .show(networkFragment)
                .hide(posFragment)
                .hide(scannerFragment)
                .hide(settingsFragment)
                .commit();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getNetworkStatus());
        orderTasks.add(OrderTaskAssembler.getMqttConnectionStatus());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}

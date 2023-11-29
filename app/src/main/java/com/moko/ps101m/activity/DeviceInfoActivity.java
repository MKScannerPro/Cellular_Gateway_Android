package com.moko.ps101m.activity;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.ps101m.AppConstants;
import com.moko.ps101m.R;
import com.moko.ps101m.activity.device.ExportDataActivity;
import com.moko.ps101m.activity.device.IndicatorSettingsActivity;
import com.moko.ps101m.activity.device.OnOffSettingsActivity;
import com.moko.ps101m.activity.device.SystemInfoActivity;
import com.moko.ps101m.activity.setting.AuxiliaryOperationActivity;
import com.moko.ps101m.activity.setting.AxisParameterActivity;
import com.moko.ps101m.activity.setting.BleSettingsActivity;
import com.moko.ps101m.activity.setting.ScanReportModeActivity;
import com.moko.ps101m.databinding.Ps101mActivityDeviceInfoBinding;
import com.moko.ps101m.dialog.AlertMessageDialog;
import com.moko.ps101m.dialog.ChangePasswordDialog;
import com.moko.ps101m.fragment.SettingsFragment;
import com.moko.ps101m.fragment.ScannerFragment;
import com.moko.ps101m.fragment.NetworkFragment;
import com.moko.ps101m.fragment.PositionFragment;
import com.moko.ps101m.utils.ToastUtils;
import com.moko.support.ps101m.MokoSupport;
import com.moko.support.ps101m.OrderTaskAssembler;
import com.moko.support.ps101m.entity.OrderCHAR;
import com.moko.support.ps101m.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceInfoActivity extends PS101BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private Ps101mActivityDeviceInfoBinding mBind;
    private FragmentManager fragmentManager;
    private NetworkFragment networkFragment;
    private PositionFragment posFragment;
    private ScannerFragment scannerFragment;
    private SettingsFragment settingsFragment;
    private boolean mReceiverTag;
    private int disConnectType;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Ps101mActivityDeviceInfoBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        fragmentManager = getSupportFragmentManager();
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
            mBind.frameContainer.postDelayed(() -> {
                List<OrderTask> orderTasks = new ArrayList<>();
                orderTasks.add(OrderTaskAssembler.getNetworkStatus());
                orderTasks.add(OrderTaskAssembler.getMqttConnectionStatus());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            }, 300);
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
        mBind.tvTitle.setText("Network");
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
                                case KEY_HEARTBEAT_INTERVAL:
                                case KEY_LOW_POWER_PERCENT:
                                case KEY_TIME_ZONE:
                                case KEY_BUZZER_SOUND_CHOOSE:
                                case KEY_VIBRATION_INTENSITY:
                                case KEY_LOW_POWER_PAYLOAD_ENABLE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(this, "Setup failed");
                                    } else {
                                        ToastUtils.showToast(this, "Setup succeed");
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

                                case KEY_UPLOAD_PRIORITY:
                                    if (length > 0) {
                                        scannerFragment.setUpLoadPriority(value[4] & 0xff);
                                    }
                                    break;
                                case KEY_LOW_POWER_PAYLOAD_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        settingsFragment.setLowPowerPayload(enable);
                                    }
                                    break;

                                case KEY_LOW_POWER_PERCENT:
                                    if (length > 0) {
                                        int lowPower = value[4] & 0xFF;
                                        settingsFragment.setLowPower(lowPower);
                                    }
                                    break;

                                case KEY_BUZZER_SOUND_CHOOSE:
                                    if (length == 1) {
                                        settingsFragment.setBuzzerSound(value[4] & 0xff);
                                    }
                                    break;

                                case KEY_VIBRATION_INTENSITY:
                                    if (length == 1) {
                                        settingsFragment.setVibrationIntensity(value[4] & 0xff);
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
        if (disConnectType == 2) {
            showAlertDialog("Change Password", "Password changed successfully!Please reconnect the device.", "OK");
        } else if (disConnectType == 3) {
            showAlertDialog(null, "No data communication for 3 minutes, the device is disconnected.", "OK");
        } else if (disConnectType == 5) {
            showAlertDialog("Factory Reset", "Factory reset successfully!\nPlease reconnect the device.", "OK");
        } else if (disConnectType == 4) {
            showAlertDialog("Dismiss", "Reboot successfully!\nPlease reconnect the device", "OK");
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
                        builder.setTitle("Dismiss");
                        builder.setCancelable(false);
                        builder.setMessage("The current system of bluetooth is not available!");
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            DeviceInfoActivity.this.setResult(RESULT_OK);
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
        mBind.tvTitle.setText("Device Settings");
        fragmentManager.beginTransaction()
                .hide(networkFragment)
                .hide(posFragment)
                .hide(scannerFragment)
                .show(settingsFragment)
                .commit();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        // device
        orderTasks.add(OrderTaskAssembler.getTimeZone());
        orderTasks.add(OrderTaskAssembler.getLowPowerPercent());
        orderTasks.add(OrderTaskAssembler.getLowPowerPayloadEnable());
        orderTasks.add(OrderTaskAssembler.getBuzzerSoundChoose());
        orderTasks.add(OrderTaskAssembler.getVibrationIntensity());
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
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getScanReportEnable());
        orderTasks.add(OrderTaskAssembler.getUploadPriority());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
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
        mBind.tvTitle.setText("Network");
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

    public void onChangePassword(View view) {
        if (isWindowLocked()) return;
        final ChangePasswordDialog dialog = new ChangePasswordDialog(this);
        dialog.setOnPasswordClicked(password -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.changePassword(password));
        });
        dialog.show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(dialog::showKeyboard);
            }
        }, 200);
    }

    public void onLocalDataSync(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, ExportDataActivity.class));
    }

    public void onIndicatorSettings(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, IndicatorSettingsActivity.class));
    }

    public void selectTimeZone(View view) {
        if (isWindowLocked()) return;
        settingsFragment.showTimeZoneDialog();
    }

    public void onLowPowerPayload(View view) {
        if (isWindowLocked()) return;
        settingsFragment.changeLowPowerPayload();
    }

    public void onBuzzer(View view) {
        if (isWindowLocked()) return;
        settingsFragment.showBuzzerDialog();
    }

    public void onVibration(View view) {
        if (isWindowLocked()) return;
        settingsFragment.showVibrationDialog();
    }

    public void selectLowPowerPrompt(View view) {
        if (isWindowLocked()) return;
        settingsFragment.showLowPowerDialog();
    }

    public void onDeviceInfo(View view) {
        if (isWindowLocked()) return;
        launcher.launch(new Intent(this, SystemInfoActivity.class));
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        if (resultCode == RESULT_OK) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Update Firmware");
            dialog.setMessage("Update firmware successfully!\nPlease reconnect the device.");
            dialog.setConfirm("OK");
            dialog.setCancelGone();
            dialog.setOnAlertConfirmListener(() -> {
                setResult(RESULT_OK);
                finish();
            });
            dialog.show(getSupportFragmentManager());
        } else if (resultCode == RESULT_FIRST_USER) {
            if (null == result.getData()) return;
            String mac = result.getData().getStringExtra(AppConstants.EXTRA_KEY_DEVICE_MAC);
            mBind.frameContainer.postDelayed(() -> {
                if (MokoSupport.getInstance().isConnDevice(mac)) {
                    MokoSupport.getInstance().disConnectBle();
                    return;
                }
                showDisconnectDialog();
            }, 500);
        }
    });

    public void onFactoryReset(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Factory Reset!");
        dialog.setMessage("After factory reset,all the data will be reseted to the factory values.");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.restore());
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onOffSetting(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, OnOffSettingsActivity.class);
        startActivity(intent);
    }
}

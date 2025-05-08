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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lib.scanneriot.IoTDMConstants;
import com.moko.lib.scanneriot.dialog.LoginDialog;
import com.moko.lib.scanneriot.utils.IoTDMSPUtils;
import com.moko.lib.scannerui.dialog.AlertMessageDialog;
import com.moko.lib.scannerui.utils.ToastUtils;
import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.R;
import com.moko.mkgw4.databinding.ActivityDeviceInfoMkgw4Binding;
import com.moko.mkgw4.entity.MokoDevice;
import com.moko.mkgw4.fragment.NetworkFragment;
import com.moko.mkgw4.fragment.PositionFragment;
import com.moko.mkgw4.fragment.ScannerFragment;
import com.moko.mkgw4.fragment.SettingsFragment;
import com.moko.mkgw4.net.Urls;
import com.moko.mkgw4.net.entity.CommonResp;
import com.moko.mkgw4.net.entity.LoginEntity;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;
import com.moko.support.mkgw4.entity.OrderCHAR;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;
import okhttp3.RequestBody;

import static com.moko.mkgw4.AppConstants.TYPE_USB;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
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
    private String mSubscribeTopic;
    private String mPublishTopic;
    private int deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceInfoMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        fragmentManager = getSupportFragmentManager();
        advName = getIntent().getStringExtra("advName");
        mac = getIntent().getStringExtra("mac");
        deviceType = getIntent().getIntExtra(AppConstants.DEVICE_TYPE, 0);
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
            List<OrderTask> orderTasks = new ArrayList<>(6);
            orderTasks.add(OrderTaskAssembler.getNetworkStatus());
            orderTasks.add(OrderTaskAssembler.getMqttConnectionStatus());
            orderTasks.add(OrderTaskAssembler.getDeviceType());
            orderTasks.add(OrderTaskAssembler.getMQTTSubscribeTopic());
            orderTasks.add(OrderTaskAssembler.getMQTTPublishTopic());
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
        scannerFragment.setAdvName(advName, deviceType);
        posFragment.setDeviceType(deviceType);
        settingsFragment.setMac(mac, deviceType);
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
                resetTimer();
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
                                case KEY_POWER_ON_METHOD:
                                case KEY_POWER_LOSS_NOTIFY:
                                    ToastUtils.showToast(this, result == 1 ? "Setup succeed" : "Setup failed");
                                    break;
                                case KEY_DELETE_BUFFER_DATA:
                                    ToastUtils.showToast(this, result == 1 ? "Setup succeed" : "Setup failed");
                                    if (deviceType == TYPE_USB) {
                                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getBufferDataCount());
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
                                        networkFragment.setNetworkStatus(networkStatus, deviceType);
                                    }
                                    break;
                                case KEY_MQTT_CONNECT_STATUS:
                                    if (length == 1) {
                                        int status = value[4] & 0xFF;
                                        networkFragment.setMqttConnectionStatus(status);
                                        if (!isTimerStart) startTimer();
                                    }
                                    break;

                                case KEY_DEVICE_MODE:
                                    if (length == 1) {
                                        networkFragment.setCellularType(value[4] & 0xff);
                                        settingsFragment.setCellularType(value[4] & 0xff);
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
                                case KEY_SUBSCRIBE_TOPIC:
                                    mSubscribeTopic = new String(Arrays.copyOfRange(value, 4, value.length));
                                    break;
                                case KEY_PUBLISH_TOPIC:
                                    mPublishTopic = new String(Arrays.copyOfRange(value, 4, value.length));
                                    break;
                                case KEY_BUFFER_DATA_COUNT:
                                    int count = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                    settingsFragment.setBufferDataCount(count);
                                    break;
                                case KEY_POWER_ON_METHOD:
                                    settingsFragment.setPowerOnMethod(value[4]);
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        resetTimer();
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
        if (null != timer) timer.cancel();
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

    private Timer timer;
    private boolean isTimerStart;

    private void startTimer() {
        timer = new Timer();
        isTimerStart = true;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    List<OrderTask> orderTasks = new ArrayList<>(2);
                    orderTasks.add(OrderTaskAssembler.getNetworkStatus());
                    orderTasks.add(OrderTaskAssembler.getMqttConnectionStatus());
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
                });
            }
        }, 3000, 3000);
    }

    public void resetTimer() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        isTimerStart = false;
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
        resetTimer();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(5);
        // device
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getPowerLossNotify());
        orderTasks.add(OrderTaskAssembler.getAutoPowerOn());
        if (deviceType == TYPE_USB) {
            orderTasks.add(OrderTaskAssembler.getBufferDataCount());
            orderTasks.add(OrderTaskAssembler.getPowerOnMethod());
        }
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void showScannerAndGetData() {
        mBind.tvTitle.setText("Scan & Report");
        fragmentManager.beginTransaction()
                .hide(networkFragment)
                .hide(posFragment)
                .show(scannerFragment)
                .hide(settingsFragment)
                .commit();
        resetTimer();
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
        resetTimer();
    }

    private void showNetworkAndGetData() {
        mBind.tvTitle.setText(advName);
        fragmentManager.beginTransaction()
                .show(networkFragment)
                .hide(posFragment)
                .hide(scannerFragment)
                .hide(settingsFragment)
                .commit();
        getNetworkStatus();
    }

    public void getNetworkStatus() {
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.getNetworkStatus());
        orderTasks.add(OrderTaskAssembler.getMqttConnectionStatus());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onUpdateFirmware(int flag) {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Update Firmware");
        dialog.setMessage(flag == 1 ? "Update firmware successfully!\nPlease reconnect the device." : "Error:DFU Failed");
        dialog.setConfirm("OK");
        dialog.setCancelGone();
        dialog.setOnAlertConfirmListener(() -> {
            setResult(RESULT_OK);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }

    public void mainSyncDevices(View view) {
        if (isWindowLocked()) return;
        // 登录
        String account = IoTDMSPUtils.getStringValue(this, IoTDMConstants.EXTRA_KEY_LOGIN_ACCOUNT, "");
        String password = IoTDMSPUtils.getStringValue(this, IoTDMConstants.EXTRA_KEY_LOGIN_PASSWORD, "");
        int env = IoTDMSPUtils.getIntValue(this, IoTDMConstants.EXTRA_KEY_LOGIN_ENV, 0);
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            LoginDialog dialog = new LoginDialog();
            dialog.setOnLoginClicked(this::login);
            dialog.show(getSupportFragmentManager());
            return;
        }
        login(account, password, env);
    }

    private void login(String account, String password, int envValue) {
        LoginEntity entity = new LoginEntity();
        entity.username = account;
        entity.password = password;
        entity.source = 1;
        if (envValue == 0)
            Urls.setCloudEnv(getApplicationContext());
        else
            Urls.setTestEnv(getApplicationContext());
        RequestBody body = RequestBody.create(Urls.JSON, new Gson().toJson(entity));
        OkGo.<String>post(Urls.loginApi(getApplicationContext()))
                .upRequestBody(body)
                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        showLoadingProgressDialog();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        Type type = new TypeToken<CommonResp<JsonObject>>() {
                        }.getType();
                        CommonResp<JsonObject> commonResp = new Gson().fromJson(response.body(), type);
                        if (commonResp.code != 200) {
                            ToastUtils.showToast(DeviceInfoActivity.this, commonResp.msg);
                            LoginDialog dialog = new LoginDialog();
                            dialog.setOnLoginClicked((account1, password1, env) -> login(account1, password1, env));
                            dialog.show(getSupportFragmentManager());
                            return;
                        }
                        // add header
                        String accessToken = commonResp.data.get("access_token").getAsString();
                        HttpHeaders headers = new HttpHeaders();
                        headers.put("Authorization", accessToken);
                        OkGo.getInstance().addCommonHeaders(headers);

                        IoTDMSPUtils.setStringValue(DeviceInfoActivity.this, IoTDMConstants.EXTRA_KEY_LOGIN_ACCOUNT, account);
                        IoTDMSPUtils.setStringValue(DeviceInfoActivity.this, IoTDMConstants.EXTRA_KEY_LOGIN_PASSWORD, password);
                        IoTDMSPUtils.setIntValue(DeviceInfoActivity.this, IoTDMConstants.EXTRA_KEY_LOGIN_ENV, envValue);

                        String macUpper = mac.replaceAll(":", "").toUpperCase();
                        MokoDevice mokoDevice = new MokoDevice();
                        mokoDevice.name = String.format("MKGW4-%s", macUpper.substring(8));
                        mokoDevice.mac = macUpper.toLowerCase();
                        mokoDevice.topicSubscribe = mSubscribeTopic;
                        mokoDevice.topicPublish = mPublishTopic;
                        mokoDevice.lwtTopic = "";
                        Intent intent = new Intent(DeviceInfoActivity.this, SyncDeviceActivity.class);
                        intent.putExtra("mokoDevice", mokoDevice);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        ToastUtils.showToast(DeviceInfoActivity.this, R.string.request_error);
                        LoginDialog dialog = new LoginDialog();
                        dialog.setOnLoginClicked((account12, password12, env) -> login(account12, password12, env));
                        dialog.show(getSupportFragmentManager());
                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingProgressDialog();
                    }
                });
    }
}

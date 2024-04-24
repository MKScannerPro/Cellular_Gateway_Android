package com.moko.mkgw4.activity.setting;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivitySystemInfoMkgw4Binding;
import com.moko.mkgw4.service.MkGw4DfuService;
import com.moko.mkgw4.utils.FileUtils;
import com.moko.mkgw4.utils.ToastUtils;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;
import com.moko.support.mkgw4.entity.OrderCHAR;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class MkGw4SystemInfoActivity extends MkGw4BaseActivity {
    private ActivitySystemInfoMkgw4Binding mBind;
    private boolean mReceiverTag = false;
    private String mDeviceMac;
    private String mDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySystemInfoMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getAdvName());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getMacAddress());
        orderTasks.add(OrderTaskAssembler.getIMEI());
        orderTasks.add(OrderTaskAssembler.getIccId());

        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_MODEL_NUMBER:
                        String productModel = new String(value);
                        mBind.tvProdutMode.setText(productModel);
                        break;
                    case CHAR_SOFTWARE_REVISION:
                        String softwareVersion = new String(value);
                        mBind.tvSoftwareVersion.setText(softwareVersion);
                        break;
                    case CHAR_FIRMWARE_REVISION:
                        String firmwareVersion = new String(value);
                        mBind.tvFirmwareVersion.setText(firmwareVersion);
                        break;
                    case CHAR_HARDWARE_REVISION:
                        String hardwareVersion = new String(value);
                        mBind.tvHardwareVersion.setText(hardwareVersion);
                        break;
                    case CHAR_MANUFACTURER_NAME:
                        String manufacture = new String(value);
                        mBind.tvManufacturer.setText(manufacture);
                        break;
                    case CHAR_PARAMS:
                        if (value.length >= 4) {
                            int header = value[0] & 0xFF;// 0xED
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = value[2] & 0xFF;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (header != 0xED || configKeyEnum == null) return;
                            int length = value[3] & 0xFF;
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_ADV_NAME:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            mDeviceName = new String(rawDataBytes);
                                            mBind.tvDeviceName.setText(mDeviceName);
                                        }
                                        break;
                                    case KEY_CHIP_MAC:
                                        if (length > 0) {
                                            byte[] macBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            String mac = MokoUtils.bytesToHexString(macBytes);
                                            StringBuilder builder = new StringBuilder(mac);
                                            builder.insert(2, ":");
                                            builder.insert(5, ":");
                                            builder.insert(8, ":");
                                            builder.insert(11, ":");
                                            builder.insert(14, ":");
                                            mDeviceMac = builder.toString().toUpperCase();
                                            mBind.tvMac.setText(mDeviceMac);
                                        }
                                        break;

                                    case KEY_IMEI:
                                        if (length > 0) {
                                            mBind.tvImei.setText(new String(Arrays.copyOfRange(value, 4, value.length)).toUpperCase());
                                        }
                                        break;

                                    case KEY_ICC_ID:
                                        if (length > 0) {
                                            mBind.tvIccId.setText(new String(Arrays.copyOfRange(value, 4, value.length)).toUpperCase());
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        });
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
                        finish();
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
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        EventBus.getDefault().unregister(this);
        finish();
    }

    public void onUpdateFirmware(View view) {
        if (isWindowLocked()) return;
        if (TextUtils.isEmpty(mDeviceName) || TextUtils.isEmpty(mDeviceMac)) return;
        launcher.launch("*/*");
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        Intent intent = new Intent();
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE_MAC, mDeviceMac);
        setResult(RESULT_FIRST_USER, intent);
        backHome();
    }

    private boolean isUpgrade;
    private int mDeviceConnectCount;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(@NonNull String deviceAddress) {
            XLog.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                ToastUtils.showToast(MkGw4SystemInfoActivity.this, "Error:DFU Failed");
                dismissDFUProgressDialog();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(MkGw4SystemInfoActivity.this);
                final Intent abortAction = new Intent(MkGw4DfuService.BROADCAST_ACTION);
                abortAction.putExtra(MkGw4DfuService.EXTRA_ACTION, MkGw4DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            XLog.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(@NonNull String deviceAddress) {
            isUpgrade = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }

        @Override
        public void onEnablingDfuMode(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(@NonNull String deviceAddress) {
            mDeviceConnectCount = 0;
            if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
                mDFUDialog.dismiss();
            }
            ToastUtils.showToast(MkGw4SystemInfoActivity.this, "DFU successful.");
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void onDfuAborted(@NonNull String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(@NonNull String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            XLog.i("Progress:" + percent + "%");
            mDFUDialog.setMessage("Progress：" + percent + "%");
        }

        @Override
        public void onError(@NonNull String deviceAddress, int error, int errorType, String message) {
            ToastUtils.showToast(MkGw4SystemInfoActivity.this, "Opps!DFU Failed. Please try again!");
            XLog.i("Error:" + message);
            dismissDFUProgressDialog();
        }
    };

    private final ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if (null == result) return;
        String firmwareFilePath = FileUtils.getPath(this, result);
        if (TextUtils.isEmpty(firmwareFilePath)) return;
        final File firmwareFile = new File(firmwareFilePath);
        if (!firmwareFile.exists() || !firmwareFilePath.toLowerCase().endsWith("zip") || firmwareFile.length() == 0) {
            ToastUtils.showToast(this, "File error!");
            return;
        }
        final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                .setDeviceName(mDeviceName)
                .setKeepBond(false)
                .setForeground(true)
                .setDisableNotification(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(this);
        }
        starter.setZip(null, firmwareFilePath);
        starter.start(this, MkGw4DfuService.class);
        showDFUProgressDialog("Waiting...");
    });
}

package com.moko.lw006.activity.setting;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityDeviceModeBinding;
import com.moko.lw006.dialog.BottomDialog;
import com.moko.lw006.utils.ToastUtils;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.OrderTaskAssembler;
import com.moko.support.lw006.entity.OrderCHAR;
import com.moko.support.lw006.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class DeviceModeActivity extends Lw006BaseActivity {
    private Lw006ActivityDeviceModeBinding mBind;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;
    private ArrayList<String> mValues;
    private int mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityDeviceModeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mValues = new ArrayList<>();
        mValues.add("Standby Mode");
        mValues.add("Timing Mode");
        mValues.add("Periodic Mode");
        mValues.add("Motion Mode");
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDeviceMode());
        mBind.tvStandbyMode.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            startActivity(new Intent(this, StandbyModeActivity.class));
        });
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
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xED) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            if (configKeyEnum == ParamsKeyEnum.KEY_DEVICE_MODE) {
                                if (result != 1) {
                                    savedParamsError = true;
                                }
                                if (savedParamsError) {
                                    ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                } else {
                                    ToastUtils.showToast(this, "Save Successfully！");
                                }
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_DEVICE_MODE) {
                                if (length > 0) {
                                    mSelected = value[4] & 0xff;
                                    mBind.tvDeviceMode.setText(mValues.get(mSelected));
                                }
                            }
                        }
                    }
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
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        setResult(RESULT_OK);
        finish();
    }

    public void selectDeviceMode(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            mBind.tvDeviceMode.setText(mValues.get(value));
            savedParamsError = false;
            showSyncingProgressDialog();
            LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setDeviceMode(value));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onTimingMode(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, TimingModeActivity.class));
    }

    public void onPeriodicMode(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, PeriodicModeActivity.class));
    }

    public void onMotionMode(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, MotionModeActivity.class));
    }
}

package com.moko.mkgw4.activity.setting;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.databinding.ActivitySyncTimeFromNtpMkgw4Binding;
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

public class SyncTimeFromNtpActivity extends BaseActivity {
    private ActivitySyncTimeFromNtpMkgw4Binding mBind;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;
    private final String FILTER_ASCII = "[ -~]*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySyncTimeFromNtpMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        initData();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getNtpEnable());
        orderTasks.add(OrderTaskAssembler.getNtpSyncInterval());
        orderTasks.add(OrderTaskAssembler.getNtpServer());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }

    private void initData() {
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }
            return null;
        };
        mBind.etServer.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64), inputFilter});
        mBind.cbSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.group.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        mBind.ivSave.setOnClickListener(v -> save());
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
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
                                case KEY_NTP_SERVER:
                                case KEY_NTP_SYNC_INTERVAL:
                                    if (result != 1) savedParamsError = true;
                                    break;

                                case KEY_NTP_SWITCH:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_NTP_SWITCH:
                                    if (length == 1) {
                                        mBind.cbSwitch.setChecked((value[4] & 0xff) == 1);
                                    }
                                    break;

                                case KEY_NTP_SYNC_INTERVAL:
                                    if (length > 0) {
                                        int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etInterval.setText(String.valueOf(interval));
                                        mBind.etInterval.setSelection(mBind.etInterval.getText().length());
                                    }
                                    break;

                                case KEY_NTP_SERVER:
                                    if (length > 0) {
                                        mBind.etServer.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        mBind.etServer.setSelection(mBind.etServer.getText().length());
                                    }
                                    break;
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

    private void save() {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            savedParamsError = false;
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.setNtpEnable(mBind.cbSwitch.isChecked() ? 1 : 0));
            if (mBind.cbSwitch.isChecked()) {
                int interval = Integer.parseInt(mBind.etInterval.getText().toString());
                orderTasks.add(OrderTaskAssembler.setNtpSyncInterval(interval));
                String server = TextUtils.isEmpty(mBind.etServer.getText()) ? null : mBind.etServer.getText().toString();
                orderTasks.add(OrderTaskAssembler.setNtpServer(server));
            }
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (mBind.cbSwitch.isChecked()) {
            if (TextUtils.isEmpty(mBind.etInterval.getText())) return false;
            int interval = Integer.parseInt(mBind.etInterval.getText().toString());
            return interval >= 1 && interval <= 720;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        if (EventBus.getDefault().isRegistered(this))
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
        EventBus.getDefault().unregister(this);
        finish();
    }
}

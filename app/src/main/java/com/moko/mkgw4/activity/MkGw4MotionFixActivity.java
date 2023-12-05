package com.moko.mkgw4.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.databinding.ActivityMotionFixMkgw4Binding;
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

/**
 *
 * @author: jun.liu
 * @date: 2023/11/28 10:32
 * @des:
 */
public class MkGw4MotionFixActivity extends MkGw4BaseActivity {
    private ActivityMotionFixMkgw4Binding mBind;
    private boolean mSavedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMotionFixMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getMotionFixEnableStart());
        orderTasks.add(OrderTaskAssembler.getMotionFixEnableInTrip());
        orderTasks.add(OrderTaskAssembler.getMotionFixIntervalInTrip());
        orderTasks.add(OrderTaskAssembler.getMotionFixEnableStop());
        orderTasks.add(OrderTaskAssembler.getMotionFixTimeoutStop());
        orderTasks.add(OrderTaskAssembler.getMotionFixEnableStationary());
        orderTasks.add(OrderTaskAssembler.getMotionFixIntervalStationary());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        mBind.tvBack.setOnClickListener(v -> finish());
        mBind.ivSave.setOnClickListener(v -> onSave());
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
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (null != value && value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xED) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_MOTION_FIX_ENABLE_WHEN_START:
                                case KEY_MOTION_FIX_ENABLE_WHEN_TRIP:
                                case KEY_MOTION_FIX_INTERVAL_WHEN_TRIP:
                                case KEY_MOTION_FIX_ENABLE_WHEN_STOP:
                                case KEY_MOTION_FIX_TIMEOUT_WHEN_STOP:
                                case KEY_MOTION_FIX_ENABLE_WHEN_STATIONARY:
                                    if (result != 1) {
                                        mSavedParamsError = true;
                                    }
                                    break;
                                case KEY_MOTION_FIX_INTERVAL_WHEN_STATIONARY:
                                    if (result != 1) {
                                        mSavedParamsError = true;
                                    }
                                    if (mSavedParamsError) {
                                        ToastUtils.showToast(this, "Setup failed！");
                                    } else {
                                        ToastUtils.showToast(this, "Setup succeed！");
                                    }
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (length == 0) return;
                            switch (configKeyEnum) {
                                case KEY_MOTION_FIX_ENABLE_WHEN_START:
                                    mBind.cbStartFix.setChecked((value[4] & 0xff) == 1);
                                    break;

                                case KEY_MOTION_FIX_ENABLE_WHEN_TRIP:
                                    mBind.cbFixInTrip.setChecked((value[4] & 0xff) == 1);
                                    break;

                                case KEY_MOTION_FIX_INTERVAL_WHEN_TRIP:
                                    int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                    mBind.etInTripInterval.setText(String.valueOf(interval));
                                    mBind.etInTripInterval.setSelection(mBind.etInTripInterval.getText().length());
                                    break;

                                case KEY_MOTION_FIX_ENABLE_WHEN_STOP:
                                    mBind.cbFixStops.setChecked((value[4] & 0xff) == 1);
                                    break;

                                case KEY_MOTION_FIX_TIMEOUT_WHEN_STOP:
                                    int timeout = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                    mBind.etStopTimeout.setText(String.valueOf(timeout));
                                    mBind.etStopTimeout.setSelection(mBind.etStopTimeout.getText().length());
                                    break;

                                case KEY_MOTION_FIX_ENABLE_WHEN_STATIONARY:
                                    mBind.cbFixStationary.setChecked((value[4] & 0xff) == 1);
                                    break;

                                case KEY_MOTION_FIX_INTERVAL_WHEN_STATIONARY:
                                    int stationaryInterval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                    mBind.etStationaryInterval.setText(String.valueOf(stationaryInterval));
                                    mBind.etStationaryInterval.setSelection(mBind.etStationaryInterval.getText().length());
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void onSave() {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setMotionFixEnableStart(mBind.cbStartFix.isChecked() ? 1 : 0));
            orderTasks.add(OrderTaskAssembler.setMotionFixEnableTrip(mBind.cbFixInTrip.isChecked() ? 1 : 0));
            if (mBind.cbFixInTrip.isChecked()) {
                int interval = Integer.parseInt(mBind.etInTripInterval.getText().toString().trim());
                orderTasks.add(OrderTaskAssembler.setMotionFixIntervalTrip(interval));
            }
            orderTasks.add(OrderTaskAssembler.setMotionFixEnableStop(mBind.cbFixStops.isChecked() ? 1 : 0));
            if (mBind.cbFixStops.isChecked()) {
                int timeout = Integer.parseInt(mBind.etStopTimeout.getText().toString().trim());
                orderTasks.add(OrderTaskAssembler.setMotionFixTimeoutStop(timeout));
            }
            orderTasks.add(OrderTaskAssembler.setMotionFixEnableStationary(mBind.cbFixStationary.isChecked() ? 1 : 0));
            if (mBind.cbFixStationary.isChecked()) {
                int interval = Integer.parseInt(mBind.etStationaryInterval.getText().toString().trim());
                orderTasks.add(OrderTaskAssembler.setMotionFixIntervalStationary(interval));
            }
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (mBind.cbFixInTrip.isChecked()) {
            if (TextUtils.isEmpty(mBind.etInTripInterval.getText())) return false;
            int interval = Integer.parseInt(mBind.etInTripInterval.getText().toString().trim());
            if (interval < 10 || interval > 86400) return false;
        }
        if (mBind.cbFixStops.isChecked()) {
            if (TextUtils.isEmpty(mBind.etStopTimeout.getText())) return false;
            int timeout = Integer.parseInt(mBind.etStopTimeout.getText().toString().trim());
            if (timeout < 3 || timeout > 180) return false;
        }
        if (mBind.cbFixStationary.isChecked()) {
            if (TextUtils.isEmpty(mBind.etStationaryInterval.getText())) return false;
            int interval = Integer.parseInt(mBind.etStationaryInterval.getText().toString().trim());
            return interval >= 1 && interval <= 1440;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

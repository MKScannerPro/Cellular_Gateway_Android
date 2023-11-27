package com.moko.lw006.activity.device;

import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivitySelftestBinding;
import com.moko.lw006.dialog.AlertMessageDialog;
import com.moko.lw006.dialog.BottomDialog;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.OrderTaskAssembler;
import com.moko.support.lw006.entity.OrderCHAR;
import com.moko.support.lw006.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelfTestActivity extends Lw006BaseActivity {
    private Lw006ActivitySelftestBinding mBind;
    private final ArrayList<String> mValues = new ArrayList<>(2);
    private int mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivitySelftestBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mValues.add("Traditional GPS module");
        mValues.add("Lora Cloud");
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getSelfTestStatus());
        orderTasks.add(OrderTaskAssembler.getPCBAStatus());
        orderTasks.add(OrderTaskAssembler.getGpsModule());
        orderTasks.add(OrderTaskAssembler.getBatteryInfo());
        orderTasks.add(OrderTaskAssembler.getMotorState());
        orderTasks.add(OrderTaskAssembler.getHwVersion());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

        mBind.tvGpsType.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(mValues, mSelected);
            dialog.setListener(value -> {
                mSelected = value;
                showSyncingProgressDialog();
                List<OrderTask> orderTask = new ArrayList<>();
                orderTask.add(OrderTaskAssembler.setGpsModule(value));
                orderTask.add(OrderTaskAssembler.getGpsModule());
                LoRaLW006MokoSupport.getInstance().sendOrder(orderTask.toArray(new OrderTask[]{}));
            });
            dialog.show(getSupportFragmentManager());
        });
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
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
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
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            if (configKeyEnum == ParamsKeyEnum.KEY_BATTERY_RESET || configKeyEnum == ParamsKeyEnum.KEY_RESET_MOTOR_STATE) {
                                if (result == 1) {
                                    AlertMessageDialog dialog = new AlertMessageDialog();
                                    dialog.setMessage("Reset Successfully！");
                                    dialog.setConfirm("OK");
                                    dialog.setCancelGone();
                                    dialog.show(getSupportFragmentManager());
                                }
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_SELFTEST_STATUS:
                                    if (length > 0) {
                                        int status = value[4] & 0xFF;
                                        mBind.tvSelftestStatus.setVisibility(status == 0 ? View.VISIBLE : View.GONE);
                                        if ((status & 0x01) == 0x01)
                                            mBind.tvGpsStatus.setVisibility(View.VISIBLE);
                                        if ((status & 0x02) == 0x02)
                                            mBind.tvAxisStatus.setVisibility(View.VISIBLE);
                                        if ((status & 0x04) == 0x04)
                                            mBind.tvFlashStatus.setVisibility(View.VISIBLE);
                                    }
                                    break;
                                case KEY_PCBA_STATUS:
                                    if (length > 0) {
                                        mBind.tvPcbaStatus.setText(String.valueOf(value[4] & 0xFF));
                                    }
                                    break;
                                case KEY_BATTERY_INFO:
                                    if (length == 36) {
                                        int runtime = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 8));
                                        mBind.tvRuntime.setText(String.format("%d s", runtime));
                                        int advTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 8, 12));
                                        mBind.tvAdvTimes.setText(String.format("%d times", advTimes));
                                        int flashTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 12, 16));
                                        mBind.tvFlashTimes.setText(String.format("%d times", flashTimes));
                                        int axisDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 16, 20));
                                        mBind.tvAxisDuration.setText(String.format("%d ms", axisDuration));
                                        int bleFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 20, 24));
                                        mBind.tvBleFixDuration.setText(String.format("%d ms", bleFixDuration));
                                        int wifiFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 24, 28));
                                        mBind.tvWifiFixDuration.setText(String.format("%d ms", wifiFixDuration));
                                        int gpsFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 28, 32));
                                        mBind.tvGpsFixDuration.setText(String.format("%d s", gpsFixDuration));
                                        int loraTransmissionTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 32, 36));
                                        mBind.tvLoraTransmissionTimes.setText(String.format("%d times", loraTransmissionTimes));
                                        int loraPower = MokoUtils.toInt(Arrays.copyOfRange(value, 36, 40));
                                        mBind.tvLoraPower.setText(String.format("%d mAS", loraPower));
                                    }
                                    break;

                                case KEY_GPS_MODULE:
                                    if (length == 1) {
                                        mSelected = value[4] & 0xff;
                                        mBind.tvGpsType.setText(mValues.get(mSelected));
                                    }
                                    break;

                                case KEY_MOTOR_STATE:
                                    //马达异常状态
                                    if (length == 1) {
                                        int result = value[4] & 0xff;
                                        mBind.tvMotorState.setText(result == 0 ? "Normal" : "Fault");
                                    }
                                    break;

                                case KEY_HARDWARE_VERSION:
                                    if (length == 1) {
                                        int result = value[4] & 0xff;
                                        mBind.tvHwVersion.setText(result == 0 ? "No" : "Traditional GPS module Supported");
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onBatteryReset(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning！");
        dialog.setMessage("Are you sure to reset battery?");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setBatteryReset());
            orderTasks.add(OrderTaskAssembler.getBatteryInfo());
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
    }

    /**
     * 重置马达状态
     *
     * @param view
     */
    public void resetMotorState(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning！");
        dialog.setMessage("Are you sure to reset motor state?");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.resetMotorState());
            orderTasks.add(OrderTaskAssembler.getMotorState());
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        finish();
    }
}

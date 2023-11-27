package com.moko.ps101m.activity.device;

import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ps101m.activity.PS101BaseActivity;
import com.moko.ps101m.databinding.Ps101mActivitySelftestBinding;
import com.moko.ps101m.dialog.AlertMessageDialog;
import com.moko.support.ps101m.MokoSupport;
import com.moko.support.ps101m.OrderTaskAssembler;
import com.moko.support.ps101m.entity.OrderCHAR;
import com.moko.support.ps101m.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SelfTestActivity extends PS101BaseActivity {
    private Ps101mActivitySelftestBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Ps101mActivitySelftestBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getSelfTestStatus());
        orderTasks.add(OrderTaskAssembler.getPCBAStatus());
        orderTasks.add(OrderTaskAssembler.getMotorState());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
                    if (null != value && value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            if (configKeyEnum == ParamsKeyEnum.KEY_RESET_MOTOR_STATE) {
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
                                        if ((status & 8) == 8) {
                                            mBind.tvNbStatus.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    break;
                                case KEY_PCBA_STATUS:
                                    if (length > 0) {
                                        mBind.tvPcbaStatus.setText(String.valueOf(value[4] & 0xFF));
                                    }
                                    break;

                                case KEY_MOTOR_STATE:
                                    //马达异常状态
                                    if (length == 1) {
                                        int result = value[4] & 0xff;
                                        mBind.tvMotorState.setText(result == 0 ? "Normal" : "Fault");
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

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
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        finish();
    }
}

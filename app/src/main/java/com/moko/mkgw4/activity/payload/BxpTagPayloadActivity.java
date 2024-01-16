package com.moko.mkgw4.activity.payload;

import android.os.Bundle;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityBxpTagPayloadBinding;
import com.moko.mkgw4.utils.ToastUtils;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;
import com.moko.support.mkgw4.entity.OrderCHAR;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

/**
 * @author: jun.liu
 * @date: 2023/12/1 10:25
 * @des:
 */
public class BxpTagPayloadActivity extends MkGw4BaseActivity {
    private ActivityBxpTagPayloadBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityBxpTagPayloadBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getBxpTagPayload());
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
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
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
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            if (configKeyEnum == ParamsKeyEnum.KEY_BXP_TAG_PAYLOAD) {
                                ToastUtils.showToast(this, (value[4] & 0xff) == 1 ? "Setup succeed" : "Setup failed");
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_BXP_TAG_PAYLOAD && length == 2) {
                                int data = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                mBind.cbRssi.setChecked((data & 0x01) == 1);
                                mBind.cbTimestamp.setChecked((data >> 1 & 0x01) == 1);
                                mBind.cbSensorStatus.setChecked((data >> 2 & 0x01) == 1);
                                mBind.cbHallTriggerCount.setChecked((data >> 3 & 0x01) == 1);
                                mBind.cbMotionTriggerCount.setChecked((data >> 4 & 0x01) == 1);
                                mBind.cbAxisData.setChecked((data >> 5 & 0x01) == 1);
                                mBind.cbBattery.setChecked((data >> 6 & 0x01) == 1);
                                mBind.cbTagId.setChecked((data >> 7 & 0x01) == 1);
                                mBind.cbDeviceName.setChecked((data >> 8 & 0x01) == 1);
                                mBind.cbRawDataAdv.setChecked((data >> 9 & 0x01) == 1);
                                mBind.cbRawDataRes.setChecked((data >> 10 & 0x01) == 1);
                            }
                        }
                    }
                }
            }
        });
    }

    private void onSave() {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        int payload = (mBind.cbRssi.isChecked() ? 1 : 0) | (mBind.cbTimestamp.isChecked() ? 1 << 1 : 0) |
                (mBind.cbSensorStatus.isChecked() ? 1 << 2 : 0) | (mBind.cbHallTriggerCount.isChecked() ? 1 << 3 : 0) |
                (mBind.cbMotionTriggerCount.isChecked() ? 1 << 4 : 0) | (mBind.cbAxisData.isChecked() ? 1 << 5 : 0) |
                (mBind.cbBattery.isChecked() ? 1 << 6 : 0) | (mBind.cbTagId.isChecked() ? 1 << 7 : 0) |
                (mBind.cbDeviceName.isChecked() ? 1 << 8 : 0) | (mBind.cbRawDataAdv.isChecked() ? 1 << 9 : 0) |
                (mBind.cbRawDataRes.isChecked() ? 1 << 10 : 0);
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setBxpTagPayload(payload));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}

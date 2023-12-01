package com.moko.ps101m.activity.payload;

import android.os.Bundle;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.ps101m.activity.PS101BaseActivity;
import com.moko.ps101m.databinding.ActivityBxpAccPayloadBinding;
import com.moko.ps101m.utils.ToastUtils;
import com.moko.support.ps101m.MokoSupport;
import com.moko.support.ps101m.OrderTaskAssembler;
import com.moko.support.ps101m.entity.OrderCHAR;
import com.moko.support.ps101m.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

/**
 * @author: jun.liu
 * @date: 2023/11/30 20:08
 * @des:
 */
public class BxpAccPayloadActivity extends PS101BaseActivity {
    private ActivityBxpAccPayloadBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityBxpAccPayloadBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getBxpAccPayload());
        mBind.tvBack.setOnClickListener(v -> finish());
        mBind.ivSave.setOnClickListener(v -> onSave());
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_BXP_ACC_PAYLOAD) {
                                ToastUtils.showToast(this, (value[4] & 0xff) == 1 ? "Setup succeed" : "Setup failed");
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_BXP_ACC_PAYLOAD && length == 2) {
                                int data = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                mBind.cbRssi.setChecked((data & 0x01) == 1);
                                mBind.cbTimestamp.setChecked((data >> 1 & 0x01) == 1);
                                mBind.cbTxPower.setChecked((data >> 2 & 0x01) == 1);
                                mBind.cbRangingData.setChecked((data >> 3 & 0x01) == 1);
                                mBind.cbAdvInterval.setChecked((data >> 4 & 0x01) == 1);
                                mBind.cbSampleRate.setChecked((data >> 5 & 0x01) == 1);
                                mBind.cbFullScale.setChecked((data >> 6 & 0x01) == 1);
                                mBind.cbMotionThreshold.setChecked((data >> 7 & 0x01) == 1);
                                mBind.cbAxisData.setChecked((data >> 8 & 0x01) == 1);
                                mBind.cbBattery.setChecked((data >> 9 & 0x01) == 1);
                                mBind.cbRawDataAdv.setChecked((data >> 10 & 0x01) == 1);
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
                (mBind.cbTxPower.isChecked() ? 1 << 2 : 0) | (mBind.cbRangingData.isChecked() ? 1 << 3 : 0) |
                (mBind.cbAdvInterval.isChecked() ? 1 << 4 : 0) | (mBind.cbSampleRate.isChecked() ? 1 << 5 : 0) |
                (mBind.cbFullScale.isChecked() ? 1 << 6 : 0) | (mBind.cbMotionThreshold.isChecked() ? 1 << 7 : 0) |
                (mBind.cbAxisData.isChecked() ? 1 << 8 : 0) | (mBind.cbBattery.isChecked() ? 1 << 9 : 0) |
                (mBind.cbRawDataAdv.isChecked() ? 1 << 10 : 0);
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setBxpAccPayload(payload));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}

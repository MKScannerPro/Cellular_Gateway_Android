package com.moko.mkgw4.activity.payload;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lib.scannerui.utils.ToastUtils;
import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.databinding.ActivityPayloadSettingsMkgw4Binding;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;
import com.moko.support.mkgw4.entity.OrderCHAR;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.moko.mkgw4.AppConstants.TYPE_USB;

/**
 * @author: jun.liu
 * @date: 2023/11/30 16:46
 * @des:
 */
public class PayloadSettingsActivity extends BaseActivity {
    private ActivityPayloadSettingsMkgw4Binding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityPayloadSettingsMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        int deviceType = getIntent().getIntExtra(AppConstants.DEVICE_TYPE, 0);
        if (deviceType == TYPE_USB) {
            mBind.ivSave.setVisibility(View.VISIBLE);
            mBind.tvBxpSensorPayload.setVisibility(View.VISIBLE);
            mBind.tvCommonItems.setVisibility(View.VISIBLE);
            mBind.cbBeaconNum.setVisibility(View.VISIBLE);
            mBind.cbSequenceNum.setVisibility(View.VISIBLE);
            mBind.ivSave.setOnClickListener(v -> onSave());
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getScanPayloadParams());
            mBind.tvBxpSensorPayload.setOnClickListener(v -> startActivity(BxpSensorPayloadActivity.class));
        }
        mBind.tvBack.setOnClickListener(v -> finish());
        mBind.tvIBeaconPayload.setOnClickListener(v -> startActivity(IBeaconPayloadActivity.class));
        mBind.tvUidPayload.setOnClickListener(v -> startActivity(EddystoneUidPayloadActivity.class));
        mBind.tvUrlPayload.setOnClickListener(v -> startActivity(EddystoneUrlPayloadActivity.class));
        mBind.tvTlmPayload.setOnClickListener(v -> startActivity(EddystoneTlmPayloadActivity.class));
        mBind.tvDeviceInfoPayload.setOnClickListener(v -> startActivity(BxpDeviceInfoPayloadActivity.class));
        mBind.tvAccPayload.setOnClickListener(v -> startActivity(BxpAccPayloadActivity.class));
        mBind.tvThPayload.setOnClickListener(v -> startActivity(BxpThPayloadActivity.class));
        mBind.tvBxpButtonPayload.setOnClickListener(v -> startActivity(BxpButtonPayloadActivity.class));
        mBind.tvBxpTagPayload.setOnClickListener(v -> startActivity(BxpTagPayloadActivity.class));
        mBind.tvPirPayload.setOnClickListener(v -> startActivity(PirPayloadActivity.class));
        mBind.tvTofPayload.setOnClickListener(v -> startActivity(MkTofPayloadActivity.class));
        mBind.tvOtherPayload.setOnClickListener(v -> startActivity(OtherPayloadActivity.class));

    }

    private void startActivity(Class<?> clz) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, clz);
        startActivity(intent);
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
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        if (flag == 0x01) {
                            // write
                            if (configKeyEnum == ParamsKeyEnum.KEY_SCAN_PAYLOAD_PARAMS) {
                                ToastUtils.showToast(this, (value[4] & 0xff) == 1 ? "Setup succeed" : "Setup failed");
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_SCAN_PAYLOAD_PARAMS) {
                                int data = value[4] & 0xff;
                                mBind.cbBeaconNum.setChecked((data & 0x01) == 1);
                                mBind.cbSequenceNum.setChecked((data >> 1 & 0x01) == 1);
                            }
                        }
                    }
                }
            }
        });
    }

    private void onSave() {
        showSyncingProgressDialog();
        int params = (mBind.cbBeaconNum.isChecked() ? 1 : 0) | (mBind.cbSequenceNum.isChecked() ? 1 << 1 : 0);
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setScanPayloadParams(params));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}

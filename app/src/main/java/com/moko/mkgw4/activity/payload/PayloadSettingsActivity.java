package com.moko.mkgw4.activity.payload;

import android.content.Intent;
import android.os.Bundle;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityPayloadSettingsBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author: jun.liu
 * @date: 2023/11/30 16:46
 * @des:
 */
public class PayloadSettingsActivity extends MkGw4BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPayloadSettingsBinding mBind = ActivityPayloadSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

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
        EventBus.getDefault().register(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}

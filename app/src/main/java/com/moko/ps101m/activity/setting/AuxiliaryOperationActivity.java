package com.moko.ps101m.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ps101m.activity.PS101BaseActivity;
import com.moko.ps101m.databinding.Ps101mActivityAuxiliaryOperationBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AuxiliaryOperationActivity extends PS101BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ps101mActivityAuxiliaryOperationBinding mBind = Ps101mActivityAuxiliaryOperationBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mBind.tvAxisDataReport.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            startActivity(new Intent(this, ThreeAxisDataReportActivity.class));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        finish();
    }

    public void onDownlinkForPos(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, DownlinkForPosActivity.class));
    }

    public void onManDownDetection(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, ManDownDetectionActivity.class));
    }

    public void onAlarmFunction(View view) {
        if (isWindowLocked()) return;
        startActivity(new Intent(this, AlarmFunctionActivity.class));
    }

}

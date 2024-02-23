package com.moko.mkgw4.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.ble.lib.task.OrderTask;
import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.MkGw4DeviceInfoActivity;
import com.moko.mkgw4.activity.setting.MkGw4LogDataActivity;
import com.moko.mkgw4.activity.setting.MkGw4SystemInfoActivity;
import com.moko.mkgw4.activity.setting.BatteryManagementActivity;
import com.moko.mkgw4.activity.setting.BleParametersActivity;
import com.moko.mkgw4.activity.setting.HeartReportSettingActivity;
import com.moko.mkgw4.activity.setting.LedSettingsActivity;
import com.moko.mkgw4.activity.setting.MkGw4SystemTimeActivity;
import com.moko.mkgw4.databinding.FragmentSettingsBinding;
import com.moko.mkgw4.dialog.AlertMessageDialog;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private FragmentSettingsBinding mBind;
    private MkGw4DeviceInfoActivity activity;
    private boolean isNotifyEnable;
    private boolean isPowerChargeEnable;
    private String mac;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentSettingsBinding.inflate(inflater, container, false);
        activity = (MkGw4DeviceInfoActivity) getActivity();
        initView();
        return mBind.getRoot();
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    private void initView() {
        mBind.ivPowerNotification.setOnClickListener(v -> {
            activity.showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.setPowerLossNotify(isNotifyEnable ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getPowerLossNotify());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        });
        mBind.ivPowerCharge.setOnClickListener(v -> {
            activity.showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.setAutoPowerOn(isPowerChargeEnable ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getAutoPowerOn());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        });

        mBind.tvLedSetting.setOnClickListener(v -> start(LedSettingsActivity.class));
        mBind.tvBleParams.setOnClickListener(v -> start(BleParametersActivity.class));
        mBind.tvHeartReportSeting.setOnClickListener(v -> start(HeartReportSettingActivity.class));
        mBind.tvSystemTime.setOnClickListener(v -> start(MkGw4SystemTimeActivity.class));
        mBind.layoutBattery.setOnClickListener(v -> start(BatteryManagementActivity.class));
        mBind.tvDeviceInfo.setOnClickListener(v -> start(MkGw4SystemInfoActivity.class));
        mBind.tvDebugMode.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MkGw4LogDataActivity.class);
            intent.putExtra(AppConstants.EXTRA_KEY_DEVICE_MAC, mac);
            startActivity(intent);
        });
        mBind.tvDeleteBufferData.setOnClickListener(v -> deleteBufferData());
        mBind.tvReboot.setOnClickListener(v -> reboot());
        mBind.tvPowerOff.setOnClickListener(v -> powerOff());
        mBind.tvReset.setOnClickListener(v -> reset());
    }

    private void reset() {
        if (activity.isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Reset device");
        dialog.setMessage("All parameters will be restored to factory settings, please confirm again");
        dialog.setCancel("Cancel");
        dialog.setConfirm("Confirm");
        dialog.setOnAlertConfirmListener(() -> {
            activity.showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.reset());
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    private void powerOff() {
        if (activity.isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Power off");
        dialog.setMessage("Please confirm again whether to power off the device.");
        dialog.setCancel("Cancel");
        dialog.setConfirm("Confirm");
        dialog.setOnAlertConfirmListener(() -> {
            activity.showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.close());
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    private void reboot() {
        if (activity.isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Reboot device");
        dialog.setMessage("Please confirm again whether to reboot the device.");
        dialog.setCancel("Cancel");
        dialog.setConfirm("Confirm");
        dialog.setOnAlertConfirmListener(() -> {
            activity.showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.reboot());
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    private void deleteBufferData() {
        if (activity.isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Delete buffer device");
        dialog.setMessage("Please confirm again whether to delete buffer data.");
        dialog.setCancel("Cancel");
        dialog.setConfirm("Confirm");
        dialog.setOnAlertConfirmListener(() -> {
            activity.showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.deleteBufferData());
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    private void start(Class<?> clz) {
        if (activity.isWindowLocked()) return;
        Intent intent = new Intent(requireActivity(), clz);
        startActivity(intent);
    }

    public void setBattery(int battery) {
        mBind.tvBattery.setText(battery + "mV");
    }

    public void setPowerLossNotify(int enable) {
        isNotifyEnable = enable == 1;
        mBind.ivPowerNotification.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
    }

    public void setPowerChargeNotify(int enable) {
        isPowerChargeEnable = enable == 1;
        mBind.ivPowerCharge.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
    }
}

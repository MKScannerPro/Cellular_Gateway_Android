package com.moko.ps101m.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ps101m.R;
import com.moko.ps101m.activity.DeviceInfoActivity;
import com.moko.ps101m.activity.setting.NtpSeverSettingActivity;
import com.moko.ps101m.databinding.Ps101mFragmentDeviceBinding;
import com.moko.ps101m.dialog.BottomDialog;
import com.moko.support.ps101m.MokoSupport;
import com.moko.support.ps101m.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceFragment extends Fragment {
    private static final String TAG = DeviceFragment.class.getSimpleName();
    private Ps101mFragmentDeviceBinding mBind;
    private ArrayList<String> mTimeZones;
    private int mSelectedTimeZone;
    private final String[] mLowPowerPrompts = {"10%", "20%", "30%", "40%", "50%", "60%"};
    private int mSelectedLowPowerPrompt;
    private final String[] buzzerSounds = {"No", "Alarm", "Normal"};
    private int buzzerSoundSelected;
    private final String[] intensityArr = {"No", "Low", "Medium", "High"};
    private final String[] dataFormat = {"JSON", "HEX"};
    private int dataTypeSelect;
    private int intensitySelected;
    private boolean mLowPowerPayloadEnable;
    private DeviceInfoActivity activity;

    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Ps101mFragmentDeviceBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        mTimeZones = new ArrayList<>();
        for (int i = -24; i <= 28; i++) {
            if (i < 0) {
                if (i % 2 == 0) {
                    mTimeZones.add(String.format("UTC%d", i / 2));
                } else {
                    mTimeZones.add(i < -1 ? String.format("UTC%d:30", (i + 1) / 2) : "UTC-0:30");
                }
            } else if (i == 0) {
                mTimeZones.add("UTC");
            } else {
                if (i % 2 == 0) {
                    mTimeZones.add(String.format("UTC+%d", i / 2));
                } else {
                    mTimeZones.add(String.format("UTC+%d:30", (i - 1) / 2));
                }
            }
        }
        initView();
        return mBind.getRoot();
    }

    private void initView() {
        mBind.tvDataFormat.setOnClickListener(v -> {
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(dataFormat)), dataTypeSelect);
            dialog.setListener(value -> {
                activity.showSyncingProgressDialog();
                dataTypeSelect = value;
                List<OrderTask> orderTasks = new ArrayList<>(2);
                orderTasks.add(OrderTaskAssembler.setDataFormat(value));
                orderTasks.add(OrderTaskAssembler.getDataFormat());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
            });
            dialog.show(activity.getSupportFragmentManager());
        });
        mBind.tvNtpServerSetting.setOnClickListener(v -> {
            Intent intent = new Intent(activity, NtpSeverSettingActivity.class);
            startActivity(intent);
        });
    }

    public void setDataFormat(int format) {
        dataTypeSelect = format;
        mBind.tvDataFormat.setText(dataFormat[format]);
    }

    public void setTimeZone(int timeZone) {
        mSelectedTimeZone = timeZone + 24;
        mBind.tvTimeZone.setText(mTimeZones.get(mSelectedTimeZone));
    }

    public void showTimeZoneDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mTimeZones, mSelectedTimeZone);
        dialog.setListener(value -> {
            mSelectedTimeZone = value;
            mBind.tvTimeZone.setText(mTimeZones.get(value));
            activity.showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setTimeZone(value - 24));
            orderTasks.add(OrderTaskAssembler.getTimeZone());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    public void setLowPowerPayload(int enable) {
        mLowPowerPayloadEnable = enable == 1;
        mBind.ivLowPowerPayload.setImageResource(mLowPowerPayloadEnable ? R.drawable.ic_checked : R.drawable.ps101_ic_unchecked);
    }

    public void setLowPower(int lowPower) {
        mSelectedLowPowerPrompt = lowPower;
        mBind.tvLowPowerPrompt.setText(mLowPowerPrompts[mSelectedLowPowerPrompt]);
        mBind.tvLowPowerPromptTips.setText(getString(R.string.low_power_prompt_tips, mLowPowerPrompts[mSelectedLowPowerPrompt]));
    }

    public void setBuzzerSound(int buzzerSound) {
        buzzerSoundSelected = buzzerSound;
        mBind.tvBuzzer.setText(buzzerSounds[buzzerSound]);
    }

    public void setVibrationIntensity(int intensity) {
        if (intensity == 0) {
            intensitySelected = 0;
        } else if (intensity == 10) {
            intensitySelected = 1;
        } else if (intensity == 50) {
            intensitySelected = 2;
        } else if (intensity == 80) {
            intensitySelected = 3;
        }
        mBind.tvVibration.setText(intensityArr[intensitySelected]);
    }

    public void changeLowPowerPayload() {
        mLowPowerPayloadEnable = !mLowPowerPayloadEnable;
        activity.showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setLowPowerReportEnable(mLowPowerPayloadEnable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getLowPowerPayloadEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void showLowPowerDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mLowPowerPrompts)), mSelectedLowPowerPrompt);
        dialog.setListener(value -> {
            mSelectedLowPowerPrompt = value;
            mBind.tvLowPowerPrompt.setText(mLowPowerPrompts[value]);
            mBind.tvLowPowerPromptTips.setText(getString(R.string.low_power_prompt_tips, mLowPowerPrompts[value]));
            activity.showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setLowPowerPercent(value));
            orderTasks.add(OrderTaskAssembler.getLowPowerPercent());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    public void showBuzzerDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(buzzerSounds)), buzzerSoundSelected);
        dialog.setListener(value -> {
            buzzerSoundSelected = value;
            mBind.tvBuzzer.setText(buzzerSounds[value]);
            activity.showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setBuzzerSound(value));
            orderTasks.add(OrderTaskAssembler.getBuzzerSoundChoose());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    public void showVibrationDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(intensityArr)), intensitySelected);
        dialog.setListener(value -> {
            intensitySelected = value;
            mBind.tvVibration.setText(intensityArr[value]);
            activity.showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            int vibrationVal;
            if (value == 0) {
                vibrationVal = 0;
            } else if (value == 1) {
                vibrationVal = 10;
            } else if (value == 2) {
                vibrationVal = 50;
            } else {
                vibrationVal = 80;
            }
            orderTasks.add(OrderTaskAssembler.setVibrationIntensity(vibrationVal));
            orderTasks.add(OrderTaskAssembler.getVibrationIntensity());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }
}

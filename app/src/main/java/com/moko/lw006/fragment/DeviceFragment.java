package com.moko.lw006.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.ble.lib.task.OrderTask;
import com.moko.lw006.R;
import com.moko.lw006.activity.DeviceInfoActivity;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006FragmentDeviceBinding;
import com.moko.lw006.dialog.BottomDialog;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.OrderTaskAssembler;

import java.util.ArrayList;

public class DeviceFragment extends Fragment {
    private static final String TAG = DeviceFragment.class.getSimpleName();
    private Lw006FragmentDeviceBinding mBind;

    private ArrayList<String> mTimeZones;
    private int mSelectedTimeZone;
    private final ArrayList<String> mLowPowerPrompts = new ArrayList<>(8);
    private int mSelectedLowPowerPrompt;
    private final ArrayList<String> buzzerSounds = new ArrayList<>(4);
    private int buzzerSoundSelected;
    private final ArrayList<String> intensityArr = new ArrayList<>(4);
    private int intensitySelected;

    private boolean mLowPowerPayloadEnable;
    private DeviceInfoActivity activity;

    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Lw006FragmentDeviceBinding.inflate(inflater, container, false);
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
        mLowPowerPrompts.add("10%");
        mLowPowerPrompts.add("20%");
        mLowPowerPrompts.add("30%");
        mLowPowerPrompts.add("40%");
        mLowPowerPrompts.add("50%");
        mLowPowerPrompts.add("60%");

        buzzerSounds.add("No");
        buzzerSounds.add("Alarm");
        buzzerSounds.add("Normal");

        intensityArr.add("No");
        intensityArr.add("Low");
        intensityArr.add("Medium");
        intensityArr.add("High");
        return mBind.getRoot();
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
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    public void setLowPowerPayload(int enable) {
        mLowPowerPayloadEnable = enable == 1;
        mBind.ivLowPowerPayload.setImageResource(mLowPowerPayloadEnable ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
    }

    public void setLowPower(int lowPower) {
        mSelectedLowPowerPrompt = lowPower;
        mBind.tvLowPowerPrompt.setText(mLowPowerPrompts.get(mSelectedLowPowerPrompt));
        mBind.tvLowPowerPromptTips.setText(getString(R.string.low_power_prompt_tips, mLowPowerPrompts.get(mSelectedLowPowerPrompt)));
    }

    public void setBuzzerSound(int buzzerSound) {
        buzzerSoundSelected = buzzerSound;
        mBind.tvBuzzer.setText(buzzerSounds.get(buzzerSound));
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
        mBind.tvVibration.setText(intensityArr.get(intensitySelected));
    }

    public void changeLowPowerPayload() {
        mLowPowerPayloadEnable = !mLowPowerPayloadEnable;
        activity.showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setLowPowerReportEnable(mLowPowerPayloadEnable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getLowPowerPayloadEnable());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void showLowPowerDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mLowPowerPrompts, mSelectedLowPowerPrompt);
        dialog.setListener(value -> {
            mSelectedLowPowerPrompt = value;
            mBind.tvLowPowerPrompt.setText(mLowPowerPrompts.get(value));
            mBind.tvLowPowerPromptTips.setText(getString(R.string.low_power_prompt_tips, mLowPowerPrompts.get(value)));
            activity.showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setLowPowerPercent(value));
            orderTasks.add(OrderTaskAssembler.getLowPowerPercent());
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    public void showBuzzerDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(buzzerSounds, buzzerSoundSelected);
        dialog.setListener(value -> {
            buzzerSoundSelected = value;
            mBind.tvBuzzer.setText(buzzerSounds.get(value));
            activity.showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setBuzzerSound(value));
            orderTasks.add(OrderTaskAssembler.getBuzzerSoundChoose());
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    public void showVibrationDialog() {
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(intensityArr, intensitySelected);
        dialog.setListener(value -> {
            intensitySelected = value;
            mBind.tvVibration.setText(intensityArr.get(value));
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
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(activity.getSupportFragmentManager());
    }
}

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
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.DeviceInfoActivity;
import com.moko.mkgw4.activity.ScannerFilterSettingsActivity;
import com.moko.mkgw4.activity.payload.PayloadSettingsActivity;
import com.moko.mkgw4.activity.setting.ScanReportModeActivity;
import com.moko.mkgw4.databinding.FragmentScannerBinding;
import com.moko.mkgw4.dialog.BottomDialog;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScannerFragment extends Fragment {
    private static final String TAG = ScannerFragment.class.getSimpleName();
    private FragmentScannerBinding mBind;
    private boolean modeEnable;
    private final String[] priorityValue = {"Latest data", "privious data"};
    private int prioritySelect;
    private String advName;

    public ScannerFragment() {
    }

    public static ScannerFragment newInstance() {
        return new ScannerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentScannerBinding.inflate(inflater, container, false);
        initListener();
        return mBind.getRoot();
    }

    public void setAdvName(String advName) {
        this.advName = advName;
    }

    private void initListener() {
        mBind.tvScannerReportMode.setOnClickListener(v -> startActivity(new Intent(requireActivity(), ScanReportModeActivity.class)));
        mBind.ivModeSwitch.setOnClickListener(v -> {
            if (null != getActivity()) {
                ((DeviceInfoActivity) getActivity()).showSyncingProgressDialog();
                List<OrderTask> orderTasks = new ArrayList<>(4);
                orderTasks.add(OrderTaskAssembler.setScanReportEnable(modeEnable ? 0 : 1));
                orderTasks.add(OrderTaskAssembler.getScanReportEnable());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
            }
        });
        mBind.tvUploadPriority.setOnClickListener(v -> {
            if (null != getActivity()) {
                BottomDialog dialog = new BottomDialog();
                dialog.setDatas(new ArrayList<>(Arrays.asList(priorityValue)), prioritySelect);
                dialog.setListener(value -> {
                    ((DeviceInfoActivity) getActivity()).showSyncingProgressDialog();
                    List<OrderTask> orderTasks = new ArrayList<>(4);
                    orderTasks.add(OrderTaskAssembler.setUploadPriority(value));
                    orderTasks.add(OrderTaskAssembler.getUploadPriority());
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
                });
                dialog.show(getActivity().getSupportFragmentManager());
            }
        });
        mBind.tvScannerFilter.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ScannerFilterSettingsActivity.class);
            intent.putExtra("advName", advName);
            startActivity(intent);
        });
        mBind.tvUpPayload.setOnClickListener(v -> startActivity(new Intent(requireActivity(), PayloadSettingsActivity.class)));
    }

    public void setModeSwitch(int modeSwitch) {
        modeEnable = modeSwitch == 1;
        mBind.ivModeSwitch.setImageResource(modeSwitch == 1 ? R.drawable.ic_checked : R.drawable.ps101_ic_unchecked);
    }

    public void setUpLoadPriority(int priority) {
        prioritySelect = priority;
        mBind.tvUploadPriority.setText(priority == 1 ? "privious data" : "Latest data");
    }
}

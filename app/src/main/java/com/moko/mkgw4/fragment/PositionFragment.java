package com.moko.mkgw4.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.mkgw4.activity.DeviceInfoActivity;
import com.moko.mkgw4.activity.FixModeActivity;
import com.moko.mkgw4.activity.GpsFixActivity;
import com.moko.mkgw4.activity.setting.AxisParameterActivity;
import com.moko.mkgw4.activity.setting.UpPayloadSettingsActivity;
import com.moko.mkgw4.databinding.FragmentPosMkgw4Binding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static com.moko.mkgw4.AppConstants.TYPE_USB;

public class PositionFragment extends Fragment {
    private static final String TAG = PositionFragment.class.getSimpleName();
    private FragmentPosMkgw4Binding mBind;
    private int deviceType;

    public PositionFragment() {
    }

    public static PositionFragment newInstance() {
        return new PositionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentPosMkgw4Binding.inflate(inflater, container, false);
        if (deviceType == TYPE_USB)
            mBind.tvUpPayloadSettings.setVisibility(View.VISIBLE);
        mBind.tvFixMode.setOnClickListener(v -> startActivity(FixModeActivity.class));
        mBind.tvGpsParams.setOnClickListener(v -> startActivity(GpsFixActivity.class));
        mBind.tvAxisParams.setOnClickListener(v -> startActivity(AxisParameterActivity.class));
        mBind.tvUpPayloadSettings.setOnClickListener(v -> startActivity(UpPayloadSettingsActivity.class));
        return mBind.getRoot();
    }

    private void startActivity(Class<?> clazz) {
        DeviceInfoActivity activity = (DeviceInfoActivity) getActivity();
        if (null == activity || activity.isWindowLocked()) return;
        Intent intent = new Intent(activity, clazz);
        startActivity(intent);
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
        if (null != mBind && deviceType == TYPE_USB) {
            mBind.tvUpPayloadSettings.setVisibility(View.VISIBLE);
        }
    }
}

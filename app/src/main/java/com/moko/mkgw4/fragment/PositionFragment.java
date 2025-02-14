package com.moko.mkgw4.fragment;

import static com.moko.mkgw4.AppConstants.TYPE_USB;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.activity.MkGw4DeviceInfoActivity;
import com.moko.mkgw4.activity.MkGw4FixModeActivity;
import com.moko.mkgw4.activity.MkGw4GpsFixActivity;
import com.moko.mkgw4.activity.setting.MkGw4AxisParameterActivity;
import com.moko.mkgw4.activity.setting.UpPayloadSettingsActivity;
import com.moko.mkgw4.databinding.FragmentPosBinding;

public class PositionFragment extends Fragment {
    private static final String TAG = PositionFragment.class.getSimpleName();
    private FragmentPosBinding mBind;
    private int deviceType;

    public PositionFragment() {
    }

    public static PositionFragment newInstance() {
        return new PositionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentPosBinding.inflate(inflater, container, false);
        if (deviceType == TYPE_USB)
            mBind.tvUpPayloadSettings.setVisibility(View.VISIBLE);
        mBind.tvFixMode.setOnClickListener(v -> startActivity(MkGw4FixModeActivity.class));
        mBind.tvGpsParams.setOnClickListener(v -> startActivity(MkGw4GpsFixActivity.class));
        mBind.tvAxisParams.setOnClickListener(v -> startActivity(MkGw4AxisParameterActivity.class));
        mBind.tvUpPayloadSettings.setOnClickListener(v -> startActivity(UpPayloadSettingsActivity.class));
        return mBind.getRoot();
    }

    private void startActivity(Class<?> clazz) {
        MkGw4DeviceInfoActivity activity = (MkGw4DeviceInfoActivity) getActivity();
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

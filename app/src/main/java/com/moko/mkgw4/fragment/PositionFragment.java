package com.moko.mkgw4.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.mkgw4.activity.MkGw4FixModeActivity;
import com.moko.mkgw4.activity.MkGw4GpsFixActivity;
import com.moko.mkgw4.activity.setting.MkGw4AxisParameterActivity;
import com.moko.mkgw4.databinding.FragmentPosBinding;

public class PositionFragment extends Fragment {
    private static final String TAG = PositionFragment.class.getSimpleName();
    private FragmentPosBinding mBind;

    public PositionFragment() {
    }

    public static PositionFragment newInstance() {
        return new PositionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentPosBinding.inflate(inflater, container, false);
        mBind.tvFixMode.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MkGw4FixModeActivity.class)));
        mBind.tvGpsParams.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MkGw4GpsFixActivity.class)));
        mBind.tvAxisParams.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MkGw4AxisParameterActivity.class)));
        return mBind.getRoot();
    }
}

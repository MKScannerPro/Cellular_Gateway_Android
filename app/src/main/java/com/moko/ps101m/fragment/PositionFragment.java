package com.moko.ps101m.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.ps101m.activity.FixModeActivity;
import com.moko.ps101m.activity.GpsFixActivity;
import com.moko.ps101m.activity.setting.AxisParameterActivity;
import com.moko.ps101m.databinding.Ps101mFragmentPosBinding;

public class PositionFragment extends Fragment {
    private static final String TAG = PositionFragment.class.getSimpleName();
    private Ps101mFragmentPosBinding mBind;

    public PositionFragment() {
    }

    public static PositionFragment newInstance() {
        return new PositionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = Ps101mFragmentPosBinding.inflate(inflater, container, false);
        mBind.tvFixMode.setOnClickListener(v -> startActivity(new Intent(requireActivity(), FixModeActivity.class)));
        mBind.tvGpsParams.setOnClickListener(v -> startActivity(new Intent(requireActivity(), GpsFixActivity.class)));
        mBind.tvAxisParams.setOnClickListener(v -> startActivity(new Intent(requireActivity(), AxisParameterActivity.class)));
        return mBind.getRoot();
    }
}

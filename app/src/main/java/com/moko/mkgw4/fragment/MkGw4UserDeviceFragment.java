package com.moko.mkgw4.fragment;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.mkgw4.databinding.FragmentUserDeviceMkgw4Binding;

public class MkGw4UserDeviceFragment extends Fragment {
    private final String FILTER_ASCII = "[ -~]*";
    private static final String TAG = MkGw4UserDeviceFragment.class.getSimpleName();
    private FragmentUserDeviceMkgw4Binding mBind;
    private String username;
    private String password;

    public MkGw4UserDeviceFragment() {
    }

    public static MkGw4UserDeviceFragment newInstance() {
        return new MkGw4UserDeviceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentUserDeviceMkgw4Binding.inflate(inflater, container, false);
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }
            return null;
        };
        mBind.etMqttUsername.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256), filter});
        mBind.etMqttPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256), filter});
        mBind.etMqttUsername.setText(username);
        mBind.etMqttPassword.setText(password);
        return mBind.getRoot();
    }

    public void setUserName(String username) {
        this.username = username;
        if (mBind == null) return;
        mBind.etMqttUsername.setText(username);
        mBind.etMqttUsername.setSelection(mBind.etMqttUsername.getText().length());
    }

    public void setPassword(String password) {
        this.password = password;
        if (mBind == null) return;
        mBind.etMqttPassword.setText(password);
        mBind.etMqttPassword.setSelection(mBind.etMqttPassword.getText().length());
    }

    public String getUsername() {
        return TextUtils.isEmpty(mBind.etMqttUsername.getText()) ? null : mBind.etMqttUsername.getText().toString();
    }

    public String getPassword() {
        return TextUtils.isEmpty(mBind.etMqttPassword.getText()) ? null : mBind.etMqttPassword.getText().toString();
    }
}

package com.moko.ps101m.fragment;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.ps101m.databinding.FragmentUserDeviceBinding;

public class UserDeviceFragment extends Fragment {
    private final String FILTER_ASCII = "[ -~]*";
    private static final String TAG = UserDeviceFragment.class.getSimpleName();
    private FragmentUserDeviceBinding mBind;
    private String username;
    private String password;

    public UserDeviceFragment() {
    }

    public static UserDeviceFragment newInstance() {
        return new UserDeviceFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentUserDeviceBinding.inflate(inflater, container, false);
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

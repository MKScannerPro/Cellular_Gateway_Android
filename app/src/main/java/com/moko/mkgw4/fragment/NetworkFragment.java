package com.moko.mkgw4.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.mkgw4.activity.setting.MqttSettingsActivity;
import com.moko.mkgw4.activity.setting.NetworkSettingsActivity;
import com.moko.mkgw4.databinding.FragmentNetworkBinding;

public class NetworkFragment extends Fragment {
    private static final String TAG = NetworkFragment.class.getSimpleName();
    private FragmentNetworkBinding mBind;

    public NetworkFragment() {
    }

    public static NetworkFragment newInstance() {
        return new NetworkFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentNetworkBinding.inflate(inflater, container, false);
        mBind.layoutMqttStatus.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MqttSettingsActivity.class)));
        mBind.layoutNetworkStatus.setOnClickListener(v -> startActivity(new Intent(requireActivity(), NetworkSettingsActivity.class)));
        return mBind.getRoot();
    }

    public void setMqttConnectionStatus(int status) {
        mBind.tvMqttStatus.setText(status == 1 ? "Connected" : "Unconnected");
    }

    public void setNetworkStatus(int networkCheck) {
        String networkCheckDisPlay = "";
        switch (networkCheck) {
            case 0:
                networkCheckDisPlay = "Unconnected";
                break;
            case 1:
                networkCheckDisPlay = "Connecting";
                break;
            case 2:
                networkCheckDisPlay = "Connected";
                break;
        }
        mBind.tvNetworkStatus.setText(networkCheckDisPlay);
    }
}

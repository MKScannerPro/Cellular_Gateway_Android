package com.moko.mkgw4.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.mkgw4.activity.MkGw4DeviceInfoActivity;
import com.moko.mkgw4.activity.setting.MkGw4MqttSettingsActivity;
import com.moko.mkgw4.activity.setting.MkGw4NetworkSettingsActivity;
import com.moko.mkgw4.databinding.FragmentNetworkBinding;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;

public class NetworkFragment extends Fragment {
    private static final String TAG = NetworkFragment.class.getSimpleName();
    private FragmentNetworkBinding mBind;
    private int deviceType = -1;

    public NetworkFragment() {
    }

    public static NetworkFragment newInstance() {
        return new NetworkFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentNetworkBinding.inflate(inflater, container, false);
        mBind.layoutMqttStatus.setOnClickListener(v -> startActivity(new Intent(requireActivity(), MkGw4MqttSettingsActivity.class)));
        mBind.layoutNetworkStatus.setOnClickListener(v -> {
            if (deviceType == -1) {
                MkGw4DeviceInfoActivity activity = (MkGw4DeviceInfoActivity) getActivity();
                if (null != activity) activity.showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDeviceType());
                return;
            }
            Intent intent = new Intent(requireActivity(), MkGw4NetworkSettingsActivity.class);
            intent.putExtra("deviceType", deviceType);
            startActivity(intent);
        });
        return mBind.getRoot();
    }

    public void setMqttConnectionStatus(int status) {
        mBind.tvMqttStatus.setText(status == 1 ? "Connected" : "Unconnected");
    }

    public void setDeviceType(int type) {
        this.deviceType = type;
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

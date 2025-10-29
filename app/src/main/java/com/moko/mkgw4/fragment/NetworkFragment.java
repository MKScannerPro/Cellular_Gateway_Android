package com.moko.mkgw4.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.activity.DeviceInfoActivity;
import com.moko.mkgw4.activity.setting.MqttSettingsActivity;
import com.moko.mkgw4.activity.setting.NetworkSettingsActivity;
import com.moko.mkgw4.databinding.FragmentNetworkMkgw4Binding;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class NetworkFragment extends Fragment {
    private static final String TAG = NetworkFragment.class.getSimpleName();
    private FragmentNetworkMkgw4Binding mBind;
    private int cellularType = -1;
    private int mNetworkStatus;
    private int mMqttStatus;
    private int deviceType;
    private DeviceInfoActivity activity;

    public NetworkFragment() {
    }

    public static NetworkFragment newInstance() {
        return new NetworkFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentNetworkMkgw4Binding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        mBind.layoutMqttStatus.setOnClickListener(v -> {
            if (activity.isWindowLocked()) return;
            activity.resetTimer();
            launcher.launch(new Intent(requireActivity(), MqttSettingsActivity.class));
        });
        mBind.layoutNetworkStatus.setOnClickListener(v -> {
            if (cellularType == -1) {
                DeviceInfoActivity activity = (DeviceInfoActivity) getActivity();
                if (null != activity) activity.showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDeviceType());
                return;
            }
            activity.resetTimer();
            Intent intent = new Intent(requireActivity(), NetworkSettingsActivity.class);
            intent.putExtra("cellularType", cellularType);
            intent.putExtra(AppConstants.DEVICE_TYPE, deviceType);
            launcher.launch(intent);
        });
        return mBind.getRoot();
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (null != activity && !activity.isFinishing() && result.getResultCode() == RESULT_OK) {
            activity.getNetworkStatus();
        }
    });

    public void setMqttConnectionStatus(int status) {
        mBind.tvMqttStatus.setText(status == 1 ? "Connected" : "Unconnected");
        this.mMqttStatus = status;
    }

    public boolean isNetworkConnected() {
        if (deviceType > 0) {
            return mMqttStatus == 1 && mNetworkStatus == 4;
        } else {
            return mMqttStatus == 1 && mNetworkStatus == 2;
        }
    }

    public void setCellularType(int type) {
        this.cellularType = type;
    }

    public void setNetworkStatus(int networkCheck, int deviceType) {
        mNetworkStatus = networkCheck;
        this.deviceType = deviceType;
        String networkCheckDisPlay = "";
        switch (networkCheck) {
            case 0:
                networkCheckDisPlay = "Unconnected";
                break;
            case 1:
                networkCheckDisPlay = deviceType > 0 ? "Registering" : "Connecting";
                break;
            case 2:
                networkCheckDisPlay = deviceType > 0 ? "Registered" : "Connected";
                break;
            case 3:
                networkCheckDisPlay = "Attaching";
                break;
            case 4:
                networkCheckDisPlay = "Connected";
                break;
        }
        mBind.tvNetworkStatus.setText(networkCheckDisPlay);
    }
}

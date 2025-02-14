package com.moko.mkgw4.fragment;

import static android.app.Activity.RESULT_OK;

import static com.moko.mkgw4.AppConstants.TYPE_USB;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.activity.MkGw4DeviceInfoActivity;
import com.moko.mkgw4.activity.setting.MkGw4MqttSettingsActivity;
import com.moko.mkgw4.activity.setting.MkGw4NetworkSettingsActivity;
import com.moko.mkgw4.databinding.FragmentNetworkBinding;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;

public class NetworkFragment extends Fragment {
    private static final String TAG = NetworkFragment.class.getSimpleName();
    private FragmentNetworkBinding mBind;
    private int cellularType = -1;
    private int mNetworkStatus;
    private int deviceType;
    private MkGw4DeviceInfoActivity activity;

    public NetworkFragment() {
    }

    public static NetworkFragment newInstance() {
        return new NetworkFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentNetworkBinding.inflate(inflater, container, false);
        activity = (MkGw4DeviceInfoActivity) getActivity();
        mBind.layoutMqttStatus.setOnClickListener(v -> {
            if (activity.isWindowLocked()) return;
            activity.resetTimer();
            launcher.launch(new Intent(requireActivity(), MkGw4MqttSettingsActivity.class));
        });
        mBind.layoutNetworkStatus.setOnClickListener(v -> {
            if (cellularType == -1) {
                MkGw4DeviceInfoActivity activity = (MkGw4DeviceInfoActivity) getActivity();
                if (null != activity) activity.showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getDeviceType());
                return;
            }
            activity.resetTimer();
            Intent intent = new Intent(requireActivity(), MkGw4NetworkSettingsActivity.class);
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
        if (deviceType == TYPE_USB) {
            mBind.tvSyncDevices2Cloud.setVisibility(status == 1 && mNetworkStatus == 4 ? View.VISIBLE : View.GONE);
        } else {
            mBind.tvSyncDevices2Cloud.setVisibility(status == 1 && mNetworkStatus == 2 ? View.VISIBLE : View.GONE);
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
                networkCheckDisPlay = deviceType == TYPE_USB ? "Registering" : "Connecting";
                break;
            case 2:
                networkCheckDisPlay = deviceType == TYPE_USB ? "Registered" : "Connected";
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

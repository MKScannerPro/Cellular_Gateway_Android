package com.moko.mkgw4.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.FragmentSslDeviceMkgw4Binding;
import com.moko.mkgw4.dialog.MkGw4BottomDialog;
import com.moko.mkgw4.utils.FileUtils;
import com.moko.mkgw4.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MkGw4SSLDeviceFragment extends Fragment {
    private static final String TAG = MkGw4SSLDeviceFragment.class.getSimpleName();
    private FragmentSslDeviceMkgw4Binding mBind;
    private MkGw4BaseActivity activity;
    private int mConnectMode;
    private final String[] values = {"CA signed server certificate", "CA certificate", "Self signed certificates"};
    private int selected;

    public MkGw4SSLDeviceFragment() {
    }

    public static MkGw4SSLDeviceFragment newInstance() {
        return new MkGw4SSLDeviceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentSslDeviceMkgw4Binding.inflate(inflater, container, false);
        activity = (MkGw4BaseActivity) getActivity();
        mBind.clCertificate.setVisibility(mConnectMode > 0 ? View.VISIBLE : View.GONE);
        mBind.cbSsl.setChecked(mConnectMode > 0);
        mBind.cbSsl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                mConnectMode = 0;
            } else {
                mConnectMode = selected + 1;
            }
            mBind.clCertificate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        if (mConnectMode > 0) {
            selected = mConnectMode - 1;
            mBind.tvCertification.setText(values[selected]);
        }
        if (selected == 0) {
            mBind.llCa.setVisibility(View.GONE);
            mBind.llClientKey.setVisibility(View.GONE);
            mBind.llClientCert.setVisibility(View.GONE);
        } else if (selected == 1) {
            mBind.llCa.setVisibility(View.VISIBLE);
            mBind.llClientKey.setVisibility(View.GONE);
            mBind.llClientCert.setVisibility(View.GONE);
        } else if (selected == 2) {
            mBind.llCa.setVisibility(View.VISIBLE);
            mBind.llClientKey.setVisibility(View.VISIBLE);
            mBind.llClientCert.setVisibility(View.VISIBLE);
        }
        return mBind.getRoot();
    }

    public boolean isValid() {
        if (mConnectMode == 2) {
            if (TextUtils.isEmpty(mBind.tvCaFile.getText())) {
                ToastUtils.showToast(activity, getString(R.string.mqtt_verify_ca));
                return false;
            }
        } else if (mConnectMode == 3) {
            if (TextUtils.isEmpty(mBind.tvCaFile.getText())) {
                ToastUtils.showToast(activity, getString(R.string.mqtt_verify_ca));
                return false;
            }
            if (TextUtils.isEmpty(mBind.tvClientKeyFile.getText())) {
                ToastUtils.showToast(activity, getString(R.string.mqtt_verify_client_key));
                return false;
            }
            if (TextUtils.isEmpty(mBind.tvClientCertFile.getText())) {
                ToastUtils.showToast(activity, getString(R.string.mqtt_verify_client_cert));
                return false;
            }
        }
        return true;
    }

    public void setConnectMode(int connectMode) {
        this.mConnectMode = connectMode;
        if (mBind == null) return;
        mBind.clCertificate.setVisibility(mConnectMode > 0 ? View.VISIBLE : View.GONE);
        if (mConnectMode > 0) {
            selected = mConnectMode - 1;
            mBind.tvCertification.setText(values[selected]);
        }
        mBind.cbSsl.setChecked(mConnectMode > 0);
        if (selected == 0) {
            mBind.llCa.setVisibility(View.GONE);
            mBind.llClientKey.setVisibility(View.GONE);
            mBind.llClientCert.setVisibility(View.GONE);
        } else if (selected == 1) {
            mBind.llCa.setVisibility(View.VISIBLE);
            mBind.llClientKey.setVisibility(View.GONE);
            mBind.llClientCert.setVisibility(View.GONE);
        } else if (selected == 2) {
            mBind.llCa.setVisibility(View.VISIBLE);
            mBind.llClientKey.setVisibility(View.VISIBLE);
            mBind.llClientCert.setVisibility(View.VISIBLE);
        }
    }

    public void selectCertificate() {
        MkGw4BottomDialog dialog = new MkGw4BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(values)), selected);
        dialog.setListener(value -> {
            selected = value;
            mBind.tvCertification.setText(values[selected]);
            if (selected == 0) {
                mConnectMode = 1;
                mBind.llCa.setVisibility(View.GONE);
                mBind.llClientKey.setVisibility(View.GONE);
                mBind.llClientCert.setVisibility(View.GONE);
            } else if (selected == 1) {
                mConnectMode = 2;
                mBind.llCa.setVisibility(View.VISIBLE);
                mBind.llClientKey.setVisibility(View.GONE);
                mBind.llClientCert.setVisibility(View.GONE);
            } else if (selected == 2) {
                mConnectMode = 3;
                mBind.llCa.setVisibility(View.VISIBLE);
                mBind.llClientKey.setVisibility(View.VISIBLE);
                mBind.llClientCert.setVisibility(View.VISIBLE);
            }
        });
        dialog.show(activity.getSupportFragmentManager());
    }

    private void setFilePath(Intent intent, TextView textView) {
        if (null == intent) return;
        Uri uri = intent.getData();
        String filePath = FileUtils.getPath(activity, uri);
        if (TextUtils.isEmpty(filePath)) {
            ToastUtils.showToast(activity, "file path error!");
        } else {
            final File file = new File(filePath);
            if (file.exists()) {
                textView.setText(filePath);
            } else {
                ToastUtils.showToast(activity, "file is not exists!");
            }
        }
    }

    private final ActivityResultLauncher<Intent> caFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> setFilePath(result.getData(), mBind.tvCaFile));

    private final ActivityResultLauncher<Intent> keyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> setFilePath(result.getData(), mBind.tvClientKeyFile));

    private final ActivityResultLauncher<Intent> certLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> setFilePath(result.getData(), mBind.tvClientCertFile));

    private Intent getFileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public void selectCAFile() {
        caFileLauncher.launch(getFileIntent());
    }

    public void selectKeyFile() {
        keyLauncher.launch(getFileIntent());
    }

    public void selectCertFile() {
        certLauncher.launch(getFileIntent());
    }

    public int getConnectMode() {
        return mConnectMode;
    }

    public String getCaPath() {
        return TextUtils.isEmpty(mBind.tvCaFile.getText()) ? null : mBind.tvCaFile.getText().toString().trim();
    }

    public String getClientKeyPath() {
        return TextUtils.isEmpty(mBind.tvClientKeyFile.getText()) ? null : mBind.tvClientKeyFile.getText().toString().trim();
    }

    public String getClientCertPath() {
        return TextUtils.isEmpty(mBind.tvClientCertFile.getText()) ? null : mBind.tvClientCertFile.getText().toString().trim();
    }
}

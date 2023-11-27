package com.moko.ps101m.fragment;

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

import com.moko.ps101m.activity.PS101BaseActivity;
import com.moko.ps101m.databinding.FragmentSslDeviceBinding;
import com.moko.ps101m.dialog.BottomDialog;
import com.moko.ps101m.utils.FileUtils;
import com.moko.ps101m.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class SSLDeviceFragment extends Fragment {
    private static final String TAG = SSLDeviceFragment.class.getSimpleName();
    private FragmentSslDeviceBinding mBind;
    private PS101BaseActivity activity;
    private int mConnectMode;
    private final String[] values = {"CA signed server certificate", "CA certificate", "Self signed certificates"};
    private int selected;

    public SSLDeviceFragment() {
    }

    public static SSLDeviceFragment newInstance() {
        return new SSLDeviceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentSslDeviceBinding.inflate(inflater, container, false);
        activity = (PS101BaseActivity) getActivity();
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
        BottomDialog dialog = new BottomDialog();
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

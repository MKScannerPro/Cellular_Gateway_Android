package com.moko.ps101m.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;

import com.elvishew.xlog.XLog;
import com.moko.ps101m.dialog.LoadingDialog;
import com.moko.ps101m.dialog.LoadingMessageDialog;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


public class PS101BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Intent intent = new Intent(this, GuideActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        XLog.i("onConfigurationChanged...");
        finish();
    }


    // 记录上次页面控件点击时间,屏蔽无效点击事件
    protected long mLastOnClickTime = 0;

    public boolean isWindowLocked() {
        long current = SystemClock.elapsedRealtime();
        if (current - mLastOnClickTime > voidDuration) {
            mLastOnClickTime = current;
            return false;
        } else {
            return true;
        }
    }

    public int voidDuration = 500;

    public boolean isWriteStoragePermissionOpen() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationPermissionOpen() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        if (null != mLoadingMessageDialog && mLoadingMessageDialog.isAdded() && !mLoadingMessageDialog.isDetached()) {
            mLoadingMessageDialog.dismissAllowingStateLoss();
        }
        mLoadingMessageDialog = null;
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null && mLoadingMessageDialog.isAdded() && !mLoadingMessageDialog.isDetached())
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    private LoadingDialog mLoadingDialog;

    protected void showLoadingProgressDialog() {
        if (null != mLoadingDialog && mLoadingDialog.isAdded() && !mLoadingDialog.isDetached()) {
            mLoadingDialog.dismissAllowingStateLoss();
        }
        mLoadingDialog = null;
        mLoadingDialog = new LoadingDialog();
        if (!mLoadingDialog.isAdded())
            mLoadingDialog.show(getSupportFragmentManager());
    }

    protected void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isAdded() && !mLoadingDialog.isDetached()) {
            mLoadingDialog.dismissAllowingStateLoss();
            mLoadingDialog = null;
        }
    }
}

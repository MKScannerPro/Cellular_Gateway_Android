package com.moko.mkgw4.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.activity.MKGW4MainActivity;
import com.moko.mkgw4.adapter.MkGw4LogDataListAdapter;
import com.moko.mkgw4.databinding.ActivityLogDataMkgw4Binding;
import com.moko.mkgw4.dialog.AlertMessageDialog;
import com.moko.mkgw4.entity.LogData;
import com.moko.mkgw4.utils.Utils;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class MkGw4LogDataActivity extends MkGw4BaseActivity implements BaseQuickAdapter.OnItemClickListener {
    public static String TAG = MkGw4LogDataActivity.class.getSimpleName();
    private ActivityLogDataMkgw4Binding mBind;
    private StringBuilder storeString;
    private ArrayList<LogData> LogDatas;
    private boolean isSync;
    private MkGw4LogDataListAdapter adapter;
    private String logDirPath;
    private int selectedCount;
    private String syncTime;
    private Animation animation = null;
    private boolean isDisconnected;
    private boolean isBack;
    private StringBuilder builder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityLogDataMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        String mDeviceMac = getIntent().getStringExtra(AppConstants.EXTRA_KEY_DEVICE_MAC).replaceAll(":", "");
        logDirPath = MKGW4MainActivity.PATH_LOGCAT + File.separator + mDeviceMac;
        LogDatas = new ArrayList<>();
        adapter = new MkGw4LogDataListAdapter();
        adapter.openLoadAnimation();
        adapter.replaceData(LogDatas);
        adapter.setOnItemClickListener(this);
        mBind.rvExportData.setLayoutManager(new LinearLayoutManager(this));
        mBind.rvExportData.setAdapter(adapter);
        EventBus.getDefault().register(this);
        File file = new File(logDirPath);
        if (file.exists()) {
            File[] logFiles = file.listFiles();
            Arrays.sort(logFiles, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0)
                        return 1;
                    else if (diff == 0)
                        return 0;
                    else
                        return -1;
                }

                public boolean equals(Object obj) {
                    return true;
                }

            });
            for (int i = 0, l = logFiles.length; i < l; i++) {
                File logFile = logFiles[i];
                LogData data = new LogData();
                data.filePath = logFile.getAbsolutePath();
                data.name = logFile.getName().replaceAll(".txt", "");
                LogDatas.add(data);
            }
            adapter.replaceData(LogDatas);
        }
        // 点击无效间隔改为1秒
        voidDuration = 1000;
        storeString = new StringBuilder();
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                isDisconnected = true;
                // 中途断开，要先保存数据
                mBind.tvSyncSwitch.setEnabled(false);
                if (isSync) {
                    isBack = true;
                    stopSync();
                } else {
                    finish();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (Objects.requireNonNull(orderCHAR) == OrderCHAR.CHAR_LOG) {
                    String log = new String(value);
                    storeString.append(log);
                    builder.insert(0, log);
                    mBind.tvContent.setText(builder.toString());
                }
            }
        });
    }


    public void onSyncSwitch(View view) {
        if (isWindowLocked()) return;
        int size = LogDatas.size();
        if (size >= 10) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Tips");
            dialog.setMessage("Up to 10 log files can be stored, please delete the useless logs first！");
            dialog.setConfirm("OK");
            dialog.setCancelGone();
            dialog.show(getSupportFragmentManager());
            return;
        }
        if (animation == null) {
            storeString = new StringBuilder();
            mBind.tvSyncSwitch.setText("Stop");
            isSync = true;
            builder = new StringBuilder();
            mBind.tvContent.setText("");
            animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            mBind.ivSync.startAnimation(animation);
            MokoSupport.getInstance().enableLogNotify();
            Calendar calendar = Calendar.getInstance();
            syncTime = Utils.calendar2strDate(calendar, "yyyy-MM-dd HH-mm-ss");
            mBind.nestScrollView.setVisibility(View.VISIBLE);
        } else {
            MokoSupport.getInstance().disableLogNotify();
            stopSync();
        }
    }

    public void writeLogFile2SDCard(String filePath) {
        String log = storeString.toString();
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(log);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onEmpty(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning!");
        dialog.setMessage("Are you sure to empty the saved debugger log?");
        dialog.setOnAlertConfirmListener(() -> {
            Iterator<LogData> iterator = LogDatas.iterator();
            while (iterator.hasNext()) {
                LogData LogData = iterator.next();
                if (!LogData.isSelected)
                    continue;
                File file = new File(LogData.filePath);
                if (file.exists())
                    file.delete();
                iterator.remove();
                selectedCount--;
            }
            if (selectedCount > 0) {
                mBind.tvEmpty.setEnabled(true);
                mBind.tvExport.setEnabled(true);
            } else {
                mBind.tvEmpty.setEnabled(false);
                mBind.tvExport.setEnabled(false);
            }
            adapter.replaceData(LogDatas);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onExport(View view) {
        if (isWindowLocked()) return;
        ArrayList<File> selectedFiles = new ArrayList<>();
        for (LogData LogData : LogDatas) {
            if (LogData.isSelected) {
                selectedFiles.add(new File(LogData.filePath));
            }
        }
        if (!selectedFiles.isEmpty()) {
            File[] files = selectedFiles.toArray(new File[]{});
            // 发送邮件
            String address = "Development@mokotechnology.com";
            String title = "Debugger Log";
            Utils.sendEmail(this, address, title, title, "Choose Email Client", files);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void backHome() {
        if (isSync) {
            MokoSupport.getInstance().disableLogNotify();
            stopSync();
        } else {
            if (isDisconnected) {
                Intent intent = new Intent(this, MKGW4MainActivity.class);
                intent.putExtra(AppConstants.EXTRA_KEY_FROM_ACTIVITY, TAG);
                startActivity(intent);
                return;
            }
            finish();
        }
    }

    private void stopSync() {
        mBind.tvSyncSwitch.setText("Start");
        isSync = false;
        // 关闭通知
        mBind.ivSync.clearAnimation();
        animation = null;
        if (storeString.length() == 0) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Tips");
            dialog.setMessage("No debug logs are sent during this process！");
            dialog.setConfirm("OK");
            dialog.setCancelGone();
            dialog.setOnAlertConfirmListener(() -> {
                if (isDisconnected) {
                    Intent intent = new Intent(this, MKGW4MainActivity.class);
                    intent.putExtra(AppConstants.EXTRA_KEY_FROM_ACTIVITY, TAG);
                    startActivity(intent);
                    return;
                }
                if (isBack)
                    finish();
            });
            dialog.show(getSupportFragmentManager());
            return;
        }
        File logDir = new File(logDirPath);
        if (!logDir.exists())
            logDir.mkdirs();
        String logFilePath = logDirPath + File.separator + String.format("%s.txt", syncTime);
        writeLogFile2SDCard(logFilePath);
        LogData LogData = new LogData();
        LogData.name = syncTime;
        LogData.filePath = logFilePath;
        LogDatas.add(LogData);
        adapter.replaceData(LogDatas);
        if (isBack) finish();
    }

    @Override
    public void onBackPressed() {
        isBack = true;
        backHome();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        LogData LogData = (LogData) adapter.getItem(position);
        if (LogData != null) {
            LogData.isSelected = !LogData.isSelected;
            if (LogData.isSelected) {
                selectedCount++;
            } else {
                selectedCount--;
            }
            if (selectedCount > 0) {
                mBind.tvEmpty.setEnabled(true);
                mBind.tvExport.setEnabled(true);
            } else {
                mBind.tvEmpty.setEnabled(false);
                mBind.tvExport.setEnabled(false);
            }
            adapter.notifyItemChanged(position);
        }
    }

    public void onBack(View view) {
        if (isWindowLocked()) return;
        isBack = true;
        backHome();
    }
}

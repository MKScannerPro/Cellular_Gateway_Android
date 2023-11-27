package com.moko.lw006.activity.device;

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
import com.moko.lw006.AppConstants;
import com.moko.lw006.R;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.activity.LoRaLW006MainActivity;
import com.moko.lw006.adapter.LogDataListAdapter;
import com.moko.lw006.databinding.Lw006ActivityLogDataBinding;
import com.moko.lw006.dialog.AlertMessageDialog;
import com.moko.lw006.entity.LogData;
import com.moko.lw006.utils.Utils;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.entity.OrderCHAR;

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

public class LogDataActivity extends Lw006BaseActivity implements BaseQuickAdapter.OnItemClickListener {
    public static String TAG = LogDataActivity.class.getSimpleName();
    private Lw006ActivityLogDataBinding mBind;
    private StringBuilder storeString;
    private ArrayList<LogData> LogDatas;
    private boolean isSync;
    private LogDataListAdapter adapter;
    private String logDirPath;
    private String mDeviceMac;
    private int selectedCount;
    private String syncTime;
    private Animation animation = null;
    private boolean isDisconnected;
    private boolean isBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityLogDataBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mDeviceMac = getIntent().getStringExtra(AppConstants.EXTRA_KEY_DEVICE_MAC).replaceAll(":", "");
        logDirPath = LoRaLW006MainActivity.PATH_LOGCAT + File.separator + mDeviceMac;
        LogDatas = new ArrayList<>();
        adapter = new LogDataListAdapter();
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
        EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                isDisconnected = true;
                // 中途断开，要先保存数据
                mBind.tvSyncSwitch.setEnabled(false);
                if (isSync)
                    stopSync();
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
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_LOG:
                        String log = new String(value);
                        storeString.append(log);
                        break;
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
            animation = AnimationUtils.loadAnimation(this, R.anim.lw006_rotate_refresh);
            mBind.ivSync.startAnimation(animation);
            LoRaLW006MokoSupport.getInstance().enableLogNotify();
            Calendar calendar = Calendar.getInstance();
            syncTime = Utils.calendar2strDate(calendar, "yyyy-MM-dd HH-mm-ss");
        } else {
            LoRaLW006MokoSupport.getInstance().disableLogNotify();
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
            String content = title;
            Utils.sendEmail(LogDataActivity.this, address, content, title, "Choose Email Client", files);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void backHome() {
        if (isSync) {
            LoRaLW006MokoSupport.getInstance().disableLogNotify();
            stopSync();
        } else {
            if (isDisconnected) {
                Intent intent = new Intent(this, LoRaLW006MainActivity.class);
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
                    Intent intent = new Intent(this, LoRaLW006MainActivity.class);
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
        if (isBack)
            finish();
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

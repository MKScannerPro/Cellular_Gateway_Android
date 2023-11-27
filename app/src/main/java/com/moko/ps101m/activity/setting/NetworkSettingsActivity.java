package com.moko.ps101m.activity.setting;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.ps101m.ExcelHelper;
import com.moko.ps101m.R;
import com.moko.ps101m.activity.PS101BaseActivity;
import com.moko.ps101m.activity.PS101MainActivity;
import com.moko.ps101m.adapter.NetworkFragmentAdapter;
import com.moko.ps101m.databinding.ActivityNetworkSettingBinding;
import com.moko.ps101m.dialog.AlertMessageDialog;
import com.moko.ps101m.dialog.BottomDialog;
import com.moko.ps101m.entity.NetworkSettings;
import com.moko.ps101m.fragment.GeneralDeviceFragment;
import com.moko.ps101m.fragment.LWTFragment;
import com.moko.ps101m.fragment.SSLDeviceFragment;
import com.moko.ps101m.fragment.UserDeviceFragment;
import com.moko.ps101m.utils.FileUtils;
import com.moko.ps101m.utils.ToastUtils;
import com.moko.ps101m.utils.Utils;
import com.moko.support.ps101m.MokoSupport;
import com.moko.support.ps101m.OrderTaskAssembler;
import com.moko.support.ps101m.entity.OrderCHAR;
import com.moko.support.ps101m.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkSettingsActivity extends PS101BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private ActivityNetworkSettingBinding mBind;
    private final String FILTER_ASCII = "[ -~]*";
    private GeneralDeviceFragment generalFragment;
    private UserDeviceFragment userFragment;
    private SSLDeviceFragment sslFragment;
    private LWTFragment lwtFragment;
    private ArrayList<Fragment> fragments;
    private boolean mSavedParamsError;
    private String expertFilePath;
    private boolean isFileError;
    private final String[] netWorkFormatArray = {"NB-IOT", "eMTC", "NB-IOT->eMTC", "eMTC->NB-IOT"};
    private int networkFormatSelect;
    private boolean mReceiverTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityNetworkSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        InputFilter inputFilter = (source, start, end, dest, dStart, dEnd) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }
            return null;
        };
        mBind.etMqttHost.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64), inputFilter});
        mBind.etMqttClientId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64), inputFilter});
        mBind.etMqttSubscribeTopic.setFilters(new InputFilter[]{new InputFilter.LengthFilter(128), inputFilter});
        mBind.etMqttPublishTopic.setFilters(new InputFilter[]{new InputFilter.LengthFilter(128), inputFilter});
        createFragment();
        NetworkFragmentAdapter adapter = new NetworkFragmentAdapter(this);
        adapter.setFragmentList(fragments);
        mBind.vpMqtt.setAdapter(adapter);
        mBind.vpMqtt.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mBind.rbGeneral.setChecked(true);
                } else if (position == 1) {
                    mBind.rbUser.setChecked(true);
                } else if (position == 2) {
                    mBind.rbSsl.setChecked(true);
                } else if (position == 3) {
                    mBind.rbLwt.setChecked(true);
                }
            }
        });
        mBind.vpMqtt.setOffscreenPageLimit(4);
        mBind.rgMqtt.setOnCheckedChangeListener(this);
        expertFilePath = PS101MainActivity.PATH_LOGCAT + File.separator + "export" + File.separator + "Settings_for_Device.xls";
        showSyncingProgressDialog();
        mBind.title.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getMQTTHost());
            orderTasks.add(OrderTaskAssembler.getMQTTPort());
            orderTasks.add(OrderTaskAssembler.getMQTTClientId());
            orderTasks.add(OrderTaskAssembler.getMQTTSubscribeTopic());
            orderTasks.add(OrderTaskAssembler.getMQTTPublishTopic());
            orderTasks.add(OrderTaskAssembler.getMQTTCleanSession());
            orderTasks.add(OrderTaskAssembler.getMQTTQos());
            orderTasks.add(OrderTaskAssembler.getMQTTKeepAlive());
            orderTasks.add(OrderTaskAssembler.getApn());
            orderTasks.add(OrderTaskAssembler.getNetworkFormat());
            orderTasks.add(OrderTaskAssembler.getMQTTUsername());
            orderTasks.add(OrderTaskAssembler.getMQTTPassword());
            orderTasks.add(OrderTaskAssembler.getMQTTConnectMode());
            orderTasks.add(OrderTaskAssembler.getMQTTLwtEnable());
            orderTasks.add(OrderTaskAssembler.getMQTTLwtRetain());
            orderTasks.add(OrderTaskAssembler.getMQTTLwtQos());
            orderTasks.add(OrderTaskAssembler.getMQTTLwtTopic());
            orderTasks.add(OrderTaskAssembler.getMQTTLwtPayload());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }, 500);
        mBind.tvNetworkFormat.setOnClickListener(v -> onNetworkFormatClick());
    }

    /**
     * 网络制式选择
     */
    private void onNetworkFormatClick() {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(netWorkFormatArray)), networkFormatSelect);
        dialog.setListener(value -> {
            networkFormatSelect = value;
            mBind.tvNetworkFormat.setText(netWorkFormatArray[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            runOnUiThread(() -> {
                dismissSyncProgressDialog();
                finish();
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header == 0xEE) {
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) return;
                            if (flag == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_MQTT_CA:
                                    case KEY_MQTT_CLIENT_CERT:
                                        if (result != 1) {
                                            mSavedParamsError = true;
                                        }
                                        break;
                                    case KEY_MQTT_CLIENT_KEY:
                                        if (mSavedParamsError) {
                                            ToastUtils.showToast(this, "Setup failed！");
                                        } else {
                                            ToastUtils.showToast(this, "Setup succeed！");
                                        }
                                        break;
                                }
                            }
                        }
                        if (header == 0xED) {
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) return;
                            int length = value[3] & 0xFF;
                            if (flag == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_MQTT_HOST:
                                    case KEY_MQTT_PORT:
                                    case KEY_MQTT_CLIENT_ID:
                                    case KEY_SUBSCRIBE_TOPIC:
                                    case KEY_PUBLISH_TOPIC:
                                    case KEY_MQTT_CLEAN_SESSION:
                                    case KEY_MQTT_QOS:
                                    case KEY_MQTT_KEEP_ALIVE:
                                    case KEY_APN:
                                    case KEY_NETWORK_FORMAT:
                                    case KEY_MQTT_USERNAME:
                                    case KEY_MQTT_PASSWORD:
                                    case KEY_CONNECT_MODE:
                                    case KEY_MQTT_LWT_ENABLE:
                                    case KEY_MQTT_LWT_RETAIN:
                                    case KEY_MQTT_LWT_QOS:
                                    case KEY_MQTT_LWT_TOPIC:
                                        if (result != 1) {
                                            mSavedParamsError = true;
                                        }
                                        break;
                                    case KEY_MQTT_LWT_PAYLOAD:
                                        if (result != 1) {
                                            mSavedParamsError = true;
                                        }
                                        if (mSavedParamsError) {
                                            ToastUtils.showToast(this, "Setup failed！");
                                        } else {
                                            ToastUtils.showToast(this, "Setup succeed！");
                                            // TODO: 2023/11/7  重启设备
                                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.restart());
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                if (length == 0) return;
                                // read
                                switch (configKeyEnum) {
                                    case KEY_MQTT_HOST:
                                        mBind.etMqttHost.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        mBind.etMqttHost.setSelection(mBind.etMqttHost.getText().length());
                                        break;
                                    case KEY_MQTT_PORT:
                                        mBind.etMqttPort.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length))));
                                        mBind.etMqttPort.setSelection(mBind.etMqttPort.getText().length());
                                        break;
                                    case KEY_MQTT_CLIENT_ID:
                                        String clientId = new String(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etMqttClientId.setText(clientId);
                                        mBind.etMqttClientId.setSelection(mBind.etMqttClientId.getText().length());
                                        break;
                                    case KEY_SUBSCRIBE_TOPIC:
                                        mBind.etMqttSubscribeTopic.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        mBind.etMqttSubscribeTopic.setSelection(mBind.etMqttSubscribeTopic.getText().length());
                                        break;
                                    case KEY_PUBLISH_TOPIC:
                                        mBind.etMqttPublishTopic.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        mBind.etMqttPublishTopic.setSelection(mBind.etMqttPublishTopic.getText().length());
                                        break;
                                    case KEY_APN:
                                        mBind.etApn.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        mBind.etApn.setSelection(mBind.etApn.getText().length());
                                        break;
                                    case KEY_NETWORK_FORMAT:
                                        networkFormatSelect = value[4] & 0xff;
                                        mBind.tvNetworkFormat.setText(netWorkFormatArray[networkFormatSelect]);
                                        break;
                                    case KEY_MQTT_CLEAN_SESSION:
                                        generalFragment.setCleanSession((value[4] & 0xff) == 1);
                                        break;
                                    case KEY_MQTT_QOS:
                                        generalFragment.setQos(value[4] & 0xff);
                                        break;
                                    case KEY_MQTT_KEEP_ALIVE:
                                        generalFragment.setKeepAlive(value[4] & 0xff);
                                        break;
                                    case KEY_MQTT_USERNAME:
                                        userFragment.setUserName(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        break;
                                    case KEY_MQTT_PASSWORD:
                                        userFragment.setPassword(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        break;
                                    case KEY_CONNECT_MODE:
                                        sslFragment.setConnectMode(value[4] & 0xff);
                                        break;
                                    case KEY_MQTT_LWT_ENABLE:
                                        lwtFragment.setLwtEnable((value[4] & 0xff) == 1);
                                        break;
                                    case KEY_MQTT_LWT_RETAIN:
                                        lwtFragment.setLwtRetain((value[4] & 0xff) == 1);
                                        break;
                                    case KEY_MQTT_LWT_TOPIC:
                                        lwtFragment.setTopic(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        break;
                                    case KEY_MQTT_LWT_PAYLOAD:
                                        lwtFragment.setPayload(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        break;
                                    case KEY_MQTT_LWT_QOS:
                                        lwtFragment.setQos(value[4] & 0xff);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void createFragment() {
        fragments = new ArrayList<>();
        generalFragment = GeneralDeviceFragment.newInstance();
        userFragment = UserDeviceFragment.newInstance();
        sslFragment = SSLDeviceFragment.newInstance();
        lwtFragment = LWTFragment.newInstance();
        fragments.add(generalFragment);
        fragments.add(userFragment);
        fragments.add(sslFragment);
        fragments.add(lwtFragment);
    }

    public void onBack(View view) {
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        finish();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.rb_general) {
            mBind.vpMqtt.setCurrentItem(0);
        } else if (checkedId == R.id.rb_user) {
            mBind.vpMqtt.setCurrentItem(1);
        } else if (checkedId == R.id.rb_ssl) {
            mBind.vpMqtt.setCurrentItem(2);
        } else if (checkedId == R.id.rb_lwt) {
            mBind.vpMqtt.setCurrentItem(3);
        }
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isParaError()) return;
        setMQTTDeviceConfig();
    }

    private boolean isParaError() {
        if (TextUtils.isEmpty(mBind.etMqttHost.getText())) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_host));
            return true;
        }
        if (TextUtils.isEmpty(mBind.etMqttPort.getText())) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_port_empty));
            return true;
        }
        String port = mBind.etMqttPort.getText().toString();
        if (Integer.parseInt(port) < 1 || Integer.parseInt(port) > 65535) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_port));
            return true;
        }
        if (TextUtils.isEmpty(mBind.etMqttClientId.getText())) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_client_id_empty));
            return true;
        }
        if (TextUtils.isEmpty(mBind.etMqttSubscribeTopic.getText())) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_topic_subscribe));
            return true;
        }
        if (TextUtils.isEmpty(mBind.etMqttPublishTopic.getText())) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_topic_publish));
            return true;
        }
        String topicSubscribe = mBind.etMqttSubscribeTopic.getText().toString();
        String topicPublish = mBind.etMqttPublishTopic.getText().toString();
        if (topicPublish.equals(topicSubscribe)) {
            ToastUtils.showToast(this, "Subscribed and published topic can't be same !");
            return true;
        }
        return !generalFragment.isValid() || !lwtFragment.isValid();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
        if (null != thread) thread.shutdown();
    }

    private void setMQTTDeviceConfig() {
        try {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>(16);
            orderTasks.add(OrderTaskAssembler.setMQTTHost(mBind.etMqttHost.getText().toString().trim()));
            orderTasks.add(OrderTaskAssembler.setMQTTPort(Integer.parseInt(mBind.etMqttPort.getText().toString().trim())));
            orderTasks.add(OrderTaskAssembler.setMQTTClientId(mBind.etMqttClientId.getText().toString().trim()));
            orderTasks.add(OrderTaskAssembler.setMQTTSubscribeTopic(mBind.etMqttSubscribeTopic.getText().toString().trim()));
            orderTasks.add(OrderTaskAssembler.setMQTTPublishTopic(mBind.etMqttPublishTopic.getText().toString().trim()));
            orderTasks.add(OrderTaskAssembler.setMQTTCleanSession(generalFragment.isCleanSession() ? 1 : 0));
            orderTasks.add(OrderTaskAssembler.setMQTTQos(generalFragment.getQos()));
            orderTasks.add(OrderTaskAssembler.setMQTTKeepAlive(generalFragment.getKeepAlive()));
            String apn = TextUtils.isEmpty(mBind.etApn.getText()) ? null : mBind.etApn.getText().toString().trim();
            orderTasks.add(OrderTaskAssembler.setApn(apn));
            orderTasks.add(OrderTaskAssembler.setNetworkFormat(networkFormatSelect));
            orderTasks.add(OrderTaskAssembler.setMQTTUsername(userFragment.getUsername()));
            orderTasks.add(OrderTaskAssembler.setMQTTPassword(userFragment.getPassword()));
            orderTasks.add(OrderTaskAssembler.setMQTTConnectMode(sslFragment.getConnectMode()));
            if (sslFragment.getConnectMode() == 2) {
                //ca证书
                File file = null;
                if (null != sslFragment.getCaPath()) file = new File(sslFragment.getCaPath());
                orderTasks.add(OrderTaskAssembler.setCA(file));
            } else if (sslFragment.getConnectMode() == 3) {
                File caFile = null;
                if (null != sslFragment.getCaPath()) caFile = new File(sslFragment.getCaPath());
                orderTasks.add(OrderTaskAssembler.setCA(caFile));
                File clientKeyFile = null;
                if (null != sslFragment.getClientKeyPath())
                    clientKeyFile = new File(sslFragment.getClientKeyPath());
                orderTasks.add(OrderTaskAssembler.setClientKey(clientKeyFile));
                File clientCertFile = null;
                if (null != sslFragment.getClientCertPath())
                    clientCertFile = new File(sslFragment.getClientCertPath());
                orderTasks.add(OrderTaskAssembler.setClientCert(clientCertFile));
            }
            orderTasks.add(OrderTaskAssembler.setMQTTLwtEnable(lwtFragment.getLwtEnable() ? 1 : 0));
            orderTasks.add(OrderTaskAssembler.setMQTTLwtRetain(lwtFragment.getLwtRetain() ? 1 : 0));
            orderTasks.add(OrderTaskAssembler.setMQTTLwtQos(lwtFragment.getQos()));
            orderTasks.add(OrderTaskAssembler.setMQTTLwtTopic(lwtFragment.getTopic()));
            orderTasks.add(OrderTaskAssembler.setMQTTLwtPayload(lwtFragment.getPayload()));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        } catch (Exception e) {
            ToastUtils.showToast(this, "File is missing");
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        dismissSyncProgressDialog();
                        AlertDialog.Builder builder = new AlertDialog.Builder(NetworkSettingsActivity.this);
                        builder.setTitle("Dismiss");
                        builder.setCancelable(false);
                        builder.setMessage("The current system of bluetooth is not available!");
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            NetworkSettingsActivity.this.setResult(RESULT_OK);
                            finish();
                        });
                        builder.show();
                    }
                }
            }
        }
    };

    public void selectCertificate(View view) {
        if (isWindowLocked()) return;
        sslFragment.selectCertificate();
    }

    public void selectCAFile(View view) {
        if (isWindowLocked()) return;
        sslFragment.selectCAFile();
    }

    public void selectKeyFile(View view) {
        if (isWindowLocked()) return;
        sslFragment.selectKeyFile();
    }

    public void selectCertFile(View view) {
        if (isWindowLocked()) return;
        sslFragment.selectCertFile();
    }

    private final ExecutorService thread = Executors.newFixedThreadPool(1);
    private ExcelHelper helper;

    public void onExportSettings(View view) {
        if (isWindowLocked()) return;
        if (isParaError()) return;
        if (null == helper) helper = new ExcelHelper();
        NetworkSettings networkBean = new NetworkSettings();
        networkBean.host = mBind.etMqttHost.getText().toString();
        networkBean.port = mBind.etMqttPort.getText().toString();
        networkBean.clientId = mBind.etMqttClientId.getText().toString();
        networkBean.subscribe = mBind.etMqttSubscribeTopic.getText().toString();
        networkBean.publish = mBind.etMqttPublishTopic.getText().toString();
        networkBean.cleanSession = generalFragment.isCleanSession();
        networkBean.qos = generalFragment.getQos();
        networkBean.keepAlive = generalFragment.getKeepAlive();
        networkBean.userName = userFragment.getUsername();
        networkBean.password = userFragment.getPassword();
        networkBean.connectMode = sslFragment.getConnectMode();
        networkBean.lwtEnable = lwtFragment.getLwtEnable();
        networkBean.lwtRetain = lwtFragment.getLwtRetain();
        networkBean.lwtQos = lwtFragment.getQos();
        networkBean.lwtTopic = lwtFragment.getTopic();
        networkBean.lwtPayload = lwtFragment.getPayload();
        networkBean.apn = TextUtils.isEmpty(mBind.etApn.getText()) ? "" : mBind.etApn.getText().toString();
        networkBean.networkFormat = networkFormatSelect;
        showLoadingProgressDialog();
        thread.execute(() -> {
            try {
                List<Map<String, String>> maps = helper.handleExcelData(networkBean);
                File file = helper.createExcel(expertFilePath);
                helper.writeToExcel(maps, file);
                runOnUiThread(() -> {
                    dismissLoadingProgressDialog();
                    ToastUtils.showToast(this, "Export success!");
                    Utils.sendEmail(this, "", "", "Settings for Device", "Choose Email Client", file);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    XLog.i(e);
                    dismissLoadingProgressDialog();
                    ToastUtils.showToast(this, "Export error");
                });
            }
        });
    }

    public void onImportSettings(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), 200);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                //得到uri，后面就是将uri转化成file的过程。
                Uri uri = data.getData();
                String paramFilePath = FileUtils.getPath(this, uri);
                if (TextUtils.isEmpty(paramFilePath)) return;
                if (!paramFilePath.endsWith(".xls")) {
                    ToastUtils.showToast(this, "Please select the correct file!");
                    return;
                }
                final File paramFile = new File(paramFilePath);
                if (null == helper) helper = new ExcelHelper();
                if (paramFile.exists()) {
                    showLoadingProgressDialog();
                    thread.execute(() -> {
                        NetworkSettings settings = helper.parseImportFile(paramFile);
                        runOnUiThread(() -> {
                            dismissLoadingProgressDialog();
                            if (null != settings) {
                                ToastUtils.showToast(this, "Import success!");
                                initData(settings);
                            } else {
                                ToastUtils.showToast(this, "Import failed!");
                            }
                        });
                    });
                } else {
                    ToastUtils.showToast(this, "File is not exists!");
                    dismissLoadingProgressDialog();
                }
            }
        }
    }

    private void initData(@NonNull NetworkSettings networkSettings) {
        mBind.etMqttHost.setText(networkSettings.host);
        mBind.etMqttPort.setText(networkSettings.port);
        mBind.etMqttClientId.setText(networkSettings.clientId);
        mBind.etMqttSubscribeTopic.setText(networkSettings.subscribe);
        mBind.etMqttPublishTopic.setText(networkSettings.publish);
        generalFragment.setCleanSession(networkSettings.cleanSession);
        generalFragment.setQos(networkSettings.qos);
        generalFragment.setKeepAlive(networkSettings.keepAlive);
        mBind.etApn.setText(networkSettings.apn);
        mBind.tvNetworkFormat.setText(netWorkFormatArray[networkSettings.networkFormat]);
        userFragment.setUserName(networkSettings.userName);
        userFragment.setPassword(networkSettings.password);
        sslFragment.setConnectMode(networkSettings.connectMode);
        lwtFragment.setLwtEnable(networkSettings.lwtEnable);
        lwtFragment.setLwtRetain(networkSettings.lwtRetain);
        lwtFragment.setQos(networkSettings.lwtQos);
        lwtFragment.setTopic(networkSettings.lwtTopic);
        lwtFragment.setPayload(networkSettings.lwtPayload);
    }

    public void onClearConfig(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage("Please confirm whether to delete all configurations in this page?");
        dialog.setConfirm("YES");
        dialog.setCancel("NO");
        dialog.setOnAlertConfirmListener(() -> {
            NetworkSettings networkSettings = new NetworkSettings();
            initData(networkSettings);
        });
        dialog.show(getSupportFragmentManager());
    }
}

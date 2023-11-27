package com.moko.support.ps101m;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.ps101m.callback.MokoScanDeviceCallback;
import com.moko.support.ps101m.entity.DeviceInfo;
import com.moko.support.ps101m.entity.OrderServices;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public final class MokoBleScanner {

    private MokoLeScanHandler mMokoLeScanHandler;
    private MokoScanDeviceCallback mMokoScanDeviceCallback;

    private Context mContext;

    public MokoBleScanner(Context context) {
        mContext = context;
    }

    public void startScanDevice(MokoScanDeviceCallback callback) {
        mMokoScanDeviceCallback = callback;
        XLog.i("Start scan");
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> scanFilterList = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setServiceData(new ParcelUuid(OrderServices.SERVICE_ADV.getUuid()), null);
        scanFilterList.add(builder.build());
//        List<ScanFilter> scanFilterList = Collections.singletonList(new ScanFilter.Builder().build());
        mMokoLeScanHandler = new MokoLeScanHandler(callback);
        scanner.startScan(scanFilterList, settings, mMokoLeScanHandler);
        callback.onStartScan();
    }

    public void stopScanDevice() {
        if (mMokoLeScanHandler != null && mMokoScanDeviceCallback != null) {
            XLog.i("End scan");
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mMokoLeScanHandler);
            mMokoScanDeviceCallback.onStopScan();
            mMokoLeScanHandler = null;
            mMokoScanDeviceCallback = null;
        }
    }

    public static class MokoLeScanHandler extends ScanCallback {

        private final MokoScanDeviceCallback callback;

        public MokoLeScanHandler(MokoScanDeviceCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            BluetoothDevice device = result.getDevice();
            ScanRecord record = result.getScanRecord();
            if (null == record) return;
            byte[] scanRecord = record.getBytes();
            String name = record.getDeviceName();
            int rssi = result.getRssi();
            if (null == scanRecord || scanRecord.length == 0 || rssi == 127) return;
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.name = name;
            deviceInfo.rssi = rssi;
            deviceInfo.mac = device.getAddress();
            deviceInfo.scanRecord = MokoUtils.bytesToHexString(scanRecord);
            deviceInfo.scanResult = result;
            callback.onScanDevice(deviceInfo);
        }
    }
}

package com.moko.mkgw4.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.mkgw4.R;
import com.moko.mkgw4.entity.AdvInfo;

public class DeviceListAdapter extends BaseQuickAdapter<AdvInfo, BaseViewHolder> {
    public DeviceListAdapter() {
        super(R.layout.mkgw4_list_item_device);
    }
    @SuppressLint("DefaultLocale")
    @Override
    protected void convert(BaseViewHolder helper, AdvInfo item) {
        final String rssi = String.format("%ddBm", item.rssi);
        helper.setText(R.id.tv_rssi, rssi);
        final String name = TextUtils.isEmpty(item.name) ? "N/A" : item.name;
        helper.setText(R.id.tv_name, name);
        helper.setText(R.id.tv_mac, String.format("MAC:%s", item.mac));
//        if (!TextUtils.isEmpty(item.uuid)) {
//            helper.setGone(R.id.layoutIBeacon, true);
//            helper.setText(R.id.tv_uuid, item.uuid);
//            helper.setText(R.id.tv_major, item.major);
//            helper.setText(R.id.tv_minor, item.minor);
//            helper.setText(R.id.tv_rssi_1m, item.rssi1M);
//        } else {
//            helper.setGone(R.id.layoutIBeacon, false);
//        }
        helper.setVisible(R.id.tv_connect, item.connectable);
        helper.addOnClickListener(R.id.tv_connect);
    }
}

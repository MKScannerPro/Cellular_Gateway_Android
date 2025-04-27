package com.moko.mkgw4.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.mkgw4.R;
import com.moko.mkgw4.entity.MokoDevice;


public class SyncDeviceAdapter extends BaseQuickAdapter<MokoDevice, BaseViewHolder> {

    public SyncDeviceAdapter() {
        super(R.layout.item_sync_device_mkgw4);
    }

    @Override
    protected void convert(BaseViewHolder helper, MokoDevice item) {
        helper.setText(R.id.tv_name, item.name);
        helper.setText(R.id.tv_mac, item.mac);
        helper.setImageResource(R.id.iv_select, item.isSelected ? R.drawable.ic_selected : R.drawable.ic_unselected);
    }
}

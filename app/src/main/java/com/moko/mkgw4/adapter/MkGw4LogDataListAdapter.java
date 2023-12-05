package com.moko.mkgw4.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.mkgw4.R;
import com.moko.mkgw4.entity.LogData;

public class MkGw4LogDataListAdapter extends BaseQuickAdapter<LogData, BaseViewHolder> {

    public MkGw4LogDataListAdapter() {
        super(R.layout.item_log_data_mkgw4);
    }

    @Override
    protected void convert(BaseViewHolder helper, LogData item) {
        helper.setText(R.id.tv_time, item.name);
        helper.setImageResource(R.id.iv_checked, item.isSelected ? R.drawable.ps101_ic_selected : R.drawable.ps101_ic_unselected);
    }
}

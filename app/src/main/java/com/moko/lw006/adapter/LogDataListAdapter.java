package com.moko.lw006.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.lw006.R;
import com.moko.lw006.entity.LogData;

public class LogDataListAdapter extends BaseQuickAdapter<LogData, BaseViewHolder> {

    public LogDataListAdapter() {
        super(R.layout.lw006_item_log_data);
    }

    @Override
    protected void convert(BaseViewHolder helper, LogData item) {
        helper.setText(R.id.tv_time, item.name);
        helper.setImageResource(R.id.iv_checked, item.isSelected ? R.drawable.lw006_ic_selected : R.drawable.lw006_ic_unselected);
    }
}

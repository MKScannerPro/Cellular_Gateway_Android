package com.moko.ps101m.adapter;

import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.ps101m.R;

/**
 * @author: jun.liu
 * @date: 2023/12/1 15:59
 * @des:
 */
public class OtherPayloadAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public OtherPayloadAdapter() {
        super(R.layout.item_other_payload);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tvTitle, "Data block " + helper.getLayoutPosition() + 1);
        if (!"000000".equals(item)) {
            EditText etType = helper.getView(R.id.et_data_type);
            EditText etMin = helper.getView(R.id.et_min);
            EditText etMax = helper.getView(R.id.et_max);
            etType.setText(item.substring(0, 2).toUpperCase());
            etMin.setText(String.valueOf(Integer.parseInt(item.substring(2, 4), 16)));
            etMax.setText(String.valueOf(Integer.parseInt(item.substring(4), 16)));
            etType.setSelection(etType.getText().length());
            etMin.setSelection(etMin.getText().length());
            etMax.setSelection(etMax.getText().length());
        }
        helper.addOnClickListener(R.id.iv_del);
    }
}

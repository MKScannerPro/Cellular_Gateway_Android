package com.moko.mkgw4.adapter;

import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.mkgw4.R;
import com.moko.mkgw4.entity.OtherTypePayloadBean;

/**
 * @author: jun.liu
 * @date: 2023/12/1 15:59
 * @des:
 */
public class OtherPayloadAdapter extends BaseQuickAdapter<OtherTypePayloadBean, BaseViewHolder> {
    public OtherPayloadAdapter() {
        super(R.layout.item_other_payload);
    }

    @Override
    protected void convert(BaseViewHolder helper, OtherTypePayloadBean item) {
        helper.setText(R.id.tvTitle, "Data block " + (helper.getAdapterPosition() + 1));
        String data = item.payload;
        EditText etType = helper.getView(R.id.et_data_type);
        EditText etMin = helper.getView(R.id.et_min);
        EditText etMax = helper.getView(R.id.et_max);
        if (!"000000".equals(item.payload)) {
            etType.setText(data.substring(0, 2).toUpperCase());
            etMin.setText(String.valueOf(Integer.parseInt(data.substring(2, 4), 16)));
            etMax.setText(String.valueOf(Integer.parseInt(data.substring(4), 16)));
            etType.setSelection(etType.getText().length());
            etMin.setSelection(etMin.getText().length());
            etMax.setSelection(etMax.getText().length());
        }else {
            etType.setText("");
            etMin.setText("");
            etMax.setText("");
        }
        helper.addOnClickListener(R.id.iv_del);
    }
}

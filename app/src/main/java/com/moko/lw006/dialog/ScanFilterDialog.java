package com.moko.lw006.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.widget.SeekBar;

import com.moko.lw006.databinding.Lw006DialogScanFilterBinding;

public class ScanFilterDialog extends BaseDialog<Lw006DialogScanFilterBinding> {
    private int filterRssi;
    private String filterName;

    @Override
    protected Lw006DialogScanFilterBinding getViewBind() {
        return Lw006DialogScanFilterBinding.inflate(getLayoutInflater());
    }

    public ScanFilterDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate() {
        mBind.tvRssi.setText(String.format("%sdBm", filterRssi + ""));
        mBind.sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rssi = progress - 127;
                mBind.tvRssi.setText(String.format("%sdBm", rssi + ""));
                filterRssi = rssi;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBind.sbRssi.setProgress(filterRssi + 127);
        if (!TextUtils.isEmpty(filterName)) {
            mBind.etFilterName.setText(filterName);
            mBind.etFilterName.setSelection(filterName.length());
        }
        setDismissEnable(true);
        mBind.ivFilterDelete.setOnClickListener(v -> mBind.etFilterName.setText(""));
        mBind.tvDone.setOnClickListener(v -> {
            listener.onDone(mBind.etFilterName.getText().toString(), filterRssi);
            dismiss();
        });
    }

    private OnScanFilterListener listener;

    public void setOnScanFilterListener(OnScanFilterListener listener) {
        this.listener = listener;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setFilterRssi(int filterRssi) {
        this.filterRssi = filterRssi;
    }

    public interface OnScanFilterListener {
        void onDone(String filterName, int filterRssi);
    }
}

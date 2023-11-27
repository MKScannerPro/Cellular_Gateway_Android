package com.moko.lw006.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;

import com.moko.lw006.R;
import com.moko.lw006.databinding.Lw006DialogChangePasswordBinding;
import com.moko.lw006.utils.ToastUtils;


public class ChangePasswordDialog extends BaseDialog<Lw006DialogChangePasswordBinding> {
    private final String FILTER_ASCII = "[ -~]*";

    private boolean passwordEnable;
    private boolean confirmPasswordEnable;

    public ChangePasswordDialog(Context context) {
        super(context);
    }

    @Override
    protected Lw006DialogChangePasswordBinding getViewBind() {
        return Lw006DialogChangePasswordBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate() {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        mBind.etPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8), filter});
        mBind.etPasswordConfirm.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8), filter});

        mBind.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEnable = count > 0;
                mBind.tvPasswordEnsure.setEnabled(passwordEnable || confirmPasswordEnable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBind.etPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordEnable = count > 0;
                mBind.tvPasswordEnsure.setEnabled(passwordEnable || confirmPasswordEnable);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBind.tvPasswordCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBind.tvPasswordEnsure.setOnClickListener(v -> {
            String password = mBind.etPassword.getText().toString();
            String passwordConfirm = mBind.etPasswordConfirm.getText().toString();
            if (password.length() != 8) {
                ToastUtils.showToast(getContext(), getContext().getString(R.string.change_password_length));
                return;
            }
            if (passwordConfirm.length() != 8) {
                ToastUtils.showToast(getContext(), getContext().getString(R.string.change_password_length));
                return;
            }
            if (!password.equals(passwordConfirm)) {
                ToastUtils.showToast(getContext(), getContext().getString(R.string.change_password_match));
                return;
            }
            dismiss();
            if (passwordClickListener != null)
                passwordClickListener.onEnsureClicked(password);
        });
    }

    private PasswordClickListener passwordClickListener;

    public void setOnPasswordClicked(PasswordClickListener passwordClickListener) {
        this.passwordClickListener = passwordClickListener;
    }

    public interface PasswordClickListener {

        void onEnsureClicked(String password);
    }

    public void showKeyboard() {
        //设置可获得焦点
        mBind.etPassword.setFocusable(true);
        mBind.etPassword.setFocusableInTouchMode(true);
        //请求获得焦点
        mBind.etPassword.requestFocus();
        //调用系统输入法
        InputMethodManager inputManager = (InputMethodManager) mBind.etPassword
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mBind.etPassword, 0);
    }
}

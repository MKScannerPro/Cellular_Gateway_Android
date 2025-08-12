package com.moko.mkgw4.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.moko.mkgw4.activity.MKGW4MainActivity;

import androidx.annotation.Nullable;

/**
 * @author: jun.liu
 * @date: 2024/4/16 15:36
 * @des:
 */
public class MkGw4NotificationActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTaskRoot()){
            Intent intent = new Intent(this, MKGW4MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(getIntent().getExtras()); // copy all extras
            startActivity(intent);
        }
        finish();
    }
}

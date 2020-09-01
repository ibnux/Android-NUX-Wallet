package com.ibnux.nuxwallet.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.databinding.ActivitySendMoneyBinding;
import com.ibnux.nuxwallet.utils.Utils;

public class SendMoneyActivity extends AppCompatActivity {
    ActivitySendMoneyBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendMoneyBinding.inflate(getLayoutInflater());
        setTitle("Send NUX");
        setContentView(binding.getRoot());
        Intent i = getIntent();
        Utils.log(i.getData().toString());
    }
}
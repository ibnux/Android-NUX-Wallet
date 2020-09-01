package com.ibnux.nuxwallet.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.fabAddDompet.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(binding.fabAddDompet==v){
            AddWalletFragment.newInstance().show(getSupportFragmentManager(),"AddWallet");
        }
    }
}
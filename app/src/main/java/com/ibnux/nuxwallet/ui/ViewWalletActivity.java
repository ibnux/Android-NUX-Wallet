package com.ibnux.nuxwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.databinding.ActivityViewWalletBinding;
import com.ibnux.nuxwallet.utils.Utils;

public class ViewWalletActivity extends AppCompatActivity {
    ActivityViewWalletBinding binding;
    String alamat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        if(!intent.hasExtra("alamat")) finish();
        alamat = intent.getStringExtra("alamat");
        Dompet dompet = ObjectBox.getDompet(alamat);
        if(dompet==null) {
            setTitle(alamat);
        }else {
            if (dompet.alamat.equals(dompet.nama)) {
                setTitle(dompet.nama);
                binding.txtWallet.setVisibility(View.GONE);
            } else {
                setTitle(dompet.nama);
                binding.txtWallet.setText(dompet.alamat);
                binding.txtWallet.setVisibility(View.VISIBLE);
            }
            binding.txtBalance.setText(Utils.nuxFormat(dompet.saldo));
        }
    }
}
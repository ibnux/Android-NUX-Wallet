package com.ibnux.nuxwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.DompetSpinnerAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.databinding.ActivitySendMoneyBinding;

public class SendMoneyActivity extends AppCompatActivity implements View.OnClickListener {
    ActivitySendMoneyBinding binding;
    String from,to;
    Dompet dompet;
    DompetSpinnerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendMoneyBinding.inflate(getLayoutInflater());
        setTitle("Send NUX");
        setContentView(binding.getRoot());

        Intent i = getIntent();
        if(i.getData()!=null) {
            //dari url
        }else if(i.hasExtra("from")){
            from = i.getStringExtra("from");
        }else if(i.hasExtra("to")){
            to = i.getStringExtra("to");
        }


        adapter = new DompetSpinnerAdapter(this, R.layout.item_dompet);
        binding.spinnerWallet.setAdapter(adapter);

        binding.btnScan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnScan){
            startActivityForResult(new Intent(this,ScanActivity.class),2345);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==2345){
                if(data.hasExtra("result")) {
                    binding.txtAlamat.setText(data.getStringExtra("result").toUpperCase());
                    binding.txtValue.requestFocus();
                }
            }
        }
    }
}
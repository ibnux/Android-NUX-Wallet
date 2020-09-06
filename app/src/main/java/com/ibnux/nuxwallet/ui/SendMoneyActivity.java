package com.ibnux.nuxwallet.ui;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.DompetSpinnerAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.databinding.ActivitySendMoneyBinding;
import com.ibnux.nuxwallet.utils.JsonCallback;
import com.ibnux.nuxwallet.utils.LongCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;

import org.json.JSONObject;

public class SendMoneyActivity extends AppCompatActivity implements View.OnClickListener {
    ActivitySendMoneyBinding binding;
    String from,to;
    Dompet dompet;
    DompetSpinnerAdapter adapter;
    boolean isDoneChekFee = false;
    boolean isSending = false;
    String transaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendMoneyBinding.inflate(getLayoutInflater());
        setTitle("Send NUX");
        setContentView(binding.getRoot());

        adapter = new DompetSpinnerAdapter(this, R.layout.item_dompet);
        binding.spinnerWallet.setAdapter(adapter);

        Intent i = getIntent();
        if(i.getData()!=null) {
            //dari url
        }else if(i.hasExtra("from")){
            from = i.getStringExtra("from");
            binding.spinnerWallet.setSelection(adapter.getPosition(from));
        }else if(i.hasExtra("to")){
            to = i.getStringExtra("to");
            binding.txtAlamat.setText(to);
            binding.spinnerWallet.setSelection(0);
            chekIsActive();
        }
        if(i.hasExtra("public_key")){
            binding.txtPK.setText(i.getStringExtra("public_key"));
            binding.cardPK.setVisibility(View.VISIBLE);
        }else {
            binding.cardPK.setVisibility(View.GONE);
        }

        binding.btnScan.setOnClickListener(this);
        binding.btnScanPK.setOnClickListener(this);
        binding.btnSend.setOnClickListener(this);


        binding.spinnerWallet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dompet = adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.txtAlamat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(binding.txtAlamat.getText().length()==24)
                    chekIsActive();
            }
        });

        binding.txtValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(binding.txtAlamat.getText().length()==24)
                    cekFee();
            }
        });

        binding.txtNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDoneChekFee = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isDoneChekFee = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
                isDoneChekFee = false;
                binding.btnSend.setText("Calculate Fee");
            }
        });
    }

    public void cekFee(){
        NuxCoin.getFee(dompet.publicKey, binding.txtAlamat.getText().toString(), binding.txtValue.getText().toString(), binding.txtNote.getText().toString(), new LongCallback() {
            @Override
            public void onLongCallback(long string) {
                Utils.log("cekFee: "+string);
                binding.txtFee.setText(String.valueOf(((int)string/100000000)));
                isDoneChekFee = true;
                binding.btnSend.setText("Send Now");
                binding.layoutStatus.setVisibility(View.GONE);
            }

            @Override
            public void onErrorCallback(int errorCode, String errorMessage) {

            }
        });
    }

    public void chekIsActive(){
        NuxCoin.getAccount(binding.txtAlamat.getText().toString(), new JsonCallback() {
            @Override
            public void onJsonCallback(JSONObject jsonObject) {
                try{
                    if(jsonObject.has("errorCode") && jsonObject.getInt("errorCode")==5){
                        binding.cardPK.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    //don't do anything
                }
            }

            @Override
            public void onErrorCallback(int errorCode, String errorMessage) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnScan) {
            startActivityForResult(new Intent(this, ScanActivity.class), 2345);
        }else if(v==binding.btnScanPK){
            startActivityForResult(new Intent(this, ScanActivity.class), 2346);
        }else if(v==binding.btnSend){
            if(binding.txtAlamat.getText().length()!=24){
                binding.txtAlamat.setError("Invalid");
                binding.txtAlamat.requestFocus();
                return;
            }
            if(binding.txtValue.getText().length()==0){
                binding.txtValue.setError("Invalid");
                binding.txtValue.requestFocus();
                return;
            }
            if(binding.btnSend.getText().toString().toLowerCase().equals("calculate fee")){
                binding.layoutStatus.setVisibility(View.VISIBLE);
                binding.txtStatus.setText("Checking Fee...");
                cekFee();
            }else{
                startActivityForResult(new Intent(this,PinActivity.class), 4268);
            }
        }
    }

    @Override
    public void finish() {
        if(!isSending)
            super.finish();
    }

    @Override
    public void onBackPressed() {
        if(!isSending)
            super.onBackPressed();
    }

    public void signingTX(String unsignedTransactionBytes, String secretPhrase){
        binding.txtStatus.setText("Offline Signing transaction");
        Intent intent = new Intent(Aplikasi.app, OfflineSigningActivity.class);
        intent.putExtra("utb",unsignedTransactionBytes);
        intent.putExtra("secret",secretPhrase);
        startActivityForResult(intent,6868);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4268){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    binding.layoutStatus.setVisibility(View.VISIBLE);
                    binding.txtStatus.setText("Sending Money...");
                    isSending = true;
                    if(binding.offlineSigning.isChecked()) {
                        NuxCoin.sendCoin(dompet, binding.txtAlamat.getText().toString(), binding.txtValue.getText().toString(),
                                binding.txtFee.getText().toString(), binding.txtNote.getText().toString(), binding.txtPK.getText().toString(), binding.txtStatus, new JsonCallback() {
                                    @Override
                                    public void onJsonCallback(JSONObject jsonObject) {
                                        isSending = false;
                                        binding.layoutStatus.setVisibility(View.GONE);
                                        try {
                                            if (jsonObject.has("unsignedTransactionBytes")) {
                                                signingTX(jsonObject.getString("unsignedTransactionBytes"), dompet.secretPhrase);
                                            } else {
                                                Utils.showToast("Sending Coin Failed!!", SendMoneyActivity.this);
                                            }
                                        } catch (Exception e) {
                                            Utils.showToast(e.getMessage(), SendMoneyActivity.this);
                                        }
                                    }

                                    @Override
                                    public void onErrorCallback(int errorCode, String errorMessage) {
                                        isSending = false;
                                        //success but not get Transaction
                                        if (errorCode == 10001) {
                                            binding.layoutStatus.setVisibility(View.GONE);
                                        } else {
                                            Utils.showToast(errorMessage, SendMoneyActivity.this);
                                        }
                                    }
                                });
                    }else {
                        NuxCoin.sendCoinOnline(dompet, binding.txtAlamat.getText().toString(), binding.txtValue.getText().toString(),
                            binding.txtFee.getText().toString(), binding.txtNote.getText().toString(), binding.txtPK.getText().toString(), binding.txtStatus, new JsonCallback() {
                                @Override
                                public void onJsonCallback(JSONObject jsonObject) {
                                    isSending = false;
                                    binding.layoutStatus.setVisibility(View.GONE);
                                    try{
                                        if(jsonObject.has("SENDCOIN") && jsonObject.getString("SENDCOIN").equals("SUCCESS")){
                                            Utils.showToast("Sending Coin Success!!\nIt will Show after network finished forging", SendMoneyActivity.this);
                                            finish();
                                        }else{
                                            Utils.showToast("Sending Coin Failed!!", SendMoneyActivity.this);
                                        }
                                    }catch (Exception e){
                                        Utils.showToast(e.getMessage(), SendMoneyActivity.this);
                                    }
                                }

                                @Override
                                public void onErrorCallback(int errorCode, String errorMessage) {
                                    isSending = false;
                                    //success but not get Transaction
                                    if(errorCode==10001){
                                        binding.layoutStatus.setVisibility(View.GONE);
                                    }else{
                                        Utils.showToast(errorMessage, SendMoneyActivity.this);
                                    }
                                }
                            });
                    }
                }
            }
        }
        if(resultCode==RESULT_OK){
            if(requestCode==2345){
                if(data.hasExtra("result")) {
                    String dt = data.getStringExtra("result");
                    if(dt.startsWith("{")){
                        try{
                            JSONObject json = new JSONObject(dt);
                            binding.txtAlamat.setText(json.getString("address"));
                            binding.txtPK.setText(json.getString("public_key"));
                        }catch (Exception e){
                            e.printStackTrace();
                            Utils.showToast("Unknown QRCode",this);
                        }
                    }else if(dt.startsWith("APK:")){
                        dt = dt.substring(4);
                        String[] dts = dt.split("APKAPKAPK");
                        binding.txtAlamat.setText(dts[0]);
                        binding.txtPK.setText(dts[1]);
                    }else {
                        binding.txtAlamat.setText(dt.toUpperCase());
                        binding.txtValue.requestFocus();
                    }
                }
            }else if(requestCode==2346){
                if(data.hasExtra("result")) {
                    String dt = data.getStringExtra("result");
                    if(dt.startsWith("{")){
                        try{
                            JSONObject json = new JSONObject(dt);
                            binding.txtAlamat.setText(json.getString("address"));
                            binding.txtPK.setText(json.getString("public_key"));
                        }catch (Exception e){
                            e.printStackTrace();
                            Utils.showToast("Unknown QRCode",this);
                        }
                    }else if(dt.startsWith("APK:")){
                        dt = dt.substring(4);
                        String[] dts = dt.split("APKAPKAPK");
                        binding.txtAlamat.setText(dts[0]);
                        binding.txtPK.setText(dts[1]);
                    }else {
                        binding.txtPK.setText(dt.toLowerCase());
                        binding.txtValue.requestFocus();
                    }
                }
            }else if(requestCode==6868){
                if(data.hasExtra("success"))
                    NuxCoin.sendingMoney(data.getStringExtra("success"),binding.txtStatus,new JsonCallback() {
                        @Override
                        public void onJsonCallback(JSONObject jsonObject) {
                            isSending = false;
                            binding.layoutStatus.setVisibility(View.GONE);
                            try{
                                if(jsonObject.has("SENDCOIN") && jsonObject.getString("SENDCOIN").equals("SUCCESS")){
                                    Utils.showToast("Sending Coin Success!!\nIt will Show after network finished forging", SendMoneyActivity.this);
                                    finish();
                                }else{
                                    Utils.showToast("Sending Coin Failed!!", SendMoneyActivity.this);
                                }
                            }catch (Exception e){
                                Utils.showToast(e.getMessage(), SendMoneyActivity.this);
                            }
                        }

                        @Override
                        public void onErrorCallback(int errorCode, String errorMessage) {
                            isSending = false;
                            //success but not get Transaction
                            if(errorCode==10001){
                                binding.layoutStatus.setVisibility(View.GONE);
                            }else{
                                Utils.showToast(errorMessage, SendMoneyActivity.this);
                            }
                        }
                    });
                else
                    Utils.showToast("Failed to sign transaction\ntry use online signing",this);
            }

        }
    }
}
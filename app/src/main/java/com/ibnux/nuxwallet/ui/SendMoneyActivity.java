package com.ibnux.nuxwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        }

        binding.btnScan.setOnClickListener(this);
        binding.btnScanPK.setOnClickListener(this);
        binding.btnSend.setOnClickListener(this);

        binding.cardPK.setVisibility(View.GONE);

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
                if(binding.txtAlamat.getText().length()==23)
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
                if(binding.txtAlamat.getText().length()==23)
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
            if(binding.txtAlamat.getText().length()==23){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4268){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    binding.layoutStatus.setVisibility(View.VISIBLE);
                    binding.txtStatus.setText("Sending Money...");
                    isSending = true;
                    NuxCoin.sendCoin(dompet, binding.txtAlamat.getText().toString(), binding.txtValue.getText().toString(),
                            binding.txtNote.getText().toString(), binding.txtStatus, new JsonCallback() {
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
        if(resultCode==RESULT_OK){
            if(requestCode==2345){
                if(data.hasExtra("result")) {
                    binding.txtAlamat.setText(data.getStringExtra("result").toUpperCase());
                    binding.txtValue.requestFocus();
                }
            }else if(requestCode==2346){
                if(data.hasExtra("result")) {
                    binding.txtPK.setText(data.getStringExtra("result").toLowerCase());
                    binding.txtValue.requestFocus();
                }
            }

        }
    }
}
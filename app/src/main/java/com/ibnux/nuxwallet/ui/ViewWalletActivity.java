package com.ibnux.nuxwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.androidnetworking.common.Priority;
import com.google.gson.Gson;
import com.ibnux.nuxwallet.Constants;
import com.ibnux.nuxwallet.adapter.TransaksiAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.databinding.ActivityViewWalletBinding;
import com.ibnux.nuxwallet.utils.JsonCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.TextCallback;
import com.ibnux.nuxwallet.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ViewWalletActivity extends AppCompatActivity implements TransaksiAdapter.TransaksiCallback,JsonCallback {
    ActivityViewWalletBinding binding;
    String alamat;
    TransaksiAdapter adapter;
    int pos = 0;
    Dompet dompet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        if(!intent.hasExtra("alamat")) finish();
        alamat = intent.getStringExtra("alamat");
        dompet = ObjectBox.getDompet(alamat);
        if(dompet==null) {
            setTitle(alamat);
            binding.txtBalance.setText("Mengambil saldo...");
            binding.txtWallet.setVisibility(View.GONE);
            NuxCoin.getAccount(alamat, Priority.HIGH, new JsonCallback() {
                @Override
                public void onJsonCallback(JSONObject jsonObject) {
                    try{
                        if(jsonObject.has("errorCode") && jsonObject.getInt("errorCode")==5){
                            binding.txtBalance.setText("Akun belum terdaftar");
                        }else if(jsonObject.has("errorDescription")){
                            binding.txtBalance.setText(jsonObject.getString("errorDescription"));
                        }else if(jsonObject.has("balanceNQT")){
                            dompet = new Dompet();
                            dompet.alamat = alamat;
                            dompet.dompetID = jsonObject.getString("account");
                            dompet.saldo = jsonObject.getLong("balanceNQT");
                            dompet.isMe = false;
                            binding.txtBalance.setText(Utils.nuxFormat(dompet.saldo));
                            NuxCoin.getPublicKey(alamat, Priority.LOW, new TextCallback() {
                                @Override
                                public void onTextCallback(String string) {
                                    dompet.publicKey = string;
                                }

                                @Override
                                public void onErrorCallback(int errorCode, String errorMessage) {

                                }
                            });
                        }
                    }catch (Exception e){
                        binding.txtBalance.setText("Mengambil saldo...");
                    }
                }

                @Override
                public void onErrorCallback(int errorCode, String errorMessage) {

                }
            });
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

        adapter = new TransaksiAdapter(this,alamat);
        binding.listTransaksi.setHasFixedSize(true);
        binding.listTransaksi.setLayoutManager(new LinearLayoutManager(this));
        binding.listTransaksi.setAdapter(adapter);
        getTransaksi();
    }

    public void getTransaksi(){
        NuxCoin.getTransactions(alamat, pos, Constants.limitGetTX, this);
    }

    @Override
    public void onJsonCallback(JSONObject jsonObject) {
        try{
            if(jsonObject.has("transactions")){
                JSONArray transactions = jsonObject.getJSONArray("transactions");
                int jml = transactions.length();
                if(jml>0) {
                    int ada = 0;
                    for (int n = 0; n < jml; n++) {
                        //TODO delete log
                        Utils.log(transactions.getJSONObject(n).toString());
                        JSONObject json = transactions.getJSONObject(n);
                        Transaksi tx = new Gson().fromJson(json.toString(),Transaksi.class);
                        if(ObjectBox.getTransaksi().query().equal(Transaksi_.transaction,tx.transaction).build().findFirst()==null) {
                            tx.timestampInsert = System.currentTimeMillis();
                            if (json.has("attachment")) {
                                if (json.getJSONObject("attachment").has("message")) {
                                    tx.message = json.getJSONObject("attachment").getString("message");
                                }
                            }
                            ObjectBox.addTransaksi(tx);
                        }else{
                            ada++;
                        }
                    }
                    if(ada==0) {
                        pos += Constants.limitGetTX;
                        getTransaksi();
                        adapter.reload();
                    }else{
                        binding.progressBar.setVisibility(View.GONE);
                    }
                }else{
                    binding.progressBar.setVisibility(View.GONE);
                }
            }else{
                binding.progressBar.setVisibility(View.GONE);
            }
        }catch (Exception e){
            Utils.showToast("Gagal parsing data",this);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onErrorCallback(int errorCode, String errorMessage) {
        binding.progressBar.setVisibility(View.GONE);
        Utils.showToast(errorMessage,this);
    }

    @Override
    public void onTransaksiClicked(Transaksi transaksi) {

    }

    @Override
    public void onTransaksiWalletClicked(String alamat) {
        Intent intent = new Intent(this, ViewWalletActivity.class);
        intent.putExtra("alamat",alamat);
        startActivity(intent);
    }
}
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

public class ViewWalletActivity extends AppCompatActivity implements View.OnClickListener, TransaksiAdapter.TransaksiCallback,JsonCallback {
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
        setTitle("NUX COIN");
        Intent intent = getIntent();
        if(!intent.hasExtra("alamat")) finish();
        alamat = intent.getStringExtra("alamat");
        binding.txtWallet.setText(alamat);
        dompet = ObjectBox.getDompet(alamat);
        if(dompet==null) {
            binding.txtBalance.setText("Mengambil saldo...");
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
            if(dompet.alamat.equals(dompet.nama)) {
                binding.txtWalletName.setVisibility(View.GONE);
            }else{
                binding.txtWalletName.setVisibility(View.VISIBLE);
                if(dompet.nama!=null)
                    binding.txtWalletName.setText(dompet.nama);
                else
                    binding.txtWalletName.setText("");
            }
            if(dompet.catatan!=null)
                binding.txtWalletNote.setText(dompet.catatan);
            else
                binding.txtWalletNote.setText("");

            binding.txtBalance.setText(Utils.nuxFormat(dompet.saldo));
        }

        adapter = new TransaksiAdapter(this,alamat);
        binding.listTransaksi.setHasFixedSize(true);
        binding.listTransaksi.setLayoutManager(new LinearLayoutManager(this));
        binding.listTransaksi.setAdapter(adapter);

        binding.btnBarcode.setOnClickListener(this);
        binding.btnSend.setOnClickListener(this);
        getTransaksi();
    }

    @Override
    public void onClick(View v) {
        if(v==binding.btnBarcode){
            Intent i = new Intent(this, QRCodeActivity.class);
            i.putExtra("alamat",alamat);
            startActivity(i);
        }else if(v==binding.btnSend){
            Intent i = new Intent(this, SendMoneyActivity.class);
            if(dompet.isMe)
                i.putExtra("from",alamat);
            else
                i.putExtra("to",alamat);
            startActivity(i);
        }
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
package com.ibnux.nuxwallet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.utils.Utils;

import java.util.List;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.MyViewHolder> {
    TransaksiCallback callback;
    String alamat;
    private List<Transaksi> datas;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtWallet,txtBalance,txtTanggal,txtCatatan;
        ImageView imageStatus;
        LinearLayout layout;
        public MyViewHolder(View v) {
            super(v);
            txtWallet = v.findViewById(R.id.txtWallet);
            txtBalance = v.findViewById(R.id.txtBalance);
            txtTanggal = v.findViewById(R.id.txtTanggal);
            imageStatus = v.findViewById(R.id.imageStatus);
            txtCatatan = v.findViewById(R.id.txtCatatan);
            layout = v.findViewById(R.id.layout);
        }
    }

    public TransaksiAdapter(TransaksiCallback callback, String alamat){
        this.callback = callback;
        this.alamat = alamat;
        reload();
    }

    public void reload(){
        datas = ObjectBox.getTransaksi().query()
                .equal(Transaksi_.recipientRS,alamat)
                .or()
                .equal(Transaksi_.senderRS,alamat)
                .orderDesc(Transaksi_.timestamp).build().find();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransaksiAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Transaksi tx = datas.get(position);
        holder.txtBalance.setText(Utils.nuxFormat(Long.parseLong(tx.amountNQT)));
        if(tx.recipientRS.equals(alamat)){
            holder.imageStatus.setImageResource(R.drawable.ic_coin_received);
            holder.txtWallet.setText(tx.senderRS);
        }else {
            holder.imageStatus.setImageResource(R.drawable.ic_coin_send);
            holder.txtWallet.setText(tx.recipientRS);
        }
        holder.txtWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null)
                    callback.onTransaksiWalletClicked(holder.txtWallet.getText().toString());
            }
        });
        if(Aplikasi.unixtime!=0L) {
            holder.txtTanggal.setVisibility(View.VISIBLE);
            holder.txtTanggal.setText(Utils.getDate(Aplikasi.unixtime + tx.blockTimestamp, "dd/MM/yyyy HH:mm"));
        }else{
            holder.txtTanggal.setVisibility(View.GONE);
        }
        if(tx.message!=null){
            holder.txtCatatan.setVisibility(View.VISIBLE);
            holder.txtCatatan.setText(tx.message);
        }else{
            holder.txtCatatan.setVisibility(View.GONE);
        }
    }

    public Transaksi getData(int pos){
        return datas.get(pos);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public interface TransaksiCallback {
        void onTransaksiClicked(Transaksi transaksi);
        void onTransaksiWalletClicked(String alamat);
    }
}

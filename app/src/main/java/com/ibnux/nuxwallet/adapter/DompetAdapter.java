package com.ibnux.nuxwallet.adapter;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.common.Priority;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.Dompet_;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.utils.JsonCallback;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;

import org.json.JSONObject;

import java.util.List;

public class DompetAdapter extends RecyclerView.Adapter<DompetAdapter.MyViewHolder> {
    DompetCallback callback;
    private List<Dompet> datas;
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtWallet,txtBalance,txtWalletName,txtWalletNote;
        LinearLayout layout;
        CardView card;
        public MyViewHolder(View v) {
            super(v);
            txtWallet = v.findViewById(R.id.txtWallet);
            txtBalance = v.findViewById(R.id.txtBalance);
            txtWalletName = v.findViewById(R.id.txtWalletName);
            txtWalletNote = v.findViewById(R.id.txtWalletNote);
            layout = v.findViewById(R.id.layout);
            card = v.findViewById(R.id.card);
        }
    }

    public DompetAdapter(DompetCallback callback, boolean isMe){
        this.callback = callback;
        reload(isMe);
    }

    public void reload(boolean isMe){
        datas = ObjectBox.getDompet().query().equal(Dompet_.isMe,isMe).orderDesc(Dompet_.saldo).build().find();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DompetAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Dompet dompet = datas.get(position);
        holder.txtBalance.setText(Utils.nuxFormat(dompet.saldo));
        holder.txtWallet.setText(dompet.alamat);
        if(dompet.alamat.equals(dompet.nama)) {
            holder.txtWalletName.setVisibility(View.GONE);
        }else{
            holder.txtWalletName.setVisibility(View.VISIBLE);
            if(dompet.nama!=null)
                holder.txtWalletName.setText(dompet.nama);
            else
                holder.txtWalletName.setText("");
        }
        if(dompet.catatan!=null)
            holder.txtWalletNote.setText(dompet.catatan);
        else
            holder.txtWalletNote.setText("");
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null)
                    callback.onDompetClicked(dompet);
            }
        });

        if(dompet.isMe){
            holder.card.setCardBackgroundColor(ContextCompat.getColor(Aplikasi.app,R.color.blue_500));
        }else{
            holder.card.setCardBackgroundColor(ContextCompat.getColor(Aplikasi.app,R.color.red_400));
        }

        NuxCoin.getAccount(dompet.alamat, Priority.LOW, new JsonCallback() {
            @Override
            public void onJsonCallback(JSONObject jsonObject) {
                try{
                    if(jsonObject.has("balanceNQT")){
                        if(dompet.saldo!=jsonObject.getLong("balanceNQT")) {
                            dompet.saldo = jsonObject.getLong("balanceNQT");
                            holder.txtBalance.setText(Utils.nuxFormat(dompet.saldo));
                            notifyDataSetChanged();
                            ObjectBox.addDompet(dompet);
                        }
                    }else if(jsonObject.has("errorCode") && jsonObject.getInt("errorCode")==5){
                        holder.txtBalance.setText("Wallet not registered");
                    }else  if(jsonObject.has("errorDescription")){
                        holder.txtBalance.setText(jsonObject.getString("errorDescription"));
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

    public Dompet getData(int pos){
        return datas.get(pos);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public interface DompetCallback {
        void onDompetClicked(Dompet dompet);
    }


}

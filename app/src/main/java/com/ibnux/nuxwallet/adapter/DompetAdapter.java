package com.ibnux.nuxwallet.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.common.Priority;
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
        TextView txtWallet,txtBalance,txtWalletBottom;
        LinearLayout layout;
        public MyViewHolder(View v) {
            super(v);
            txtWallet = v.findViewById(R.id.txtWallet);
            txtBalance = v.findViewById(R.id.txtBalance);
            txtWalletBottom = v.findViewById(R.id.txtWalletBottom);
            layout = v.findViewById(R.id.layout);
        }
    }

    public DompetAdapter(DompetCallback callback){
        this.callback = callback;
        reload();
    }

    public void reload(){
        datas = ObjectBox.getDompet().query().equal(Dompet_.isMe,true).orderDesc(Dompet_.saldo).build().find();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DompetAdapter.MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dompet, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Dompet dompet = datas.get(position);
        holder.txtBalance.setText(Utils.nuxFormat(dompet.saldo));
        if(dompet.alamat.equals(dompet.nama)) {
            holder.txtWallet.setText(dompet.alamat);
            holder.txtWalletBottom.setVisibility(View.GONE);
        }else{
            holder.txtWallet.setText(dompet.nama);
            holder.txtWalletBottom.setVisibility(View.VISIBLE);
            holder.txtWalletBottom.setText(dompet.alamat);
        }
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null)
                    callback.onDompetClicked(dompet);
            }
        });

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
                        holder.txtBalance.setText("Akun belum terdaftar");
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

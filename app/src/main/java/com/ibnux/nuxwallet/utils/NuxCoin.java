package com.ibnux.nuxwallet.utils;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NuxCoin {

    /*
    {
        "errorDescription": "Incorrect transactionBytes: null",
        "errorCode": 4,
        "requestProcessingTime": 5,
        "error": "java.nio.BufferUnderflowException"
    }
     */

    public static void getAccount(String alamat, JsonCallback callback) {
        getAccount(alamat,Priority.MEDIUM,callback);
    }

    public static void getAccount(String alamat, Priority priority, JsonCallback callback){
        Utils.log("getAccount: "+alamat);
        String server = ObjectBox.getServer();
        Utils.log("getAccount "+server+" "+alamat);
        AndroidNetworking.get(server+"/nxt?requestType=getAccount&account="+alamat)
                .setPriority(priority)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if(callback!=null){
                            callback.onJsonCallback(response);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        if(callback!=null){
                            callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                        }
                    }
                });
    }

    public static void getPublicKey(String alamat, Priority priority, TextCallback callback){
        String server = ObjectBox.getServer();
        Utils.log("getAccount "+server+" "+alamat);
        AndroidNetworking.get(server+"/nxt?requestType=getAccountPublicKey&account="+alamat)
                .setPriority(priority)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if(callback!=null){
                            try {
                                callback.onTextCallback(response.getString("publicKey"));
                            }catch (Exception e){
                                callback.onTextCallback(null);
                            }
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        if(callback!=null){
                            callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                        }
                    }
                });
    }

    public static void getTime(LongCallback callback){
        String server = ObjectBox.getServer();
        Utils.log("getTime");
        AndroidNetworking.get(server+"/nxt?requestType=getTime")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Utils.log("getTime: "+response.toString());
                            long waktu = (response.getLong("unixtime")-response.getLong("time"))*1000L;
                            Utils.log("getTime: "+waktu);
                            if(waktu>0) {
                                Aplikasi.sp.edit().putLong("unixtime", waktu).apply();
                                Aplikasi.unixtime = waktu;
                                if(callback!=null) callback.onLongCallback(waktu);
                            }
                        }catch (Exception e){
                            if(callback!=null) callback.onErrorCallback(1, e.getMessage());
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        if(callback!=null){
                            callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                        }
                    }
                });
    }

    public static void getTransactions(String alamat, int from, int limit, JsonCallback callback){
        String server = ObjectBox.getServer();
        Utils.log("getTransactions "+server+" "+alamat);
        AndroidNetworking.get(server+"/nxt")
                .addQueryParameter("requestType","getBlockchainTransactions")
                .addQueryParameter("account",alamat)
                .addQueryParameter("firstIndex",from+"")
                .addQueryParameter("lastIndex",limit+"")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (callback != null) {
                                if (!response.has("errorDescription")) {
                                    callback.onJsonCallback(response);
                                } else {
                                    callback.onErrorCallback(response.getInt("errorCode"), response.getString("errorDescription"));
                                }
                            }
                        }catch (Exception e){
                            callback.onErrorCallback(1, e.getMessage());
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        if(callback!=null){
                            callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                        }
                    }
                });
    }

    public static void sendCoinOnline(Dompet fromDompet, String toDompet, long jumlah, JsonCallback callback){
        String server = ObjectBox.getServer();
        Map<String, Object> body = new HashMap<>();
        body.put("recipient",toDompet);
        body.put("amountNQT",jumlah);
        body.put("feeNQT","2");
        body.put("deadline","60");
        body.put("secretPhrase",fromDompet.secretPhrase);
        AndroidNetworking.post(server+"/nxt?requestType=sendMoney")
                .addBodyParameter(body)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if(callback!=null)
                            callback.onJsonCallback(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        if(callback!=null){
                            callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                        }
                    }

                });

    }

    // SENDCOIN STEP 1
    public static void sendCoin(Dompet fromDompet, String toDompet, String jumlah, String message, TextView progress, JsonCallback callback){
        Utils.log("SendCoin to "+toDompet+" "+jumlah+" "+message);
        if(jumlah.length()<8) jumlah += "00000000";
        String server = ObjectBox.getServer();
        if(progress!=null) progress.setText("Requesting transaction...");
        Map<String, Object> body = new HashMap<>();
        body.put("recipient", toDompet);
        body.put("amountNQT", jumlah);
        body.put("feeNQT", "0");
        body.put("deadline","60");
        if(message!=null && message.length()>0)
            body.put("message",message);
        body.put("publicKey",fromDompet.publicKey);
        AndroidNetworking.post(server+"/nxt?requestType=sendMoney")
                .addBodyParameter(body)
        .build()
        .getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("unsignedTransactionBytes")) {
                        signingTransaction(fromDompet, response.getString("unsignedTransactionBytes"), progress, callback);
                    }else{
                        if(callback!=null){
                            callback.onErrorCallback(response.getInt("errorCode"), response.getString("errorDescription"));
                        }
                    }
                }catch (Exception e){
                    if(callback!=null){
                        callback.onErrorCallback(1, e.getMessage());
                    }
                }
            }

            @Override
            public void onError(ANError error) {
                if(callback!=null){
                    callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                }
            }
        });

    }
    // SENDCOIN STEP 2
    public static void signingTransaction(Dompet fromDompet,String unsignedTransactionBytes, TextView progress, JsonCallback callback){
        Utils.log("signingTransaction to "+unsignedTransactionBytes);
        String server = ObjectBox.getServer();
        if(progress!=null) progress.setText("Signing transaction...");
        AndroidNetworking.get(server+"/nxt")
                .addQueryParameter("requestType","signTransaction")
                .addQueryParameter("unsignedTransactionBytes",unsignedTransactionBytes)
                .addQueryParameter("secretPhrase",fromDompet.secretPhrase)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if(response.has("errorCode")){
                                if(callback!=null){
                                    callback.onErrorCallback(response.getInt("errorCode"), response.getString("errorDescription"));
                                }
                            }else{
                                if(response.has("transactionBytes")){
                                    sendingMoney(response.getString("transactionBytes"), progress, callback);
                                }
                            }
                        }catch (Exception e){
                            if(callback!=null){
                                callback.onErrorCallback(1, e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        if(callback!=null){
                            callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                        }
                    }
                });
    }
    // SENDCOIN STEP 3
    public static void sendingMoney(String transactionBytes, TextView progress, JsonCallback callback){
        Utils.log("sendingMoney to "+transactionBytes);
        String server = ObjectBox.getServer();
        if(progress!=null) progress.setText("Submit transaction...");
        AndroidNetworking.post(server+"/nxt?requestType=broadcastTransaction")
                .addBodyParameter("transactionBytes",transactionBytes)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Utils.log("sendingMoney Success ");
                        try{
                            Utils.log("sendingMoney "+response.toString());
                            if(response.has("errorCode")){
                                Utils.log("sendingMoney "+response.toString());
                                if(callback!=null){
                                    callback.onErrorCallback(response.getInt("errorCode"), response.getString("errorDescription"));
                                }
                            }else{
                                if(response.has("transaction")){
                                    getTransaction(response.getString("transaction"), progress, callback);
                                }
                            }
                        }catch (Exception e){
                            if(callback!=null){
                                callback.onErrorCallback(10001, e.getMessage());
                            }
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Utils.log("sendingMoney error ");
                        if(callback!=null){
                            callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                        }
                    }
                });
    }

    // SENDCOIN STEP 4
    public static void getTransaction(String transaction, TextView progress, JsonCallback callback){
        Utils.log("getTransaction  "+transaction);
        String server = ObjectBox.getServer();
        if(progress!=null) progress.setText("Sending Coin Success!!\n" +
                "Getting transaction detail...");
        AndroidNetworking.get(server+"/nxt?requestType=getTransaction")
                .addQueryParameter("transaction",transaction)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            response.put("SENDCOIN","SUCCESS");
                            if(response.has("errorCode")){
                                if(callback!=null){
                                    callback.onJsonCallback(response);
                                }
                            }else{
                                Transaksi tx = new Gson().fromJson(response.toString(),Transaksi.class);
                                tx.timestampInsert = System.currentTimeMillis();
                                if(response.has("attachment")){
                                    if(response.getJSONObject("attachment").has("message")){
                                        tx.message = response.getJSONObject("attachment").getString("message");
                                    }
                                }
                                ObjectBox.addTransaksi(tx);
                                callback.onJsonCallback(response);
                            }
                        }catch (Exception e){
                            if(callback!=null){
                                callback.onErrorCallback(0, "Sending COIN SUCCESS, but get Transaction failed");
                            }
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("SENDCOIN", "SUCCESS");
                            callback.onJsonCallback(json);
                        }catch (Exception e){
                            callback.onErrorCallback(0, "Sending COIN SUCCESS, but get Transaction failed");
                        }
                    }
                });
    }

    public static void getFee(String publicKey, String toDompet, String jumlah, String message, LongCallback callback){
        if(jumlah.length()<8) jumlah += "00000000";
        String server = ObjectBox.getServer();
        Map<String, Object> body = new HashMap<>();
        body.put("recipient",toDompet);
        body.put("amountNQT",jumlah);
        body.put("feeNQT","0");
        if(message!=null && message.length()>0)
            body.put("message",message);
        body.put("deadline","60");
        body.put("publicKey",publicKey);
        Utils.log("getFee: "+body.toString());
        AndroidNetworking.post(server+"/nxt?requestType=sendMoney")
                .addBodyParameter(body)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(callback!=null){
                            try {
                                Utils.log(response.toString());
                                    callback.onLongCallback(response.getJSONObject("transactionJSON").getLong("feeNQT"));
                            }catch (Exception e){
                                callback.onLongCallback(0L);
                            }
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        if(callback!=null){
                            callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                        }
                    }
                });


    }
}

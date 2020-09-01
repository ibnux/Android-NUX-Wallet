package com.ibnux.nuxwallet.utils;

import android.app.AlertDialog;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
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

    public static void getAccount(String server, String alamat, JsonCallback callback) {
        getAccount(server,alamat,Priority.MEDIUM,callback);
    }

    public static void getAccount(String server, String alamat, Priority priority, JsonCallback callback){
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

    public static void getAccountID(String server, String secret, JsonCallback callback){
        AndroidNetworking.get(server+"/nxt?requestType=getAccountId&secretPhrase="+secret)
                .setPriority(Priority.MEDIUM)
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

    public static void sendCoinOnline(String server, Dompet fromDompet, String toDompet, long jumlah, JsonCallback callback){
        Map<String, Object> body = new HashMap<>();
        body.put("recipient",toDompet);
        body.put("amountNQT",jumlah);
        body.put("feeNQT",2);
        body.put("deadline",60);
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
    public static void sendCoin(String server, Dompet fromDompet, String toDompet, long jumlah, String message, AlertDialog progress, JsonCallback callback){
        if(progress!=null) progress.setMessage("Requesting transaction...");
        Map<String, Object> body = new HashMap<>();
        body.put("recipient", toDompet);
        body.put("amountNQT", jumlah);
        body.put("feeNQT", 0);
        body.put("deadline",60);
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
                        signingTransaction(server, fromDompet, response.getString("unsignedTransactionBytes"), progress, callback);
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
    public static void signingTransaction(String server, Dompet fromDompet,String unsignedTransactionBytes, AlertDialog progress, JsonCallback callback){
        if(progress!=null) progress.setMessage("Signing transaction...");
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
                                    sendingMoney(server, response.getString("transactionBytes"), progress, callback);
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
    public static void sendingMoney(String server,String transactionBytes, AlertDialog progress, JsonCallback callback){
        if(progress!=null) progress.setMessage("Submit transaction...");
        AndroidNetworking.post(server+"/nxt?requestType=broadcastTransaction")
                .addBodyParameter("transactionBytes",transactionBytes)
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
                                    getTransaction(server, response.getString("transaction"), progress, callback);
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

    // SENDCOIN STEP 4
    public static void getTransaction(String server,String transaction, AlertDialog progress, JsonCallback callback){
        if(progress!=null) progress.setMessage("Sending Coin Success!!\n" +
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

    public static void getFee(String server, Dompet fromDompet, String toDompet, long jumlah, String message, LongCallback callback){
        Map<String, Object> body = new HashMap<>();
        body.put("recipient",toDompet);
        body.put("amountNQT",jumlah);
        body.put("feeNQT",0);
        if(message!=null && message.length()>0)
            body.put("message",message);
        body.put("deadline",60);
        body.put("publicKey",fromDompet.publicKey);
        AndroidNetworking.post(server+"/nxt?requestType=sendMoney")
                .addBodyParameter(body)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(callback!=null){
                            try {
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

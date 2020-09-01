package com.ibnux.nuxwallet.utils;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.ibnux.nuxwallet.data.Dompet;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NuxCoin {

    public static void getAccount(String server, String alamat, JsonCallback callback){
        AndroidNetworking.get(server+"/nxt?requestType=getAccount&account="+alamat)
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

    public static void sendMoneyOnline(String server, Dompet fromDompet, String toDompet, long jumlah, JsonCallback callback){
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

    public static void sendMoney(String server, Dompet fromDompet, String toDompet, long jumlah, JsonCallback callback){
        Map<String, Object> body = new HashMap<>();
        body.put("recipient",toDompet);
        body.put("amountNQT",jumlah);
        body.put("feeNQT",2);
        body.put("deadline",60);
        body.put("publicKey",fromDompet.publicKey);
        AndroidNetworking.post(server+"/nxt?requestType=sendMoney")
                .addBodyParameter(body)
        .build()
        .getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                signingTransaction(server,fromDompet,toDompet, response, callback);
            }

            @Override
            public void onError(ANError error) {
                if(callback!=null){
                    callback.onErrorCallback(error.getErrorCode(), error.getErrorBody());
                }
            }
        });

    }

    public static void signingTransaction(String server, Dompet fromDompet, String toDompet,JSONObject response, JsonCallback callback){

    }

    public static void getFee(String server, Dompet fromDompet, String toDompet, long jumlah, LongCallback callback){
        Map<String, Object> body = new HashMap<>();
        body.put("recipient",toDompet);
        body.put("amountNQT",jumlah);
        body.put("feeNQT",2);
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

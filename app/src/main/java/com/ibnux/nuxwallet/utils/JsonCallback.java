package com.ibnux.nuxwallet.utils;

import org.json.JSONObject;

public interface JsonCallback {
    public void onJsonCallback(JSONObject jsonObject);
    public void onErrorCallback(int errorCode, String errorMessage);

}

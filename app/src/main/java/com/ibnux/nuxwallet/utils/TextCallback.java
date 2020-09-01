package com.ibnux.nuxwallet.utils;

public interface TextCallback {
    public void onTextCallback(String string);
    public void onErrorCallback(int errorCode, String errorMessage);

}

package com.ibnux.nuxwallet;


import android.content.SharedPreferences;

import androidx.multidex.MultiDexApplication;

import com.androidnetworking.AndroidNetworking;
import com.ibnux.nuxwallet.data.ObjectBox;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class Aplikasi extends MultiDexApplication {
    public static  SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        ObjectBox.init(this);
        AndroidNetworking.initialize(getApplicationContext());
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(true) //default: true
                .trackActivities(true) //default: false
                .minTimeBetweenCrashesMs(2000) //default: 3000
                .errorDrawable(R.mipmap.ic_launcher)
                .apply();
        sp = getSharedPreferences("settings",0);
    }
}

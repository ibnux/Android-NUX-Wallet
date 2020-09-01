package com.ibnux.nuxwallet.data;

import android.content.Context;

import com.ibnux.nuxwallet.Aplikasi;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class ObjectBox {
    private static BoxStore boxStore;

    public static void init(Context context) {
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .build();
    }

    public static BoxStore get() { return boxStore; }

    public static Box<Dompet> getDompet(){
        return get().boxFor(Dompet.class);
    }

    public static Box<Server> getBoxServer(){
        return get().boxFor(Server.class);
    }

    public static long addDompet(Dompet dompet){
        Dompet ada = getDompet().query().equal(Dompet_.alamat,dompet.alamat).build().findFirst();
        if(ada!=null){
            getDompet().remove(ada);
        }
        return getDompet().put(dompet);
    }

    public static Dompet getDompet(String alamat){
        return getDompet().query().equal(Dompet_.alamat,alamat).build().findFirst();
    }

    public static String getServer(){
        return Aplikasi.sp.getString("server","https://coin.ibnux.net");
    }
}

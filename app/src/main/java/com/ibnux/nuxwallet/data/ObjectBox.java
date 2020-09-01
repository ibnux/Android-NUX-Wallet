package com.ibnux.nuxwallet.data;

import android.content.Context;

import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.Constants;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class ObjectBox {
    private static BoxStore boxStore;
    private static Box<Dompet> boxDompet;
    private static Box<Server> boxServer;
    private static Box<Transaksi> boxTransaksi;

    public static void init(Context context) {
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .build();
    }

    public static BoxStore get() { return boxStore; }

    public static Box<Dompet> getDompet(){
        return (boxDompet==null)? get().boxFor(Dompet.class) : boxDompet;
    }

    public static Box<Server> getBoxServer(){
        return (boxServer==null)? get().boxFor(Server.class):boxServer;
    }

    public static Box<Transaksi> getTransaksi(){
        return (boxTransaksi==null)?get().boxFor(Transaksi.class):boxTransaksi;
    }

    public static long addDompet(Dompet dompet){
        Dompet ada = getDompet().query().equal(Dompet_.alamat,dompet.alamat).build().findFirst();
        if(ada!=null){
            dompet.id = ada.id;
        }
        return getDompet().put(dompet);
    }

    public static Dompet getDompet(String alamat){
        return getDompet().query().equal(Dompet_.alamat,alamat).build().findFirst();
    }

    public static long addTransaksi(Transaksi transaksi){
        Transaksi ada = getTransaksi().query().equal(Transaksi_.transaction,transaksi.transaction).build().findFirst();
        if(ada!=null){
            getTransaksi().remove(ada);
        }
        return getTransaksi().put(transaksi);
    }

    public static String getServer(){
        return Aplikasi.sp.getString("server", Constants.defaultServer);
    }


}

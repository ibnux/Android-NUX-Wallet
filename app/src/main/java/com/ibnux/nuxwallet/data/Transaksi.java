package com.ibnux.nuxwallet.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Transaksi {
    @Id
    public long id;
    public long jumlah;
    public long tanggal;
    public String tanggalText;
    public String catatan;
}

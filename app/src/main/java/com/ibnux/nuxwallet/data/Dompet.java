package com.ibnux.nuxwallet.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Dompet {
    @Id
    public long id;
    public String nama;
    public String catatan;
    public long saldo;
    public String dompetID;
    public String alamat;
    public String publicKey;
    public boolean isMe;
    public String secretPhrase;

    public boolean isMe(){
        return (secretPhrase!=null && !secretPhrase.isEmpty());
    }
}

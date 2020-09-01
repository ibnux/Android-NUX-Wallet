package com.ibnux.nuxwallet.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Transaksi {
    @Id
    public long id;
    public String transaction;
    public String senderRS;
    public String recipientRS;
    public String senderPublicKey;
    public long blockTimestamp;
    public long timestamp;
    public long timestampInsert;
    public String amountNQT;
    public String feeNQT;
    public String message;
    public String sender;
    public String recipient;
    public String signature;
    public String block;
    public String ecBlockId;
    public int confirmations;
    public int deadline;
    public long height;
    public int type;
    public int subtype;
}

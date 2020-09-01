package com.ibnux.nuxwallet.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Server {
    @Id
    public long id;
    public String url;
    public String nama;
}

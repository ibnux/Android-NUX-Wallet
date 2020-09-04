package com.ibnux.nuxwallet.ui;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ibnux.nuxwallet.adapter.ServerAdapter;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Server;
import com.ibnux.nuxwallet.databinding.ActivityPeersBinding;

public class PeersActivity extends AppCompatActivity implements ServerAdapter.ServerCallback, View.OnClickListener{
    ActivityPeersBinding binding;
    ServerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPeersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("SERVER");

        adapter = new ServerAdapter(this);
        binding.recycleView.setHasFixedSize(true);
        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));
        binding.recycleView.setAdapter(adapter);

        binding.fabAddPeer.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v==binding.fabAddPeer){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add peer URL?");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
            input.setHint("http://coin.ibnux.net:1234");
            input.setSelectAllOnFocus(true);
            builder.setView(input);
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String url = input.getText().toString();
                    if(!url.isEmpty()) {
                        if (url.endsWith("/")) {
                            url = url.substring(0, url.length() - 1);
                        }
                        ObjectBox.addServer(url);
                        adapter.reload();
                    }
                }
            });
            builder.setNegativeButton("Cancel",null);
            builder.show();
        }
    }

    @Override
    public void onServerClicked(Server server) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Server URL?");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
        input.setHint("http://coin.ibnux.net:1234");
        input.setText(server.url);
        input.setSelectAllOnFocus(true);
        builder.setView(input);
        builder.setPositiveButton("Save changes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = input.getText().toString();
                if(!url.isEmpty()) {
                    if (url.endsWith("/")) {
                        url = url.substring(0, url.length() - 1);
                    }
                    server.url = url;
                    ObjectBox.getBoxServer().put(server);
                    adapter.reload();
                }
            }
        });
        builder.setNegativeButton("Set as default server", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ObjectBox.setServer(server.url);
                finish();
            }
        });
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ObjectBox.getBoxServer().remove(server);
                adapter.reload();
            }
        });
        builder.show();
    }
}
package com.ibnux.nuxwallet.ui;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.DompetAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.databinding.ActivityHomeBinding;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, DompetAdapter.DompetCallback,TabLayout.OnTabSelectedListener {
    ActivityHomeBinding binding;
    DompetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.fabAddDompet.setOnClickListener(this);

        adapter = new DompetAdapter(this, true);

        binding.listDompet.setHasFixedSize(true);
        binding.listDompet.setLayoutManager(new LinearLayoutManager(this));
        binding.listDompet.setAdapter(adapter);

        binding.txtServer.setText(ObjectBox.getServer());
        binding.txtServer.setOnClickListener(this);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.listDompet);
        if(Aplikasi.unixtime==0L){
            NuxCoin.getTime(null);
        }

        binding.tabLayout.addOnTabSelectedListener(this);

        //startActivityForResult(new Intent(this,PinActivity.class), 4268);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4268){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    Aplikasi.isLogin = true;
                } else {
                    finish();
                }
            }else {
                finish();
            }
        }else if(requestCode==4269){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    Aplikasi.setPin(null);
                    startActivityForResult(new Intent(this, PinActivity.class), 4268);
                } else {
                    Utils.showToast("PIN Not change", this);
                }
            } else {
                Utils.showToast("PIN Not change", this);
            }

        }else if(requestCode==4270){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    backupAll();
                } else {
                    Utils.showToast("PIN Not change", this);
                }
            } else {
                Utils.showToast("PIN Not change", this);
            }
        }else if(requestCode==4271){
            if(resultCode==RESULT_OK) {
                if (data.hasExtra("SUKSES")) {
                    backupAll();
                } else {
                    Utils.showToast("PIN Not change", this);
                }
            } else {
                Utils.showToast("PIN Not change", this);
            }
        }

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if(tab.getPosition() == 0){
            adapter.reload(true);
        }else{
            adapter.reload(false);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View v) {
        if(binding.fabAddDompet==v){
            AddWalletFragment.newInstance().show(getSupportFragmentManager(),"AddWallet");
        }else if(binding.txtServer==v){
            startActivity(new Intent(this,PeersActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
        }
    }

    @Override
    public void onDompetClicked(Dompet dompet) {
        Intent intent = new Intent(this, ViewWalletActivity.class);
        intent.putExtra("alamat",dompet.alamat);
        startActivity(intent);
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            int position = viewHolder.getAdapterPosition();
            Dompet dompet = adapter.getData(position);
            String deskripsi = dompet.alamat+
                    ((dompet.nama!=null && !dompet.nama.equals(dompet.alamat))?"\n"+dompet.nama:"")+
                    ((dompet.catatan!=null)?"\n"+dompet.catatan:"");
            new AlertDialog.Builder(HomeActivity.this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage(deskripsi)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
                        }
                    })
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new AlertDialog.Builder(HomeActivity.this)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle("Are You sure?")
                                    .setMessage(deskripsi)
                                    .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ObjectBox.getTransaksi().query().equal(Transaksi_.senderRS,dompet.alamat).build().remove();
                                            ObjectBox.getTransaksi().query().equal(Transaksi_.recipientRS,dompet.alamat).build().remove();
                                            ObjectBox.getDompet().remove(dompet);
                                            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
                                        }
                                    })
                                    .setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
                                        }
                                    })
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.reload(binding.tabLayout.getSelectedTabPosition()==0);
                        }
                    })
                    .show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_nav_changepin:
                startActivityForResult(new Intent(this,PinActivity.class), 4269);
                return true;
            case R.id.menu_nav_backup:
                startActivityForResult(new Intent(this,PinActivity.class), 4270);
                return true;
            case R.id.menu_nav_restore:
                startActivityForResult(new Intent(this,PinActivity.class), 4271);
                return true;
            case R.id.menu_nav_peers:
                startActivity(new Intent(this,PeersActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void backupAll(){

    }

    public void restoreAll(){

    }

}
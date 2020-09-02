package com.ibnux.nuxwallet.ui;

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

import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.adapter.DompetAdapter;
import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.data.Transaksi_;
import com.ibnux.nuxwallet.databinding.ActivityHomeBinding;
import com.ibnux.nuxwallet.utils.NuxCoin;
import com.ibnux.nuxwallet.utils.Utils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, DompetAdapter.DompetCallback {
    ActivityHomeBinding binding;
    DompetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.fabAddDompet.setOnClickListener(this);

        adapter = new DompetAdapter(this);

        binding.listDompet.setHasFixedSize(true);
        binding.listDompet.setLayoutManager(new LinearLayoutManager(this));
        binding.listDompet.setAdapter(adapter);

        binding.txtServer.setText(ObjectBox.getServer());

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.listDompet);
        if(Aplikasi.unixtime==0L){
            NuxCoin.getTime(null);
        }

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
        }

    }

    @Override
    public void onClick(View v) {
        if(binding.fabAddDompet==v){
            AddWalletFragment.newInstance().show(getSupportFragmentManager(),"AddWallet");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.reload();
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
                            adapter.reload();
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
                                            adapter.reload();
                                        }
                                    })
                                    .setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            adapter.reload();
                                        }
                                    })
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.reload();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
package com.ibnux.nuxwallet.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.ibnux.nuxwallet.R;
import com.ibnux.nuxwallet.utils.NuxCoin;

public class AirdropRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airdrop_request);
        //NuxCoin.requestAirdop();
    }
}
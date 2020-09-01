package com.ibnux.nuxwallet.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.JavascriptInterface;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.data.Dompet;
import com.ibnux.nuxwallet.data.ObjectBox;
import com.ibnux.nuxwallet.databinding.ActivityWalletGeneratorBinding;

import im.delight.android.webview.AdvancedWebView;

public class WalletGeneratorActivity extends AppCompatActivity implements AdvancedWebView.Listener {
    ActivityWalletGeneratorBinding binding;
    AlertDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.webview.setListener(this, this);
        binding.webview.setMixedContentAllowed(false);
        binding.webview.loadUrl("file:///android_asset/alamat/index.html");
        binding.webview.addJavascriptInterface(new WebAppInterface(this), "Wallet");

    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        binding.webview.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        binding.webview.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.webview.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        binding.webview.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (!binding.webview.onBackPressed()) { return; }
        super.onBackPressed();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) { }

    @Override
    public void onPageFinished(String url) { }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) { }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) { }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void pilihDompet(String pass,String wallet,String accountid,String publickey) {
            if(pass.length()>10 && wallet.length()>10){
                Dompet dompet = new Dompet();
                dompet.alamat = wallet;
                dompet.nama = wallet;
                dompet.isMe = true;
                dompet.secretPhrase = pass;
                dompet.saldo = 0;
                dompet.publicKey = publickey;
                dompet.dompetID = Long.parseLong(accountid);
                ObjectBox.addDompet(dompet);
                finish();
            }
        }
    }


}
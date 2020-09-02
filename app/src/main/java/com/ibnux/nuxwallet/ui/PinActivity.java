package com.ibnux.nuxwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.databinding.ActivityPinBinding;
import com.ibnux.nuxwallet.utils.Utils;


public class PinActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityPinBinding binding;
    boolean isNew = false;
    String pinnew = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("PIN CODE");
        String pin = Aplikasi.getPin();
        if (pin == null) {
            binding.txtInfo.setText("Create New PIN");
            isNew = true;
        }
        binding.button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(isNew){
            if(pinnew==null){
                pinnew = binding.inputPin.getText().toString();
                binding.txtInfo.setText("Repeat New PIN");
                binding.inputPin.setText("");
                Utils.vibrate();
            }else{
                if(pinnew.equals(binding.inputPin.getText().toString())){
                    Aplikasi.setPin(Utils.SHA256(pinnew));
                    Intent i = getIntent();
                    i.putExtra("SUKSES","SUKSES");
                    setResult(RESULT_OK,i);
                    finish();
                }else{
                    Utils.vibrate();
                    Utils.showToast("PIN DIFFERENT, Please repeat",this);
                    binding.txtInfo.setText("Create New PIN");
                    binding.inputPin.setText("");
                    pinnew = null;
                }
            }
        }else{
            if(Aplikasi.getPin().equals(Utils.SHA256(binding.inputPin.getText().toString()))){
                Intent i = getIntent();
                i.putExtra("SUKSES","SUKSES");
                setResult(RESULT_OK,i);
                finish();
            }else{
                Utils.vibrate();
                Utils.showToast("WRONG PIN",this);
                binding.inputPin.setText("");
                pinnew = null;
            }
        }
    }

    public void addpin(View view){
        String tv = ((TextView)view).getText().toString();
        if(tv.equals("clr")){
            binding.inputPin.setText("");
        }else if(tv.equals("del")){
            String txt =  binding.inputPin.getText().toString();
            if(txt.length()>0)
                binding.inputPin.setText(txt.substring(0,txt.length()-1));
        }else{
            binding.inputPin.append(tv);
        }
    }
}
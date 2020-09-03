package com.ibnux.nuxwallet.ui;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ibnux.nuxwallet.databinding.FragmentAddWalletBinding;

import static android.app.Activity.RESULT_OK;

public class AddWalletFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    FragmentAddWalletBinding binding;

    public static AddWalletFragment newInstance() {
        final AddWalletFragment fragment = new AddWalletFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddWalletBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnScanBarcode.setOnClickListener(this);
        binding.btnGenerate.setOnClickListener(this);
        binding.btnScan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(binding.btnScanBarcode==v){
            startActivityForResult(new Intent(getContext(), ScanActivity.class), 2345);
        }else  if(binding.btnGenerate==v){
            startActivity(new Intent(getContext(),WalletGeneratorActivity.class));
            dismiss();
        }else  if(binding.btnScan==v){
            startActivityForResult(new Intent(getContext(), ScanActivity.class), 2346);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==2345){
                if(data.hasExtra("result")) {
                    // Validasi dan simpan kartu
                }
            }else if(requestCode==2346){
                if(data.hasExtra("result")) {
                    Intent i = new Intent(getContext(), SendMoneyActivity.class);
                    i.putExtra("to",data.getStringExtra("result").toUpperCase());
                    startActivity(i);
                    dismiss();
                }
            }

        }
    }
}
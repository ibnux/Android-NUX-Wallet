package com.ibnux.nuxwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ibnux.nuxwallet.databinding.FragmentAddWalletBinding;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     ItemListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
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
    }

    @Override
    public void onClick(View v) {
        if(binding.btnScanBarcode==v){
        }else  if(binding.btnGenerate==v){
            startActivity(new Intent(getContext(),WalletGeneratorActivity.class));
        }
        dismiss();
    }
}
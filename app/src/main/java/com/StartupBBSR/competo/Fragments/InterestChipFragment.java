package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.StartupBBSR.competo.databinding.FragmentInterestChipBinding;

import androidx.fragment.app.Fragment;


public class InterestChipFragment extends Fragment {

    private FragmentInterestChipBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInterestChipBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        return view;
    }
}
package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.StartupBBSR.competo.databinding.FragmentEventPalBinding;

public class EventPalFragment extends Fragment {

    private FragmentEventPalBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventPalBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }
}
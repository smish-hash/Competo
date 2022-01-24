package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.StartupBBSR.competo.databinding.FragmentEventPalBinding;

public class EventPalFragment extends Fragment {

    private FragmentEventPalBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventPalBinding.inflate(inflater, container, false);

        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        if (navHostFragment != null) {
            StartFragment fragment = (StartFragment) navHostFragment.getParentFragment();
            if (fragment != null) {
                fragment.setTitleText(3);
            }
        }

        return binding.getRoot();
    }


    protected void onGoHomeBackPressed() {
        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        if (navHostFragment != null) {
            StartFragment startFragment = (StartFragment) navHostFragment.getParentFragment();
            if (startFragment != null)
                startFragment.loadFragment(1);
        }
    }
}
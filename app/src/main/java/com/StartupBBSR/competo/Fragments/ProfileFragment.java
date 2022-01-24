package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.databinding.FragmentProfileBinding;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentProfileBinding.inflate(inflater, container, false);

        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        if (navHostFragment != null) {
            StartFragment fragment = (StartFragment) navHostFragment.getParentFragment();
            if (fragment != null) {
                fragment.setTitleText(5);
            }
        }

        return binding.getRoot();
    }

    public void onGoHomeOnBackPressed() {
        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        if (navHostFragment != null) {
            StartFragment fragment = (StartFragment) navHostFragment.getParentFragment();
            if (fragment != null) {
                fragment.loadFragment(1);
            }
        }
    }

    protected void findTeamMate() {
        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        if (navHostFragment != null) {
            StartFragment startFragment = (StartFragment) navHostFragment.getParentFragment();
            if (startFragment != null)
                startFragment.loadFragment(3);
        }
    }
}
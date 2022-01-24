package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.StartupBBSR.competo.R;

public class EventFragment extends Fragment {

    public EventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        if (navHostFragment != null) {
            StartFragment fragment = (StartFragment) navHostFragment.getParentFragment();
            if (fragment != null) {
                fragment.setTitleText(2);
            }
        }

        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    protected void findTeamMate() {
        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        if (navHostFragment != null) {
            StartFragment startFragment = (StartFragment) navHostFragment.getParentFragment();
            if (startFragment != null)
                startFragment.loadFragment(3);
        }
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
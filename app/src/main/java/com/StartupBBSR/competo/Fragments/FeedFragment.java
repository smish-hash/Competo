package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.StartupBBSR.competo.databinding.FragmentFeedBinding;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FeedFragment extends Fragment {

    private FragmentFeedBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }

    public void onClickViewAllEvents() {
        HomeFragment homeFragment = (HomeFragment) getParentFragment();
        homeFragment.viewAllEvents();
    }

    public void findTeamMate() {
        HomeFragment homeFragment = (HomeFragment) getParentFragment();
        homeFragment.findTeamMate();
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
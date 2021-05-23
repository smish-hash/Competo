package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.databinding.FragmentEventMainBinding;
import com.StartupBBSR.competo.databinding.FragmentManageEventMainBinding;


public class ManageEventMainFragment extends Fragment {

    private FragmentManageEventMainBinding binding;

    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageEventMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        binding.topAppBar.setNavigationOnClickListener(view1 -> getActivity().finish());

        binding.topAppBar.setOnMenuItemClickListener(item -> {
            int menu = item.getItemId();
            if (menu == R.id.btnAddEvent){
                navController.navigate(R.id.action_manageEventMainFragment_to_addEventFragment);
            }
            return false;
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Nav controller won't work if not initialised in OnViewCreated
        navController = Navigation.findNavController(view);
    }
}
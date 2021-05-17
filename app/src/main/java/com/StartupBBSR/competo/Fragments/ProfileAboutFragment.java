package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.StartupBBSR.competo.Models.UserModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentProfileAboutBinding;

public class ProfileAboutFragment extends Fragment {

    private FragmentProfileAboutBinding binding;
    private UserModel userModel;
    private Constant constant;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileAboutBinding.inflate(getLayoutInflater(), container, false);

        constant = new Constant();
        userModel = (UserModel) getActivity().getIntent().getSerializableExtra(constant.getUserModelObject());

        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        To load bio into about section
        loadData();
    }

    private void loadData() {
        if (userModel.getUserBio() != null){
            binding.tvProfileBio.setText(userModel.getUserBio());
        }
    }
}
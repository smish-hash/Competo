package com.StartupBBSR.competo.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.StartupBBSR.competo.Activity.EditProfileActivity;
import com.StartupBBSR.competo.Adapters.InterestChipAdapter;
import com.StartupBBSR.competo.Models.UserModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentProfileBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    // tab titles
    private String[] profileTabTitles = new String[]{"About", "My Events", "Interests", "Updates"};

    private String[] mDataSet;

    private UserModel userModel;
    private Constant constant;

    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        constant = new Constant();
        userModel = (UserModel) getActivity().getIntent().getSerializableExtra(constant.getUserModelObject());

        binding.btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditProfileActivity.class)
                .putExtra(constant.getUserModelObject(), userModel));
            }
        });

        binding.btnEditInterests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_profileFragment2_to_interestChipFragment);
            }
        });


        init();
        initDataSet();

        RecyclerView recyclerView = binding.interestChipRecyclerView;
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL));
        InterestChipAdapter adapter = new InterestChipAdapter(mDataSet);
        recyclerView.setAdapter(adapter);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadData();

//        navController = Navigation.findNavController(view);

        binding.pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                binding.pullToRefresh.setRefreshing(false);
            }
        });

        if (userModel.getUserLinkedin() == null){
            binding.ivGotolinkedin.setVisibility(View.GONE);
        }

        binding.ivGotolinkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri linkedinUri = Uri.parse(userModel.getUserLinkedin());
                Intent intent = new Intent(Intent.ACTION_VIEW, linkedinUri);
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        binding.profileName.setText(userModel.getUserName());
        String imgurl = userModel.getUserPhoto();
        if (imgurl != null){
            binding.progressBar.setVisibility(View.VISIBLE);
            loadUsingGlide(imgurl);
        }
    }

    private void loadUsingGlide(String imgurl) {

        Glide.with(getContext()).
                load(imgurl).
                listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        binding.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        binding.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(binding.profileImage);
    }

    private void init() {
        binding.profileViewPager.setAdapter(new ProfileViewPagerFragmentAdapter(this));

        new TabLayoutMediator(binding.profileTablayout,
                binding.profileViewPager, ((tab, position) ->
                tab.setText(profileTabTitles[position])
        )).attach();
    }

    private void initDataSet() {
        mDataSet = new String[10];
        for (int i = 0; i < 10; i++) {
            mDataSet[i] = "Element #" + i;
        }
    }

    private class ProfileViewPagerFragmentAdapter extends FragmentStateAdapter {
        public ProfileViewPagerFragmentAdapter(@NonNull ProfileFragment fragmentActivity) {
            super(fragmentActivity);

        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ProfileAboutFragment();
                case 1:
                    return new ProfileMyeventsFragment();
                case 2:
                    return new ProfileInterestsFragment();
                case 3:
                    return new ProfileUpdatesFragment();
            }
            return new ProfileAboutFragment();
        }

        @Override
        public int getItemCount() {
            return profileTabTitles.length;
        }
    }
}
package com.StartupBBSR.competo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.Models.UserModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentInboxNewBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class InboxNewFragment extends Fragment {

    private FragmentInboxNewBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private String userID;

    private UserModel userModel;
    private Constant constant;
    private TabLayout inbox_tablayout;
    private NavController navController;

    //    Tab titles
    private String[] inboxTabTitles = new String[]{"Messages", "Groups", "Message Requests"};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.action_inboxNewFragment3_to_startFragment);

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentInboxNewBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getUid();

        constant = new Constant();
        userModel = new UserModel();

        Transition transition = new Fade();
        transition.setDuration(600);
        transition.addTarget(R.id.tvInboxInfo);

        binding.btnInboxInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.tvInboxInfo.getVisibility() == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(binding.getRoot(), transition);
                    binding.tvInboxInfo.setVisibility(View.GONE);
                }
                else {
                    TransitionManager.beginDelayedTransition(binding.getRoot(), transition);
                    binding.tvInboxInfo.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        init();
        getRequestCounts();
    }

    private void init() {
        binding.inboxViewPager.setAdapter(new InboxViewPagerFragmentAdapter(this));
        binding.inboxViewPager.setSaveEnabled(false);

        new TabLayoutMediator(binding.inboxTablayout, binding.inboxViewPager,
                ((tab, position) -> tab.setText(inboxTabTitles[position]))).attach();

//
    }

        private void getRequestCounts() {
        firestoreDB.collection(constant.getRequests())
                .document(userID)
                .collection(constant.getRequests())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = 0;

                            for (DocumentSnapshot document : task.getResult()) {
                                count++;
                            }

                            if (count != 0){
                                binding.inboxTablayout.getTabAt(2).getOrCreateBadge().setNumber(count);
                            } else {
                                binding.inboxTablayout.getTabAt(2).removeBadge();
                            }
                        }
                    }
                });
    }


    private class InboxViewPagerFragmentAdapter extends FragmentStateAdapter {

        public InboxViewPagerFragmentAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new InboxFragment();
                case 1:
                    return new TeamFragment();
                case 2:
                    return new MessageRequestFragment();
            }
            return new InboxFragment();
        }

        @Override
        public int getItemCount() {
            return inboxTabTitles.length;
        }
    }
}
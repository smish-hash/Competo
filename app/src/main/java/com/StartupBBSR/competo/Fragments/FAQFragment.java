package com.StartupBBSR.competo.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.StartupBBSR.competo.Adapters.FAQAdapter;
import com.StartupBBSR.competo.Models.EventModel;
import com.StartupBBSR.competo.Models.FAQModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FaqItemBinding;
import com.StartupBBSR.competo.databinding.FragmentFaqBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.auto.value.AutoAnnotation;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class FAQFragment extends Fragment {

    private FragmentFaqBinding binding;
    private NavController navController;
    private FirebaseFirestore firestoreDB;

    private FAQAdapter adapter;
    private Constant constant;
    private CollectionReference collectionReference;
    private FirestoreRecyclerOptions<FAQModel> options;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFaqBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.feedbackFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_aboutUsFragment_to_feedbackFragment);
            }
        });

        binding.btnCloseFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        constant = new Constant();
        firestoreDB = FirebaseFirestore.getInstance();
        collectionReference = firestoreDB.collection(constant.getFAQ());

        initData();
        initRecycler();

        return view;
    }

    private void initData() {
        Query query = collectionReference.orderBy(constant.getFaqId());

        options = new FirestoreRecyclerOptions.Builder<FAQModel>()
                .setQuery(query, FAQModel.class)
                .build();
    }

    private void initRecycler() {
        binding.FAQRecyclerview.setHasFixedSize(true);
        adapter = new FAQAdapter(options, getContext());
        binding.FAQRecyclerview.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        binding.FAQRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && binding.feedbackFab.getVisibility() == View.VISIBLE) {
                    binding.feedbackFab.hide();
                } else if (dy < 0 && binding.feedbackFab.getVisibility() != View.VISIBLE) {
                    binding.feedbackFab.show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
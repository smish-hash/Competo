package com.StartupBBSR.competo.Fragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.Activity.ManageEventActivity;
import com.StartupBBSR.competo.Adapters.EventFragmentAdapter;
import com.StartupBBSR.competo.Models.EventModel;
import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentEventMainBinding;
import com.airbnb.lottie.model.content.ShapeStroke;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static androidx.core.content.ContextCompat.getSystemService;

public class EventMainFragment extends Fragment implements EventFilterBottomSheetDialog.BottomSheetListener {

    private FragmentEventMainBinding binding;
    private EventFragmentAdapter adapter;
    private FirebaseFirestore firestoreDB;

    private NavController navController;

    private Constant constant;
    private CollectionReference collectionReference;

    private EventFilterBottomSheetDialog bottomSheetDialog;

    private List<EventModel> eventList;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                if (navHostFragment != null) {
                    EventFragment eventFragment = (EventFragment) navHostFragment.getParentFragment();
                    if (eventFragment != null) {
                        eventFragment.onGoHomeBackPressed();
                    }
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firestoreDB = FirebaseFirestore.getInstance();
        constant = new Constant();
        collectionReference = firestoreDB.collection(constant.getEvents());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        binding.AddEvent.setOnClickListener(view1 -> {
            Intent intent = new Intent (getContext(), ManageEventActivity.class);
            startActivity(intent);
        });



        bottomSheetDialog = new EventFilterBottomSheetDialog(getContext());
        bottomSheetDialog.setTargetFragment(this, 0);


        binding.btnEventFilter.setOnClickListener(view12 -> bottomSheetDialog.show(getParentFragmentManager().beginTransaction(), "eventFilterSheet"));

        return view;
    }

    private void search(String newText) {
        eventList = new ArrayList<>();

        collectionReference.orderBy("eventDateStamp").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                eventList.clear();
                for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots) {
                    EventModel model = snapshot.toObject(EventModel.class);
                    if (model.getEventDateStamp() > new Date().getTime()) {
                        if (model.getEventTitle().toLowerCase().contains(newText.toLowerCase()))
                            eventList.add(model);
                    }
                }
                if (eventList.size() == 0) {
                    binding.eventRecyclerView.setVisibility(View.GONE);
                    binding.tvNoEventFound.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoEventFound.setVisibility(View.GONE);
                    binding.eventRecyclerView.setVisibility(View.VISIBLE);
                    initRecycler();
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();
//        initRecycler();

        navController = Navigation.findNavController(view);

        binding.eventRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && binding.AddEvent.getVisibility() == View.VISIBLE) {
                    binding.AddEvent.hide();
                } else if (dy < 0 && binding.AddEvent.getVisibility() != View.VISIBLE) {
                    binding.AddEvent.show();
                }
            }
        });

        binding.eventRefreshLayout.setOnRefreshListener(() -> {
            initData();
            binding.eventRefreshLayout.setRefreshing(false);
        });
    }

    private void initRecycler() {
        binding.eventRecyclerView.setHasFixedSize(true);
        adapter = new EventFragmentAdapter(getContext(), eventList);

        adapter.setOnItemClickListener(model -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("eventDetails", model);
            bundle.putString("from", "event");
            navController.navigate(R.id.action_eventMainFragment_to_eventDetailsFragment, bundle);
        });

        binding.eventRecyclerView.setAdapter(adapter);
    }


    private void initData() {
        eventList = new ArrayList<>();
        collectionReference.orderBy("eventDateStamp").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    EventModel model = snapshot.toObject(EventModel.class);
                    if (model.getEventDateStamp() > new Date().getTime())
                        eventList.add(model);
                }

                if (eventList.size() > 0) {
                    binding.eventRecyclerView.setVisibility(View.VISIBLE);
                    binding.tvEventStatus.setVisibility(View.GONE);
                    initRecycler();

                } else {
                    binding.eventRecyclerView.setVisibility(View.GONE);
                    binding.tvEventStatus.setVisibility(View.VISIBLE);
                }
            } else {
                binding.eventRecyclerView.setVisibility(View.GONE);
                binding.tvEventStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onApplyButtonClicked(List<String> selectedFilters) {
        eventList = new ArrayList<>();

        if (selectedFilters.size() > 0) {
            collectionReference.orderBy("eventDateStamp").get().addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots) {
                        EventModel model = snapshot.toObject(EventModel.class);
                        if (model.getEventDateStamp() > new Date().getTime()) {
                            if (model.getEventTags() != null) {
                                for (String tag: selectedFilters) {
                                    if (model.getEventTags().contains(tag))
                                        eventList.add(model);
                                }
                            }
                        }
                    }
                    if (eventList.size() == 0) {
                        binding.eventRecyclerView.setVisibility(View.GONE);
                        binding.tvNoEventFound.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoEventFound.setVisibility(View.GONE);
                        binding.eventRecyclerView.setVisibility(View.VISIBLE);
                        initRecycler();
                    }
                }
            });
        } else {
            initData();
        }
    }
}

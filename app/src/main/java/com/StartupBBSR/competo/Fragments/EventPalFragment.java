package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.StartupBBSR.competo.Adapters.EventPalUserAdapter;
import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.Models.EventPalUserItemModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.databinding.FragmentEventPalBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class EventPalFragment extends Fragment {

    public static final String TAG = "sheet";

    private FragmentEventPalBinding binding;
    //    For Users
    private List<EventPalUserItemModel> mUserDataSet;
    //    For Skill sets
    private List<String> mSkillDataSet;


    private FirebaseFirestore firestoreDB;

    private EventPalUserAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventPalBinding.inflate(inflater, container, false);


        firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firestoreDB.collection("Users");

        View view = binding.getRoot();

//        Dummy Data Set
        initDataSet();

        SnapHelper snapHelper = new LinearSnapHelper();

        Query query = collectionReference.orderBy("Name");
        FirestoreRecyclerOptions<EventPalModel> options = new FirestoreRecyclerOptions.Builder<EventPalModel>()
                .setQuery(query, EventPalModel.class)
                .build();

        RecyclerView recyclerView = binding.eventPalRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        snapHelper.attachToRecyclerView(recyclerView);
//        EventPalUserAdapter adapter = new EventPalUserAdapter(getContext(), mUserDataSet, mSkillDataSet);
        adapter = new EventPalUserAdapter(getContext(), options);
        adapter.setOnItemClickListener(new EventPalUserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
//                Toast.makeText(getContext(), "Item Click: " + mUserDataSet.get(position).getName(), Toast.LENGTH_SHORT).show();
                TextView name = itemView.findViewById(R.id.tvEventPalUserName);
                Toast.makeText(getContext(), "Item Click: " + name.getText().toString(), Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onButtonClick(View itemView, int position) {
                TextView name = itemView.findViewById(R.id.tvEventPalUserName);
                Toast.makeText(getContext(), "Button: " + name.getText().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBottomSheetToggleClick(View itemView, int position) {

                View bottomSheet = itemView.findViewById(R.id.EventPalBottomSheet);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                ImageView btnBottomSheet = itemView.findViewById(R.id.btnBottomSheet);

                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    Log.d(TAG, "onButtonClick: STATE_COLLAPSED");
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    itemView.findViewById(R.id.tvEventPalUserAbout).setVisibility(View.VISIBLE);
                    btnBottomSheet.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    Log.d(TAG, "onButtonClick: STATE_EXPANDED");
                    itemView.findViewById(R.id.tvEventPalUserAbout).setVisibility(View.GONE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    btnBottomSheet.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                }
            }

        });
        recyclerView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void initDataSet() {
        mSkillDataSet = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mSkillDataSet.add("Skill #" + i);
        }

        mUserDataSet = new ArrayList<>();
        mUserDataSet.add(new EventPalUserItemModel("https://firebasestorage.googleapis.com/v0/b/event-collab-27e46.appspot.com/o/ProfileImages%2FuFuWMJVC2dY9EpA5K2KU168HVoC2?alt=media&token=45d3b17f-004c-48d6-8775-1e38e0b1630a", "Satyajit Mishra", "This will contain about Satyajit Mishra", mSkillDataSet));
        mUserDataSet.add(new EventPalUserItemModel("https://firebasestorage.googleapis.com/v0/b/event-collab-27e46.appspot.com/o/ProfileImages%2F6T63UOJDR2Ms3TUwIT8VlxxjjpF2?alt=media&token=7017c7d4-2036-47e5-aca7-2fb176c9b5b4", "Soumyajeet Mishra", "This will contain about Soumyajeet Mishra", mSkillDataSet));
        mUserDataSet.add(new EventPalUserItemModel("https://firebasestorage.googleapis.com/v0/b/event-collab-27e46.appspot.com/o/ProfileImages%2FuFuWMJVC2dY9EpA5K2KU168HVoC2?alt=media&token=45d3b17f-004c-48d6-8775-1e38e0b1630a", "Smish", "This will contain about Smish", mSkillDataSet));
        mUserDataSet.add(new EventPalUserItemModel("https://firebasestorage.googleapis.com/v0/b/event-collab-27e46.appspot.com/o/ProfileImages%2FuFuWMJVC2dY9EpA5K2KU168HVoC2?alt=media&token=45d3b17f-004c-48d6-8775-1e38e0b1630a", "Aashish", "This will contain about Aashish", mSkillDataSet));
    }
}
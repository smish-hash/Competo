package com.StartupBBSR.competo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.StartupBBSR.competo.Adapters.CreateTeamUserListAdapter;
import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.TeamAddMemberBottomsheetLayoutBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AddTeamBottomSheetDialog extends BottomSheetDialogFragment {
    private TeamAddMemberBottomsheetLayoutBinding binding;
    private Context context;
    private List<String> memberIds;

    private FirebaseFirestore firestoreDB;
    private Constant constant;

    private CollectionReference collectionReference;
    private CreateTeamUserListAdapter adapter;
    private FirestoreRecyclerOptions<EventPalModel> options;

    public static final String TAG = "addTeam";

    private AddMemberBottomSheetListener listener;


    public interface AddMemberBottomSheetListener {
        void onAddMembersButtonClicked(List<EventPalModel> selectedMembers);
    }

    public AddTeamBottomSheetDialog(Context context, List<String> memberIds) {
        this.context = context;
        this.memberIds = memberIds;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        listener = (AddMemberBottomSheetListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = TeamAddMemberBottomsheetLayoutBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firestoreDB = FirebaseFirestore.getInstance();
        constant = new Constant();
        collectionReference = firestoreDB.collection(constant.getUsers());

        initData();

        binding.btnAddMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnAddMembers.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<EventPalModel> selectedMembers = adapter.getSelected();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (selectedMembers.size() + memberIds.size() <= 6) {
                                    binding.btnAddMembers.setVisibility(View.VISIBLE);
                                    binding.progressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "runner: " + selectedMembers.size() + ", " + memberIds.size());
                                    listener.onAddMembersButtonClicked(selectedMembers);
                                    dismiss();
                                } else if (selectedMembers.size() == 0){
                                    binding.btnAddMembers.setVisibility(View.VISIBLE);
                                    binding.progressBar.setVisibility(View.GONE);
                                    dismiss();
                                } else {
                                    Toast.makeText(context, "Team exceeding 6 members", Toast.LENGTH_SHORT).show();
                                    binding.btnAddMembers.setVisibility(View.VISIBLE);
                                    binding.progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        return view;
    }

    private void initData() {
        Query query = collectionReference.orderBy(constant.getUserIdField()).whereNotIn(constant.getUserIdField(), memberIds);
        options = new FirestoreRecyclerOptions.Builder<EventPalModel>()
                .setQuery(query, EventPalModel.class)
                .build();

        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = binding.addMemberRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        adapter = new CreateTeamUserListAdapter(options, context);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}

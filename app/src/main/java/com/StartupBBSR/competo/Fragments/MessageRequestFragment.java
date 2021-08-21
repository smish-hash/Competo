package com.StartupBBSR.competo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.StartupBBSR.competo.Adapters.MessageRequestAdapter;
import com.StartupBBSR.competo.Models.MessageModel;
import com.StartupBBSR.competo.Models.RequestModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentMessageRequestBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MessageRequestFragment extends Fragment {

    private FragmentMessageRequestBinding binding;

    private Constant constant;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private String userID;

    private CollectionReference collectionReference, connectionRef;

    private MessageRequestAdapter adapter;
    private FirestoreRecyclerOptions<RequestModel> options;

    private NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.action_messageRequestFragment_to_inboxMainFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMessageRequestBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getUid();

        constant = new Constant();

        collectionReference = firestoreDB.collection(constant.getRequests())
                .document(userID)
                .collection(constant.getRequests());

        connectionRef = firestoreDB.collection(constant.getChatConnections());

        binding.requestLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_messageRequestFragment_to_inboxMainFragment);
            }
        });

        checkRequestCount();
        initData();
        initRecyclerView();

        return view;
    }

    private void checkRequestCount() {
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int count = 0;

                    for (DocumentSnapshot document : task.getResult()) {
                        count++;
                    }

                    if (count != 0){
                        binding.tvNoRequests.setVisibility(View.GONE);
                        binding.requestRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        binding.requestRecyclerView.setVisibility(View.GONE);
                        binding.tvNoRequests.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void initData() {
        Query query = collectionReference.orderBy("timestamp");

        options = new FirestoreRecyclerOptions.Builder<RequestModel>()
                .setQuery(query, RequestModel.class)
                .build();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = binding.requestRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        adapter = new MessageRequestAdapter(options, getContext());
        adapter.setOnButtonClickListener(new MessageRequestAdapter.onButtonClickListener() {
            @Override
            public void onAcceptButtonClick(DocumentSnapshot snapshot) {

                RequestModel requestModel = snapshot.toObject(RequestModel.class);
                String message = requestModel.getRequestMessage();
                String senderID = requestModel.getSenderID();
                Long timestamp = requestModel.getTimestamp();

                MessageModel messageModel = new MessageModel(senderID, userID, message, timestamp);
                messageModel.setSeen(false);

                binding.progressBar.setVisibility(View.VISIBLE);

                firestoreDB.collection(constant.getChats())
                        .document(userID)
                        .collection(constant.getMessages())
                        .document(senderID)
                        .collection(constant.getMessages())
                        .add(messageModel)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                firestoreDB.collection(constant.getChats())
                                        .document(senderID)
                                        .collection(constant.getMessages())
                                        .document(userID)
                                        .collection(constant.getMessages())
                                        .add(messageModel)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {

//                                                Update Connection
                                                connectionRef.document(userID)
                                                        .update("Connections", FieldValue.arrayUnion(senderID))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                connectionRef.document(senderID)
                                                                        .update("Connections", FieldValue.arrayUnion(firebaseAuth.getUid()))
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                snapshot.getReference().delete();
                                                                                Toast.makeText(getContext(), "Request moved to Inbox", Toast.LENGTH_SHORT).show();
                                                                                binding.progressBar.setVisibility(View.GONE);
                                                                                checkRequestCount();
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });
                            }
                        });

            }

            @Override
            public void onRejectButtonClick(DocumentSnapshot snapshot) {
                snapshot.getReference().delete();
                Toast.makeText(getContext(), "Request Deleted", Toast.LENGTH_SHORT).show();
                checkRequestCount();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
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
            adapter.startListening();
        }
    }
}
package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.StartupBBSR.competo.Adapters.ChatUserListAdapter;
import com.StartupBBSR.competo.Adapters.NewChatUserListAdapter;
import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.Models.UserModel;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.FragmentInboxMainBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class InboxMainFragment extends Fragment {

    private FragmentInboxMainBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private String userID;

    private Constant constant;

    private DocumentReference documentReference, chatRef;
    private CollectionReference collectionReference;

    private NavController navController;

//  private ChatUserListAdapter adapter;
    private NewChatUserListAdapter adapter;
    private FirestoreRecyclerOptions<EventPalModel> options;

    private Query query;
    List<String> chatUsers = new ArrayList<>();
    List<String> chatList;
    public static final String TAG = "chatUser";

    // TODO: 9/26/2021
    //testing sorting using new adapter
    private List<EventPalModel> mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInboxMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getUid();

        constant = new Constant();

        documentReference = firestoreDB.collection(constant.getUsers()).document(userID);

        collectionReference = firestoreDB.collection(constant.getUsers());

        chatRef = firestoreDB.collection(constant.getChatConnections())
                .document(userID);

        chatRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    chatUsers = (List<String>) documentSnapshot.get(constant.getConnections());
                    Log.d(TAG, "onComplete: " + chatUsers);

                    if (chatUsers != null){
                        initData();
                        binding.tvNoMessages.setVisibility(View.GONE);
                        binding.chatListRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        binding.chatListRecyclerView.setVisibility(View.GONE);
                        binding.tvNoMessages.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

//        getRequestCounts();


        binding.inboxRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            chatUsers = (List<String>) documentSnapshot.get(constant.getConnections());
                            Log.d(TAG, "onComplete: " + chatUsers);
                            binding.inboxRefreshLayout.setRefreshing(false);
                            if (chatUsers != null){
                                initData();
                                binding.tvNoMessages.setVisibility(View.GONE);
                                binding.chatListRecyclerView.setVisibility(View.VISIBLE);
                            } else {
                                binding.chatListRecyclerView.setVisibility(View.GONE);
                                binding.tvNoMessages.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getContext(), "Could not refresh", Toast.LENGTH_SHORT).show();
                        binding.inboxRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        return view;
    }

    private void initData() {

        chatList = chatUsers;

        mList = new ArrayList<>();

        collectionReference.orderBy("time", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mList.clear();

                for (QueryDocumentSnapshot snapshot: queryDocumentSnapshots) {
                    EventPalModel model = snapshot.toObject(EventPalModel.class);
                    if (chatList.contains(model.getUserID())) {
                        mList.add(model);
                    }

                }
                initRecyclerView();
            }
        });

    }

    private void initRecyclerView() {

        RecyclerView recyclerView = binding.chatListRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
//        adapter = new ChatUserListAdapter(options, getContext());
        adapter = new NewChatUserListAdapter(getContext(), mList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        /*if (adapter != null) {
            adapter.startListening();
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        /*if (adapter != null) {
            adapter.stopListening();
        }*/
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }
}
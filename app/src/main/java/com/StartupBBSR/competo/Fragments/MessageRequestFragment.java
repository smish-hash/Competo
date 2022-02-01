package com.StartupBBSR.competo.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.StartupBBSR.competo.Adapters.MessageRequestAdapter;
import com.StartupBBSR.competo.Models.MessageModel;
import com.StartupBBSR.competo.Models.RequestModel;
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
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

public class MessageRequestFragment extends Fragment {

    private FragmentMessageRequestBinding binding;

    private Constant constant;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private String userID;

    private CollectionReference collectionReference, connectionRef;

    private MessageRequestAdapter adapter;
    private FirestoreRecyclerOptions<RequestModel> options;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

                    if (count != 0) {
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
                                                        .update(constant.getConnections(), FieldValue.arrayUnion(senderID))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                connectionRef.document(senderID)
                                                                        .update(constant.getConnections(), FieldValue.arrayUnion(firebaseAuth.getUid()))
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                firestoreDB.collection(constant.getUsers())
                                                                                        .document(userID)
                                                                                        .update("time", timestamp)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                firestoreDB.collection(constant.getUsers())
                                                                                                        .document(senderID)
                                                                                                        .update("time", timestamp)
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                snapshot.getReference().delete();
                                                                                                                Toast.makeText(getContext(), "Request moved to Inbox", Toast.LENGTH_SHORT).show();
                                                                                                                binding.progressBar.setVisibility(View.GONE);
                                                                                                                checkRequestCount();

                                                                                                                firestoreDB.collection("token").document(senderID).get().addOnCompleteListener(task1 -> {
                                                                                                                    if (task1.isSuccessful()) {
                                                                                                                        DocumentSnapshot document = task1.getResult();
                                                                                                                        if (document.exists()) {
                                                                                                                            Log.d("data", "DocumentSnapshot data: " + document.getString("token"));
                                                                                                                            firestoreDB.collection("Users").document(userID).get().addOnCompleteListener(task3 -> {
                                                                                                                                if (task3.isSuccessful()) {
                                                                                                                                    DocumentSnapshot document3 = task3.getResult();
                                                                                                                                    if (document3.exists()) {
                                                                                                                                        Log.d("data", "DocumentSnapshot data: " + document3.getString("Name"));
                                                                                                                                        sendfcm(document.getString("token"),document3.getString("Name"));
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                                        } else {
                                                                                                                            Log.d("data", "No such document");
                                                                                                                        }
                                                                                                                    } else {
                                                                                                                        Log.d("data", "get failed with ", task.getException());
                                                                                                                    }
                                                                                                                });
                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        });
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

    public void sendfcm(String token,String name)
    {
        Runnable runnable = () -> {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON,"{\n" +
                    "    \"data\" : {\n" +
                    "      \"id\" : \""+firebaseAuth.getUid()+"\",\n" +
                    "      \"category\" : \"request\",\n" +
                    "      \"title\":\"Request Accepted\",\n" +
                    "      \"body\":\""+name+" has accepted your message request\"\n" +
                    "    },\n" +
                    "    \"to\":\""+token+"\"\n" +
                    "}");
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "key=AAAABmOW__8:APA91bFEiWxr4rRQa3M_5n-w-5XDjLnQ9nf2IgAs1r0ppfwgTLZoGgOJmRAF1pt59hHqdMZ74AmAx1lkk0HaCuLwUCsHi_M_BWEZAGwkXyp-57YJk_pGmGWwJKNEU_bnJLl7bv7VDPzy")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.d("response",response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
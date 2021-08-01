package com.StartupBBSR.competo.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;

import com.StartupBBSR.competo.Adapters.NewChatAdapter;
import com.StartupBBSR.competo.Models.MessageModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.ActivityChatDetailBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;

    private Constant constant;
    private MessageModel messageModel;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;

    private String status = "";

    private NewChatAdapter newChatAdapter;
    private RecyclerView recyclerView;
    private List<MessageModel> mMessage;

    private final int limit = 13;
    private DocumentSnapshot lastVisible;

    private boolean isScrolling = false;
    private boolean isLastItemReached = false;

    private ListenerRegistration isSeenlistenerRegistration1, isSeenlistenerRegistration2;
    private EventListener<QuerySnapshot> eventListener1, eventListener2;

    private String senderID, receiverID, receiverName, receiverPhoto;

    private CollectionReference collectionReference;
    private DocumentReference receiverRef, userRef, userMessageNumberRef;
    private CollectionReference seenRef1, seenRef2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        constant = new Constant();

        senderID = firebaseAuth.getUid();
        receiverID = getIntent().getStringExtra("receiverID");
        receiverName = getIntent().getStringExtra("receiverName");
        receiverPhoto = getIntent().getStringExtra("receiverPhoto");

        updateReceiverInfo(receiverName, receiverPhoto);

        userMessageNumberRef = firestoreDB.collection(constant.getMessageNumber()).document(senderID);

        userRef = firestoreDB.collection(constant.getUsers()).document(senderID);
        receiverRef = firestoreDB.collection(constant.getUsers()).document(receiverID);
        getReceiverUpdates();

        status("Online");

        collectionReference = firestoreDB.collection(constant.getChats())
                .document(senderID)
                .collection(constant.getMessages())
                .document(receiverID)
                .collection(constant.getMessages());

        binding.btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!binding.etMessage.getText().toString().equals("")) {

//                    Notification

                    userMessageNumberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    int value = Integer.parseInt(document.getString(constant.getMessageNumber()));

                                    value++;

                                    Map<String, Object> city = new HashMap<>();

                                    city.put("messagenumber", String.valueOf(value));

//                                    DocumentReference docRef3 = firestoreDB.collection("messagenumber").document(firebaseAuth.getUid());
                                    userMessageNumberRef.set(city);
                                }
                            }
                        }
                    });

                    String message = binding.etMessage.getText().toString().trim();
                    Long timestamp = new Date().getTime();

                    messageModel = new MessageModel(senderID, receiverID, message, timestamp);
                    messageModel.setSeen(false);

                    binding.etMessage.setText("");

                    firestoreDB.collection(constant.getChats())
                            .document(senderID)
                            .collection(constant.getMessages())
                            .document(receiverID)
                            .collection(constant.getMessages())
                            .add(messageModel)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    firestoreDB.collection(constant.getChats())
                                            .document(receiverID)
                                            .collection(constant.getMessages())
                                            .document(senderID)
                                            .collection(constant.getMessages())
                                            .add(messageModel)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
//                                                    Message sent
                                                }
                                            });
                                }
                            });
                }
            }
        });

//        For read receipts
        seenRef1 = firestoreDB.collection(constant.getChats())
                .document(senderID)
                .collection(constant.getMessages())
                .document(receiverID)
                .collection(constant.getMessages());

        seenRef2 = firestoreDB.collection(constant.getChats())
                .document(receiverID)
                .collection(constant.getMessages())
                .document(senderID)
                .collection(constant.getMessages());

        seenMessage(senderID, receiverID);

        if (isSeenlistenerRegistration1 == null && isSeenlistenerRegistration2 == null) {
            isSeenlistenerRegistration1 = seenRef1.addSnapshotListener(eventListener1);
            isSeenlistenerRegistration2 = seenRef2.addSnapshotListener(eventListener2);
        }

        initNewRecycler();

    }

    private void updateReceiverInfo(String receiverName, String receiverPhoto) {

        binding.chatUserName.setText(receiverName);

        if (receiverPhoto != null)
            Glide.with(getApplicationContext())
                    .load(Uri.parse(receiverPhoto))
                    .into(binding.chatUserImage);
        else
            Glide.with(getApplicationContext())
                    .load(R.drawable.ic_baseline_person_24)
                    .into(binding.chatUserImage);
    }

    private void getReceiverUpdates() {
        receiverRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }

                String receiverName = value.getString("Name");
                String receiverPhoto = value.getString("Photo");

                updateReceiverInfo(receiverName, receiverPhoto);
            }
        });
    }

    private void initNewRecycler() {
        recyclerView = binding.chatRecyclerView;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void readMessage(String senderID, String receiverID) {
        mMessage = new ArrayList<>();

        collectionReference.orderBy("timestamp", Query.Direction.DESCENDING).limit(limit)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mMessage.clear();

                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    MessageModel model = snapshot.toObject(MessageModel.class);
                    if (model.getReceiverID().equals(senderID) && model.getSenderID().equals(receiverID)
                            || model.getReceiverID().equals(receiverID) && model.getSenderID().equals(senderID)) {
                        mMessage.add(model);
                    }

                    newChatAdapter = new NewChatAdapter(ChatDetailActivity.this, mMessage);
                    recyclerView.setAdapter(newChatAdapter);

//                    Pagination
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    Log.d("paginate", "onSuccess-Last Visible: " + (queryDocumentSnapshots.size() - 1));

                    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isScrolling = true;
                            }
                        }

                        @Override
                        public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                            int visibleItemCount = linearLayoutManager.getChildCount();
                            int totalItemCount = linearLayoutManager.getItemCount();

                            if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                                isScrolling = false;
                                Query nextQuery = collectionReference.orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible).limit(limit);
                                nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                        if (t.isSuccessful()) {
                                            for (DocumentSnapshot d : t.getResult()) {
                                                MessageModel messageModel = d.toObject(MessageModel.class);
                                                mMessage.add(messageModel);
                                            }

                                            newChatAdapter.notifyDataSetChanged();

                                            if (t.getResult().size() - 1 >= 0) {
                                                lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                            }

                                            if (t.getResult().size() < limit) {
                                                isLastItemReached = true;
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    };

                    recyclerView.addOnScrollListener(onScrollListener);
                }
            }
        });
    }


    private void seenMessage(String senderID, String receiverID) {

        eventListener1 = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("error", "Listen failed.", error);
                    return;
                }

                for (DocumentSnapshot snapshot : value) {
                    MessageModel messageModel = snapshot.toObject(MessageModel.class);
                    if (messageModel.getReceiverID().equals(senderID) && messageModel.getSenderID().equals(receiverID)) {
                        snapshot.getReference().update("seen", true);
                    }
                }
            }
        };

        eventListener2 = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("error", "Listen failed.", error);
                    return;
                }

                for (DocumentSnapshot snapshot : value) {
                    MessageModel messageModel = snapshot.toObject(MessageModel.class);
                    if (messageModel.getReceiverID().equals(senderID) && messageModel.getSenderID().equals(receiverID)) {
                        snapshot.getReference().update("seen", true);
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("error", "onEvent: ", error);
                    return;
                }

                readMessage(senderID, receiverID);
            }
        });

        receiverRef.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("error", "listen:error", error);
                    return;
                }

                receiverRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.contains("status")) {
                                status = snapshot.getString("status");
                                if (status.equals("Online"))
                                    binding.chatUserStatus.setVisibility(View.VISIBLE);
                                else {
                                    binding.chatUserStatus.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });
            }
        });

        isSeenlistenerRegistration1 = seenRef1.addSnapshotListener(eventListener1);
        isSeenlistenerRegistration2 = seenRef2.addSnapshotListener(eventListener2);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (isSeenlistenerRegistration1 != null) {
            isSeenlistenerRegistration1.remove();
        }

        if (isSeenlistenerRegistration2 != null) {
            isSeenlistenerRegistration2.remove();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("Offline");
        Log.d("status", "onPauseChat: Offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("Online");
        Log.d("status", "onResumeChat: Online");
    }

    private void status(String status) {
        userRef.update("status", status);
    }
}
package com.StartupBBSR.competo.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.StartupBBSR.competo.Activity.ChatDetailActivity;
import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.Adapters.EventPalUserAdapter;
import com.StartupBBSR.competo.Adapters.NewEventPalAdapter;
import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.Models.RequestModel;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.Utils.ScaleLayoutManager;
import com.StartupBBSR.competo.databinding.AlertlayoutrequestBinding;
import com.StartupBBSR.competo.databinding.FragmentEventPalMainBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

public class EventPalMainFragment extends Fragment {

    public static final String TAG = "sheet";

    private FirebaseFirestore firestoreDB;
    private FirebaseAuth firebaseAuth;
    private String userID;

    private Constant constant;

    private EventPalUserAdapter adapter;

    private CollectionReference collectionReference;
    private FirestoreRecyclerOptions<EventPalModel> options;

    private EventPalModel eventPalModel;
    private List<EventPalModel> mList, mSearchList;

    private NavController navController;

    private FragmentEventPalMainBinding binding;

    private NewEventPalAdapter newEventPalAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ((MainActivity) getActivity()).onGoHomeOnBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEventPalMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getUid();

        constant = new Constant();

        collectionReference = firestoreDB.collection(constant.getUsers());

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

        initData();
        initRecycler();

        return view;
    }

    private void search(String newText) {

        mList = new ArrayList<>();

        collectionReference.orderBy(constant.getUserNameField()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mList.clear();
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    EventPalModel model = snapshot.toObject(EventPalModel.class);
                    if (!model.getUserID().equals(userID)) {
                        if (model.getName().toLowerCase().contains(newText.toLowerCase()))
                            mList.add(model);
                    }

                }
                if (mList.size() == 0) {
                    binding.eventPalRecyclerView.setVisibility(View.GONE);
                    binding.tvnoUserFound.setVisibility(View.VISIBLE);
                } else {
                    binding.tvnoUserFound.setVisibility(View.GONE);
                    binding.eventPalRecyclerView.setVisibility(View.VISIBLE);
                    initRecycler();
                }

            }
        });

        /*Query userSearchQuery = collectionReference
                .orderBy(constant.getUserNameField())
                .whereGreaterThanOrEqualTo(constant.getUserNameField(), newText);

        options = new FirestoreRecyclerOptions.Builder<EventPalModel>()
                .setQuery(userSearchQuery, EventPalModel.class)
                .build();*/
    }

    private void initData() {

        mList = new ArrayList<>();

        collectionReference.orderBy(constant.getUserNameField()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mList.clear();

                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    EventPalModel model = snapshot.toObject(EventPalModel.class);
                    if (!model.getUserID().equals(userID) && model.getPhoto() != null)
                        mList.add(model);
                }
                Collections.shuffle(mList);
                initRecycler();
            }
        });
    }

    private void initRecycler() {
        SnapHelper snapHelper = new LinearSnapHelper();
        RecyclerView recyclerView = binding.eventPalRecyclerView;
        recyclerView.setLayoutManager(new ScaleLayoutManager(getActivity(), ScaleLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(recyclerView);

        newEventPalAdapter = new NewEventPalAdapter(getContext(), mList);
        newEventPalAdapter.setOnItemClickListener(new NewEventPalAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(EventPalModel model) {
                firestoreDB.collection(constant.getChatConnections()).document(userID)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot connectionListSnapshot = task.getResult();
                            List<String> connectionList = (List<String>) connectionListSnapshot.get("Connections");
                            if (connectionList != null) {
                                if (!connectionList.contains(model.getUserID())) {
//                                    Create new request
                                    showCreateRequestDialog(model);

                                } else {
//                                    Chat already present
                                    Intent intent = new Intent(getContext(), ChatDetailActivity.class);
                                    intent.putExtra("receiverID", model.getUserID());
                                    intent.putExtra("receiverName", model.getName());
                                    intent.putExtra("receiverPhoto", model.getPhoto());
                                    startActivity(intent);
                                }
                            } else {
                                showCreateRequestDialog(model);
                            }
                        }
                    }
                });
            }
        });


        /*adapter = new EventPalUserAdapter(getContext(), options);
        adapter.setOnItemClickListener(new EventPalUserAdapter.OnItemClickListener() {
            @Override
            public void onButtonClick(DocumentSnapshot snapshot) {
                EventPalModel model = snapshot.toObject(EventPalModel.class);

                firestoreDB.collection(constant.getChatConnections()).document(userID)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot connectionListSnapshot = task.getResult();
                            List<String> connectionList = (List<String>) connectionListSnapshot.get("Connections");
                            if (connectionList != null) {
                                if (!connectionList.contains(model.getUserID())) {
//                                    Create new request
                                    showCreateRequestDialog(model);
                                } else {
//                                    Chat already present
                                    Intent intent = new Intent(getContext(), ChatDetailActivity.class);
                                    intent.putExtra("receiverID", model.getUserID());
                                    intent.putExtra("receiverName", model.getName());
                                    intent.putExtra("receiverPhoto", model.getPhoto());
                                    startActivity(intent);
                                }
                            } else {
                                showCreateRequestDialog(model);
                            }
                        }
                    }
                });
            }

            *//*@Override
            public void onBottomSheetToggleClick(View itemView, int position) {

                View bottomSheet = itemView.findViewById(R.id.EventPalBottomSheet);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                // ImageView btnBottomSheet = itemView.findViewById(R.id.btnBottomSheet);

                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {

                    Log.d(TAG, "onButtonClick: STATE_COLLAPSED");
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    itemView.findViewById(R.id.tvEventPalUserAbout).setVisibility(View.VISIBLE);
                    // btnBottomSheet.setImageResource(R.drawable.down_arrow);

                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                    Log.d(TAG, "onButtonClick: STATE_EXPANDED");
                    itemView.findViewById(R.id.tvEventPalUserAbout).setVisibility(View.GONE);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    //  btnBottomSheet.setImageResource(R.drawable.ic_upperarrow);

                }
            }*//*
        });*/

        recyclerView.setAdapter(newEventPalAdapter);
//        adapter.startListening();
    }

    private void showCreateRequestDialog(EventPalModel model) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertlayoutrequestBinding alertlayoutrequestBinding = AlertlayoutrequestBinding.inflate(getLayoutInflater());
        View alertView = alertlayoutrequestBinding.getRoot();
        builder.setView(alertView);
        builder.setTitle("Connect with " + model.getName())
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (!alertlayoutrequestBinding.input.getText().toString().trim().isEmpty()) {

                            String requestMesssage = alertlayoutrequestBinding.input.getText().toString().trim();
                            RequestModel requestModel = new RequestModel(userID, requestMesssage, new Date().getTime());

                            firestoreDB.collection(constant.getRequests())
                                    .document(model.getUserID())
                                    .collection(constant.getRequests())
                                    .document(userID)
                                    .set(requestModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(), "Request Sent", Toast.LENGTH_SHORT).show();

                                            firestoreDB.collection("token").document(model.getUserID()).get().addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Log.d("data", "DocumentSnapshot data: " + document.getString("token"));
                                                        sendfcm(document.getString("token"));
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
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onStart() {
        super.onStart();
//        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
//        adapter.stopListening();
    }

    public void sendfcm(String token)
    {
        Runnable runnable = () -> {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON,"{ \"notification\": {\n" +
                    "    \"title\": \"Request\",\n" +
                    "    \"body\": \"You have a new message request\"\n" +
                    "  },\n" +
                    "  \"to\" : \"/topics/Event\"\n" +
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
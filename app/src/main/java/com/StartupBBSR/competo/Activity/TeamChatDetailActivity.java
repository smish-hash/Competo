package com.StartupBBSR.competo.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.StartupBBSR.competo.Adapters.TeamChatAdapter;
import com.StartupBBSR.competo.Fragments.AddTeamBottomSheetDialog;
import com.StartupBBSR.competo.Models.EventPalModel;
import com.StartupBBSR.competo.Models.TeamMessageModel;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.Utils.Constant;
import com.StartupBBSR.competo.databinding.ActivityTeamChatDetailBinding;
import com.StartupBBSR.competo.databinding.ViewmembersAlertLayoutBinding;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TeamChatDetailActivity extends AppCompatActivity implements AddTeamBottomSheetDialog.AddMemberBottomSheetListener {

    private ActivityTeamChatDetailBinding binding;

    private String teamName, teamImage, teamID, teamCreatorID;
    private List<String> teamMembers;

    private TeamMessageModel teamMessageModel;
    private Constant constant;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private String userID, userName;

    private FirestoreRecyclerOptions<TeamMessageModel> options;
    private TeamChatAdapter adapter;

    private CollectionReference collectionReference;
    private DocumentReference teamReference;

    private List<String> memberNameList = new ArrayList<>();
    private ArrayAdapter<String> memberNameListAdapter;
    private ListView memberNameListView;


    private AddTeamBottomSheetDialog addTeamBottomSheetDialog;

    public static final String TAG = "teamChat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeamChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getUid();

        constant = new Constant();

        teamName = getIntent().getStringExtra("teamName");
        teamImage = getIntent().getStringExtra("teamImage");
        teamID = getIntent().getStringExtra("teamID");
        teamCreatorID = getIntent().getStringExtra("teamCreatorID");
        teamMembers = getIntent().getStringArrayListExtra("teamMembers");

        binding.teamName.setText(teamName);
        Glide.with(TeamChatDetailActivity.this).load(Uri.parse(teamImage)).into(binding.teamImage);

        collectionReference = firestoreDB.collection(constant.getTeamChats())
                .document(teamID)
                .collection(constant.getTeamMessages());

        teamReference = firestoreDB.collection(constant.getTeams()).document(teamID);

        DocumentReference documentReference = firestoreDB.collection(constant.getUsers()).document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        userName = snapshot.getString(constant.getUserNameField());
                    }
                }
            }
        });

//        Loading the creator name into the toolbar
        DocumentReference adminDocRef = firestoreDB.collection(constant.getUsers()).document(teamCreatorID);
        adminDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        binding.teamCreatorName.setText("Created by " + snapshot.getString(constant.getUserNameField()));
                    }
                }
            }
        });


        binding.btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!binding.etMessage.getText().toString().equals("")) {

                    String message = binding.etMessage.getText().toString().trim();
                    String messageID = collectionReference.document().getId();
                    String senderID = userID;
                    String senderName = userName;
                    long messageTime = new Date().getTime();

                    teamMessageModel = new TeamMessageModel(message, messageID, senderID, senderName, messageTime);
                    binding.etMessage.setText("");

                    collectionReference.document(messageID).set(teamMessageModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                }
            }
        });


//        Setting the menu.
        if (teamCreatorID.equals(userID)) {
            binding.toolbar2.getMenu().add(Menu.NONE, 1, Menu.NONE, "Add Members");
        }

        binding.toolbar2.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.viewMembers:
                        viewMembers();
                        return true;

                    case R.id.exitTeam:
                        exitTeam();
                        return true;

                    case 1:
                        addMembers();
                        return true;
                    default:
                        return false;
                }
            }
        });

        initData();
        initRecyclerview();
        getMembers();
    }


    private void initData() {
        Query query = collectionReference.orderBy("timestamp");
        options = new FirestoreRecyclerOptions.Builder<TeamMessageModel>()
                .setQuery(query, TeamMessageModel.class)
                .build();
    }

    private void initRecyclerview() {
        RecyclerView chatRecyclerView = binding.teamChatRecyclerView;
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setHasFixedSize(true);
        adapter = new TeamChatAdapter(options, this);
        chatRecyclerView.setAdapter(adapter);
    }

    private void getMembers() {
        for (String id : teamMembers) {
            DocumentReference documentReference = firestoreDB.collection(constant.getUsers()).document(id);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (snapshot.exists()) {
                        memberNameList.add(snapshot.getString(constant.getUserNameField()));
                    }
                }
            });
        }
    }

    private void viewMembers() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TeamChatDetailActivity.this);
        builder.setTitle("Team members");
        ViewmembersAlertLayoutBinding viewmembersAlertLayoutBinding = ViewmembersAlertLayoutBinding.inflate(getLayoutInflater());
        View view = viewmembersAlertLayoutBinding.getRoot();
        memberNameListView = viewmembersAlertLayoutBinding.membersListView;
        memberNameListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, memberNameList);
        memberNameListView.setAdapter(memberNameListAdapter);
        memberNameListAdapter.notifyDataSetChanged();
        builder.setView(view);
        builder.show();
    }

    private void exitTeam() {

        AlertDialog.Builder builder = new AlertDialog.Builder(TeamChatDetailActivity.this);
        builder.setTitle("Exit Team");
        builder.setMessage("Are you sure to exit the team?\n-You will not be able to access or read previous messages");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ProgressDialog dialog = new ProgressDialog(TeamChatDetailActivity.this);
                dialog.setMessage("Exiting...");
                dialog.show();

                DocumentReference connectionRef = firestoreDB.collection(constant.getChatConnections()).document(userID);

                connectionRef.update(constant.getTeamConnections(), FieldValue.arrayRemove(teamID))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                DocumentReference teamRef = firestoreDB.collection(constant.getTeams()).document(teamID);
                                teamRef.update(constant.getTeamMemberField(), FieldValue.arrayRemove(userID))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(TeamChatDetailActivity.this, "Exit Successful", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                                finish();
                                            }
                                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(TeamChatDetailActivity.this, "Exit Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void addMembers() {
        ProgressDialog dialog = new ProgressDialog(TeamChatDetailActivity.this);
        dialog.setMessage("Loading");
        dialog.show();
        firestoreDB.collection(constant.getTeams()).document(teamID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                List<String> membersIDs = (List<String>) snapshot.get("teamMembers");
                Log.d(TAG, "onComplete: " + membersIDs);
                dialog.dismiss();
                if (membersIDs.size() < 6) {
                    addTeamBottomSheetDialog = new AddTeamBottomSheetDialog(TeamChatDetailActivity.this, membersIDs);
                    addTeamBottomSheetDialog.show(getSupportFragmentManager(), "AddMemberBottomSheet");
                } else {
                    Toast.makeText(TeamChatDetailActivity.this, "Cannot add more members", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onStart() {
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

    @Override
    public void onAddMembersButtonClicked(List<EventPalModel> selectedMembers) {
        CollectionReference teamCollectionRef = firestoreDB.collection(constant.getChatConnections());
        for (EventPalModel model: selectedMembers){
//            Updating team members list
            teamReference.update("teamMembers", FieldValue.arrayUnion(model.getUserID()));
//            Updating chat connection
            teamCollectionRef.document(model.getUserID()).update(constant.getTeamConnections(), FieldValue.arrayUnion(teamID));
            Toast.makeText(TeamChatDetailActivity.this, "Team Updated", Toast.LENGTH_SHORT).show();
        }
    }
}
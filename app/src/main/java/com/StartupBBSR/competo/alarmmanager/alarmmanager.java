package com.StartupBBSR.competo.alarmmanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

public class alarmmanager extends BroadcastReceiver {

    private static final String TAG = "data";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    int value = 0;

    @Override
    public void onReceive(Context context, Intent intent) {

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        DocumentReference docRef = firestoreDB.collection("ChatConnections").document(firebaseAuth.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        List<String> connections = (List<String>) document.get("Connections");
                        int connectionslast = connections.size();
                        List<String> teamconnections = (List<String>) document.get("TeamConnection");
                        int teamlast = teamconnections.size();
                        for(int i = 0;i<connectionslast;i++)
                        {
                            if(connections.get(i) == null)
                            {
                                break;
                            }
                            else
                            {
                                Log.d(TAG,connections.get(i));
                                CollectionReference docRef1 = firestoreDB.collection("Chats").document(firebaseAuth.getUid() + connections.get(i)).collection("Messages");
                                int finalI = i;
                                docRef1.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                                        value++;
                                                        Log.d(TAG, String.valueOf(value));
                                                    }

                                                    if(finalI == connectionslast-1)
                                                    {
                                                        DocumentReference docRef2 = firestoreDB.collection("messagenumber").document(firebaseAuth.getUid());
                                                        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();
                                                                    if (document.exists()) {
                                                                        Log.d(TAG, "messagenumber data: " + document.getData());
                                                                        String messagenumber = document.getString("messagenumber");
                                                                        Log.d(TAG, "messagenumber1 data: " + value);
                                                                        if(value > Integer.parseInt(document.getString("messagenumber")))
                                                                        {
                                                                            Log.d(TAG, "success data: " + Integer.parseInt(document.getString("messagenumber")));

                                                                            int noti = value - Integer.parseInt(document.getString("messagenumber"));

                                                                            Intent i = new Intent(context, MainActivity.class);
                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                                            PendingIntent pendingintent = PendingIntent.getActivity(context, 0, i, 0);

                                                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "chatnotification")
                                                                                    .setAutoCancel(true)
                                                                                    .setSmallIcon(R.drawable.ic_baseline_settings_24)
                                                                                    .setContentTitle("New Notification")
                                                                                    .setContentText("You have " +noti+ " new notifications")
                                                                                    .setContentIntent(pendingintent)
                                                                                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                                                                                    .setPriority(NotificationCompat.PRIORITY_HIGH);

                                                                            NotificationManager notificationmanager = context.getSystemService(NotificationManager.class);

                                                                            notificationmanager.notify(2, builder.build());

                                                                            Map<String, Object> city = new HashMap<>();

                                                                            city.put("messagenumber", String.valueOf(value));

                                                                            DocumentReference docRef3 = firestoreDB.collection("messagenumber").document(firebaseAuth.getUid());
                                                                            docRef3.set(city);
                                                                        }
                                                                    } else {
                                                                        Log.d(TAG, "No such document");

                                                                        Map<String, Object> city = new HashMap<>();

                                                                        city.put("messagenumber", String.valueOf(value));

                                                                        DocumentReference docRef3 = firestoreDB.collection("messagenumber").document(firebaseAuth.getUid());
                                                                        docRef3.set(city);
                                                                    }
                                                                } else {
                                                                    Log.d(TAG, "get failed with ", task.getException());
                                                                }
                                                            }
                                                        });
                                                    }

                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        }

                        for(int i = 0;i<teamlast;i++)
                        {
                            if(teamconnections.get(i) == null)
                            {
                                break;
                            }
                            else
                            {
                                Log.d(TAG,teamconnections.get(i));
                                CollectionReference docRef1 = firestoreDB.collection("TeamChats").document(teamconnections.get(i)).collection("TeamMessages");
                                docRef1.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                                        value++;
                                                        Log.d(TAG, String.valueOf(value));
                                                    }

                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }
}

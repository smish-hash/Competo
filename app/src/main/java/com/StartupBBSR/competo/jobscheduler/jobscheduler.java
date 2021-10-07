package com.StartupBBSR.competo.jobscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.net.URL;

public class jobscheduler extends JobService {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    String channel_name = "Job_service";
    String channel_id = "service3";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("Job service","Job Started");

        dobackgroundwork(params);

        return true;
    }

    private void dobackgroundwork(JobParameters params)
    {
        new Thread(() -> {

            ////////////////////////////////////////////////////////////////////////////////////

            db.collection("Events").addSnapshotListener((value, error) -> {
                for(DocumentChange dc : value.getDocumentChanges())
                {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Log.d("Data added", String.valueOf(dc.getDocument().getData()));
                        sendnotification("A new EVENT is added",dc.getDocument().getString("eventPoster"), 1,true);
                        // TODO: 06-10-2021 insert a condition
                    }

                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                        Log.d("Data Modified",dc.getDocument().getString("eventThumbnailPoster"));

                        sendnotification("An EVENT is modified",dc.getDocument().getString("eventPoster"),2,false);
                    }

                    if (dc.getType() == DocumentChange.Type.REMOVED) {
                        Log.d("Data removed", String.valueOf(dc.getDocument().getData()));
                        sendnotification("An EVENT is removed",dc.getDocument().getString("eventPoster"),3,false);
                    }
                }
            });

            ////////////////////////////////////////////////////////////////////////////////////

            ////////////////////////////////////////////////////////////////////////////////////

            db.collection("Requests").document(firebaseAuth.getUid()).collection("Requests").addSnapshotListener((value, error) -> {

                for(DocumentChange dc : value.getDocumentChanges())
                {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Log.d("Data added", String.valueOf(dc.getDocument().getData()));
                        sendnotification("You have a new MESSAGE REQUEST","https://media.wired.com/photos/5d09594a62bcb0c9752779d9/master/pass/Transpo_G70_TA-518126.jpg", 4,false);
                    }

                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                        Log.d("Data Modified", String.valueOf(dc.getDocument().getData()));
                        sendnotification("A MESSAGE REQUEST is modified","https://media.wired.com/photos/5d09594a62bcb0c9752779d9/master/pass/Transpo_G70_TA-518126.jpg", 5,false);
                    }

                    if (dc.getType() == DocumentChange.Type.REMOVED) {
                        Log.d("Data removed", String.valueOf(dc.getDocument().getData()));
                        sendnotification("A MESSAGE REQUEST is removed","https://media.wired.com/photos/5d09594a62bcb0c9752779d9/master/pass/Transpo_G70_TA-518126.jpg", 6,false);
                    }
                }
            });

            ////////////////////////////////////////////////////////////////////////////////////////

            ////////////////////////////////////////////////////////////////////////////////////////

            db.collection("Chats").document(firebaseAuth.getUid()).collection("Messages").addSnapshotListener((value, error) -> {

                for(DocumentChange dc : value.getDocumentChanges())
                {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Log.d("Data added", String.valueOf(dc.getDocument().getData()));
                        sendnotification("You have a new CHAT MESSAGE","https://media.wired.com/photos/5d09594a62bcb0c9752779d9/master/pass/Transpo_G70_TA-518126.jpg", 7,false);
                    }

                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                        Log.d("Data Modified", String.valueOf(dc.getDocument().getData()));
                        sendnotification("A chat is MODIFIED","https://media.wired.com/photos/5d09594a62bcb0c9752779d9/master/pass/Transpo_G70_TA-518126.jpg", 8,false);
                    }

                    if (dc.getType() == DocumentChange.Type.REMOVED) {
                        Log.d("Data removed", String.valueOf(dc.getDocument().getData()));
                        sendnotification("A CHAT is REMOVED","https://media.wired.com/photos/5d09594a62bcb0c9752779d9/master/pass/Transpo_G70_TA-518126.jpg", 9,false);
                    }
                }
            });

            ////////////////////////////////////////////////////////////////////////////////////////

            //Log.d("job service","Job finished");
            //jobFinished(params,false);
        }).start();
    }

    private void sendnotification(String data, String image_address, int id, Boolean condition) {
        new Thread(() -> {
            Bitmap image = null;
            if(condition == true)
            {
                try {
                    URL url = new URL(image_address);
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch(IOException e) {
                    System.out.println(e);
                }
            }

            if(Build.VERSION.SDK_INT>=26)
            {
                NotificationChannel notificationchannel = new NotificationChannel(channel_id,channel_name, NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(notificationchannel);

                Intent intent = new Intent(jobscheduler.this, MainActivity.class);
                PendingIntent pendingintent = PendingIntent.getActivities(jobscheduler.this,0,new Intent[]{intent},0);
                NotificationCompat.Builder notification = new NotificationCompat.Builder(jobscheduler.this,channel_id);
                        notification.setContentTitle("Notification");
                        notification.setContentText(data);
                        notification.setSmallIcon(R.drawable.ic_settings);
                        if(condition==true)
                        {
                            notification.setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(image));
                        }

                        notification.setContentIntent(pendingintent);
                        notification.build();

                manager.notify(id,notification.build());
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("Job service","Job cancelled before completion");
        return false;
    }
}

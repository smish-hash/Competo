package com.StartupBBSR.competo.jobscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class jobscheduler extends JobService {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    String channel_name = "Job_service";
    String channel_id = "service3";


    @Override
    public boolean onStartJob(JobParameters params) {
        db.collection("messagenumber").document(firebaseAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d("Data recieved", String.valueOf(value.getData()));
                //notificationchannel(String.valueOf(value.getData()));
                dobackgroundwork(params);
            }
        });

        return true;
    }

    private void dobackgroundwork(JobParameters params)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT>=26)
                {
                    NotificationChannel notificationchannel = new NotificationChannel(channel_id,channel_name, NotificationManager.IMPORTANCE_NONE);
                    NotificationManager manager = getSystemService(NotificationManager.class);
                    manager.createNotificationChannel(notificationchannel);

                    Intent intent = new Intent(jobscheduler.this, MainActivity.class);
                    PendingIntent pendingintent = PendingIntent.getActivities(jobscheduler.this,0,new Intent[]{intent},0);
                    Notification notification = new NotificationCompat.Builder(jobscheduler.this,channel_id)
                            .setContentTitle("Job Service")
                            .setContentText("hemlo")
                            .setSmallIcon(R.drawable.ic_settings)
                            .setContentIntent(pendingintent)
                            .build();

                    manager.notify(6,notification);
                }
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}

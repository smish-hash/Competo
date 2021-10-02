package com.StartupBBSR.competo.backgroundservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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

public class backgroundservice extends BroadcastReceiver {

    String channel_name = "Background_service";
    String channel_id = "service2";

    @Override
    public void onReceive(Context context, Intent intent) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        db.collection("messagenumber").document(firebaseAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d("Data recieved", String.valueOf(value.getData()));

                NotificationChannel notificationchannel;
                NotificationManager manager;

                if(Build.VERSION.SDK_INT>=26)
                {
                    notificationchannel = new NotificationChannel(channel_id,channel_name, NotificationManager.IMPORTANCE_DEFAULT);
                    manager = context.getSystemService(NotificationManager.class);
                    manager.createNotificationChannel(notificationchannel);

                    Intent intent = new Intent(context, MainActivity.class);
                    PendingIntent pendingintent = PendingIntent.getActivities(context,0,new Intent[]{intent},0);
                    Notification notification = new NotificationCompat.Builder(context,channel_id)
                            .setContentTitle("Background Service")
                            .setContentText(String.valueOf(value.getData()))
                            .setSmallIcon(R.drawable.ic_settings)
                            .setContentIntent(pendingintent)
                            .build();

                    manager.notify(5, notification);
                }

            }
        });

    }
}

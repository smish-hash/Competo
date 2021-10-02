package com.StartupBBSR.competo.backgroundservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
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

public class backgroundservice extends Service {

    String channel_name = "Background_service";
    String channel_id = "service2";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        db.collection("messagenumber").document(firebaseAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d("Data recieved", String.valueOf(value.getData()));
                notificationchannel(String.valueOf(value.getData()));
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationchannel("hi");

        return START_STICKY;
    }

    private void notificationchannel(String data)
    {
        if(Build.VERSION.SDK_INT>=26)
        {
            NotificationChannel notificationchannel = new NotificationChannel(channel_id,channel_name, NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationchannel);

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingintent = PendingIntent.getActivities(this,0,new Intent[]{intent},0);
            Notification notification = new NotificationCompat.Builder(this,channel_id)
                    .setContentTitle("Background Service")
                    .setContentText(data)
                    .setSmallIcon(R.drawable.ic_settings)
                    .setContentIntent(pendingintent)
                    .build();

            manager.notify(5,notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Service Alert", "onDestroy: Service is destroyed :( ");
        Intent broadcastIntent = new Intent(this, backgroundservicerestart.class);
        sendBroadcast(broadcastIntent);
    }
}

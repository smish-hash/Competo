package com.StartupBBSR.competo.foregroundservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.R;

public class foregroundservice extends Service {

    String channel_name = "Foreground_service";
    String channel_id = "service1";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notificationchannel()
    {
        if(Build.VERSION.SDK_INT>=26)
        {
            NotificationChannel notificationchannel = new NotificationChannel(channel_id,channel_name, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationchannel);
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingintent = PendingIntent.getActivities(this,0,new Intent[]{intent},0);
        Notification notification = new NotificationCompat.Builder(this,channel_id)
                .setContentTitle("Foreground Service")
                .setContentText("Service is running in the background")
                .setSmallIcon(R.drawable.ic_settings)
                .setContentIntent(pendingintent)
                .build();

        startForeground(1,notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationchannel();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

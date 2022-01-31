package com.StartupBBSR.competo.Firebasemessaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

import com.StartupBBSR.competo.Activity.MainActivity;
import com.StartupBBSR.competo.Fragments.EventMainFragment;
import com.StartupBBSR.competo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.Objects;

public class firebasemessagingservice extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {

        if(Objects.equals(remoteMessage.getData().get("category"), "event"))
        {
                geteventmessage(remoteMessage.getData().get("title"),remoteMessage.getData().get("body"));
        }
        else if(Objects.equals(remoteMessage.getData().get("category"), "chat"))
        {
                getchatmessage(remoteMessage.getData().get("title"),remoteMessage.getData().get("body"),remoteMessage.getData().get("id"));
        }
        else if(Objects.equals(remoteMessage.getData().get("category"), "request"))
        {
                getrequestmessage(remoteMessage.getData().get("title"),remoteMessage.getData().get("body"));
        }
        else if(Objects.equals(remoteMessage.getData().get("category"), "team"))
        {
                getteammessage(remoteMessage.getData().get("title"),remoteMessage.getData().get("body"),remoteMessage.getData().get("teamId"));
        }
        super.onMessageReceived(remoteMessage);
    }

    public void geteventmessage(String title, String body) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationManager notificationmanager1 = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel("event_notification", "Events", NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setDescription("this is fcm event channel");

            notificationmanager1.createNotificationChannel(channel1);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "event_notification")
                .setSmallIcon(R.drawable.teamos_one_point_four_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int oneTimeID = (int) SystemClock.uptimeMillis();
        notificationmanager1.notify(oneTimeID, builder.build());
    }

    public void getrequestmessage(String title, String body) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationManager notificationmanager2 = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel2 = new NotificationChannel("request_notification", "Requests", NotificationManager.IMPORTANCE_DEFAULT);
            channel2.setDescription("this is fcm request channel");

            notificationmanager2.createNotificationChannel(channel2);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "request_notification")
                .setSmallIcon(R.drawable.teamos_one_point_four_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        int oneTimeID = (int) SystemClock.uptimeMillis();
        notificationmanager2.notify(oneTimeID, builder.build());
    }

    public void getchatmessage(String title, String body, String id) {

        SharedPreferences sharedPreferences = getSharedPreferences(id,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        int count = sharedPreferences.getInt("chatCount",0);
        int randomValue = (int) (Calendar.getInstance().getTimeInMillis() % 1000000000);
        int chat_notification_id = sharedPreferences.getInt("chat_notification_id",0);
        if(chat_notification_id == 0)
        {
            myEdit.putInt("chat_notification_id",randomValue);
        }
        if(count == 0)
        {
            myEdit.putString("chatMsg1",body);
            myEdit.putInt("chatCount",1);
        }
        else if(count == 1)
        {
            myEdit.putString("chatMsg2",body);
            myEdit.putInt("chatCount",2);
        }
        else
        {
            myEdit.putString("chatMsg1",sharedPreferences.getString("chatMsg2",null));
            myEdit.putString("chatMsg2",body);
        }
        myEdit.apply();

        Person user = new Person.Builder().setIcon(null).setName("Chat").build();
        NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(user).addMessage(sharedPreferences.getString("chatMsg1",null), Calendar.getInstance().getTimeInMillis(),title)
                .addMessage(sharedPreferences.getString("chatMsg2",null), Calendar.getInstance().getTimeInMillis(),title);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationManager notificationmanager3 = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel3 = new NotificationChannel("chat_notification", "Chats", NotificationManager.IMPORTANCE_DEFAULT);
            channel3.setDescription("this is fcm chat channel");

            notificationmanager3.createNotificationChannel(channel3);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat_notification")
                .setSmallIcon(R.drawable.teamos_one_point_four_logo)
                .setStyle(style)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //int oneTimeID = (int) SystemClock.uptimeMillis();
        notificationmanager3.notify(sharedPreferences.getInt("chat_notification_id",0), builder.build());
    }

    public void getteammessage(String title, String body, String id) {

        SharedPreferences sharedPreferences = getSharedPreferences(id,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        int count = sharedPreferences.getInt("teamCount",0);
        int randomValue = (int) (Calendar.getInstance().getTimeInMillis() % 1000000000);
        int chat_notification_id = sharedPreferences.getInt("team_notification_id",0);
        if(chat_notification_id == 0)
        {
            myEdit.putInt("team_notification_id",randomValue);
        }
        if(count == 0)
        {
            myEdit.putString("teamMsg1",body);
            myEdit.putString("teamTitle1",title);
            myEdit.putInt("teamCount",1);
        }
        else if(count == 1)
        {
            myEdit.putString("teamMsg2",body);
            myEdit.putString("teamTitle2",title);
            myEdit.putInt("teamCount",2);
        }
        else if(count == 2)
        {
            myEdit.putString("teamMsg3",body);
            myEdit.putString("teamTitle3",title);
            myEdit.putInt("teamCount",3);
        }
        else
        {
            myEdit.putString("teamMsg1",sharedPreferences.getString("teamMsg2",null));
            myEdit.putString("teamMsg2",sharedPreferences.getString("teamMsg3",null));
            myEdit.putString("teamMsg3",body);

            myEdit.putString("teamTitle1",sharedPreferences.getString("teamTitle2",null));
            myEdit.putString("teamTitle2",sharedPreferences.getString("teamTitle3",null));
            myEdit.putString("teamTitle3",title);
        }
        myEdit.apply();

        Person user = new Person.Builder().setIcon(null).setName("Teams").build();
        NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(user).addMessage(sharedPreferences.getString("teamMsg1",null), Calendar.getInstance().getTimeInMillis(),sharedPreferences.getString("teamTitle1",null))
                .addMessage(sharedPreferences.getString("teamMsg2",null), Calendar.getInstance().getTimeInMillis(),sharedPreferences.getString("teamTitle2",null))
                .addMessage(sharedPreferences.getString("teamMsg3",null), Calendar.getInstance().getTimeInMillis(),sharedPreferences.getString("teamTitle3",null));


        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationManager notificationmanager4 = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel4 = new NotificationChannel("team_notification", "Teams", NotificationManager.IMPORTANCE_DEFAULT);
            channel4.setDescription("this is fcm team channel");

            notificationmanager4.createNotificationChannel(channel4);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "team_notification")
                .setSmallIcon(R.drawable.teamos_one_point_four_logo)
                .setStyle(style)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //int oneTimeID = (int) SystemClock.uptimeMillis();
        notificationmanager4.notify(sharedPreferences.getInt("team_notification_id",0), builder.build());
    }
}

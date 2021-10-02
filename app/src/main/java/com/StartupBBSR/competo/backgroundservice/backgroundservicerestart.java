package com.StartupBBSR.competo.backgroundservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class backgroundservicerestart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("Service Alert", "Service started again");
        context.startService(new Intent(context, backgroundservice.class));
    }
}

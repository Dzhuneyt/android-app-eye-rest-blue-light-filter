package com.hasmobi.eyerest.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.hasmobi.eyerest.base.Application;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.services.SchedulerService;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.services.OverlayService;

public class OnBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Application.refreshServices(context);
        }
    }
}

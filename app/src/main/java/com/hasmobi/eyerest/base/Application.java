package com.hasmobi.eyerest.base;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.services.OverlayService;
import com.hasmobi.eyerest.services.SchedulerService;

public class Application extends android.app.Application {

	public FirebaseAnalytics analytics;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        final FirebaseRemoteConfigSettings fbs = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(fbs);
        mFirebaseRemoteConfig.setDefaults(R.xml.default_configs);
        mFirebaseRemoteConfig.fetch()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(getClass().toString(), "Remote config Fetch succeeded");
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Log.d(getClass().toString(), "Remote config Fetch Failed");
                        }
                    }
                });

		analytics = FirebaseAnalytics.getInstance(this);
    }

    static public boolean canDrawOverlay(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(context)) {
                // continue here - permission was granted
                return true;
            }
        } else {
            // SDK version lower than 23, the permission was granted during app installation
            return true;
        }
        return false;
    }

    static public void refreshServices(Context context) {
        if (OverlayService.isEnabled(context)) {
            if (SchedulerService.isEnabled(context)) {
                context.stopService(new Intent(context, OverlayService.class));
                context.startService(new Intent(context, SchedulerService.class));
            } else {
                context.stopService(new Intent(context, SchedulerService.class));
                context.startService(new Intent(context, OverlayService.class));
            }
        } else {
            context.stopService(new Intent(context, SchedulerService.class));
            context.stopService(new Intent(context, OverlayService.class));
        }
    }


    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}

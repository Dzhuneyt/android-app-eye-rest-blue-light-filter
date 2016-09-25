package com.hasmobi.eyerest.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.hasmobi.eyerest.base.Application;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.activities.MainActivity;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.custom_views.OverlayView;

public class OverlayService extends Service {
    private OverlayView mView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(getClass().getSimpleName(), "onStartCommand");

        final SharedPreferences sp = Prefs.get(getBaseContext());

        if (!OverlayService.isEnabled(getBaseContext())) {
            Log.e(getClass().toString(), "OverlayService enabled but the screen dim is disabled. Shutting down service...");
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!Application.canDrawOverlay(getBaseContext())) {
            Log.e(getClass().toString(), "Permission not granted, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        int opacityPercent = sp.getInt(Constants.PREF_DIM_LEVEL, 20);
        int color = sp.getInt(Constants.PREF_OVERLAY_COLOR, Color.BLACK);

        if (mView == null) {
            mView = new OverlayView(this);

            mView.setOpacityPercent(opacityPercent);
            mView.setColor(color);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.horizontalMargin = 0;
            params.verticalMargin = 0;

            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

            // Add the viw as screen overlay
            wm.addView(mView, params);
        } else {
            mView.setOpacityPercent(opacityPercent);
            mView.setColor(color);

            mView.redraw();
        }

        showNotification();

        return START_STICKY;
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_remove_red_eye_white_24dp)
                        .setContentTitle("Screen brightness optimized")
                        .setContentText("Tap to edit settings");

        mBuilder.setOngoing(true);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        startForeground(1000, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(getClass().getSimpleName(), "onDestroy");

        if (mView != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
            mView = null;
        }

        stopForeground(true);
    }

    static public boolean isEnabled(Context context) {
        SharedPreferences prefs = Prefs.get(context);
        return prefs.getBoolean(Constants.PREF_EYEREST_ENABLED, false);
    }

    static public boolean enable(Context context) {
        SharedPreferences prefs = Prefs.get(context);

        boolean wasEnabledNow = true;

        if (prefs.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
            wasEnabledNow = false;
        } else {
            prefs.edit().putBoolean(Constants.PREF_EYEREST_ENABLED, true).apply();
        }

        Application.refreshServices(context);

        if (wasEnabledNow) {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);

            Bundle b = new Bundle();
            b.putString(FirebaseAnalytics.Param.ITEM_ID, Constants.ANALYTICS_EVENT_OVERLAY_SERVICE);
            b.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "enabled");
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, b);
        }

        return wasEnabledNow;
    }

    static public boolean disable(Context context) {
        SharedPreferences prefs = Prefs.get(context);

        boolean wasDisabledNow = true;

        if (!prefs.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
            wasDisabledNow = false;
        } else {
            prefs.edit().putBoolean(Constants.PREF_EYEREST_ENABLED, false).apply();
        }

        Application.refreshServices(context);

        if (wasDisabledNow) {
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);

            Bundle b = new Bundle();
            b.putString(FirebaseAnalytics.Param.ITEM_ID, Constants.ANALYTICS_EVENT_OVERLAY_SERVICE);
            b.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "disabled");
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, b);
        }

        return wasDisabledNow;
    }
}
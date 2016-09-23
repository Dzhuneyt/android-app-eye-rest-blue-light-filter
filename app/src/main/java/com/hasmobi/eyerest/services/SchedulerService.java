package com.hasmobi.eyerest.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.base.Prefs;

import java.util.Calendar;

/**
 * Once triggered, this service will:
 * 1. Check if the "scheduled dim" is enabled
 * 1.1. If not, die and do nothing else.
 * 1.2 If enabled, schedule itself to be restarted every 30 minutes
 * 2. Enable or disable the "Overlay service" depending on the current time of day
 * <p>
 * The service is triggered:
 * - On device boot
 * - Manually, when you toggle the scheduler feature from the app
 */
public class SchedulerService extends Service {

    public SchedulerService() {
    }

    @Override
    public void onCreate() {
        Log.d(getClass().toString(), "onCreate");

        super.onCreate();

        final AlarmManager am = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);

        // Schedule this service to be restarted every 30 minutes
        Intent iEnd = new Intent(getBaseContext(), SchedulerService.class);
        PendingIntent piEnd = PendingIntent.getService(getBaseContext(), 0, iEnd, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_HALF_HOUR, piEnd);

        Log.d(getClass().toString(), "Scheduler startup at " + Calendar.getInstance().getTime() + ". Will repeat.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(getClass().toString(), "onStartCommand");

        if (!Prefs.get(getBaseContext()).getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
            // If the Scheduler is not enabled, this service should NEVER run again (unless invoked again manually)

            cancelAlarms();
            stopSelf();

            Log.d(getClass().toString(), "Scheduler not enabled. Recurring service is stopping itself...");
            return START_NOT_STICKY;
        } else {
            // Scheduler IS enabled

            startOrStopScreenDim();
            return START_STICKY;
        }
    }

    private void startOrStopScreenDim() {
        final SharedPreferences sp = Prefs.get(getBaseContext());

        if (sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
            Calendar cBegin = this._getCalendarForStart();
            Calendar cEnd = this._getCalendarForEnd();

            Log.d(getClass().toString(), "Screen darkens between " + cBegin.getTime() + " and " + cEnd.getTime());
            Log.d(getClass().toString(), "Now is " + Calendar.getInstance().getTime());

            Calendar calendar = Calendar.getInstance();

            if (calendar.getTimeInMillis() > cBegin.getTimeInMillis() && calendar.getTimeInMillis() < cEnd.getTimeInMillis()) {
                startService(new Intent(getBaseContext(), OverlayService.class));
                Log.d(getClass().toString(), "Screen darken started");
                Log.d(getClass().toString(), "Remaining seconds until lightening: " + (cEnd.getTimeInMillis() - calendar.getTimeInMillis()) / 1000);
            } else {
                stopService(new Intent(getBaseContext(), OverlayService.class));
                Log.d(getClass().toString(), "Screen darken stopped");
                Log.d(getClass().toString(), "Remaining seconds until darkening: " + (cBegin.getTimeInMillis() - calendar.getTimeInMillis()) / 1000);
            }
        } else {
            stopService(new Intent(getBaseContext(), OverlayService.class));
            Log.d(getClass().toString(), "Screen darken inactive. Stopping...");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().toString(), "onDestroy");

        cancelAlarms();

        super.onDestroy();
    }

    private Calendar _getCalendarForStart() {
        final SharedPreferences sp = Prefs.get(getBaseContext());

        final int scheduleFromHour = sp.getInt("scheduleFromHour", 20);
        final int scheduleFromMinute = sp.getInt("scheduleFromMinute", 0);

        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, scheduleFromHour);
        c.set(Calendar.MINUTE, scheduleFromMinute);
        c.clear(Calendar.SECOND);
        return c;
    }

    private Calendar _getCalendarForEnd() {
        final SharedPreferences sp = Prefs.get(getBaseContext());

        final int hour = sp.getInt("scheduleToHour", 6);
        final int minute = sp.getInt("scheduleToMinute", 0);

        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.clear(Calendar.SECOND);

        // Roll over +1 day to accommodate cases where the screen dimmer restore
        // happens on the next morning
        if (c.getTimeInMillis() < _getCalendarForStart().getTimeInMillis()) {
            c.add(Calendar.DATE, 1);
        }
        return c;
    }

    private void cancelAlarms() {
        final AlarmManager am = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);

        Intent iBegin = new Intent(getBaseContext(), SchedulerService.class);
        PendingIntent piBegin = PendingIntent.getService(getBaseContext(), 0, iBegin, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(piBegin);
    }

    static public boolean isEnabled(Context context) {
        SharedPreferences prefs = Prefs.get(context);
        return prefs.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false);
    }

    static public boolean enable(Context context) {
        SharedPreferences prefs = Prefs.get(context);

        boolean wasEnabledNow = true;

        context.stopService(new Intent(context, OverlayService.class));
        if (prefs.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
            wasEnabledNow = false;
        } else {
            prefs.edit().putBoolean(Constants.PREF_SCHEDULER_ENABLED, true).apply();
        }
        context.startService(new Intent(context, OverlayService.class));

        return wasEnabledNow;
    }

    static public boolean disable(Context context) {
        SharedPreferences prefs = Prefs.get(context);

        boolean wasDisabledNow = true;

        context.stopService(new Intent(context, OverlayService.class));
        if (!prefs.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
            wasDisabledNow = false;
        } else {
            prefs.edit().putBoolean(Constants.PREF_SCHEDULER_ENABLED, false).apply();
        }

        return wasDisabledNow;
    }
}

package com.hasmobi.eyerest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

public class SchedulerService extends Service {

	public SchedulerService() {
	}

	@Override
	public void onCreate() {
		Log.d(getClass().toString(), "onCreate");

		super.onCreate();

		final AlarmManager am = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);

		// Start a repeating service call hourly that will restart this
		// service, which will in turn enable or disable the screen
		// darkening service depending on which hour of the day it is
		Intent iEnd = new Intent(getBaseContext(), SchedulerService.class);
		PendingIntent piEnd = PendingIntent.getService(getBaseContext(), 0, iEnd, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 15, piEnd);

		Log.d(getClass().toString(), "Scheduler startup at " + Calendar.getInstance().getTime() + ". Will repeat.");
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(getClass().toString(), "onStartCommand");

		final SharedPreferences sp = Prefs.get(getBaseContext());

		// If the Scheduler is not enabled, this service should NEVER run
		if (!sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
			cancelAlarms();
			stopSelf();

			Log.d(getClass().toString(), "Scheduler not enabled. Stopping self service...");
			return START_NOT_STICKY;
		}

		if (sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
			Calendar cBegin = this.getCalendarForStart();
			Calendar cEnd = this.getCalendarForEnd();

			Log.d(getClass().toString(), "Screen darkens between " + cBegin.getTime() + " and " + cEnd.getTime());
			Log.d(getClass().toString(), "Now is " + Calendar.getInstance().getTime());

			Calendar calendar = Calendar.getInstance();

			if (calendar.getTimeInMillis() > cBegin.getTimeInMillis() && calendar.getTimeInMillis() < cEnd.getTimeInMillis()) {
				startService(new Intent(getBaseContext(), OverlayService.class));
				Log.d(getClass().toString(), "Screen darken started");
			} else {
				stopService(new Intent(getBaseContext(), OverlayService.class));
				Log.d(getClass().toString(), "Screen darken stopped");
			}
		} else {
			stopService(new Intent(getBaseContext(), OverlayService.class));
			Log.d(getClass().toString(), "Screen darken inactive. Stopping...");
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(getClass().toString(), "onDestroy");

		cancelAlarms();

		super.onDestroy();
	}

	private Calendar getCalendarForStart() {
		final SharedPreferences sp = Prefs.get(getBaseContext());

		final int scheduleFromHour = sp.getInt("scheduleFromHour", 20);
		final int scheduleFromMinute = sp.getInt("scheduleFromMinute", 0);

		final Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, scheduleFromHour);
		c.set(Calendar.MINUTE, scheduleFromMinute);
		c.clear(Calendar.SECOND);
		return c;
	}

	private Calendar getCalendarForEnd() {
		final SharedPreferences sp = Prefs.get(getBaseContext());

		final int hour = sp.getInt("scheduleToHour", 6);
		final int minute = sp.getInt("scheduleToMinute", 0);

		final Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.clear(Calendar.SECOND);

		if (c.getTimeInMillis() < getCalendarForStart().getTimeInMillis()) {
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
}

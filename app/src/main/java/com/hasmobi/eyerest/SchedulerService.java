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

	private static final String ACTION_SCREEN_DARKEN_START = "begin_screen_darken";
	private static final String ACTION_SCREEN_DARKEN_END = "end_screen_darken";

	public SchedulerService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String intentAction = intent.getAction();

		Log.d(getClass().toString(), "onStartCommand using intent action: " + intentAction + " at " + Calendar.getInstance().getTime());

		final SharedPreferences sp = Prefs.get(getBaseContext());

		// If the Scheduler is not enabled, this service should NEVER run
		if (!sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
			cancelAlarms();
			stopSelf();
			return START_NOT_STICKY;
		}

		final int scheduleFromHour = sp.getInt("scheduleFromHour", 20);
		final int scheduleFromMinute = sp.getInt("scheduleFromMinute", 0);

		final int scheduleToHour = sp.getInt("scheduleToHour", 6);
		final int scheduleToMinute = sp.getInt("scheduleToMinute", 0);

		long now = Calendar.getInstance().getTimeInMillis();

		final AlarmManager am = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);

		// Prepare the Calendar object that corresponds to the
		// first run timestamp of the screen darkening recurrence start
		Calendar cBeginScreenDarken = Calendar.getInstance();
		cBeginScreenDarken.set(Calendar.HOUR_OF_DAY, scheduleFromHour);
		cBeginScreenDarken.set(Calendar.MINUTE, scheduleFromMinute);
		cBeginScreenDarken.clear(Calendar.SECOND);

		if (cBeginScreenDarken.getTimeInMillis() < now) {
			startService(new Intent(this, OverlayService.class));
		}

		// Start a repeating service call every day that will
		// enable the "screen darkening" at a given time of the day
		Intent iBegin = new Intent(getBaseContext(), OverlayService.class);
		PendingIntent piBegin = PendingIntent.getService(getBaseContext(), 0, iBegin, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC, cBeginScreenDarken.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piBegin);

		Log.d(getClass().toString(), "Scheduled screen darkening at " + cBeginScreenDarken.getTime() + " and every day after that");

		// Prepare the Calendar object that corresponds to the
		// first run timestamp of the screen darkening recurrence end
		Calendar cEndScreenDarken = Calendar.getInstance();
		cEndScreenDarken.set(Calendar.HOUR_OF_DAY, scheduleToHour);
		cEndScreenDarken.set(Calendar.MINUTE, scheduleToMinute);
		cEndScreenDarken.clear(Calendar.SECOND);

		// Start a repeating service call every day that will
		// disable the "screen darkening" at a given time of the day
		Intent iEnd = new Intent(getBaseContext(), OverlayService.class);
		PendingIntent piEnd = PendingIntent.getService(getBaseContext(), 0, iEnd, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC, cEndScreenDarken.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piEnd);

		Log.d(getClass().toString(), "Scheduled screen darkening end at " + cEndScreenDarken.getTime() + " and every day after that");

		return START_STICKY;
	}

	public int onStartCommandOld(Intent intent, int flags, int startId) {

		final String intentAction = intent.getAction();

		Log.d(getClass().toString(), "onStartCommand using intent action: " + intentAction + " at " + Calendar.getInstance().getTime());

		final SharedPreferences sp = Prefs.get(getBaseContext());

		// If the Scheduler is not enabled, this service should NEVER run
		if (!sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
			cancelAlarms();
			stopSelf();
			return START_NOT_STICKY;
		}

		if (intentAction != null) {
			if (intentAction.equals(ACTION_SCREEN_DARKEN_START)) {
				Log.d(getClass().toString(), "Starting scheduled screen darkening");
				startService(new Intent(getBaseContext(), OverlayService.class));
			} else if (intentAction.equals(ACTION_SCREEN_DARKEN_END)) {
				Log.d(getClass().toString(), "Ending scheduled screen darkening");
				stopService(new Intent(getBaseContext(), OverlayService.class));
			}
		} else {
			// Service started regularly (from an activity), not self call

			final int scheduleFromHour = sp.getInt("scheduleFromHour", 20);
			final int scheduleFromMinute = sp.getInt("scheduleFromMinute", 0);

			final int scheduleToHour = sp.getInt("scheduleToHour", 6);
			final int scheduleToMinute = sp.getInt("scheduleToMinute", 0);

			long now = Calendar.getInstance().getTimeInMillis();

			final AlarmManager am = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);

			// Prepare the Calendar object that corresponds to the
			// first run timestamp of the screen darkening recurrence start
			Calendar cBeginScreenDarken = Calendar.getInstance();
			cBeginScreenDarken.set(Calendar.HOUR_OF_DAY, scheduleFromHour);
			cBeginScreenDarken.set(Calendar.MINUTE, scheduleFromMinute);
			cBeginScreenDarken.clear(Calendar.SECOND);

			if (cBeginScreenDarken.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
				// schedule for the same time tomorrow, but tomorrow
				cBeginScreenDarken.add(Calendar.DATE, 1);
			}

			// Start a repeating service call every day that will
			// enable the "screen darkening" at a given time of the day
			Intent iBegin = new Intent(getBaseContext(), SchedulerService.class);
			iBegin.setAction(ACTION_SCREEN_DARKEN_START);
			PendingIntent piBegin = PendingIntent.getService(getBaseContext(), 0, iBegin, PendingIntent.FLAG_UPDATE_CURRENT);
			//am.setRepeating(AlarmManager.RTC, cBeginScreenDarken.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piBegin);

			// Set the "enabling" of the service to run at time X today
			// If time X has already passed, the Pending Intent will be fired
			// now (as soon as possible)
			if (android.os.Build.VERSION.SDK_INT < 19) {
				am.set(AlarmManager.RTC, cBeginScreenDarken.getTimeInMillis(), piBegin);
			} else {
				am.setExact(AlarmManager.RTC, cBeginScreenDarken.getTimeInMillis(), piBegin);
			}

			Log.d(getClass().toString(), "Scheduled screen darkening at " + cBeginScreenDarken.getTime() + " and every day after that");

			// Prepare the Calendar object that corresponds to the
			// first run timestamp of the screen darkening recurrence end
			Calendar cEndScreenDarken = Calendar.getInstance();
			cEndScreenDarken.set(Calendar.HOUR_OF_DAY, scheduleToHour);
			cEndScreenDarken.set(Calendar.MINUTE, scheduleToMinute);
			cEndScreenDarken.clear(Calendar.SECOND);

			// Start a repeating service call every day that will
			// disable the "screen darkening" at a given time of the day
			Intent iEnd = new Intent(getBaseContext(), SchedulerService.class);
			iEnd.setAction(ACTION_SCREEN_DARKEN_END);
			PendingIntent piEnd = PendingIntent.getService(getBaseContext(), 0, iEnd, PendingIntent.FLAG_UPDATE_CURRENT);
			//am.setRepeating(AlarmManager.RTC, cEndScreenDarken.getTimeInMillis(), AlarmManager.INTERVAL_DAY, piEnd);

			//Log.d(getClass().toString(), "Scheduled screen darkening end at " + cEndScreenDarken.getTime() + " and every day after that");

			return START_STICKY;
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		cancelAlarms();

		super.onDestroy();
	}

	private void cancelAlarms() {
		final AlarmManager am = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);

		Intent iBegin = new Intent(getBaseContext(), SchedulerService.class);
		iBegin.setAction(ACTION_SCREEN_DARKEN_START);
		PendingIntent piBegin = PendingIntent.getService(getBaseContext(), 0, iBegin, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent iEnd = new Intent(getBaseContext(), SchedulerService.class);
		iEnd.setAction(ACTION_SCREEN_DARKEN_END);
		PendingIntent piEnd = PendingIntent.getService(getBaseContext(), 0, iEnd, PendingIntent.FLAG_UPDATE_CURRENT);

		am.cancel(piBegin);
		am.cancel(piEnd);
	}
}

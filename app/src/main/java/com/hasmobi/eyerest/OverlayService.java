package com.hasmobi.eyerest;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import java.util.Calendar;

public class OverlayService extends Service {
	OverlayView mView;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(getClass().getSimpleName(), "onStartCommand");

		final SharedPreferences sp = Prefs.get(getBaseContext());

		if (!sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
			Log.e(getClass().toString(), "OverlayService enabled but the screen dim is disabled. Shutting down service...");
			stopSelf();
			return START_NOT_STICKY;
		}

		if (sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
			Log.d(getClass().toString(), "Probably called by scheduler");
			long now = Calendar.getInstance().getTimeInMillis();

			final int scheduleFromHour = sp.getInt("scheduleFromHour", 20);
			final int scheduleFromMinute = sp.getInt("scheduleFromMinute", 0);

			final int scheduleToHour = sp.getInt("scheduleToHour", 6);
			final int scheduleToMinute = sp.getInt("scheduleToMinute", 0);

			Calendar cBegin = Calendar.getInstance();
			cBegin.set(Calendar.HOUR_OF_DAY, scheduleFromHour);
			cBegin.set(Calendar.MINUTE, scheduleFromMinute);
			cBegin.clear(Calendar.SECOND);

			Calendar cEnd = Calendar.getInstance();
			cEnd.set(Calendar.HOUR_OF_DAY, scheduleToHour);
			cEnd.set(Calendar.MINUTE, scheduleToMinute);
			cEnd.clear(Calendar.SECOND);

			if (cBegin.getTimeInMillis() > now) {
				Log.d(getClass().toString(), "The scheduler will darken the screen at " + cBegin.getTime());
				stopSelf();
				return START_NOT_STICKY;
			} else {
				Log.d(getClass().toString(), "Screen was dimmed this many seconds ago:" + (cBegin.getTimeInMillis() - now) / 1000);
			}
			if (cEnd.getTimeInMillis() < now) {
				Log.d(getClass().toString(), "The scheduler already ended the screen darkening session this many seconds ago " + (cEnd.getTimeInMillis() - now) / 1000);
				stopSelf();
				return START_NOT_STICKY;
			} else {
				Log.d(getClass().toString(), "Remaining seconds until full brightness is restored: " + (cEnd.getTimeInMillis() - now) / 1000);
			}
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
						.setSmallIcon(R.drawable.ic_check_white_48dp)
						.setContentTitle("Eye strain reduced")
						.setContentText("Tap to edit settings or disable");

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
}
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

public class OverlayService extends Service {
	OverlayView mView;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(getClass().getSimpleName(), "onStartCommand");

		SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
		int opacityPercent = sp.getInt("opacity_percent", 20);
		int color = sp.getInt("overlay_color", Color.BLACK);

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

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(getClass().getSimpleName(), "onCreate");
	}

	private void showNotification() {
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.icon)
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
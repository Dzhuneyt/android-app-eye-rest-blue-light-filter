package com.hasmobi.eyerest.base;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.hasmobi.eyerest.R;

public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Resources res = getResources();
		String key1 = res.getString(R.string.key1);
		String key2 = res.getString(R.string.key1);
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

	private Tracker mTracker;

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 *
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.analytics);
		}
		return mTracker;
	}
}

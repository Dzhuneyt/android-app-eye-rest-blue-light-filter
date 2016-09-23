package com.hasmobi.eyerest.base;

import android.content.res.Resources;

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

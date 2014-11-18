package com.hasmobi.eyerest;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class DApplication extends Application{

	public Tracker tracker;

	@Override
	public void onCreate() {
		super.onCreate();

		GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(this);

		this.tracker = googleAnalytics.newTracker(R.xml.analytics);
	}
}

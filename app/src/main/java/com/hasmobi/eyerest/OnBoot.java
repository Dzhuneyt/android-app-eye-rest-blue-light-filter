package com.hasmobi.eyerest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class OnBoot extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			// Set the alarm here.

			SharedPreferences sp = Prefs.get(context);

			boolean darkeningEnabled = sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false);
			boolean scheduledDarkeningOn = sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false);

			if (scheduledDarkeningOn) {
				// Let the scheduler start the regular service if needed
				// (if we are in the specified interval)
				context.startService(new Intent(context, SchedulerService.class));
			} else {
				if (darkeningEnabled) {
					context.startService(new Intent(context, OverlayService.class));
				}
			}
		}
	}
}

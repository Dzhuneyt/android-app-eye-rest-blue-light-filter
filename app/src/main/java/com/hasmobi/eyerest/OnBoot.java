package com.hasmobi.eyerest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBoot extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			// Set the alarm here.
			boolean enabled = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("filter_enabled", false);

			if (enabled) {
				context.startService(new Intent(context, OverlayService.class));
			}
		}
	}
}

package com.hasmobi.eyerest.custom_views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import com.hasmobi.eyerest.services.SchedulerService;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.services.OverlayService;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;


public class DiscreeteSeekBar extends DiscreteSeekBar {

    private void attachListener() {
        this.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            final SharedPreferences sp = Prefs.get(getContext());
            int currentProgress = 0;
            final Handler h = new Handler();
            final Runnable r = new Runnable() {

                @Override
                public void run() {
                    sp.edit().putInt(Constants.PREF_DIM_LEVEL, currentProgress).apply();

                    if (sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
                        getContext().startService(new Intent(getContext(), SchedulerService.class));
                    } else {
                        getContext().startService(new Intent(getContext(), OverlayService.class));
                    }
                }
            };

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int intensityPercent, boolean fromUser) {
                if (!fromUser) return;

                Log.d(getClass().toString(), "onProgressChanged");

                currentProgress = intensityPercent;

                // Delay all saves to shared prefs by 300 milliseconds
                // and cancel all current pending saves. This is
                // to avoid saving too much times in one second if you
                // move the slider quickly
                // (which causes lag due to too much file writes)
                h.removeCallbacks(r);
                h.postDelayed(r, 300);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
            }
        });
    }

    public DiscreeteSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        attachListener();
    }

    public DiscreeteSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        attachListener();
    }

    public DiscreeteSeekBar(Context context) {
        super(context);
        attachListener();
    }
}

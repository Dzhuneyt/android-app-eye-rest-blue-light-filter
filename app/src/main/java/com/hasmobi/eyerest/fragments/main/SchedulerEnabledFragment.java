package com.hasmobi.eyerest.fragments.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.base.Application;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.helpers.IShowHideScheduler;
import com.hasmobi.eyerest.services.SchedulerService;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SchedulerEnabledFragment extends Fragment {

    private IShowHideScheduler bridge;

    private CountDownTimer timer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scheduler_enabled, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        bridge = ((IShowHideScheduler) getActivity());
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button bFrom = (Button) view.findViewById(R.id.bFrom);
        final Button bTo = (Button) view.findViewById(R.id.bTo);

        Button b = (Button) view.findViewById(R.id.bSchedule);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchedulerService.disable(getContext());
                bridge.showOrHideSchedulerUI(false);
            }
        });

        reloadButtonUIs();

        bFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sp = Prefs.get(getContext());
                final int scheduleFromHour = sp.getInt("scheduleFromHour", 20);
                final int scheduleFromMinute = sp.getInt("scheduleFromMinute", 0);

                TimePickerDialog dpd = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
                                Context ctx = view.getContext();
                                final SharedPreferences sp = Prefs.get(ctx);
                                sp.edit().putInt("scheduleFromHour", hourOfDay).putInt("scheduleFromMinute", minute).apply();
                                reloadButtonUIs();
                            }
                        },
                        scheduleFromHour,
                        scheduleFromMinute,
                        true
                );
                dpd.show(getActivity().getFragmentManager(), "timepicker_dialog");
            }
        });
        bTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sp = Prefs.get(getContext());
                final int scheduleToHour = sp.getInt("scheduleToHour", 6);
                final int scheduleToMinute = sp.getInt("scheduleToMinute", 0);

                TimePickerDialog dpd = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
                                Context ctx = view.getContext();
                                final SharedPreferences sp = Prefs.get(ctx);
                                sp.edit().putInt("scheduleToHour", hourOfDay).putInt("scheduleToMinute", minute).apply();
                                reloadButtonUIs();
                            }
                        },
                        scheduleToHour,
                        scheduleToMinute,
                        true
                );
                dpd.show(getActivity().getFragmentManager(), "timepicker_dialog");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void reloadButtonUIs() {
        if (getView() == null) return;

        final SharedPreferences sp = Prefs.get(getContext());

        final int scheduleFromHour = sp.getInt("scheduleFromHour", 20);
        final int scheduleFromMinute = sp.getInt("scheduleFromMinute", 0);

        final int scheduleToHour = sp.getInt("scheduleToHour", 6);
        final int scheduleToMinute = sp.getInt("scheduleToMinute", 0);

        final Button bFrom = (Button) getView().findViewById(R.id.bFrom);
        final Button bTo = (Button) getView().findViewById(R.id.bTo);
        final TextView tvRemaining = (TextView) getView().findViewById(R.id.tvTimeRemaining);

        Calendar cNow = Calendar.getInstance();
        Calendar cStart = SchedulerService._getCalendarForStart(getContext());
        final Calendar cEnd = SchedulerService._getCalendarForEnd(getContext());

        if (timer != null) {
            timer.cancel();
        }

        final String time_remaining_to_darken_label = getResources().getString(R.string.time_remaining_to_darken_label);
        final String time_remaining_to_lighten_label = getResources().getString(R.string.time_remaining_to_lighten_label);

        tvRemaining.setVisibility(View.VISIBLE);

        if (cNow.getTimeInMillis() > cStart.getTimeInMillis() && cNow.getTimeInMillis() < cEnd.getTimeInMillis()) {
            // We are now in the darkening period
            long remainingMillisToLighten = cEnd.getTimeInMillis() - cNow.getTimeInMillis();
            timer = new CountDownTimer(remainingMillisToLighten, 1000) {

                public void onTick(long millisUntilFinished) {
                    String t = time_remaining_to_lighten_label + ": " + String.format(Locale.getDefault(), "%tT", (millisUntilFinished - TimeZone.getDefault().getRawOffset()));
                    tvRemaining.setText(t);
                }

                public void onFinish() {
                    reloadButtonUIs();
                }
            }.start();
        } else if (cNow.getTimeInMillis() < cStart.getTimeInMillis()) {
            // Darkening not yet started for today
            long remainingMillisToDarken = cStart.getTimeInMillis() - cNow.getTimeInMillis();
            timer = new CountDownTimer(remainingMillisToDarken, 1000) {

                public void onTick(long millisUntilFinished) {
                    String t = time_remaining_to_darken_label + ": " + String.format(Locale.getDefault(), "%tT", (millisUntilFinished - TimeZone.getDefault().getRawOffset()));
                    tvRemaining.setText(t);
                }

                public void onFinish() {
                    reloadButtonUIs();
                }
            }.start();
        } else {
            // Darkening has ended for today
            tvRemaining.setVisibility(View.GONE);
        }

        bFrom.setText(String.format(Locale.getDefault(), "%02d:%02d", scheduleFromHour, scheduleFromMinute));
        bTo.setText(String.format(Locale.getDefault(), "%02d:%02d", scheduleToHour, scheduleToMinute));

        if (getContext() != null) {
            Application.refreshServices(getContext());
        }
    }
}
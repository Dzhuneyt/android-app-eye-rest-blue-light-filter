package com.hasmobi.eyerest.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.helpers.IShowHideScheduler;
import com.hasmobi.eyerest.services.OverlayService;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class SchedulerEnabledFragment extends Fragment {

    private IShowHideScheduler bridge;

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
                Prefs.get(getContext()).edit().putBoolean(Constants.PREF_SCHEDULER_ENABLED, false).apply();

                if (OverlayService.isEnabled(getContext())) {
                    getContext().startService(new Intent(getContext(), OverlayService.class));
                }

                bridge.showOrHideSchedulerUI(false);
            }
        });

        reloadButtonUIs();

//		cbEnableScheduledDim.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean schedulerEnable) {
//				Prefs.get(buttonView.getContext()).edit().putBoolean(Constants.PREF_SCHEDULER_ENABLED, schedulerEnable).apply();
//
//				reloadButtonUIs();
//			}
//		});

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

    private void reloadButtonUIs() {
        if (getView() == null) return;

        final SharedPreferences sp = Prefs.get(getContext());

        final int scheduleFromHour = sp.getInt("scheduleFromHour", 20);
        final int scheduleFromMinute = sp.getInt("scheduleFromMinute", 0);

        final int scheduleToHour = sp.getInt("scheduleToHour", 6);
        final int scheduleToMinute = sp.getInt("scheduleToMinute", 0);

        LinearLayout ll = (LinearLayout) getView().findViewById(R.id.llWrapHourPickers);
        final Button bFrom = (Button) getView().findViewById(R.id.bFrom);
        final Button bTo = (Button) getView().findViewById(R.id.bTo);

        bFrom.setText(String.format("%02d:%02d", scheduleFromHour, scheduleFromMinute));
        bTo.setText(String.format("%02d:%02d", scheduleToHour, scheduleToMinute));

        if (!sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
            getContext().stopService(new Intent(getContext(), OverlayService.class));
        } else {
            getContext().startService(new Intent(getContext(), OverlayService.class));
        }
    }
}
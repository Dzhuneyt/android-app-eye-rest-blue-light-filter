package com.hasmobi.eyerest.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.base.Application;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.helpers.IShowHideScheduler;
import com.hasmobi.eyerest.services.SchedulerService;

public class SchedulerDisabledFragment extends Fragment {

    private IShowHideScheduler bridge;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View r = inflater.inflate(R.layout.fragment_scheduler_disabled, container, false);
        Button b = (Button) r.findViewById(R.id.bSchedule);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchedulerService.enable(getContext());
                bridge.showOrHideSchedulerUI(true);
            }
        });
        return r;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        bridge = ((IShowHideScheduler) getActivity());
    }
}

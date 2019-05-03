package com.hasmobi.eyerest.fragments.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.base.Application;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.helpers.RequestDrawOverAppsPermission;
import com.hasmobi.eyerest.services.OverlayService;

public class WelcomeFragment extends Fragment {

    private FirebaseAnalytics analytics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        analytics = ((Application) (getActivity().getApplication())).analytics;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final Button bEnable = view.findViewById(R.id.bEnable);
        bEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context ctx = (getContext() == null ? v.getContext() : getContext());
                final RequestDrawOverAppsPermission permissionRequester = new RequestDrawOverAppsPermission(getActivity());
                if (!permissionRequester.canDrawOverlays()) {
                    // Show alert dialog
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setMessage("Applying a blue light filter to overlay the screen, requires a special permission called \"Display over other apps\". \n\nPlease, allow this permission on the next screen so that the blue light filter can be applied");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "Allow permission",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    permissionRequester.requestPermissionDrawOverOtherApps();
                                }
                            });


                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else {
                    // System permission already approved
                    SharedPreferences sp = Prefs.get(ctx);
                    sp.edit().putBoolean(Constants.PREF_EYEREST_ENABLED, true).apply();
                    sp.edit().putInt(Constants.PREF_DIM_LEVEL, 40).apply();

                    ctx.startService(new Intent(getContext(), OverlayService.class));
                    OverlayService.enable(v.getContext());

                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Done! It was that easy.", Snackbar.LENGTH_LONG).show();

                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main, new SettingsFragment()).commit();

                    Bundle bundle = new Bundle();
                    analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
                }
            }
        });
    }
}

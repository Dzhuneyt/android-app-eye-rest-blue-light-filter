package com.hasmobi.eyerest.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.activities.MainActivity;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.helpers.RequestDrawOverAppsPermission;
import com.hasmobi.eyerest.services.OverlayService;

import java.lang.ref.WeakReference;

public class WelcomeFragment extends Fragment {

    private RequestDrawOverAppsPermission permissionRequester;
    private boolean permissionGranted = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Prepare the "android.permission.SYSTEM_ALERT_WINDOW"
        // on-demand permission checking helper for devices with API 23+
        permissionRequester = new RequestDrawOverAppsPermission(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().toString(), "onActivityResult:" + requestCode + ":" + resultCode + ":" + (data != null ? data.toString() : ""));
        if (permissionRequester.requestCodeMatches(requestCode)) {
            if (permissionRequester.needsPermissionApproval()) {
                // User cancelled without approving permission request
                permissionGranted = false;
            } else {
                permissionGranted = true;
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (!permissionRequester.needsPermissionApproval()) {
            // Permission given during app installation
            permissionGranted = true;
        }

        final ShimmerFrameLayout container = (ShimmerFrameLayout) view.findViewById(R.id.shimmer_view_container);
        container.setBaseAlpha(0.7f);
        container.setDuration(2000);
        container.startShimmerAnimation();


        view.findViewById(R.id.bEnable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!permissionGranted) {
                    permissionRequester.requestPermission();
                    return;
                }

                if (actionBar != null) {
                    actionBar.show();
                }

                OverlayService.enable(v.getContext());

                SharedPreferences sp = Prefs.get(getContext());
                sp.edit().putInt(Constants.PREF_DIM_LEVEL, 40).apply();

                v.getContext().startService(new Intent(v.getContext(), OverlayService.class));

                final WeakReference<View> snackbar = new WeakReference<>(getActivity().findViewById(android.R.id.content));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View snackbarView = snackbar.get();
                        if (snackbarView != null) {
                            final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                            Snackbar.make(snackbarView, mFirebaseRemoteConfig.getString("after_enable_message"), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, 2000);


                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main, new SettingsFragment()).commit();
            }
        });
    }
}

package com.hasmobi.eyerest.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.fragment.app.Fragment;

import com.hasmobi.eyerest.base.Application;

public class RequestDrawOverAppsPermission {

    private Activity activity = null;
    private Fragment fragment = null;
    private Context context = null;

    public RequestDrawOverAppsPermission(Activity activity) {
        this.activity = activity;
    }

    public RequestDrawOverAppsPermission(Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.getContext();
    }

    /**
     * code to post/handler request for permission
     */
    private final static int REQUEST_CODE = 5463;

    public boolean needsPermissionApproval() {
        /** check if we already  have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                return true;
            }
        }
        return false;
    }

    public void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));

            fragment.startActivityForResult(intent, REQUEST_CODE);
        }
    }

    public boolean requestCodeMatches(int requestCode) {
        return REQUEST_CODE == requestCode;
    }

    public boolean canDrawOverlays() {
        return Application.canDrawOverlay(activity.getBaseContext());
    }

    public void requestPermissionDrawOverOtherApps() {
        final Context context = activity.getBaseContext();

        /* check if we already have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                /* if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                /* request permission via start activity for result */

                activity.startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }
}

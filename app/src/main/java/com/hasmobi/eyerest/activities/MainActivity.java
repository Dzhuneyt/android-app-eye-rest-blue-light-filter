package com.hasmobi.eyerest.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.crash.FirebaseCrash;
import com.hasmobi.eyerest.base.Constants;
import com.hasmobi.eyerest.fragments.main.SchedulerDisabledFragment;
import com.hasmobi.eyerest.helpers.IShowHideScheduler;
import com.hasmobi.eyerest.services.OverlayService;
import com.hasmobi.eyerest.base.Prefs;
import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.fragments.main.SchedulerEnabledFragment;
import com.hasmobi.eyerest.services.SchedulerService;
import com.hasmobi.eyerest.fragments.main.SettingsFragment;
import com.hasmobi.eyerest.fragments.main.WelcomeFragment;
import com.hasmobi.eyerest.billingutil.IabHelper;
import com.hasmobi.eyerest.billingutil.IabResult;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IShowHideScheduler {

    // Google In-App Billing related:
    public IInAppBillingService mBillingService;
    IabHelper iabHelper;

    private String TAG = getClass().toString();

    private ServiceConnection _mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBillingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mBillingService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google In-App Billing related:
        Intent iBilling = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        iBilling.setPackage("com.android.vending");
        //bindService(iBilling, _mServiceConn, Context.BIND_AUTO_CREATE);

        final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmDE8PlnfNAzau5GPe/EAjl0yJnK0DisSeqtvvohrQROtoHXtaFXSXsmyzIfHrfkbt3V2GhJVG3mNwUy7HJ6qUzvU9C2TPb1Rkhhu/86FmaxUTGT6pgF1d3rekM1Urw7FNwjlh4VVdDhqI2myoJfQBwHvEFyLIgZP5ij2xCidqP36ZoGTaQkYI8nVDTm76tdRkFF5uS0UUooHkbdK1AfhvVr3rpZK/JdWMKAS5AmT0PgPC1nDa41XzcUfQEAt+2GVdYXUJ/WifE/PgGiHsOcPsaYKsB711t0SVGhLbn71cwUIc/KhbjdpRizkgzGoTwboO7Jh9U6oUOnQkxrmg8qW6QIDAQAB";

        // compute your public key and store it in base64EncodedPublicKey
        iabHelper = new IabHelper(getBaseContext(), base64EncodedPublicKey);
        iabHelper.enableDebugLogging(true, TAG);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
            }
        });

        final SharedPreferences sp = Prefs.get(getBaseContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            if (!sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main, new WelcomeFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.main, new SettingsFragment()).commit();
            }
        }


        if (sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
            // Start the scheduler and leave the control of starting/stopping
            // the main service (OverlayService.class) to it
            startService(new Intent(getBaseContext(), SchedulerService.class));
        } else {
            if (sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
                startService(new Intent(getBaseContext(), OverlayService.class));
            } else {
                stopService(new Intent(getBaseContext(), OverlayService.class));
            }
        }

//        CheckBox cbEnable = (CheckBox) findViewById(R.id.cbEnable);
//        if (cbEnable != null) {
//            cbEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    final SharedPreferences sp = Prefs.get(getBaseContext());
//
//                    sp.edit().putBoolean(Constants.PREF_EYEREST_ENABLED, isChecked).apply();
//
//                    if (isChecked) {
//                        if (sp.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
//                            getBaseContext().startService(new Intent(getBaseContext(), SchedulerService.class));
//                        } else {
//                            getBaseContext().startService(new Intent(getBaseContext(), OverlayService.class));
//                        }
//                    } else {
//                        getBaseContext().stopService(new Intent(getBaseContext(), OverlayService.class));
//                    }
//                }
//            });
//
//            cbEnable.setChecked(sp.getBoolean(Constants.PREF_EYEREST_ENABLED, false));
//        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        final SharedPreferences prefs = Prefs.get(getBaseContext());

        if (prefs.getBoolean(Constants.PREF_SCHEDULER_ENABLED, false)) {
            startService(new Intent(getBaseContext(), SchedulerService.class));
        } else {
            stopService(new Intent(getBaseContext(), SchedulerService.class));
        }

        if (prefs.getBoolean(Constants.PREF_EYEREST_ENABLED, false)) {
            startService(new Intent(getBaseContext(), OverlayService.class));
        } else {
            stopService(new Intent(getBaseContext(), OverlayService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBillingService != null) {
            //unbindService(_mServiceConn);
        }

        if (iabHelper != null) iabHelper.dispose();
        iabHelper = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fm = getSupportFragmentManager();

        // Clear the backstack, so pressing the back button exits the app
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (id == R.id.nav_personalize) {
            // Handle the camera action
            fm.beginTransaction().replace(R.id.main, new SettingsFragment()).commit();
        } else if (id == R.id.nav_schedule) {
            fm.beginTransaction().replace(R.id.main, new SchedulerEnabledFragment()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void showOrHideSchedulerUI(boolean show) {
        if (show) {
            Log.d(getClass().toString(), "Enabling scheduler");
            getSupportFragmentManager().beginTransaction().replace(R.id.llSchedulerContainer, new SchedulerEnabledFragment()).commit();
        } else {
            Log.d(getClass().toString(), "Disabling scheduler");
            getSupportFragmentManager().beginTransaction().replace(R.id.llSchedulerContainer, new SchedulerDisabledFragment()).commit();
        }
    }
}

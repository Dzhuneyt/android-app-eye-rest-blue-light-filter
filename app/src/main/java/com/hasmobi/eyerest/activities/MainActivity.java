package com.hasmobi.eyerest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.hasmobi.eyerest.R;
import com.hasmobi.eyerest.base.Application;
import com.hasmobi.eyerest.billingutil.IabHelper;
import com.hasmobi.eyerest.billingutil.IabResult;
import com.hasmobi.eyerest.billingutil.Inventory;
import com.hasmobi.eyerest.billingutil.Purchase;
import com.hasmobi.eyerest.fragments.main.SchedulerDisabledFragment;
import com.hasmobi.eyerest.fragments.main.SchedulerEnabledFragment;
import com.hasmobi.eyerest.fragments.main.SettingsFragment;
import com.hasmobi.eyerest.fragments.main.WelcomeFragment;
import com.hasmobi.eyerest.helpers.IShowHideScheduler;
import com.hasmobi.eyerest.services.OverlayService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IShowHideScheduler {

    // Google In-App Billing related:
    private IabHelper iabHelper;

    private FirebaseAnalytics mFirebaseAnalytics;

    private String TAG = getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Is this a restart due to e.g. config changes (orientation change, etc)
        boolean isRestart = (savedInstanceState != null);

        bootstrap(isRestart);
    }

    private void bootstrap(boolean isRestart) {

        // Prepare Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, new Bundle());

        prepareActionBarAndDrawer();

        if (!isRestart) {
            // On first launch, navigate to the correct fragment
            if (OverlayService.isEnabled(getBaseContext())) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main, new SettingsFragment()).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.main, new WelcomeFragment()).commit();
            }
        }

        prepareBilling();

        startServices();
    }

    private void prepareActionBarAndDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_remove_red_eye_white_24dp);
        toolbar.setTitleMarginStart(Application.dpToPx(getBaseContext(), 25));
        setSupportActionBar(toolbar);
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //toggle.setDrawerIndicatorEnabled(false);
        //drawer.setDrawerListener(toggle);
        //toggle.syncState();
        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
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

        Application.refreshServices(getBaseContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iabHelper != null) {
            iabHelper.dispose();
            iabHelper = null;
        }
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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

    /**
     * Prepare the Google In App Billing helper asynchronously so it's
     * ready for calls afterwards
     */
    private void prepareBilling() {
        // Google In-App Billing related:
        Intent iBilling = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        iBilling.setPackage("com.android.vending");

        final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmDE8PlnfNAzau5GPe/EAjl0yJnK0DisSeqtvvohrQROtoHXtaFXSXsmyzIfHrfkbt3V2GhJVG3mNwUy7HJ6qUzvU9C2TPb1Rkhhu/86FmaxUTGT6pgF1d3rekM1Urw7FNwjlh4VVdDhqI2myoJfQBwHvEFyLIgZP5ij2xCidqP36ZoGTaQkYI8nVDTm76tdRkFF5uS0UUooHkbdK1AfhvVr3rpZK/JdWMKAS5AmT0PgPC1nDa41XzcUfQEAt+2GVdYXUJ/WifE/PgGiHsOcPsaYKsB711t0SVGhLbn71cwUIc/KhbjdpRizkgzGoTwboO7Jh9U6oUOnQkxrmg8qW6QIDAQAB";

        // compute your public key and store it in base64EncodedPublicKey
        iabHelper = new IabHelper(getBaseContext(), base64EncodedPublicKey);
        iabHelper.enableDebugLogging(true, TAG);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                    return;
                }

                // Hooray, IAB is fully set up!
                Log.d(TAG, "Google IAB is set up.");
//                iabHelper.launchSubscriptionPurchaseFlow(MainActivity.this, "android.test.purchased", 1001, new IabHelper.OnIabPurchaseFinishedListener() {
//                    @Override
//                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
//                        Log.d(getClass().toString(), "onIabPurchaseFinished");
//
//                    }
//                });
//                iabHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
//                    @Override
//                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
//                        Log.d(getClass().toString(), result.getMessage());
//                        Log.d(getClass().toString(), inv.toString());
//                    }
//                });

            }
        });
    }

    private void startServices() {
        Application.refreshServices(getBaseContext());
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

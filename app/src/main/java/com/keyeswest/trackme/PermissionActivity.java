package com.keyeswest.trackme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.keyeswest.trackme.utilities.BatteryStatePreferences;

import timber.log.Timber;

import static com.keyeswest.trackme.utilities.BatteryStatePreferences.LOW_BATTERY_THRESHOLD;
import static com.keyeswest.trackme.utilities.BatteryStatePreferences.setLowBatteryState;

public class PermissionActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private TextView mPermissionTextView;

    private TextView mNoPermissionTextView;

    private Button mExitButton;

    private boolean mAskedOnce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("onCreate invoked");

        // As the first activity to run, read the battery level and save low battery
        // state information to shared preferences. Required in case app starts with battery
        // in low battery state or the battery has charged since app exited with battery in
        // a low battery state.
        setCurrentBatteryLevel();

        setContentView(R.layout.activity_permission);

        mPermissionTextView = findViewById(R.id.perm_justify_tv);
        mNoPermissionTextView = findViewById(R.id.exit_tv);

        mExitButton = findViewById(R.id.exit_btn);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mNoPermissionTextView.setVisibility(View.GONE);
        mExitButton.setVisibility(View.GONE);
        mAskedOnce = false;
    }



    @Override
    protected void onResume() {
        super.onResume();

        Timber.d("onResume invoked");

        if (!checkPermissions()) {

            requestPermissions();

        }else{
            startTripListActivity();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("onStart invoked");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("onSaveInstanceState invoked");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Timber.d("onRestoreInstanceState invoked");
    }
//------------------------------ Permission Checking -----------------------------------------//


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Timber.i( "onRequestPermissionResult");
        mPermissionTextView.setVisibility(View.GONE);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Timber.i( "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d( "Permission granted.");
                startTripListActivity();
            } else {

                // Permission denied.

                mNoPermissionTextView.setVisibility(View.VISIBLE);
                mExitButton.setVisibility(View.VISIBLE);


                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless.
                showSnackBar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    /**
     * Return the current state of the permissions needed.
     *   returns false if permission has not been granted
     */
    private boolean checkPermissions() {

        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(PermissionActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {

        mPermissionTextView.setVisibility(View.VISIBLE);
        // returns true if the user initially denies request but did not check never ask again
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Timber.i( "Displaying permission rationale to provide additional context.");

            showSnackBar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Timber.i( "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            if (! mAskedOnce) {
                startLocationPermissionRequest();
                mAskedOnce = true;
            }else{
                mNoPermissionTextView.setVisibility(View.VISIBLE);
                mExitButton.setVisibility(View.VISIBLE);
            }
        }
    }


    private void showSnackBar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    private void startTripListActivity(){
        mNoPermissionTextView.setVisibility(View.GONE);
        mExitButton.setVisibility(View.GONE);
        Intent intent = TripListActivity.newIntent(this);
        startActivity(intent);
        finish();
    }


    /**
     * Read the current battery level and save low battery state to information to shared
     * preferences.
     */
    private void setCurrentBatteryLevel() {

        float batteryPercentage = BatteryStatePreferences.getCurrentBatteryPercentLevel(this);

        Timber.d("Initial battery percentage= %s", Float.toString(batteryPercentage));
        if (batteryPercentage <= LOW_BATTERY_THRESHOLD ){
            setLowBatteryState(this,true);
        }else{
            setLowBatteryState(this,false);
        }
    }


}

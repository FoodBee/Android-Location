package com.prudhvir3ddy.android_location;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;

public class LocationMainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //request codes
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private static final String PLACE_EXTRA_LATITUDE = "latitude";
    private static final String PLACE_EXTRA_LONGITUDE = "longitude";

    //TAG for logs
    private static final String TAG = LocationMainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initalize places this should be on top
        if(getResources().getString(R.string.google_maps_key).equals("YOUR_KEY_HERE")){
            Toast.makeText(getApplicationContext(),"PLEASE ADD YOUR GOOGLE MAPS API KEY IN PROJECT",Toast.LENGTH_LONG).show();
        }
        Places.initialize(this, getResources().getString(R.string.google_maps_key));

        //for asking permission for location
        getLocation();

        Button mSelectLocationManualButton = findViewById(R.id.button);


        SearchLocationDialog searchLocationDialog = new SearchLocationDialog();

        mSelectLocationManualButton.setOnClickListener(v -> searchLocationDialog.show(getSupportFragmentManager(), "searchDialog"));


    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(LocationMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showThePermissionNeedDialog(1);
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }

        } else {
            createLocationRequest();
            Log.d(TAG, "getLocation: permission granted");
        }
    }

    private void createLocationRequest() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        Log.d(TAG, "getLocation: permission granted- 2");
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // location requests here.
            // ...
            Log.d(TAG, "getLocation: permission granted - 3");
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "getLocation: permission granted - 4");
                            Log.d(TAG, location.getLatitude() + " " + location.getLongitude());
                            Intent intent = new Intent(LocationMainActivity.this, MapsActivity.class);
                            intent.putExtra(PLACE_EXTRA_LATITUDE, location.getLatitude());
                            intent.putExtra(PLACE_EXTRA_LONGITUDE, location.getLongitude());
                            startActivity(intent);
                        }
                    });

        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(LocationMainActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }


    private void showThePermissionNeedDialog(int status) {
        AlertDialog.Builder locationBuilder = new AlertDialog.Builder(LocationMainActivity.this);
        if (status == 0) {
            locationBuilder.setTitle(getString(R.string.location_permission_denied))
                .setMessage(R.string.location_permission_denied_message)
                .setNegativeButton("I'M SURE", (dialog, which) -> dialog.cancel())
                .setPositiveButton("RETRY", (dialog, which) -> {
                    dialog.cancel();
                    getLocation();
                });
        }else if(status==1){
            locationBuilder.setMessage(getString(R.string.settings_permission))
                    .setPositiveButton("GO TO SETTINGS", (dialog, which) -> {
                       dialog.cancel();
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                    });

        }
        locationBuilder.show();
    }


    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {// If the permission is granted, get the location,
            // otherwise, show a Toast
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                //when user denies the location permission
                showThePermissionNeedDialog(0);
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}

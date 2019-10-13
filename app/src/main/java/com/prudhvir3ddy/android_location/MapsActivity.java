package com.prudhvir3ddy.android_location;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String PLACE_EXTRA_LATITUDE = "latitude";
    private static final String PLACE_EXTRA_LONGITUDE = "longitude";
    private Location lastLocation;
    private Marker marker;
    private TextView locAddressTv;
    private ProgressBar locPb;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Button confirmLocationBtn = findViewById(R.id.confirm_loc_btn);
        locAddressTv = findViewById(R.id.location_address_tv);
        locPb = findViewById(R.id.loc_pb);

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra(PLACE_EXTRA_LATITUDE, 0);
        double longitude = intent.getDoubleExtra(PLACE_EXTRA_LONGITUDE, 0);

        lastLocation = new Location("");
        lastLocation.setLatitude(latitude);
        lastLocation.setLongitude(longitude);


        startIntentService();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        confirmLocationBtn.setOnClickListener(v -> {

        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng current = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap source = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.marker, options);

        mMap.setMinZoomPreference(10f);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(current)
                .title(getString(R.string.fooddeliver))
                .icon(BitmapDescriptorFactory.fromBitmap(source));

        if (marker == null)
            marker = mMap.addMarker(markerOptions);


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(current).zoom(18f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        mMap.setOnCameraMoveListener(() -> {
            LatLng center = mMap.getCameraPosition().target;
            lastLocation.setLongitude(center.longitude);
            lastLocation.setLatitude(center.latitude);
            marker.setPosition(center);


        });

        mMap.setOnCameraIdleListener(this::startIntentService);
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

    private void startIntentService() {
        locPb.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        AddressResultReceiver resultReceiver = new AddressResultReceiver(handler);
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }


    class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }
            //displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                locPb.setVisibility(View.GONE);
                locAddressTv.setText(addressOutput);
            }

        }
    }
}

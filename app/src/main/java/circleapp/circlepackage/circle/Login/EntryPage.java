package circleapp.circlepackage.circle.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

import circleapp.circlepackage.circle.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntryPage extends AppCompatActivity {

    private Button agreeContinue;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FirebaseAnalytics firebaseAnalytics;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
        }
    };
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);
        displayLocationSettingsRequest(getApplicationContext());
        firebaseAnalytics = FirebaseAnalytics.getInstance(EntryPage.this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(EntryPage.this);
        //bundle to send to fb
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, 1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "In Entry page");
        firebaseAnalytics.logEvent("entrypage", bundle);
        firebaseAnalytics.setCurrentScreen(EntryPage.this, "Start screen", null);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(EntryPage.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
                return;
            } else {
                getLocation();
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //to avoid error
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG","get Location Called!!!");
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,
                1, mLocationListener);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            mLocationListener.onLocationChanged(location);
            Log.d("TAG","Listener");
        });
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.i("TAG", "All location settings are satisfied.");
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(EntryPage.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        Log.i("TAG", "PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                    break;
            }
        });
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(EntryPage.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);


        if(requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(EntryPage.this,
                        "Location is required to continue",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d("TAG","Listener Called!!!!");
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(EntryPage.this);
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setFastestInterval(1000);
            locationRequest.setInterval(4000);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            fusedLocationProviderClient.getLastLocation();
            SystemClock.sleep(500);
            startActivity(new Intent(EntryPage.this, PhoneLogin.class));
            finish();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}

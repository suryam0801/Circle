package circleapp.circlepackage.circle.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
    LocationCallback locationCallback= new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);
        firebaseAnalytics = FirebaseAnalytics.getInstance(EntryPage.this);
//bundle to send to fb
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, 1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "In Entry page");
        firebaseAnalytics.logEvent("entrypage", bundle);
        firebaseAnalytics.setCurrentScreen(EntryPage.this, "Start screen", null);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(EntryPage.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestLocationPermission();
                return;
            } else {
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

                startActivity(new Intent(EntryPage.this, PhoneLogin.class));
                finish();

            } else {
                Toast.makeText(EntryPage.this,
                        "Location is required to continue",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


}

package circleapp.circlepackage.circle.Login;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Trace;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.LocationHelper;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntryPage extends AppCompatActivity{

    private static final String TAG = EntryPage.class.getSimpleName();
    private Button agreeContinue;
    AnalyticsLogEvents analyticsLogEvents;
    Trace myTrace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);
        analyticsLogEvents = new AnalyticsLogEvents();
        RuntimePermissionHelper runtimePermissionHelper = new RuntimePermissionHelper(EntryPage.this);
        LocationHelper locationHelper = new LocationHelper(EntryPage.this);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {
            agreeContinue.setClickable(false);
            if(runtimePermissionHelper.isPermissionAvailable(ACCESS_FINE_LOCATION)){
                Toast.makeText(EntryPage.this, "Getting your location. Please wait.", Toast.LENGTH_SHORT).show();
                LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                @SuppressLint("MissingPermission")
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null){
                    analyticsLogEvents.logEvents(EntryPage.this, "get_last_location", "lastLocationExists","Entry_page");
                    locationHelper.getAddress(location);
                }
                else{
                    analyticsLogEvents.logEvents(EntryPage.this, "get_current_location", "lastLocationEmpty","Entry_page");
                    locationHelper.getLocation();

                }
            } else {
                analyticsLogEvents.logEvents(EntryPage.this, "location_permission", "location_off","app_open");
                runtimePermissionHelper.requestPermissionsIfDenied(ACCESS_FINE_LOCATION);
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LocationHelper locationHelper = new LocationHelper(EntryPage.this);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            locationHelper.getLocation();
            Toast.makeText(EntryPage.this, "Getting your location. Please wait.", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        agreeContinue.setEnabled(true);
        Log.d(TAG,"Activity Resumed");
    }
}

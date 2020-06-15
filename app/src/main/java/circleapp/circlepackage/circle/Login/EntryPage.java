package circleapp.circlepackage.circle.Login;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;


import com.google.firebase.crashlytics.CrashlyticsRegistrar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;

import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.LocationHelper;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntryPage extends AppCompatActivity{

    private static final String TAG = EntryPage.class.getSimpleName();
    private Button agreeContinue;
    AnalyticsLogEvents analyticsLogEvents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);
        analyticsLogEvents = new AnalyticsLogEvents();
        RuntimePermissionHelper runtimePermissionHelper = new RuntimePermissionHelper(EntryPage.this);
        LocationHelper locationHelper = new LocationHelper(EntryPage.this);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {
            if(runtimePermissionHelper.isPermissionAvailable(ACCESS_FINE_LOCATION)){
                locationHelper.getLocation();

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
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

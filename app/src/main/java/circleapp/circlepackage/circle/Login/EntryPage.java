package circleapp.circlepackage.circle.Login;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;


import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.LocationHelper;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntryPage extends AppCompatActivity{

    private static final String TAG = EntryPage.class.getSimpleName();
    private Button agreeContinue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);
        RuntimePermissionHelper runtimePermissionHelper = new RuntimePermissionHelper(EntryPage.this);
        LocationHelper locationHelper = new LocationHelper(EntryPage.this);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {
            if(runtimePermissionHelper.isPermissionAvailable(ACCESS_FINE_LOCATION)){
                locationHelper.getLocation();

            } else {
                runtimePermissionHelper.requestPermissionsIfDenied(ACCESS_FINE_LOCATION);
            }
        });
    }
}

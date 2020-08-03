package circleapp.circlepackage.circle.ui.Login.EntryPage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.LocationHelper.LocationHelper;
import circleapp.circlepackage.circle.ui.Login.PhoneNumberEntry.PhoneLogin;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntryPage extends AppCompatActivity{

    private static final String TAG = EntryPage.class.getSimpleName();
    private Button agreeContinue;
    private Boolean locationUpdateStatus = false;
    private LocationHelper locationHelper = new LocationHelper(getApplication());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocationHelperObserver();

        setContentView(R.layout.activity_entry_page);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);


        agreeContinue.setOnClickListener(view -> {
            Permissions.check(this/*context*/, ACCESS_FINE_LOCATION, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    agreeContinue.setText("Getting Location");
                    locationHelper.getLocation();
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
        });

    }

    private void setLocationHelperObserver(){
        locationHelper = ViewModelProviders.of(this).get(LocationHelper.class);
        locationHelper.listenForLocationUpdates(locationUpdateStatus).observe(this, locationUpdates -> {
            if(locationUpdates==false){
                buildAlertMessageNoGps();
            }
            else if(locationUpdates)
            {
                goToNextActivity();
            }
        });
    }

    private void goToNextActivity(){
        Intent intent = new Intent(this, PhoneLogin.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        agreeContinue.setEnabled(true);
    }

    //alert box..
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        activity.setProgressBarVisibility(false);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        Toast.makeText(getApplicationContext(),"Enable Location to Continue",Toast.LENGTH_LONG).show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}

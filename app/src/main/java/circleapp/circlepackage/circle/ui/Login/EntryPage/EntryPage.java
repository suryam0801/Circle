package circleapp.circlepackage.circle.ui.Login.EntryPage;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import circleapp.circlepackage.circle.Utils.LocationHelper;
import circleapp.circlepackage.circle.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntryPage extends AppCompatActivity{

    private static final String TAG = EntryPage.class.getSimpleName();
    private Button agreeContinue;
    LocationHelper locationHelper;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);
        locationHelper = new LocationHelper(EntryPage.this);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {
            Permissions.check(this/*context*/, ACCESS_FINE_LOCATION, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    agreeContinue.setText("Getting Location");
                    getUserLocation();
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
        });

    }

    private void getUserLocation(){
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null){
            locationHelper.getAddress(location);
        }
        else{
            locationHelper.getLocation();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        agreeContinue.setEnabled(true);
        Log.d(TAG,"Activity Resumed");
    }
}

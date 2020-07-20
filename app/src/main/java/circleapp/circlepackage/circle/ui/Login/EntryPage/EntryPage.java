package circleapp.circlepackage.circle.ui.Login.EntryPage;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import circleapp.circlepackage.circle.Utils.LocationHelper;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
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
        RuntimePermissionHelper runtimePermissionHelper = new RuntimePermissionHelper(EntryPage.this);
        locationHelper = new LocationHelper(EntryPage.this);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {
            agreeContinue.setClickable(false);
            if(runtimePermissionHelper.isPermissionAvailable(ACCESS_FINE_LOCATION)){
                agreeContinue.setText("Getting Location");
               getUserLocation();
            } else {
                runtimePermissionHelper.requestPermissionsIfDenied(ACCESS_FINE_LOCATION);
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Toast.makeText(EntryPage.this, "Getting your location. Please wait.", Toast.LENGTH_SHORT).show();
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            getUserLocation();
        }
        else
            agreeContinue.setClickable(true);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

package circleapp.circlepackage.circle.Login;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntryPage extends AppCompatActivity {

    private static final String TAG = EntryPage.class.getSimpleName();
    private Button agreeContinue;
    private FusedLocationProviderClient client;
    private String ward, district,mCountryDialCode,mCountryCode,mCountryName;
    String[] options;
    List<String> al = new ArrayList<String>();
    int pos;
    public static final String PREF_NAME= "LOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);

        client = LocationServices.getFusedLocationProviderClient(this);

        RuntimePermissionHelper runtimePermissionHelper = new RuntimePermissionHelper(EntryPage.this);
        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {

            if(runtimePermissionHelper.isPermissionAvailable(ACCESS_FINE_LOCATION)){
                getLocation();
            } else {
                runtimePermissionHelper.requestPermissionsIfDenied(ACCESS_FINE_LOCATION);
            }
        });

    }
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    @SuppressLint("MissingPermission")
    public void getLocation(){
        client.getLastLocation().addOnSuccessListener(location -> {
            if(location != null){
                try {
                    getAddress(location);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                finalizeAndNextActivity();
            }
        });
    }
    public void getAddress(Location location) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        getCountry(addresses);
    }

    private void getCountry(List<Address> addresses) {
        options = EntryPage.this.getResources().getStringArray(R.array.countries_array);
        al = Arrays.asList(options);
        mCountryName = addresses.get(0).getCountryName();
        String countrycode = addresses.get(0).getCountryCode();
        Log.d(TAG,"Location :: "+mCountryName+" :: "+countrycode);

        for (String cn : al)
        {
            pos = pos+1;
            if (cn.equals(mCountryName))
            {
                pos = pos - 1;
                String contryDialCode = null;
                String[] arrContryCode=EntryPage.this.getResources().getStringArray(R.array.DialingCountryCode);
                for(int i=0; i<arrContryCode.length; i++){
                    String[] arrDial = arrContryCode[i].split(",");
                    if(arrDial[1].trim().equals(countrycode.trim())){
                        contryDialCode = arrDial[0];
                        mCountryDialCode="+"+contryDialCode;
                        //need to figure out different
                        if(addresses.get(0).getCountryName().toLowerCase().equals("united states")) {
                            district = addresses.get(0).getSubAdminArea();
                            ward = addresses.get(0).getLocality();
                        } else {
                            district = addresses.get(0).getSubAdminArea();
                            //logic to get ward from address line
                            Scanner scan = new Scanner(addresses.get(0).getAddressLine(0));
                            scan.useDelimiter(",");
                            List<String> parsing = new ArrayList<>();
                            while (scan.hasNext()) {
                                String w = String.valueOf(scan.next());
                                if(w.trim().equals(district.trim())) {
                                    ward = parsing.get(parsing.size()-1);
                                } else {
                                    parsing.add(w);
                                }
                            }
                        }
                        setSessionLocation(mCountryName,pos,district,ward,mCountryDialCode);
                        break;
                    }
                }
            }
        }
    }
    public void setSessionLocation(String countryname, int position, String district, String ward, String mCountryDialCode)
    {
        Intent intent = new Intent(EntryPage.this, PhoneLogin.class);
        intent.putExtra("pos", position);
        intent.putExtra("countryName",countryname);
        intent.putExtra("dialCode",mCountryDialCode);
        intent.putExtra("ward", ward.trim());
        intent.putExtra("district", district.trim());
        startActivity(intent);
        finish();
        Log.d(TAG,district+"::pos="+position+"::"+ward+"::"+mCountryDialCode);
//        SharedPreferences sharedPref = getSharedPreferences("MyPref", Activity.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString("mCountryName",countryname);
//        editor.putInt("pos", position);
//        editor.putString("mCountryDialCode", mCountryDialCode);
//        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        statusCheck();
    }
}

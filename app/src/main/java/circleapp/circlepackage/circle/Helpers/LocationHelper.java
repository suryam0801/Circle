package circleapp.circlepackage.circle.Helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import circleapp.circlepackage.circle.Login.EntryPage;
import circleapp.circlepackage.circle.Login.InterestTagPicker;
import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.R;

public class LocationHelper{

    Location loc;
    LocationManager locationManager;
    Activity activity;
    private static final String TAG = EntryPage.class.getSimpleName();
    private Button agreeContinue;
    private FusedLocationProviderClient client;
    private String ward, district,mCountryDialCode,mCountryCode,mCountryName;
    String[] options;
    List<String> al = new ArrayList<String>();
    int pos;
//    progressDialog = new abstract ProgressDialog(activity);
    public LocationHelper(Activity activity)  {
        this.activity = activity;
    }


    @SuppressLint("MissingPermission")
    public void getLocation()
    {

        //Criterias for location access
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        //looping control for location access
        final Looper looper = null;
        //listener class for location
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //update the current location
                getAddress(location);
                locationManager.removeUpdates(this);
//                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        //for single update of the location
        locationManager.requestSingleUpdate(criteria, locationListener, looper);
        //to check the location service is enabled or not
//        locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
        statusCheck();

    }

    //fun to get the address of the user
    public void getAddress(Location location)  {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(activity, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            getCountry(addresses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCountry(List<Address> addresses) {
        options = activity.getResources().getStringArray(R.array.countries_array);
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
                String[] arrContryCode=activity.getResources().getStringArray(R.array.DialingCountryCode);
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
    //intent to phone login
    public void setSessionLocation(String countryname, int position, String district, String ward, String mCountryDialCode)
    {
        Intent intent = new Intent(activity, PhoneLogin.class);
        intent.putExtra("pos", position);
        intent.putExtra("countryName",countryname);
        intent.putExtra("dialCode",mCountryDialCode);
        intent.putExtra("ward", ward.trim());
        intent.putExtra("district", district.trim());
        activity.startActivity(intent);
        activity.finish();
        Log.d(TAG,district+"::pos="+position+"::"+ward+"::"+mCountryDialCode);
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    //alert box..
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        activity.setProgressBarVisibility(false);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        Toast.makeText(activity,"Enable Location to Continue",Toast.LENGTH_LONG).show();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}

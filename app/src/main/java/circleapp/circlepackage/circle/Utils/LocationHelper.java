package circleapp.circlepackage.circle.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.util.Log;
import android.widget.Toast;


import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.OtpVerification.PhoneCallbacksListener;
import circleapp.circlepackage.circle.data.LocalObjectModels.LoginUserObject;
import circleapp.circlepackage.circle.ui.Login.EntryPage.EntryPage;
import circleapp.circlepackage.circle.ui.Login.PhoneNumberEntry.PhoneLogin;
import circleapp.circlepackage.circle.R;

public class LocationHelper extends ViewModel {

    private LocationManager locationManager;
    private String ward, district,mCountryDialCode,mCountryName;
    private String[] options;
    private static List<String> al = new ArrayList<String>();
    private static int pos;
    private static Criteria gpsSignalCriteria;
    private static LoginUserObject loginUserObject;
    private static Context mContext;

    LocationUpdatedListener locationUpdatedListener;
    public void setLocationUpdatedListener(LocationUpdatedListener locationUpdatedListener, Context context) {
        this.locationUpdatedListener = locationUpdatedListener;
        mContext = context;
    }


    @SuppressLint("MissingPermission")
    public void getLocation()
    {

        //Criterias for location access
        setGpsSignalCriteriaParams();

        //listener class for location
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //update the current location
                getAddress(location);
                locationManager.removeUpdates(this);
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
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        //for single update of the location
        //locationManager.requestSingleUpdate(criteria, locationListener, looper);
        locationManager.requestLocationUpdates(500,1000,gpsSignalCriteria,locationListener, null);
        //check if gps is available
        statusCheck();

    }

    //fun to get the address of the user
    public void getAddress(Location location)  {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(mContext, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            getCountry(addresses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //fun to set current country, district and ward of user
    private void getCountry(List<Address> addresses){
        options = mContext.getResources().getStringArray(R.array.countries_array);
        al = Arrays.asList(options);
        mCountryName = addresses.get(0).getCountryName();
        String countrycode = addresses.get(0).getCountryCode();

        for (String cn : al)
        {
            pos = pos+1;
            if (cn.equals(mCountryName))
            {
                pos = pos - 1;
                String contryDialCode = null;
                String[] arrContryCode=mContext.getResources().getStringArray(R.array.DialingCountryCode);
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
                                if ((!w.isEmpty() || !district.isEmpty()) ||(w !=null || district != null) )
                                {
                                    if(w.trim().equals(district.trim())) {
                                        ward = parsing.get(parsing.size()-1);
                                    } else {
                                        parsing.add(w);
                                    }
                                }
                                else
                                    {
                                        getLocation();
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
        loginUserObject = new LoginUserObject();
        loginUserObject.setPosition(position);
        loginUserObject.setCountryName(countryname);
        loginUserObject.setCountryDialCode(mCountryDialCode);
        loginUserObject.setDistrict(district.trim());
        loginUserObject.setCompletePhoneNumber("");
        loginUserObject.setUid("");
        if(ward != null)
            loginUserObject.setWard(ward.trim());
        else
            loginUserObject.setWard("default");
        SessionStorage.saveLoginUserObject((Activity) mContext, loginUserObject);
        locationUpdatedListener.onLocationUpdated(1);
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationUpdatedListener.onLocationUpdated(0);
        }
    }
    private void setGpsSignalCriteriaParams(){
        gpsSignalCriteria = new Criteria();
        gpsSignalCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        gpsSignalCriteria.setPowerRequirement(Criteria.POWER_LOW);
        gpsSignalCriteria.setAltitudeRequired(false);
        gpsSignalCriteria.setBearingRequired(false);
        gpsSignalCriteria.setSpeedRequired(false);
        gpsSignalCriteria.setCostAllowed(true);
        gpsSignalCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        gpsSignalCriteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
    }

}

package circleapp.circlepackage.circle.Utils.LocationHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.LocalObjectModels.LoginUserObject;
import circleapp.circlepackage.circle.data.LocalObjectModels.TempLocation;

import static androidx.core.content.ContextCompat.getSystemService;

public class LocationHelper extends ViewModel {

    private LocationManager locationManager;
    private Location location;
    private String ward, district,mCountryName;
    private static Criteria gpsSignalCriteria;
    private static TempLocation tempLocation;
    private LocationListener locationListener;
    private Context mContext;
    private GlobalVariables globalVariables = new GlobalVariables();

    private MutableLiveData<Boolean> isLocationSuccess;
    public MutableLiveData<Boolean> listenForLocationUpdates(Boolean updatedLocationStatus, Context context) {
        if (!updatedLocationStatus) {
            isLocationSuccess = new MutableLiveData<>();
        }
        else {
            getLocation(context);
        }
        return isLocationSuccess;
    }


    @SuppressLint("MissingPermission")
    public void getLocation(Context context)
    {
        mContext = context;
        //get Last known location
        LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null){
            getAddress(location);
        }
        else{
            //Criterias for location access
            setGpsSignalCriteriaParams();
            setUpLocationChangedListener();

            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(500,1000,gpsSignalCriteria,locationListener, null);
            //check if gps is available
            statusCheck();
        }

    }

    private void setUpLocationChangedListener(){
        //listener class for location
        locationListener = new LocationListener() {
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

        mCountryName = addresses.get(0).getCountryName();
        String countrycode = addresses.get(0).getCountryCode();
        if(addresses.get(0).getCountryName().toLowerCase().equals("united states")) {
            district = addresses.get(0).getSubAdminArea();
            ward = addresses.get(0).getLocality();
        }else {
            district = addresses.get(0).getSubAdminArea();
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
                else {
                    ward = "default";
                }
            }
        }
        setSessionLocation(mCountryName,district,ward,countrycode);
    }
    //intent to phone login
    public void setSessionLocation(String countryname, String district, String ward, String mCountryDialCode)
    {
        tempLocation = new TempLocation();
        tempLocation.setCountryName(countryname);
        tempLocation.setCountryDialCode(mCountryDialCode);
        tempLocation.setDistrict(district.trim());
        tempLocation.setWard(ward);
        globalVariables.saveCurrentTempLocation(tempLocation);
        isLocationSuccess.setValue(true);
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            isLocationSuccess.setValue(false);
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

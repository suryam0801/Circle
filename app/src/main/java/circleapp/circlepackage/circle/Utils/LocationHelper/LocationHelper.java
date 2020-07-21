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
import android.os.Bundle;


import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.LocalObjectModels.LoginUserObject;

public class LocationHelper extends ViewModel {

    private LocationManager locationManager;
    private String ward, district,mCountryName;
    private static Criteria gpsSignalCriteria;
    private static LoginUserObject loginUserObject;
    private LocationListener locationListener;
    private static Context mContext;

    LocationUpdatedListener locationUpdatedListener;
    public void setLocationUpdatedListener(LocationUpdatedListener locationUpdatedListener) {
        this.locationUpdatedListener = locationUpdatedListener;
        mContext = (Context) locationUpdatedListener;
    }


    @SuppressLint("MissingPermission")
    public void getLocation()
    {

        //Criterias for location access
        setGpsSignalCriteriaParams();
        setUpLocationChangedListener();

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(500,1000,gpsSignalCriteria,locationListener, null);
        //check if gps is available
        statusCheck();

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
        loginUserObject = new LoginUserObject();
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

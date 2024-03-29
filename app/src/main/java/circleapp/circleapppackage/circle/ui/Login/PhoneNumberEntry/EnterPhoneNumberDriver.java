package circleapp.circleapppackage.circle.ui.Login.PhoneNumberEntry;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import circleapp.circleapppackage.circle.Model.LocalObjectModels.LoginUserObject;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ui.Login.OtpVerification.OtpActivity;

public class EnterPhoneNumberDriver {
    private GlobalVariables globalVariables = new GlobalVariables();
    public void EnterPhoneNumberDriver(){}
    public String  setCountryCode(String code, String[] arrContryCode){
        String contryDialCode = null;
        for (String s : arrContryCode) {
            String[] arrDial = s.split(",");
            if (arrDial[1].trim().equals(code.trim())) {
                contryDialCode = arrDial[0];
                break;
            }
        }
        return contryDialCode;
    }
    public String getCountryCode(String countryName) {

        // Get all country codes in a string array.
        String[] isoCountryCodes = Locale.getISOCountries();
        Map<String, String> countryMap = new HashMap<>();
        Locale locale;
        String name;

        // Iterate through all country codes:
        for (String code : isoCountryCodes) {
            // Create a locale using each country code
            locale = new Locale("", code);
            // Get country name for each code.
            name = locale.getDisplayCountry();
            // Map all country names and codes in key - value pairs.
            countryMap.put(name, code);
        }

        // Return the country code for the given country name using the map.
        // Here you will need some validation or better yet
        // a list of countries to give to user to choose from.
        return countryMap.get(countryName); // "NL" for Netherlands.
    }
    public void savePhoneNumberToSession(Activity activity, String country_code, String phone_number){
        LoginUserObject loginUserObject = new LoginUserObject();
        loginUserObject.setCompletePhoneNumber(country_code + phone_number);
        globalVariables.saveCurrentLoginUserObject(loginUserObject);
    }
    public boolean isPhoneNumber10Digits(String phoneNumber){
        return phoneNumber.length() == 10;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void sendIntentsToOtpActivityAndFinish(Activity activity){
        activity.finishAfterTransition();
        Intent otpIntent = new Intent(activity, OtpActivity.class);
        activity.startActivity(otpIntent);
    }
}

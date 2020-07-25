package circleapp.circlepackage.circle.ui.Login.PhoneNumberEntry;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.LocalObjectModels.TempLocation;

public class GetSearchableSpinnerLocation {
    private Context mContext;
    private String[] options;
    private String mCountryName, mCountryDialCode;
    private List<String> al = new ArrayList<String>();
    private int pos;
    private TempLocation tempLocation;
    private GlobalVariables globalVariables = new GlobalVariables();
    public int getPositionOfSpinner(Context context) {
        mContext = context;
        getLocationFromSession();
        options = context.getResources().getStringArray(R.array.countries_array);
        al = Arrays.asList(options);
        int temp = 0;
        for (String cn : al) {
            if(temp == 1){
                Log.d("LocationParams", pos+mCountryName+mCountryDialCode);
                break;
            }
            pos = pos + 1;
            if (cn.equals(mCountryName)) {
                pos = pos - 1;
                String contryDialCode = null;
                String[] arrContryCode = mContext.getResources().getStringArray(R.array.DialingCountryCode);
                for (int i = 0; i < arrContryCode.length; i++) {
                    String[] arrDial = arrContryCode[i].split(",");
                    if (arrDial[1].trim().equals(mCountryDialCode.trim())) {
                        contryDialCode = arrDial[0];
                        mCountryDialCode="+"+contryDialCode;
                        temp = 1;

                        Log.d("LocationParams", pos+mCountryName+mCountryDialCode);
                        break;
                    }
                }
            }
        }
        return pos;
    }
    public String getmCountryDialCode(){
        return mCountryDialCode;
    }

    private void getLocationFromSession(){
        tempLocation = globalVariables.getCurrentTempLocation();
        mCountryName = tempLocation.getCountryName();
        mCountryDialCode = tempLocation.getCountryDialCode();
        Log.d("LocationParams", mCountryName+mCountryDialCode);
    }
}

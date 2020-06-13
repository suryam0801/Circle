package circleapp.circlepackage.circle.Login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import circleapp.circlepackage.circle.R;

public class PhoneLogin extends AppCompatActivity {

    private static final String TAG = PhoneLogin.class.getSimpleName();
    private EditText mCountryCode;
    private EditText mPhoneNumber;
    private Button mGenerateBtn;
    private ProgressBar mLoginProgress;
    private TextView mLoginFeedbackText;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String complete_phone_number = "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private Spinner ccp;
    String[] options;

    private FusedLocationProviderClient client;
    List<String> al = new ArrayList<String>();
    int pos;

    public PhoneAuthProvider.ForceResendingToken resendingToken;


    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mCountryCode = findViewById(R.id.country_code_text);
        mPhoneNumber = findViewById(R.id.phone_number_text);
        mGenerateBtn = findViewById(R.id.generate_btn);
        mLoginProgress = findViewById(R.id.login_progress_bar);
        mLoginFeedbackText = findViewById(R.id.login_form_feedback);
        ccp = findViewById(R.id.ccp);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        client = LocationServices.getFusedLocationProviderClient(this);

        options = PhoneLogin.this.getResources().getStringArray(R.array.countries_array);
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        String countryName = tm.getNetworkCountryIso();
        al = Arrays.asList(options);

        client.getLastLocation().addOnSuccessListener(location -> {
            if(location != null){
                List<Address> addresses=null;
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    System.out.println("add in string "+addresses.toArray().toString());
                    String countryname = addresses.get(0).getCountryName();
                    String countrycode = addresses.get(0).getCountryCode();
                    Log.d(TAG,"Location :: "+countryname+" :: "+countrycode);

                    for (String cn : al)
                    {
                        pos = pos+1;
                        if (cn.equals(countryname))
                        {
                            ccp.setSelection(pos-1);
                            String code = getCountryCode(ccp.getSelectedItem().toString());
                            String contryDialCode = null;
                            String[] arrContryCode=PhoneLogin.this.getResources().getStringArray(R.array.DialingCountryCode);
                            for(int i=0; i<arrContryCode.length; i++){
                                String[] arrDial = arrContryCode[i].split(",");
                                if(arrDial[1].trim().equals(countrycode.trim())){
                                    contryDialCode = arrDial[0];
                                    mCountryCode.setText("+"+contryDialCode);
                                    break;
                                }
                            }
                        }
                    }


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });



        ccp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String code = getCountryCode(ccp.getSelectedItem().toString());
                String contryDialCode = null;
                String[] arrContryCode=PhoneLogin.this.getResources().getStringArray(R.array.DialingCountryCode);
                for(int i=0; i<arrContryCode.length; i++){
                    String[] arrDial = arrContryCode[i].split(",");
                    if(arrDial[1].trim().equals(code.trim())){
                        contryDialCode = arrDial[0];
                        mCountryCode.setText("+"+contryDialCode);
                        break;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCountryCode.setText("");
            }
        });
        mCountryCode.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mCountryCode, InputMethodManager.SHOW_IMPLICIT);
                        mCountryCode.setShowSoftInputOnFocus(true);
                        mCountryCode.setSelection(mCountryCode.getText().length());
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        mGenerateBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                //geting the Phone number from  user
                String country_code = mCountryCode.getText().toString();
                String phone_number = mPhoneNumber.getText().toString();


                //combining the country code and mobile number
                complete_phone_number = country_code + phone_number;
                editor.putString("key_name5", complete_phone_number);
                editor.apply();

                //checking the Edittexts are non-empty
                if(country_code.isEmpty() || phone_number.isEmpty()){
                    mLoginFeedbackText.setText("Please fill in the form to continue.");
                    mLoginFeedbackText.setVisibility(View.VISIBLE);
                } else {
                    mLoginProgress.setVisibility(View.VISIBLE);
                    mGenerateBtn.setEnabled(false);
                    //Sending the OTP to the user mobile number
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            complete_phone_number,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLogin.this,
                            mCallbacks
                    );
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Here we can add the code for Auto Read the OTP
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //Display the Error msg to the user through the Textview when error occurs
                mLoginFeedbackText.setText("Verification Failed, please try again."+e.toString());
                Log.d("EDITORVIEW", "error: " + e.toString());
                mLoginFeedbackText.setVisibility(View.VISIBLE);
                mLoginProgress.setVisibility(View.INVISIBLE);
                mGenerateBtn.setEnabled(true);
            }

            @Override
            public void onCodeSent(final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                //Opening the OtpActivity after the code(OTP) sent to the users mobile number
                                setResendingToken(forceResendingToken);
                                Intent otpIntent = new Intent(PhoneLogin.this, OtpActivity.class);
                                otpIntent.putExtra("AuthCredentials", s);
                                otpIntent.putExtra("phn_num", complete_phone_number);
                                otpIntent.putExtra("resendToken", forceResendingToken);
                                startActivity(otpIntent);
                                finish();
                            }
                        },
                        5000);
            }
        };
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


    public PhoneAuthProvider.ForceResendingToken getResendingToken() {
        return resendingToken;
    }

    public void setResendingToken(PhoneAuthProvider.ForceResendingToken resendingToken) {
        this.resendingToken = resendingToken;
    }

}


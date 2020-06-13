package circleapp.circlepackage.circle.Login;

import android.annotation.SuppressLint;
import android.app.Activity;
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
    public static final String PREF_NAME= "LOCATION";
    private Spinner ccp;
    private String ward, district,mCountryDialCode,mCountryName;

    private FusedLocationProviderClient client;
    String[] options;
    List<String> al = new ArrayList<String>();
    int pos;

    public PhoneAuthProvider.ForceResendingToken resendingToken;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        //To set the Fullscreen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFormat(PixelFormat.RGB_565);

        mCountryCode = findViewById(R.id.country_code_text);
        mPhoneNumber = findViewById(R.id.phone_number_text);
        mPhoneNumber.requestFocus();
        mGenerateBtn = findViewById(R.id.generate_btn);
        mLoginProgress = findViewById(R.id.login_progress_bar);
        mLoginFeedbackText = findViewById(R.id.login_form_feedback);
        ccp = findViewById(R.id.ccp);

        pos=getIntent().getIntExtra("pos",1234);
        mCountryName = getIntent().getStringExtra("countryName");
        mCountryDialCode = getIntent().getStringExtra("dialCode");
        ward = getIntent().getStringExtra("ward");
        district = getIntent().getStringExtra("district");
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        @SuppressLint("WrongConstant") SharedPreferences sh= getSharedPreferences("MySharedPref",MODE_APPEND);
//
//
//        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//        String countryCode = tm.getSimCountryIso();
//        String countryName = tm.getNetworkCountryIso();
//        int pos = sh.getInt("pos",0);
//        String mCountryName = sh.getString("mCountryName","def");
//        String mCountryDialCode = sh.getString("mCountryDialCode","def");
//        Log.d(TAG,pos+"::"+mCountryDialCode+"::"+ward+"::"+district);

        options = PhoneLogin.this.getResources().getStringArray(R.array.countries_array);
        al = Arrays.asList(options);
        ccp.setSelection(pos);
        mCountryCode.setText(mCountryDialCode);
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
                                otpIntent.putExtra("ward",ward);
                                otpIntent.putExtra("district",district);
                                startActivity(otpIntent);
                                Log.d(TAG,pos+"::"+mCountryDialCode+"::"+ward+"::"+district);
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


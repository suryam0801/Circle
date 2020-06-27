package circleapp.circlepackage.circle.Login;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

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

import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.LocationHelper;
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
    private String ward, district,mCountryDialCode,mCountryName,failcount;

    private FusedLocationProviderClient client;
    String[] options;
    List<String> al = new ArrayList<String>();
    int pos;
    AnalyticsLogEvents analyticsLogEvents;

    AlertDialog.Builder confirmation;

    public PhoneAuthProvider.ForceResendingToken resendingToken;
    LocationHelper locationHelper;

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mCountryCode = findViewById(R.id.country_code_text);
        mPhoneNumber = findViewById(R.id.phone_number_text);
        mPhoneNumber.requestFocus();
        mGenerateBtn = findViewById(R.id.generate_btn);
        mLoginProgress = findViewById(R.id.login_progress_bar);
        mLoginFeedbackText = findViewById(R.id.login_form_feedback);
        ccp = findViewById(R.id.ccp);
        analyticsLogEvents = new AnalyticsLogEvents();

        confirmation = new AlertDialog.Builder(this);


        pos=getIntent().getIntExtra("pos",1234);
        mCountryName = getIntent().getStringExtra("countryName");
        mCountryDialCode = getIntent().getStringExtra("dialCode");
        ward = getIntent().getStringExtra("ward");
        district = getIntent().getStringExtra("district");
        failcount = getIntent().getStringExtra("fail");
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        options = PhoneLogin.this.getResources().getStringArray(R.array.countries_array);
        al = Arrays.asList(options);
        ccp.setSelection(pos);
        mCountryCode.setText(mCountryDialCode);
        mGenerateBtn.setEnabled(true);
        ccp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                analyticsLogEvents.logEvents(PhoneLogin.this,"select_county","location_different","phone_login");
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

                if(country_code.isEmpty() || phone_number.isEmpty()){
                    mLoginFeedbackText.setText("Please fill in the form to continue.");
                    mLoginFeedbackText.setVisibility(View.VISIBLE);
                } else {
                    if (phone_number.length() ==10) {
                        Log.d(TAG,"FailCount:: "+failcount);

                        if (failcount != "1") {

                            mGenerateBtn.setEnabled(false);

                            confirmation.setMessage("Are you sure is this your number " + complete_phone_number)
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            Intent otpIntent = new Intent(PhoneLogin.this, OtpActivity.class);
                                            otpIntent.putExtra("phn_num", complete_phone_number);
                                            otpIntent.putExtra("ward", ward);
                                            otpIntent.putExtra("district", district);
                                            startActivity(otpIntent);
                                            //                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                            //                                        intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
                                            //                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            //                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                            //                                        startActivity(intent);}
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            mPhoneNumber.requestFocus();
                                        }
                                    });
                            AlertDialog alertDialog = confirmation.create();
                            alertDialog.setTitle("Confirmation");
                            alertDialog.show();
                        }
                        else
                            {
                                confirmation.setMessage("If you enter a wrong number this time have to reopen the Application so check twice :: "+complete_phone_number)
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Intent otpIntent = new Intent(PhoneLogin.this, OtpActivity.class);
                                                otpIntent.putExtra("phn_num", complete_phone_number);
                                                otpIntent.putExtra("ward", ward);
                                                otpIntent.putExtra("district", district);
                                                startActivity(otpIntent);
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                                mPhoneNumber.requestFocus();
                                            }
                                        });
                                AlertDialog alertDialog = confirmation.create();
                                alertDialog.setTitle("Confirmation");
                                alertDialog.show();
                            }
                    }
                    else
                        {
                            Toast.makeText(getApplicationContext(), "Enter a Valid 10-digit Number", Toast.LENGTH_SHORT).show();
//                            mLoginFeedbackText.setText("Enter the 10 digit Number");
//                            mLoginFeedbackText.setVisibility(View.VISIBLE);
                        }

                }
            }
        });

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

    @Override
    protected void onResume() {
        super.onResume();

        mGenerateBtn.setEnabled(true);

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mGenerateBtn.setEnabled(true);
    }
}
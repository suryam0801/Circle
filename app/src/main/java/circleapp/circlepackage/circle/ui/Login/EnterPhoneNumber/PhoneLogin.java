package circleapp.circlepackage.circle.ui.Login.EnterPhoneNumber;

import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Login.OtpActivity;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.data.LocalObjectModels.LoginUserObject;

public class PhoneLogin extends AppCompatActivity {

    private static final String TAG = PhoneLogin.class.getSimpleName();
    private EditText countryCodeEditText;
    private EditText phoneNumberEditText;
    private Button mGenerateBtn;
    private ProgressBar mLoginProgress;
    private TextView mLoginFeedbackText;
    private String complete_phone_number = "";
    public static final String PREF_NAME= "LOCATION";
    private Spinner ccp;
    private String ward, district,mCountryDialCode,mCountryName;
    String[] options;
    List<String> al = new ArrayList<String>();
    int pos;
    AlertDialog.Builder confirmation;
    private LoginUserObject loginUserObject;

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        countryCodeEditText = findViewById(R.id.country_code_text);
        phoneNumberEditText = findViewById(R.id.phone_number_text);
        phoneNumberEditText.requestFocus();
        mGenerateBtn = findViewById(R.id.generate_btn);
        mLoginProgress = findViewById(R.id.login_progress_bar);
        mLoginFeedbackText = findViewById(R.id.login_form_feedback);
        ccp = findViewById(R.id.ccp);

        confirmation = new AlertDialog.Builder(this);
        //get intents from LocationHelper
        setLocationParams();

        options = PhoneLogin.this.getResources().getStringArray(R.array.countries_array);
        al = Arrays.asList(options);
        ccp.setSelection(pos);
        countryCodeEditText.setText(mCountryDialCode);
        mGenerateBtn.setEnabled(true);
        ccp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //manually set country code from picker
                String code = getCountryCode(ccp.getSelectedItem().toString());
                String[] arrContryCode=PhoneLogin.this.getResources().getStringArray(R.array.DialingCountryCode);
                String contryDialCode = setCountryCode(code, arrContryCode);
                countryCodeEditText.setText("+"+contryDialCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                countryCodeEditText.setText("");
            }
        });

        mGenerateBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                //geting the Phone number from  user
                String country_code = countryCodeEditText.getText().toString();
                String phone_number = phoneNumberEditText.getText().toString();

                mGenerateBtn.setBackgroundResource(R.drawable.unpressable_button);
                mGenerateBtn.setTextColor(R.color.black);

                //combining the country code and mobile number
                complete_phone_number = country_code + phone_number;

                if(country_code.isEmpty() || phone_number.isEmpty()){
                    mLoginFeedbackText.setText("Please fill in the form to continue.");
                    mLoginFeedbackText.setVisibility(View.VISIBLE);
                } else {
                    if (phone_number.length() ==10) {
                            mGenerateBtn.setEnabled(false);

                            confirmation.setMessage("Are you sure is this your number " + complete_phone_number)
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            loginUserObject.setCompletePhoneNumber(country_code + phone_number);
                                            SessionStorage.saveLoginUserObject(PhoneLogin.this, loginUserObject);
                                            sendIntentsToOtpActivityAndFinish();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                            mGenerateBtn.setEnabled(true);
                                            mGenerateBtn.setBackgroundResource(R.drawable.gradient_button);
                                            mGenerateBtn.setTextColor(R.color.white);
                                            phoneNumberEditText.requestFocus();
                                        }
                                    });
                            AlertDialog alertDialog = confirmation.create();
                            alertDialog.setTitle("Confirmation");
                            alertDialog.show();
                        }
                    else
                    {
                        //When Input is not a 10 digit number
                        Toast.makeText(getApplicationContext(), "Enter a Valid 10-digit Number", Toast.LENGTH_SHORT).show();
                        mGenerateBtn.setEnabled(true);
                        mGenerateBtn.setBackgroundResource(R.drawable.gradient_button);
                    }

                }
            }
        });

    }
    private void setLocationParams(){
        loginUserObject = SessionStorage.getLoginUserObject(this);
        pos = loginUserObject.getPosition();
        mCountryName = loginUserObject.getCountryName();
        mCountryDialCode = loginUserObject.getCountryDialCode();
        ward = loginUserObject.getWard();
        district = loginUserObject.getDistrict();
    }
    private String  setCountryCode(String code, String[] arrContryCode){
        String contryDialCode = null;
        for(int i=0; i<arrContryCode.length; i++){
            String[] arrDial = arrContryCode[i].split(",");
            if(arrDial[1].trim().equals(code.trim())){
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sendIntentsToOtpActivityAndFinish(){
        finishAfterTransition();
        Intent otpIntent = new Intent(PhoneLogin.this, OtpActivity.class);
        startActivity(otpIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGenerateBtn.setEnabled(true);
        mGenerateBtn.setBackgroundResource(R.drawable.gradient_button);

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mGenerateBtn.setEnabled(true);
        mGenerateBtn.setBackgroundResource(R.drawable.gradient_button);
    }
}
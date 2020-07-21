package circleapp.circlepackage.circle.ui.Login.PhoneNumberEntry;

import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.List;

import circleapp.circlepackage.circle.R;

import static circleapp.circlepackage.circle.ViewModels.LoginViewModels.PhoneNumberEntry.EnterPhoneNumberDriver.getCountryCode;
import static circleapp.circlepackage.circle.ViewModels.LoginViewModels.PhoneNumberEntry.EnterPhoneNumberDriver.isPhoneNumber10Digits;
import static circleapp.circlepackage.circle.ViewModels.LoginViewModels.PhoneNumberEntry.EnterPhoneNumberDriver.sendIntentsToOtpActivityAndFinish;
import static circleapp.circlepackage.circle.ViewModels.LoginViewModels.PhoneNumberEntry.EnterPhoneNumberDriver.setCompletePhoneNumber;
import static circleapp.circlepackage.circle.ViewModels.LoginViewModels.PhoneNumberEntry.EnterPhoneNumberDriver.setCountryCode;

public class PhoneLogin extends AppCompatActivity {

    private static final String TAG = PhoneLogin.class.getSimpleName();
    private EditText countryCodeEditText;
    private EditText phoneNumberEditText;
    private Button mGenerateBtn;
    private ProgressBar mLoginProgress;
    private TextView mLoginFeedbackText;
    private Spinner ccp;
    private String mCountryDialCode;
    private getSearchableSpinnerLocation getSearchableSpinnerLocation = new getSearchableSpinnerLocation();
    String[] options;
    List<String> al = new ArrayList<String>();
    int pos;
    AlertDialog.Builder confirmation;

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

        pos = getSearchableSpinnerLocation.getPositionOfSpinner(this);
        options = PhoneLogin.this.getResources().getStringArray(R.array.countries_array);
        al = Arrays.asList(options);
        ccp.setSelection(pos);
        mCountryDialCode = getSearchableSpinnerLocation.getmCountryDialCode();
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
                if(isPhoneNumber10Digits(phone_number)){
                    mGenerateBtn.setEnabled(false);
                    confirmation.setMessage("Are you sure is this your number " + country_code + phone_number)
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setCompletePhoneNumber(PhoneLogin.this, country_code, phone_number);
                                    sendIntentsToOtpActivityAndFinish(PhoneLogin.this);
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
        });

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
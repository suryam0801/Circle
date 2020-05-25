package circleapp.circlepackage.circle.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.R;

public class PhoneLogin extends AppCompatActivity {

    private EditText mCountryCode;
    private EditText mPhoneNumber;
    private Button mGenerateBtn;
    private ProgressBar mLoginProgress;
    private TextView mLoginFeedbackText;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String complete_phone_number = "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        //To set the Fullscreen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();


        mCountryCode = findViewById(R.id.country_code_text);
        mPhoneNumber = findViewById(R.id.phone_number_text);
        mGenerateBtn = findViewById(R.id.generate_btn);
        mLoginProgress = findViewById(R.id.login_progress_bar);
        mLoginFeedbackText = findViewById(R.id.login_form_feedback);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();


        mGenerateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //geting the Phone number from  user
                String country_code = mCountryCode.getText().toString();
                String phone_number = mPhoneNumber.getText().toString();

                //combining the country code and mobile number
                complete_phone_number = "+" + country_code + phone_number;
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
                            15,
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
                                Intent otpIntent = new Intent(PhoneLogin.this, OtpActivity.class);
                                otpIntent.putExtra("AuthCredentials", s);
                                otpIntent.putExtra("phn_num", complete_phone_number);
                                startActivity(otpIntent);
                            }
                        },
                        5000);
            }
        };
    }
}

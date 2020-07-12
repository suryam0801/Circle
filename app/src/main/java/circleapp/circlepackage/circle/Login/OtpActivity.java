package circleapp.circlepackage.circle.Login;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

import static circleapp.circlepackage.circle.R.color.grey;

public class OtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacksresend, mCallbacks;
    private String ward, district, mCountryDialCode, mCountryName;
    private String mAuthVerificationId, phn_number;
    private PinEntryEditText mOtpText;
    private Button mVerifyBtn;
    private ProgressBar mOtpProgress;
    private TextView mOtpFeedback;
    private TextView resendTextView;
    private int counter = 30;
    CountDownTimer otpResendTimer;
    int pos;
    boolean autofill = false;

    AlertDialog.Builder confirmation;
    ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        //Getting Firebase instances
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        ward = getIntent().getStringExtra("ward");
        district = getIntent().getStringExtra("district");
        phn_number = getIntent().getStringExtra("phn_num");
        pos = getIntent().getIntExtra("pos", 0);
        mCountryName = getIntent().getStringExtra("countryName");
        mCountryDialCode = getIntent().getStringExtra("dialCode");
        //Getting AuthCredentials from the PhoneLogin page
//        mAuthVerificationId = getIntent().getStringExtra("AuthCredentials");
        resendingToken = getIntent().getParcelableExtra("resendToken");
        progressDialog = new ProgressDialog(OtpActivity.this);
        confirmation = new AlertDialog.Builder(this);
        progressDialog.setTitle("Verifying your Number...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        Log.d("otpactivity","started_activity");

        mOtpFeedback = findViewById(R.id.otp_form_feedback);
        mOtpProgress = findViewById(R.id.otp_progress_bar);
        mOtpText = findViewById(R.id.otp_text_view);
        mVerifyBtn = findViewById(R.id.verify_btn);
        resendTextView = findViewById(R.id.resend_otp_counter);
        HelperMethods.increaseTouchArea(resendTextView);
        resendTextView.setClickable(false);

        mVerifyBtn.setText("Verify OTP");
        confirmation.setMessage("There was an error in verifying your number. Please try again!")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("otpactivity","incorrect_number");
                        dialog.dismiss();
                        finishAfterTransition();
                        Intent intent = new Intent(OtpActivity.this, PhoneLogin.class);
                        intent.putExtra("pos", pos);
                        intent.putExtra("countryName", mCountryName);
                        intent.putExtra("dialCode", mCountryDialCode);
                        if (ward == null)
                            intent.putExtra("ward", "default");
                        else
                            intent.putExtra("ward", ward.trim());
                        intent.putExtra("district", district.trim());
                        intent.putExtra("ward", ward);
                        intent.putExtra("district", district);
                        intent.putExtra("fail", "1");
                        startActivity(intent);
                    }
                });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Here we can add the code for Auto Read the OTP
                Log.d("otpactivity", "onVerificationCompleted:: " + phoneAuthCredential.getSmsCode());
                if(phoneAuthCredential.getSmsCode()!=null){
                    mOtpText.setText(phoneAuthCredential.getSmsCode());
                    autofill = true;
                }
                progressDialog.dismiss();

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //Display the Error msg to the user through the Textview when error occurs
                progressDialog.dismiss();
                AlertDialog alertDialog = confirmation.create();
                alertDialog.setTitle("Alert");
                alertDialog.show();
                Log.d("otpactivity", "onVerificationFailed");
            }

            @Override
            public void onCodeSent(final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                //super.onCodeSent(s, forceResendingToken);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                //Opening the OtpActivity after the code(OTP) sent to the users mobile number

                                otpResendTimer = new CountDownTimer(30000, 1000) {

                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        resendTextView.setText("Resend OTP in: " + counter);
                                        Log.d("otpactivitycounter",""+counter);

                                        if (counter != 0) {
                                            counter--;
                                        }
                                        else {
                                            otpResendTimer.cancel();
                                            setResendOtpButton();
                                        }

                                    }

                                    @Override
                                    public void onFinish() {
                                        setResendOtpButton();
                                    }
                                }.start();
                                mVerifyBtn.setBackgroundResource(R.drawable.gradient_button);
                                progressDialog.dismiss();
                                mOtpText.requestFocus();
                                mAuthVerificationId = s;
                                mVerifyBtn.setClickable(true);
                                mVerifyBtn.setEnabled(true);
                                if(autofill==true){
                                    mVerifyBtn.setText("Verifying OTP");
                                    mVerifyBtn.performClick();
                                }
                                Log.d("OtpActivity", s);
                                Toast.makeText(getApplicationContext(), "OTP Sent successfully", Toast.LENGTH_SHORT).show();
                            }
                        },
                        5000);
            }
        };

        mCallbacksresend = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d("otpactivity","verificationcomplete");
                // Here we can add the code for Auto Read the OTP
                if(phoneAuthCredential.getSmsCode()!=null){
                    mOtpText.setText(phoneAuthCredential.getSmsCode());
                    autofill = true;
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d("otpactivity","verificationfailed");
                //Display the Error msg to the user through the Textview when error occurs
            }

            @Override
            public void onCodeSent(final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                //super.onCodeSent(s, forceResendingToken);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.d("otpactivity","otpsent");
                                mAuthVerificationId = s;
                                //Opening the OtpActivity after the code(OTP) sent to the users mobile number
                                Toast.makeText(getApplicationContext(), "OTP Resent successfully", Toast.LENGTH_SHORT).show();
                            }
                        },
                        5000);
            }
        };

        mVerifyBtn.setOnClickListener(v -> {
            String otp = mOtpText.getText().toString();
            mOtpFeedback.setVisibility(View.INVISIBLE);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            if (otp.isEmpty()) {
                mOtpFeedback.setVisibility(View.VISIBLE);
                mOtpFeedback.setText("Please fill in the form and try again.");

            } else {

                mOtpProgress.setVisibility(View.VISIBLE);
                mVerifyBtn.setEnabled(false);
                mVerifyBtn.setClickable(false);

                //Pasing the OTP and credentials for the Verification
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
                Log.d("OtpActivity", "credential:: " + credential.getSmsCode());
                signInWithPhoneAuthCredential(credential);
            }
        });

    }

    //Function to check the given OTP
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(OtpActivity.this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //get the user instance from the firebase
                            final FirebaseUser FBuser = task.getResult().getUser();
                            final String uid = FBuser.getUid();
                            //To check the users is already registered or not
                            FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(OtpActivity.this).get(FirebaseRetrievalViewModel.class);

                            LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(uid);
                            liveData.observe(OtpActivity.this, dataSnapshot -> {
                                if (dataSnapshot.exists()) {
                                    User user = dataSnapshot.getValue(User.class);
                                    String string = new Gson().toJson(user);
                                    SessionStorage.saveUser(OtpActivity.this, user);
                                    sendUserToHome();
                                } else {
                                    senduserToReg();
                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                mOtpProgress.setVisibility(View.INVISIBLE);
                                mVerifyBtn.setEnabled(true);
                                mVerifyBtn.setClickable(true);
                                // The verification code entered was invalid
                                mOtpFeedback.setVisibility(View.VISIBLE);
                                mOtpFeedback.setText("Incorrect OTP, Please try again");
                            }
                        }
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        phn_number = getIntent().getStringExtra("phn_num");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phn_number,
                60,
                TimeUnit.SECONDS,
                OtpActivity.this,
                mCallbacks
        );
//        to check the user and change the BUtton text based on the user
    }
    private void setResendOtpButton(){
        resendTextView.setText("Click here to resend OTP");
        resendTextView.setTextColor(Color.parseColor("#6CACFF"));
        resendTextView.setClickable(true);
        resendTextView.setOnClickListener(view -> {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phn_number,
                    15,
                    TimeUnit.SECONDS,
                    OtpActivity.this,
                    mCallbacksresend
            );
        });
    }

    //Function to send the  user to HomePage
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void sendUserToHome() {
        mOtpProgress.setVisibility(View.INVISIBLE);
        mVerifyBtn.setEnabled(true);
        finishAfterTransition();
        Intent homeIntent = new Intent(OtpActivity.this, ExploreTabbedActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(homeIntent);
    }

    //Function to send the user to Registration Page
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void senduserToReg() {
        mOtpProgress.setVisibility(View.INVISIBLE);
        mVerifyBtn.setEnabled(true);
        finishAfterTransition();
        Intent homeIntent = new Intent(OtpActivity.this, GatherUserDetails.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeIntent.putExtra("phn", phn_number);
        homeIntent.putExtra("ward", ward);
        homeIntent.putExtra("district", district);
        //homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(homeIntent);
        Log.d("OtpActivity", ward + "::" + district);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("OtpActivity", "onPause:: " + mOtpText.getText());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("OtpActivity", "onResume");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        Intent intent = new Intent(OtpActivity.this, PhoneLogin.class);
        intent.putExtra("pos", pos);
        intent.putExtra("countryName", mCountryName);
        intent.putExtra("dialCode", mCountryDialCode);
        if (ward == null)
            intent.putExtra("ward", "default");
        else
            intent.putExtra("ward", ward.trim());
        intent.putExtra("district", district.trim());
        intent.putExtra("ward", ward);
        intent.putExtra("district", district);
        intent.putExtra("fail", "1");
        startActivity(intent);
    }
}
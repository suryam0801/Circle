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
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacksresend,mCallbacks;
    private String ward, district;
    private String mAuthVerificationId, phn_number;
    private PinEntryEditText mOtpText;
    private Button mVerifyBtn;
    private ProgressBar mOtpProgress;
    private TextView mOtpFeedback;
    private TextView resendTextView;
    private int counter = 30;
    int failcounter;

    AlertDialog.Builder confirmation,verifyfail;
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
        //Getting AuthCredentials from the PhoneLogin page
//        mAuthVerificationId = getIntent().getStringExtra("AuthCredentials");
        resendingToken = getIntent().getParcelableExtra("resendToken");
        progressDialog = new ProgressDialog(OtpActivity.this);
        confirmation = new AlertDialog.Builder(this);
        verifyfail = new AlertDialog.Builder(this);
        progressDialog.setTitle("Verifying your Number...");
        progressDialog.show();
        progressDialog.setCancelable(false);


        mOtpFeedback = findViewById(R.id.otp_form_feedback);
        mOtpProgress = findViewById(R.id.otp_progress_bar);
        mOtpText = findViewById(R.id.otp_text_view);
        mVerifyBtn = findViewById(R.id.verify_btn);
        mVerifyBtn.setEnabled(false);
        mVerifyBtn.setClickable(false);
        mVerifyBtn.setBackgroundResource(R.drawable.unpressable_button);
        mVerifyBtn.setTextColor(R.color.black);
//Intimate the user for his low internet speed
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        int downSpeed = nc.getLinkDownstreamBandwidthKbps();
        int upSpeed = nc.getLinkUpstreamBandwidthKbps();
        Log.d("OtpActivity","Intenet Speed ::"+ downSpeed);
        if (downSpeed <10240)
        {
            Toast.makeText(this,"Your Internet speed is very Low",Toast.LENGTH_SHORT).show();
        }
        resendTextView = findViewById(R.id.resend_otp_counter);
        HelperMethods.increaseTouchArea(resendTextView);
        resendTextView.setClickable(false);

        confirmation = new AlertDialog.Builder(this);
        verifyfail = new AlertDialog.Builder(this);
        verifyfail.setMessage("You have Entered Wrong Number 2 times so reopen the app to continue")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        finishAffinity();
                    }
                });

        confirmation.setMessage("Your Number seems Incorrect Enter your Number Correctly!!")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent= new Intent(OtpActivity.this, PhoneLogin.class);
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
                Log.d("TAG","onVerificationCompleted:: "+phoneAuthCredential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //Display the Error msg to the user through the Textview when error occurs
                failcounter = failcounter+1;
                progressDialog.dismiss();
                if (failcounter != 2)
                {
                    AlertDialog alertDialog = confirmation.create();
                    alertDialog.setTitle("Alert");
                    alertDialog.show();
                }
                else
                {
                    AlertDialog dialog = verifyfail.create();
                    dialog.setTitle("Alert");
                    dialog.show();
                }

            }

            @Override
            public void onCodeSent(final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                //Opening the OtpActivity after the code(OTP) sent to the users mobile number
                                new CountDownTimer(3000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        resendTextView.setText("Resend OTP in: " + counter);

                                        if (counter!=0)
                                        {
                                            counter--;
                                        }
                                        else
                                            {
                                                resendTextView.setVisibility(View.VISIBLE);
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

                                    }
                                    @Override
                                    public void onFinish() {
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
                                }.start();
                                mVerifyBtn.setBackgroundResource(R.drawable.gradient_button);
                                progressDialog.dismiss();
                                mOtpText.requestFocus();
                                mAuthVerificationId = s;
                                mVerifyBtn.setClickable(true);
                                mVerifyBtn.setEnabled(true);
                                Log.d("OtpActivity",s);
                                Toast.makeText(OtpActivity.this, "OTP Sent successfully", Toast.LENGTH_SHORT).show();
                            }
                        },
                        0);
            }
        };
        mCallbacksresend = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Here we can add the code for Auto Read the OTP
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //Display the Error msg to the user through the Textview when error occurs
            }

            @Override
            public void onCodeSent(final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                mAuthVerificationId = s;
                                //Opening the OtpActivity after the code(OTP) sent to the users mobile number
                                Toast.makeText(OtpActivity.this, "OTP Sended succssfully", Toast.LENGTH_SHORT).show();
                            }
                        },
                        5000);
            }
        };

        mVerifyBtn.setOnClickListener(v -> {
            String otp = mOtpText.getText().toString();
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
                Log.d("OtpActivity","credential:: "+credential.getSmsCode());
                signInWithPhoneAuthCredential(credential);
            }
        });
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential ){

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(OtpActivity.this, new OnCompleteListener<AuthResult>() {
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
                                    HelperMethods.storeUserFile(string, OtpActivity.this);
                                } else {
                                    HelperMethods.senduserToReg(OtpActivity.this,phn_number,ward,district);
                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mOtpFeedback.setVisibility(View.VISIBLE);
                                mOtpFeedback.setText("There was an error verifying OTP");
                            }
                        }
                        mOtpProgress.setVisibility(View.INVISIBLE);
                        mVerifyBtn.setEnabled(true);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        phn_number = getIntent().getStringExtra("phn_num");
        FirebaseWriteHelper.PhoneAuth(this,phn_number);
        if (mCurrentUser != null) {
            //old user
            mVerifyBtn.setText("Verify & Login");
        } else {
            //new user
            mVerifyBtn.setText("Verify & Register");
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("OtpActivity","onPause:: "+mOtpText.getText());
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("OtpActivity","onResume");
    }
}
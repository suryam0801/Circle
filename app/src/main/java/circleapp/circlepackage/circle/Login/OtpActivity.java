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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

import static circleapp.circlepackage.circle.R.color.grey;

public class OtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String mAuthVerificationId, phn_number;
    private PinEntryEditText mOtpText;
    private Button mVerifyBtn;
    private ProgressBar mOtpProgress;
    private DatabaseReference usersDB;
    private FirebaseDatabase database;
    private TextView mOtpFeedback;
    private TextView resendTextView;
    private int counter = 30;
    private String ward, district;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacksresend,mCallbacks;
    int failcounter;

    AlertDialog.Builder confirmation,verifyfail;

    private User userldb;
    AnalyticsLogEvents analyticsLogEvents;
    //    private AppDatabase lDb;
    String doc_id;
    ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        analyticsLogEvents = new AnalyticsLogEvents();

        //Getting Firebase instances
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        usersDB = database.getReference("Users");
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
//        mOtpText.requestFocus();
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(mOtpText, InputMethodManager.SHOW_IMPLICIT);
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

                        Intent intent= new Intent(OtpActivity.this,PhoneLogin.class);
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
                                new CountDownTimer(900000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        resendTextView.setText("Resend OTP in: " + counter);
                                        counter--;
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
                                            resendTextView.setVisibility(View.GONE);
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
                                Toast.makeText(getApplicationContext(), "OTP Sent successfully", Toast.LENGTH_SHORT).show();
                            }
                        },
                        5000);
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
                                analyticsLogEvents.logEvents(OtpActivity.this,"resent_otp","button_pressed","otp_activity");
                                //Opening the OtpActivity after the code(OTP) sent to the users mobile number
                                Toast.makeText(getApplicationContext(), "OTP Sended succssfully", Toast.LENGTH_SHORT).show();
                            }
                        },
                        5000);
            }
        };

        mVerifyBtn.setOnClickListener(v -> {
            String otp = mOtpText.getText().toString();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            if (otp.isEmpty()) {
                analyticsLogEvents.logEvents(OtpActivity.this,"empty_otp","otp_text_empty","otp_activity");
                mOtpFeedback.setVisibility(View.VISIBLE);
                mOtpFeedback.setText("Please fill in the form and try again.");

            } else {

                mOtpProgress.setVisibility(View.VISIBLE);
                mVerifyBtn.setEnabled(false);

                //Pasing the OTP and credentials for the Verification
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
                Log.d("OtpActivity","credential:: "+credential.getSmsCode());
                signInWithPhoneAuthCredential(credential);
            }
        });

    }

    //Function to check the given OTP
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(OtpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //get the user instance from the firebase
                            final FirebaseUser user = task.getResult().getUser();
                            final String uid = user.getUid();
                            //To check the users is already registered or not
                            usersDB.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        analyticsLogEvents.logEvents(OtpActivity.this,"otp_success","existing_user","otp_activity");
                                        User user = dataSnapshot.getValue(User.class);
                                        String string = new Gson().toJson(user);
                                        SessionStorage.saveUser(OtpActivity.this, user);
                                        storeUserFile(string, getApplicationContext());
                                    } else {
                                        analyticsLogEvents.logEvents(OtpActivity.this,"otp_success_new_user","new_user","otp_activity");
                                        senduserToReg();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                analyticsLogEvents.logEvents(OtpActivity.this,"invalid_otp","wrong_otp","otp_activity");
                                mOtpFeedback.setVisibility(View.VISIBLE);
                                mOtpFeedback.setText("There was an error verifying OTP");
                            }
                        }
                        mOtpProgress.setVisibility(View.INVISIBLE);
                        mVerifyBtn.setEnabled(true);
                    }
                });
    }

    private void storeUserFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            sendUserToHome();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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
        if (mCurrentUser != null) {
            //old user
            mVerifyBtn.setText("Verify & Login");
        } else {
            //new user
            mVerifyBtn.setText("Verify & Register");
        }
    }

    //Function to send the  user to HomePage
    public void sendUserToHome() {
        Intent homeIntent = new Intent(OtpActivity.this, ExploreTabbedActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(homeIntent);
        finish();
    }

    //Function to send the user to Registration Page
    private void senduserToReg() {
        Intent homeIntent = new Intent(OtpActivity.this, GatherUserDetails.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeIntent.putExtra("phn", phn_number);
        homeIntent.putExtra("ward",ward);
        homeIntent.putExtra("district",district);
        //homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(homeIntent);
        Log.d("OtpActivity",ward+"::"+district);
        finish();
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
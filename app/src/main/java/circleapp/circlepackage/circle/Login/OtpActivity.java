package circleapp.circlepackage.circle.Login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
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

import circleapp.circlepackage.circle.MainActivity;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;

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
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAnalytics mFirebaseAnalytics;

    private User userldb;
    //    private AppDatabase lDb;
    String doc_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(OtpActivity.this, "OTP Verification", null /* class override */);

        //To set the Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();

        //Getting Firebase instances
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
//        database.setPersistenceEnabled(true); //persistence automatically handles offline behavior
        database = FirebaseDatabase.getInstance();
        usersDB = database.getReference("Users");

        //Getting AuthCredentials from the PhoneLogin page
        mAuthVerificationId = getIntent().getStringExtra("AuthCredentials");
        phn_number = getIntent().getStringExtra("phn_num");
        resendingToken = getIntent().getParcelableExtra("resendToken");

        mOtpFeedback = findViewById(R.id.otp_form_feedback);
        mOtpProgress = findViewById(R.id.otp_progress_bar);
        mOtpText = findViewById(R.id.otp_text_view);
        mVerifyBtn = findViewById(R.id.verify_btn);
        resendTextView = findViewById(R.id.resend_otp_counter);
        resendTextView.setClickable(false);

        new CountDownTimer(30000, 1000) {
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
                            mCallbacks
                    );
                    resendTextView.setVisibility(View.GONE);
                });
            }
        }.start();

        mVerifyBtn.setOnClickListener(v -> {
            String otp = mOtpText.getText().toString();

            if (otp.isEmpty()) {
                mOtpFeedback.setVisibility(View.VISIBLE);
                mOtpFeedback.setText("Please fill in the form and try again.");

            } else {

                mOtpProgress.setVisibility(View.VISIBLE);
                mVerifyBtn.setEnabled(false);

                //Pasing the OTP and credentials for the Verification
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
                signInWithPhoneAuthCredential(credential);
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
            }

            @Override
            public void onCodeSent(final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                //Opening the OtpActivity after the code(OTP) sent to the users mobile number
                                Toast.makeText(getApplicationContext(), "OTP Resent", Toast.LENGTH_SHORT).show();
                            }
                        },
                        5000);
            }
        };
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
                            usersDB.child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        User user = dataSnapshot.getValue(User.class);
                                        String string = new Gson().toJson(user);
                                        storeUserFile(string, getApplicationContext());
                                    } else {
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
        //to check the user and change the BUtton text based on the user
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
        Intent homeIntent = new Intent(OtpActivity.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }

    //Function to send the user to Registration Page
    private void senduserToReg() {
        Intent homeIntent = new Intent(OtpActivity.this, GatherUserDetails.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeIntent.putExtra("phn", phn_number);
        startActivity(homeIntent);
        finish();
    }
}

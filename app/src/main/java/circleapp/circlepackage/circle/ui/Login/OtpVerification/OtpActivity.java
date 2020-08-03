package circleapp.circlepackage.circle.ui.Login.OtpVerification;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;

import java.util.Objects;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.LocalObjectModels.LoginUserObject;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.OtpVerification.OtpViewModel;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.OtpVerification.PhoneCallbacksListener;
import circleapp.circlepackage.circle.ui.ExploreTabbedActivity;
import circleapp.circlepackage.circle.ui.Login.PhoneNumberEntry.PhoneLogin;
import circleapp.circlepackage.circle.ui.Login.UserRegistration.NewUserProfileCreation;

public class OtpActivity extends AppCompatActivity implements PhoneCallbacksListener {

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacksresend, mCallbacks;
    private String mAuthVerificationId, phn_number;
    private PinEntryEditText mOtpText;
    private Button mVerifyBtn;
    private ProgressBar mOtpProgress;
    private TextView mOtpFeedback;
    private TextView resendTextView;
    private int counter = 30;
    CountDownTimer otpResendTimer;
    boolean autofill = false;
    private LoginUserObject loginUserObject;
    public OtpViewModel otpViewModel;
    private GlobalVariables globalVariables = new GlobalVariables();
  
    AlertDialog.Builder confirmation;
    public ProgressDialog progressDialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

//        FirebaseWriteHelper.getUser();
        setTempUserObject();
        InitUIElements();
        initProgressbar();
        initAlertDialog();
        HelperMethodsUI.increaseTouchArea(resendTextView);
        resendTextView.setClickable(false);
        mVerifyBtn.setText("Verify OTP");
        otpViewModel = ViewModelProviders.of(OtpActivity.this).get(OtpViewModel.class);
        otpViewModel.setPhoneCallbacksListener(OtpActivity.this);
    }

    //button click listener function
    public void verifyOtp(View view){
        String otp = mOtpText.getText().toString();
        mOtpFeedback.setVisibility(View.INVISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        UpdateUI(otp);
    }

    public void initAlertDialog(){
        confirmation = new AlertDialog.Builder(this);
        confirmation.setMessage("There was an error in verifying your number. Please try again!")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendBackToPhoneNumberEntry();
                    }
                });
    }

    public void initProgressbar(){
        progressDialog = new ProgressDialog(OtpActivity.this);
        progressDialog.setTitle("Verifying your Number...");
        progressDialog.show();
        progressDialog.setCancelable(false);
    }

    public void UpdateUI(String otp){
        if (otp.isEmpty()) {
            mOtpFeedback.setVisibility(View.VISIBLE);
            mOtpFeedback.setText("Please fill in the form and try again.");

        } else {

            mOtpProgress.setVisibility(View.VISIBLE);
            mVerifyBtn.setEnabled(false);
            mVerifyBtn.setClickable(false);

            //Pasing the OTP and credentials for the Verification
            try{
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
//                signInWithPhoneAuthCredential(credential);
                otpViewModel.signInWithPhoneAuthCredential(credential,this);
            }catch (Exception e){
                Toast.makeText(this, "Verification Code is wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void InitUIElements(){
        mOtpFeedback = findViewById(R.id.otp_form_feedback);
        mOtpProgress = findViewById(R.id.otp_progress_bar);
        mOtpText = findViewById(R.id.otp_text_view);
        mVerifyBtn = findViewById(R.id.verify_btn);
        resendTextView = findViewById(R.id.resend_otp_counter);

    }
    public void setTempUserObject(){
        loginUserObject = globalVariables.getCurrentLoginUserObject();
        phn_number = loginUserObject.getCompletePhoneNumber();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        phn_number = getIntent().getStringExtra("phn_num");
        otpViewModel.sendVerificationCode(phn_number,OtpActivity.this);
//        to check the user and change the BUtton text based on the user

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sendBackToPhoneNumberEntry(){
        finishAfterTransition();
        Intent intent = new Intent(OtpActivity.this, PhoneLogin.class);
        startActivity(intent);
    }
    private void setResendOtpButton(){
        resendTextView.setText("Click here to resend OTP");
        resendTextView.setTextColor(Color.parseColor("#6CACFF"));
        resendTextView.setClickable(true);
        resendTextView.setOnClickListener(view -> {
            otpViewModel.resendVerificationCode(phn_number);
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
    public void senduserToReg(String uid) {
        mOtpProgress.setVisibility(View.INVISIBLE);
        mVerifyBtn.setEnabled(true);
        finishAfterTransition();
        Intent homeIntent = new Intent(OtpActivity.this, NewUserProfileCreation.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginUserObject.setUid(uid);
        globalVariables.saveCurrentLoginUserObject(loginUserObject);

        startActivity(homeIntent);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        Intent intent = new Intent(OtpActivity.this, PhoneLogin.class);
        startActivity(intent);
    }

    @Override
    public void onVerificationCompleted() {

    }

    @Override
    public void onVerificationCodeDetected(String code) {
        if(code!=null){
            mOtpText.setText(code);
            autofill = true;
        }
        progressDialog.dismiss();
    }

    @Override
    public void onVerificationFailed(String message) {
        progressDialog.dismiss();
        AlertDialog alertDialog = confirmation.create();
        alertDialog.setTitle("Alert");
        alertDialog.show();
    }

    @Override
    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken token) {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        //Opening the OtpActivity after the code(OTP) sent to the users mobile number

                        otpResendTimer = new CountDownTimer(30000, 1000) {

                            @Override
                            public void onTick(long millisUntilFinished) {
                                resendTextView.setText("Resend OTP in: " + counter);

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
                        if(autofill){
                            mVerifyBtn.setText("Verifying OTP");
                            mVerifyBtn.performClick();
                        }
                        Toast.makeText(getApplicationContext(), "OTP Sent successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                5000);
    }

    @SuppressLint("NewApi")
    @Override
    public void signinCompleted(Task<AuthResult> task) {
        //get the user instance from the firebase
        final FirebaseUser FBuser = Objects.requireNonNull(task.getResult()).getUser();
        final String uid = FBuser.getUid();
        //To check the users is already registered or not
        UserViewModel viewModel = ViewModelProviders.of(OtpActivity.this).get(UserViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(uid);
        liveData.observe(OtpActivity.this, dataSnapshot -> {
            if (dataSnapshot.exists()) {
                User user = dataSnapshot.getValue(User.class);
                globalVariables.saveCurrentUser(user);
                sendUserToHome();
            } else {
                senduserToReg(uid);
            }
        });
    }

    @Override
    public void signinFailed(boolean b) {
        if (b) {
            mOtpProgress.setVisibility(View.INVISIBLE);
            mVerifyBtn.setEnabled(true);
            mVerifyBtn.setClickable(true);
            // The verification code entered was invalid
            mOtpFeedback.setVisibility(View.VISIBLE);
            mOtpFeedback.setText("Incorrect OTP, Please try again");
        }
    }
}
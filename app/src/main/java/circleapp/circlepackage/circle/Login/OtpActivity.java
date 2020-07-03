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
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
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
    private DatabaseReference usersDB;
    private FirebaseDatabase database;
    private TextView mOtpFeedback;
    private TextView resendTextView;
    private int counter = 30;
    int failcounter;

    AlertDialog.Builder confirmation,verifyfail;

    private User userldb;
    //    private AppDatabase lDb;
    String doc_id;
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
        FirebaseWriteHelper.verifyUser(phn_number,mOtpText,mVerifyBtn,progressDialog,resendTextView,this,mOtpFeedback,mOtpProgress,ward,district);
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
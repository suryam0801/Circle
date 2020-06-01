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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import circleapp.circlepackage.circle.Explore.Explore;
//import circleapp.circlepackage.circle.LocalDatabase.AppDatabase;
import circleapp.circlepackage.circle.LocalDatabase.AppExecutors;
import circleapp.circlepackage.circle.MainActivity;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class OtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String mAuthVerificationId, phn_number;
    private EditText mOtpText;
    private Button mVerifyBtn;
    private ProgressBar mOtpProgress;
    private DatabaseReference usersDB;
    private FirebaseDatabase database;
    private TextView mOtpFeedback;
    private User userldb;
//    private AppDatabase lDb;
    String doc_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

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

        mOtpFeedback = findViewById(R.id.otp_form_feedback);
        mOtpProgress = findViewById(R.id.otp_progress_bar);
        mOtpText = findViewById(R.id.otp_text_view);
        mVerifyBtn = findViewById(R.id.verify_btn);

        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            usersDB.child(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
//                                        userldb = dataSnapshot.getValue(User.class);
//
//                                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                if (userldb!=null)
//                                                {
//                                                    lDb.userDao().insertUser(userldb);
//                                                    Log.d("OtpActivity","user data stored locally"+userldb);
//                                                }
//
//                                            }
//                                        });

                                        sendUserToHome();
                                    } else {
                                        GatherUserDetails g = new GatherUserDetails();
                                        g.credentialSetter(credential);
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
        Intent homeIntent = new Intent(OtpActivity.this, Explore.class);
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

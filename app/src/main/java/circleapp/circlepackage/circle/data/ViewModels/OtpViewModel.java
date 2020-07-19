package circleapp.circlepackage.circle.data.ViewModels;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.ui.Login.OtpVerification.OtpActivity;

public class OtpViewModel extends ViewModel {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    //    public  PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private OtpActivity otpActivity = new OtpActivity();
    PhoneCallbacksListener phoneCallbacksListener;

    public void setPhoneCallbacksListener(PhoneCallbacksListener phoneCallbacksListener) {
        this.phoneCallbacksListener = phoneCallbacksListener;
    }

    //    public void callbacklistener(){
//    public void PhoneAuth() {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                String smscode = phoneAuthCredential.getSmsCode();
                phoneCallbacksListener.onVerificationCodeDetected(smscode);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                phoneCallbacksListener.onVerificationFailed(e.toString());
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//            super.onCodeSent(s, forceResendingToken);
                resendingToken = forceResendingToken;
                phoneCallbacksListener.onCodeSent(s, forceResendingToken);
            }
        };
//}

        public void signInWithPhoneAuthCredential (PhoneAuthCredential
        credential, PhoneCallbacksListener listener){
            FirebaseWriteHelper.getAuthToken().signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                listener.signinCompleted(task);
                            } else {
                                listener.signinFailed(task.getException() instanceof FirebaseAuthInvalidCredentialsException);
                            }
                        }
                    });

        }

        public void sendVerificationCode (String phone, OtpActivity otpActivity){
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phone.trim(),
                    60,
                    TimeUnit.SECONDS,
                    otpActivity,
                    mCallbacks
            );
        }
        public void resendVerificationCode (String phone){
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phone.trim(),
                    30,
                    TimeUnit.SECONDS,
                    TaskExecutors.MAIN_THREAD,
                    mCallbacks,
                    resendingToken
            );
        }
    }


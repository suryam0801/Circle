package circleapp.circlepackage.circle.ViewModels.LoginViewModels.OtpVerification;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;

public class OtpViewModel extends ViewModel {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    //    public  PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
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

        public void sendVerificationCode (String phone, Activity activity){
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phone.trim(),
                    60,
                    TimeUnit.SECONDS,
                    activity,
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


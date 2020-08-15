package circleapp.circleapppackage.circle.ViewModels.LoginViewModels.OtpVerification;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthProvider;

//interface code
public interface PhoneCallbacksListener {
     void onVerificationCompleted();
     void onVerificationCodeDetected(String code);
     void onVerificationFailed(String message);
     void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken token);
     void signinCompleted(Task<AuthResult> task);
     void signinFailed(boolean b);
}

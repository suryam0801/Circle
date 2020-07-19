package circleapp.circlepackage.circle.data.ViewModels;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthProvider;

//interface code
public interface PhoneCallbacksListener {
    public void onVerificationCompleted();
    public void onVerificationCodeDetected(String code);
    public void onVerificationFailed(String message);
    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken token);
    public void signinCompleted(Task<AuthResult> task);
    public void signinFailed(boolean b);
}

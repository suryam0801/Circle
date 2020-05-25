package circleapp.circlepackage.circle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.Explore.Explore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth currentUser;
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //To set the Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();

        currentUser = FirebaseAuth.getInstance();

        if (currentUser.getCurrentUser() != null) {
            Log.d(TAG, currentUser.getCurrentUser().getUid());
            startActivity(new Intent(MainActivity.this, Explore.class));
            finish();
        } else {
            startActivity(new Intent(MainActivity.this, PhoneLogin.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser.getCurrentUser() != null) {
            Log.d(TAG, currentUser.getCurrentUser().getUid());
            startActivity(new Intent(MainActivity.this, Explore.class));
            finish();
        } else {
            startActivity(new Intent(MainActivity.this, PhoneLogin.class));
            finish();
        }
    }
}
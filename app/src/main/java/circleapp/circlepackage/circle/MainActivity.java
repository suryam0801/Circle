package circleapp.circlepackage.circle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.MainDisplay.Explore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth currentUser;
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUser=FirebaseAuth.getInstance();

//        if(currentUser.getCurrentUser()!=null)
//        {
//            Log.d(TAG, currentUser.getCurrentUser().getUid());
//            startActivity(new Intent(MainActivity.this, Explore.class));
//            finish();
//        } else {
//            startActivity(new Intent(MainActivity.this, PhoneLogin.class));
//            finish();
//        }

    }
    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser.getCurrentUser()!=null)
        {
            Log.d(TAG, currentUser.getCurrentUser().getUid());
            startActivity(new Intent(MainActivity.this, Explore.class));
            finish();
        } else {
            startActivity(new Intent(MainActivity.this, PhoneLogin.class));
            finish();
        }

    }
}
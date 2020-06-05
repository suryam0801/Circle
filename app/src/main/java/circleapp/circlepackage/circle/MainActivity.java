package circleapp.circlepackage.circle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.Explore.Explore;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth currentUser;
    private FirebaseAnalytics firebaseAnalytics;
    public static final String TAG = MainActivity.class.getSimpleName();
    private int mainActivityFb = 0;
    private String userDistrict, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //To set the Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();

        // Obtain the Firebase Analytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //bundle to send to fb
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, mainActivityFb);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "In main activity bitch");

        //Logs an app event.
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        firebaseAnalytics.logEvent("Checkout_analytics", bundle);

        //Sets whether analytics collection is enabled for this app on this device.
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);

        //Sets the minimum engagement time required before starting a session. The default value is 10000 (10 seconds)
        //firebaseAnalytics.setMinimumSessionDuration(15000);

        //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes).
        firebaseAnalytics.setSessionTimeoutDuration(5000);

        readFromFile(getApplicationContext());

        //Sets the user ID property.
        firebaseAnalytics.setUserId(String.valueOf(userId));
        //Sets a user property to a given value.
        firebaseAnalytics.setUserProperty("District", userDistrict);

    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("user.txt");

            if ( inputStream != null || !inputStream.equals("") ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();

                User user = new Gson().fromJson(ret, User.class);
                SessionStorage.saveUser(MainActivity.this, user);
                userDistrict = user.getDistrict();
                userId = user.getUserId();

                startActivity(new Intent(MainActivity.this, Explore.class));
                finish();

            } else {
                startActivity(new Intent(MainActivity.this, PhoneLogin.class));
                finish();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            startActivity(new Intent(MainActivity.this, PhoneLogin.class));
            finish();
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
            startActivity(new Intent(MainActivity.this, PhoneLogin.class));
            finish();
        }

        return ret;
    }

    @Override
    protected void onStart() {
        super.onStart();

        readFromFile(getApplicationContext());
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, 1234);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "In onStart at main");
        firebaseAnalytics.logEvent("Checkout_analytics_at_onSTART", bundle);
    }
}
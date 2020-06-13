package circleapp.circlepackage.circle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Login.EntryPage;
import circleapp.circlepackage.circle.ObjectModels.User;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseDatabase database;
    private DatabaseReference usersDB;
    AnalyticsLogEvents analyticsLogEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
        analyticsLogEvents = new AnalyticsLogEvents();

        database = FirebaseDatabase.getInstance();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("PERSISTENCECHECK", Activity.MODE_PRIVATE);
        if (prefs.getBoolean(MainActivity.class.getCanonicalName(), true)) {
            prefs.edit().putBoolean(MainActivity.class.getCanonicalName(),false).apply();
            database.setPersistenceEnabled(true);
        }

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            usersDB = database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            usersDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        User user = dataSnapshot.getValue(User.class);
                        SessionStorage.saveUser(MainActivity.this, user);
                        analyticsLogEvents.logEvents(MainActivity.this, user.getUserId(), "existing_user","app_open");
                        startActivity(new Intent(MainActivity.this, ExploreTabbedActivity.class));
                        finish();
                    } else {
                        analyticsLogEvents.logEvents(MainActivity.this, "null_user", "logged_out_user","app_open");
                        startActivity(new Intent(MainActivity.this, EntryPage.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    startActivity(new Intent(MainActivity.this, EntryPage.class));
                    finish();
                }
            });
        } else {
            analyticsLogEvents.logEvents(MainActivity.this, "null_user", "new_user","app_open");
            startActivity(new Intent(MainActivity.this, EntryPage.class));
            finish();
        }
    }
}
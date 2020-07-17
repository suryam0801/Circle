package circleapp.circlepackage.circle.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Login.EntryPage;
import circleapp.circlepackage.circle.ui.Login.OnBoarding.get_started_first_page;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseDatabase database;
    private DatabaseReference usersDB, notificationDB;
    private NotificationManagerCompat notificationManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
        notificationManager = NotificationManagerCompat.from(this);

        database = FirebaseDatabase.getInstance();
        SharedPreferences persistenceCheckPrefs = getApplicationContext().getSharedPreferences("PERSISTENCECHECK", Activity.MODE_PRIVATE);
        if (persistenceCheckPrefs.getBoolean(MainActivity.class.getCanonicalName(), true)) {
            persistenceCheckPrefs.edit().putBoolean(MainActivity.class.getCanonicalName(),false).apply();
            database.setPersistenceEnabled(true);
        }

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            //notificationCountGetter();
            usersDB = database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            usersDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        User user = dataSnapshot.getValue(User.class);
                        SessionStorage.saveUser(MainActivity.this, user);
                        startActivity(new Intent(MainActivity.this, ExploreTabbedActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(MainActivity.this, EntryPage.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    startActivity(new Intent(MainActivity.this, get_started_first_page.class));
//                    startActivity(new Intent(MainActivity.this, EntryPage.class));
                    finish();
                }
            });
        } else {
            startActivity(new Intent(MainActivity.this, get_started_first_page.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionStorage.saveFilters(this, null);
    }
}
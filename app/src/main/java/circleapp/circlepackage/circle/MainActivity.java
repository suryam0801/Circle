package circleapp.circlepackage.circle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Login.EntryPage;
import circleapp.circlepackage.circle.ObjectModels.User;
import static circleapp.circlepackage.circle.App.CHANNEL_1_ID;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseDatabase database;
    private DatabaseReference usersDB, notificationDB;
    AnalyticsLogEvents analyticsLogEvents;
    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
        analyticsLogEvents = new AnalyticsLogEvents();
        notificationManager = NotificationManagerCompat.from(this);

        database = FirebaseDatabase.getInstance();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("PERSISTENCECHECK", Activity.MODE_PRIVATE);
        if (prefs.getBoolean(MainActivity.class.getCanonicalName(), true)) {
            prefs.edit().putBoolean(MainActivity.class.getCanonicalName(),false).apply();
            database.setPersistenceEnabled(true);
        }

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            notificationCountGetter();
            usersDB = database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            usersDB.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void notificationCountGetter(){
        int counter = 0;
        notificationDB = database.getReference().child("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        notificationDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationListener((int) dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void notificationListener(int count){
        final int[] c = {0};
        notificationDB = database.getReference().child("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        notificationDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                circleapp.circlepackage.circle.ObjectModels.Notification notification = dataSnapshot.getValue(circleapp.circlepackage.circle.ObjectModels.Notification.class);
                if(c[0] >= count){
                    sendOnChannel1(notification);
                } else {
                    c[0] = c[0] +1;
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                analyticsLogEvents.logCrash("Notification_error","Main_activity");
            }
        });
    }

    public void sendOnChannel1(circleapp.circlepackage.circle.ObjectModels.Notification notif) {
        String state = notif.getState();
        String name = "";
        String description = "";
        PendingIntent contentIntent = null;

        switch (state) {
            case "Accepted":
                name = "Application Accepted";
                description = "Accepted into " + notif.getCircleName();
                Intent intent_accept = new Intent(this, ExploreTabbedActivity.class);
                contentIntent = PendingIntent.getActivity(this, 0, intent_accept, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case "Rejected":
                name = "Application Rejected";
                description = "Denied from: " + notif.getCircleName();
                Intent intent_reject = new Intent(this, ExploreTabbedActivity.class);
                contentIntent = PendingIntent.getActivity(this, 0, intent_reject, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case "broadcast_added":
                name = "New BroadCast Added";
                description = "New broadcast in: " + notif.getCircleName();
                Intent intent_broadcast = new Intent(this, ExploreTabbedActivity.class);
                contentIntent = PendingIntent.getActivity(this, 0, intent_broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case "new_applicant":
                name = "New Applicant";
                description = "New Applicant in: " + notif.getCircleName();
                Intent intent_applicant = new Intent(this, ExploreTabbedActivity.class);
                contentIntent = PendingIntent.getActivity(this, 0, intent_applicant, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            default:
                break;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_1_ID);
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentTitle(name)
//                .setContentText(description)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(name)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .build();
        notificationManager.notify(1, notification);
        analyticsLogEvents.logEvents(MainActivity.this, "notification_sent", "success", "main_activity");
    }
}
package circleapp.circlepackage.circle;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
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
import circleapp.circlepackage.circle.Login.get_started_first_page;
import circleapp.circlepackage.circle.ObjectModels.User;

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
/*    public void notificationCountGetter(){
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
                name = "New Post Added in: " + notif.getCircleName();
                description = notif.getFrom()+": "+notif.getMessage();
                Intent intent_broadcast = new Intent(this, ExploreTabbedActivity.class);
                contentIntent = PendingIntent.getActivity(this, 0, intent_broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case "comment_added":
                name = "New Comment in: " + notif.getCircleName();
                description = notif.getFrom()+": "+notif.getMessage();
                Intent intent_comment = new Intent(this, ExploreTabbedActivity.class);
                contentIntent = PendingIntent.getActivity(this, 0, intent_comment, PendingIntent.FLAG_UPDATE_CURRENT);
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


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(name)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .build();
//        notificationManager.notify(1, notification);
    }*/
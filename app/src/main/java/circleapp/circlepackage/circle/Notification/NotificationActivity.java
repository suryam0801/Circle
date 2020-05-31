package circleapp.circlepackage.circle.Notification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import circleapp.circlepackage.circle.Explore.Explore;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.R;

public class NotificationActivity extends AppCompatActivity {

    private String TAG = NotificationActivity.class.getSimpleName();
    private ListView thisWeekListView, previousListView;
    private List<Notification> thisWeekNotifs, previousNotifs;
    private NotificationAdapter adapterThisWeek, adapterPrevious;
    private ImageButton back;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference notifyDb;
    private FirebaseAuth currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        thisWeekListView = findViewById(R.id.thisweek_notifications_display);
        previousListView = findViewById(R.id.all_time_notifications_display);
        back = findViewById(R.id.bck_notifications);
        thisWeekNotifs = new ArrayList<>();
        previousNotifs = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance();
        notifyDb = database.getReference("Notifications").child(currentUser.getCurrentUser().getUid());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotificationActivity.this, Explore.class));
                finish();
            }
        });

        loadNotifications();
    }

    private void loadNotifications() {
            notifyDb.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Notification notification=dataSnapshot.getValue(Notification.class);

                    String currentTimeStamp = getCurrentTimeStamp();
                    Scanner scan = new Scanner(currentTimeStamp);
                    scan.useDelimiter("-");
                    int currentDay = Integer.parseInt(scan.next());
                    int currentMonth = Integer.parseInt(scan.next());

                    String date = notification.getDate();
                    scan = new Scanner(date);
                    scan.useDelimiter("-");
                    int notificationDay = Integer.parseInt(scan.next());
                    int notificationMonth = Integer.parseInt(scan.next());

                    if(Math.abs(notificationDay - currentDay) > 6 || Math.abs(notificationMonth - currentMonth) >= 1)
                        previousNotifs.add(notification);
                    else
                        thisWeekNotifs.add(notification);

                    adapterThisWeek = new NotificationAdapter(getApplicationContext(), thisWeekNotifs);
                    adapterPrevious = new NotificationAdapter(getApplicationContext(), previousNotifs);

                    previousListView.setAdapter(adapterPrevious);
                    thisWeekListView.setAdapter(adapterThisWeek);

                    Utility.setListViewHeightBasedOnChildren(thisWeekListView);
                    Utility.setListViewHeightBasedOnChildren(previousListView);


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


    public static class Utility {

        public static void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                // pre-condition
                return;
            }

            int totalHeight = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();
        }
    }

    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}

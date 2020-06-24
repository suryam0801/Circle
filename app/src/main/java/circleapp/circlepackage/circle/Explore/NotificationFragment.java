package circleapp.circlepackage.circle.Explore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CircleWall.FullPageBroadcastCardView;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Explore.NotificationAdapter;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.R;

public class NotificationFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ListView thisWeekListView, previousListView;
    private List<Notification> thisWeekNotifs, previousNotifs;
//    private NotificationAdapter adapterThisWeek, adapterPrevious;
    private NotificationAdapter adapterThisWeek, adapterPrevious;
    private TextView prevnotify;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference notifyDb, circlesDB;
    private FirebaseAuth currentUser;
    AnalyticsLogEvents analyticsLogEvents;
    private Circle circle;

    public NotificationFragment() {
        // Required empty public constructor
    }


    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        analyticsLogEvents= new AnalyticsLogEvents();
        thisWeekListView = view.findViewById(R.id.thisweek_notifications_display);
        previousListView = view.findViewById(R.id.all_time_notifications_display);
        prevnotify = view.findViewById(R.id.prevnotifytext);
        thisWeekNotifs = new ArrayList<>();
        previousNotifs = new ArrayList<>();
        circle = new Circle();
        currentUser = FirebaseAuth.getInstance();
        notifyDb = database.getReference("Notifications").child(currentUser.getCurrentUser().getUid());
        circlesDB = database.getReference("Circles");

        loadNotifications();



        return view;
    }


    private void loadNotifications() {
        notifyDb.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Notification notification = dataSnapshot.getValue(Notification.class);

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

                if (Math.abs(notificationDay - currentDay) > 6 || Math.abs(notificationMonth - currentMonth) >= 1)
                    previousNotifs.add(0, notification);
                else
                    thisWeekNotifs.add(0, notification);

                if (previousNotifs.size() == 0) {
                    prevnotify.setVisibility(View.INVISIBLE);
                } else {
                    prevnotify.setVisibility(View.VISIBLE);
                }

                adapterThisWeek = new NotificationAdapter(getContext(),thisWeekNotifs);
                adapterPrevious = new NotificationAdapter(getContext(), previousNotifs);

                previousListView.setAdapter(adapterPrevious);
                thisWeekListView.setAdapter(adapterThisWeek);

                NotificationFragment.Utility.setListViewHeightBasedOnChildren(thisWeekListView);
                NotificationFragment.Utility.setListViewHeightBasedOnChildren(previousListView);

                thisWeekListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Notification curent = thisWeekNotifs.get(position);
                        Log.d("Notification Fragment","Notification list :: "+curent.toString());
                        String circleid = curent.getCircleId();
                        circlesDB.child(circleid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Circle circle = dataSnapshot.getValue(Circle.class);
                                if (circle != null)
                                {

                                    Log.d("Notification Fragment","Circle list :: "+circle.toString());
                                    if (circle.getMembersList().containsKey(currentUser.getCurrentUser().getUid())) {
                                        analyticsLogEvents.logEvents(getContext(), "notification_clicked_wall","to_circle_wall","notification");
                                        SessionStorage.saveCircle((Activity) getContext(), circle);
                                        Intent intent = new Intent(getContext(), CircleWall.class);
                                        intent.putExtra("broadcastPos", position);
                                        intent.putExtra("broadcastId", thisWeekNotifs.get(position).getBroadcastId());
                                        startActivity(intent);
                                        ((Activity) getContext()).finish();
                                    } else {
                                        analyticsLogEvents.logEvents(getContext(), "notification_clicked_invalid_user","not_part_of_circle","notification");
                                        Toast.makeText(getContext(), "Not a member of this circle anymore", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    analyticsLogEvents.logEvents(getContext(), "notification_clicked_invalid_user","not_part_of_circle","notification");
                                    Toast.makeText(getContext(), "The Circle has been deleted by Creator", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });

                previousListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Notification curent = thisWeekNotifs.get(position);
                        String circleid = curent.getCircleId();
                        circlesDB.child(circleid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Circle circle = dataSnapshot.getValue(Circle.class);
                                String state = curent.getState();
                                if (state.equalsIgnoreCase("Accepted") || state.equalsIgnoreCase("Rejected")) {
                                    SessionStorage.saveCircle((Activity) getContext(), circle);
                                    startActivity(new Intent(getContext(), CircleWall.class));
                                    ((Activity) getContext()).finish();
                                } else if (state.equalsIgnoreCase("broadcast_added")) {
                                    SessionStorage.saveCircle((Activity) getContext(), circle);
                                    startActivity(new Intent(getContext(), CircleWall.class));
                                    ((Activity) getContext()).finish();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });

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


    public static String getCurrentTimeStamp() {
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

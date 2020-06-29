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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

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
import circleapp.circlepackage.circle.Helpers.FirebaseUtils;

import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.NotifyUIObject;
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
        thisWeekListView = view.findViewById(R.id.thisweek_notifications_display);
        previousListView = view.findViewById(R.id.all_time_notifications_display);
        prevnotify = view.findViewById(R.id.prevnotifytext);
        thisWeekNotifs = new ArrayList<>();
        previousNotifs = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance();
        notifyDb = database.getReference("Notifications").child(currentUser.getCurrentUser().getUid());
        circlesDB = database.getReference("Circles");
        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        String type = "notify";
        NotifyUIObject notifyUIObject = new NotifyUIObject(type,getContext(),notifyDb,prevnotify,previousNotifs,thisWeekNotifs,adapterPrevious,adapterThisWeek,previousListView,thisWeekListView);
        FirebaseUtils.FBUtils(notifyUIObject);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

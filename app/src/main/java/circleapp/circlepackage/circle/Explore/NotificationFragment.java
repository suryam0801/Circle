package circleapp.circlepackage.circle.Explore;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


import circleapp.circlepackage.circle.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.User;
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
    private User user;
    ProgressDialog progressDialog;

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
        user = SessionStorage.getUser(getActivity());
        thisWeekNotifs = new ArrayList<>();
        previousNotifs = new ArrayList<>();
        String type = "notify";
        long startTime = System.currentTimeMillis();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsNotificationsLiveData(user.getUserId());

        liveData.observe(this, dataSnapshot -> {
            if (dataSnapshot != null) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    setNotifsView(notification);
                    Log.d("wflkn", (startTime - System.currentTimeMillis())+"");
            }
        });
progressDialog.dismiss();
        return view;
    }

    private void setNotifsView(Notification notification){
        long startTime = System.currentTimeMillis();
        Log.d("Notifi-delay","delay1");
//        HelperMethods.OrderNotification(getContext(),prevnotify,notification,previousNotifs,thisWeekNotifs,adapterPrevious,adapterThisWeek,previousListView,thisWeekListView);
        String currentTimeStamp = HelperMethods.getCurrentTimeStamp();
        Log.d("Notifi-delay","delay2");
        Scanner scan = new Scanner(currentTimeStamp);
        scan.useDelimiter("-");
        int currentDay = Integer.parseInt(scan.next());
        int currentMonth = Integer.parseInt(scan.next());

        String date = notification.getDate();
        scan = new Scanner(date);
        scan.useDelimiter("-");
        int notificationDay = Integer.parseInt(scan.next());
        int notificationMonth = Integer.parseInt(scan.next());
        Log.d("Notifi-delay","delay3");
        if (Math.abs(notificationDay - currentDay) > 6 || Math.abs(notificationMonth - currentMonth) >= 1)
            previousNotifs.add(0, notification);
        else
            thisWeekNotifs.add(0, notification);

        if (previousNotifs.size() == 0) {
            prevnotify.setVisibility(View.INVISIBLE);
        } else {
            prevnotify.setVisibility(View.VISIBLE);
        }
        Log.d("Notifi-delay","delay4");
        adapterThisWeek = new NotificationAdapter(getContext(), thisWeekNotifs);
        adapterPrevious = new NotificationAdapter(getContext()
                , previousNotifs);

        previousListView.setAdapter(adapterPrevious);
        thisWeekListView.setAdapter(adapterThisWeek);
        Log.d("Notifi-delay","delay5");
        HelperMethods.setListViewHeightBasedOnChildren(thisWeekListView);
        HelperMethods.setListViewHeightBasedOnChildren(previousListView);
        Log.d("Notifi-delay","delay6");

        thisWeekListView.setOnItemClickListener((parent, view1, position, id) -> {
            Notification curent = thisWeekNotifs.get(position);
            HelperMethods.NotifyOnclickListener(getContext(),curent,position,thisWeekNotifs.get(position).getBroadcastId());
        });

        previousListView.setOnItemClickListener((parent, view1, position, id) -> {
            Notification curent = previousNotifs.get(position);
            HelperMethods.NotifyOnclickListener(getContext(),curent,position,previousNotifs.get(position).getBroadcastId());
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
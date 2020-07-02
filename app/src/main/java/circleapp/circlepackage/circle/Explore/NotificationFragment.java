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
            if (dataSnapshot != null){

                setNotifsView(dataSnapshot);
            }
        });
progressDialog.dismiss();
        return view;
    }

    private void setNotifsView(DataSnapshot snapshot){
        long startTime = System.currentTimeMillis();
        for (DataSnapshot ds : snapshot.getChildren()) {
            Notification notification = ds.getValue(Notification.class);
        Log.d("Notifi-delay",notification.toString());
        HelperMethods.OrderNotification(getContext(),prevnotify,notification,previousNotifs,thisWeekNotifs,adapterPrevious,adapterThisWeek,previousListView,thisWeekListView);
    }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
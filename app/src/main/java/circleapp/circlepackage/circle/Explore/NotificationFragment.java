package circleapp.circlepackage.circle.Explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
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
    private List<Notification> thisWeekNotifs, previousNotifs, notifs = new ArrayList<Notification>();;
    //    private NotificationAdapter adapterThisWeek, adapterPrevious;
    private NotificationAdapter adapterThisWeek, adapterPrevious;
    private TextView prevnotify;
    private User user;

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

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<String[]> liveData = viewModel.getDataSnapsNotificationsLiveData(user.getUserId());

        liveData.observe(this, returnArray -> {
            Notification notification = new Gson().fromJson(returnArray[0], Notification.class);
            //setNotifsView(notification);
            if(!notifs.contains(notification))
                notifs.add(notification);
            setNotifsView(notifs);
        });
        return view;
    }

    private void setNotifsView(List<Notification> notifs){
        HelperMethods.OrderNotification(getContext(),prevnotify,notifs,previousNotifs,thisWeekNotifs,adapterPrevious,adapterThisWeek,previousListView,thisWeekListView);

        HelperMethods.setListViewHeightBasedOnChildren(thisWeekListView);
        HelperMethods.setListViewHeightBasedOnChildren(previousListView);

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
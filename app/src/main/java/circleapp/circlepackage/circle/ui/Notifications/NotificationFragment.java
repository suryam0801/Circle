package circleapp.circlepackage.circle.ui.Notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Notification;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.NotificationsViewModel;

public class NotificationFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView thisWeekListView, previousListView;
    private List<Notification> thisWeekNotifs, previousNotifs;
    private NotificationAdapter adapterThisWeek, adapterPrevious;
    private TextView prevnotify;
    private User user;
    private LiveData<String[]> liveData;
    private GlobalVariables globalVariables = new GlobalVariables();

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
        InitUIElements(view);
        setNotifObserver();
        return view;
    }

    private void InitUIElements(View view) {
        thisWeekListView = view.findViewById(R.id.thisweek_notifications_display);
        previousListView = view.findViewById(R.id.all_time_notifications_display);
        prevnotify = view.findViewById(R.id.prevnotifytext);
        user = globalVariables.getCurrentUser();
        thisWeekNotifs = new ArrayList<>();
        previousNotifs = new ArrayList<>();

    }

    private void setNotifObserver(){
        NotificationsViewModel viewModel = ViewModelProviders.of(this).get(NotificationsViewModel.class);
        liveData = viewModel.getDataSnapsNotificationsLiveData(user.getUserId());
        liveData.observe(this, returnArray -> {
            Notification notification = new Gson().fromJson(returnArray[0], Notification.class);
            if(notification!=null)
                setNotifsView(notification);
        });
    }

    private void setNotifsView(Notification notification) {
        HelperMethodsUI.OrderNotification(getContext(), prevnotify, notification, previousNotifs, thisWeekNotifs, adapterPrevious, adapterThisWeek, previousListView, thisWeekListView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onPause() {
        liveData.removeObservers(this);
        super.onPause();
    }
}
package circleapp.circlepackage.circle.Helpers;

import android.content.Context;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

import circleapp.circlepackage.circle.Explore.NotificationAdapter;
import circleapp.circlepackage.circle.ObjectModels.Notification;

public class UiHelper {

    public static void NotifyUIFragment(Context context, Notification notification,TextView prevnotify, List<Notification> previousNotifs, List<Notification> thisWeekNotifs, NotificationAdapter adapterPrevious, NotificationAdapter adapterThisWeek, ListView previousListView, ListView thisWeekListView)
    {

        HelperMethods.OrderNotification(context,prevnotify,notification,previousNotifs,thisWeekNotifs,adapterPrevious,adapterThisWeek,previousListView,thisWeekListView);

        HelperMethods.setListViewHeightBasedOnChildren(thisWeekListView);
        HelperMethods.setListViewHeightBasedOnChildren(previousListView);

        thisWeekListView.setOnItemClickListener((parent, view, position, id) -> {
            Notification curent = thisWeekNotifs.get(position);
            HelperMethods.NotifyOnclickListener(context,curent,position,thisWeekNotifs.get(position).getBroadcastId());
        });

        previousListView.setOnItemClickListener((parent, view, position, id) -> {
            Notification curent = previousNotifs.get(position);
            HelperMethods.NotifyOnclickListener(context,curent,position,previousNotifs.get(position).getBroadcastId());
        });

    }
}
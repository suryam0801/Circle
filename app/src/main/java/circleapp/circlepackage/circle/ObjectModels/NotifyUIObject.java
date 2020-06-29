package circleapp.circlepackage.circle.ObjectModels;

import android.content.Context;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

import circleapp.circlepackage.circle.Explore.NotificationAdapter;

public class NotifyUIObject {
    String type;
    Context context;
    DatabaseReference notifyDb;
    TextView prevnotify;
    List<Notification> previousNotifs, thisWeekNotifs;
    NotificationAdapter adapterPrevious;
    circleapp.circlepackage.circle.Explore.NotificationAdapter adapterThisWeek;
    ListView previousListView, thisWeekListView;

    public NotifyUIObject(String type, Context context, DatabaseReference notifyDb, TextView prevnotify, List<Notification> previousNotifs, List<Notification> thisWeekNotifs, NotificationAdapter adapterPrevious, NotificationAdapter adapterThisWeek, ListView previousListView, ListView thisWeekListView) {
        this.type = type;
        this.context = context;
        this.notifyDb = notifyDb;
        this.prevnotify = prevnotify;
        this.previousNotifs = previousNotifs;
        this.thisWeekNotifs = thisWeekNotifs;
        this.adapterPrevious = adapterPrevious;
        this.adapterThisWeek = adapterThisWeek;
        this.previousListView = previousListView;
        this.thisWeekListView = thisWeekListView;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public DatabaseReference getNotifyDb() {
        return notifyDb;
    }

    public void setNotifyDb(DatabaseReference notifyDb) {
        this.notifyDb = notifyDb;
    }

    public TextView getPrevnotify() {
        return prevnotify;
    }

    public void setPrevnotify(TextView prevnotify) {
        this.prevnotify = prevnotify;
    }

    public List<Notification> getPreviousNotifs() {
        return previousNotifs;
    }

    public void setPreviousNotifs(List<Notification> previousNotifs) {
        this.previousNotifs = previousNotifs;
    }

    public List<Notification> getThisWeekNotifs() {
        return thisWeekNotifs;
    }

    public void setThisWeekNotifs(List<Notification> thisWeekNotifs) {
        this.thisWeekNotifs = thisWeekNotifs;
    }

    public NotificationAdapter getAdapterPrevious() {
        return adapterPrevious;
    }

    public void setAdapterPrevious(NotificationAdapter adapterPrevious) {
        this.adapterPrevious = adapterPrevious;
    }

    public NotificationAdapter getAdapterThisWeek() {
        return adapterThisWeek;
    }

    public void setAdapterThisWeek(NotificationAdapter adapterThisWeek) {
        this.adapterThisWeek = adapterThisWeek;
    }

    public ListView getPreviousListView() {
        return previousListView;
    }

    public void setPreviousListView(ListView previousListView) {
        this.previousListView = previousListView;
    }

    public ListView getThisWeekListView() {
        return thisWeekListView;
    }

    public void setThisWeekListView(ListView thisWeekListView) {
        this.thisWeekListView = thisWeekListView;
    }
}

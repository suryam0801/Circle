package circleapp.circleapppackage.circle.ui.Notifications;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Notification;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.NotificationsViewModel;
import circleapp.circleapppackage.circle.ui.ExploreTabbedActivity;

public class Notifications extends AppCompatActivity {

    private RecyclerView thisWeekListView;
    private List<Notification> thisWeekNotifs;
    private ImageButton backBtn;
    private NotificationAdapter adapterThisWeek;
    private User user;
    private LiveData<String[]> liveData;
    private GlobalVariables globalVariables = new GlobalVariables();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        thisWeekListView = findViewById(R.id.thisweek_notifications_display);
        backBtn = findViewById(R.id.bck_notifications);

        user = globalVariables.getCurrentUser();
        thisWeekNotifs = new ArrayList<>();

        backBtn.setOnClickListener(v->{
            onBackPressed();
        });

        setNotifObserver();
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
        HelperMethodsUI.OrderNotification(this, notification, thisWeekNotifs, adapterThisWeek, thisWeekListView);
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed(){
        startActivity(new Intent(this, ExploreTabbedActivity.class));
        finishAfterTransition();
    }
}
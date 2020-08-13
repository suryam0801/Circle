package circleapp.circlepackage.circle.DataLayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Notification;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView.CircleWall;

public class NotificationRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void writeCommentNotifications(Context context, Notification notification, HashMap<String, Boolean> listenersList, String message, String title) {
        Set<String> member;
        UserViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
        if (listenersList != null) {
            listenersList.remove(globalVariables.getCurrentUser().getUserId());
            member = listenersList.keySet();
            if(member!=null)
            for (String i : member)
            {
                LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(i);
                liveData.observe((LifecycleOwner) context, dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if(user!=null){
                            liveData.removeObservers((LifecycleOwner) context);
                            String tokenId = user.getToken_id();
                            String state = "comment";
                            Log.d("commentnotif",i);
                            if(message!=null)
                            HelperMethodsBL.pushFCM(state, null,tokenId,notification,null, message,title, null,null,null, notification.getCircleId());
                            globalVariables.getFBDatabase().getReference("/Notifications").child(i).child(notification.getNotificationId()).setValue(notification);
                        }
                    } else {
                    }
                });
            }

        }

    }


    public void writeBroadcastNotifications(Context context, Notification notification, HashMap<String, Boolean> membersList, Broadcast broadcast) {

        Set<String> member;
        UserViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
        String apiurl = "https://circle-d8cc7.web.app/api/";
        if (membersList != null) {
            member = membersList.keySet();
            for (String i : member)

            {
                LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(i);
                Log.d("Push NR user",i);
                liveData.observe((LifecycleOwner) context, dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if(user!=null){
                            liveData.removeObservers((LifecycleOwner) context);
                            String tokenId = user.getToken_id();
                            String state = "broadcast";
                            if(broadcast!=null)
                            HelperMethodsBL.pushFCM(state, null, tokenId,notification,broadcast, null, null,null,null,null, notification.getCircleId());
                            globalVariables.getFBDatabase().getReference("/Notifications").child(i).child(notification.getNotificationId()).setValue(notification);
                        }
                    } else {
                    }
                });
            }


        }
    }

    public void writeNormalNotifications(Notification notification, String token_id, String name) {
        String state = "applicant";
        String application_state = notification.getState();
        HelperMethodsBL.pushFCM(state,application_state, token_id,notification,null, null, name,null, null,null, notification.getCircleId());
        globalVariables.getFBDatabase().getReference("/Notifications").child(notification.getNotify_to()).child(notification.getNotificationId()).setValue(notification);
    }

    public String getNotificationId(String objectId) {
        String notificationId = globalVariables.getFBDatabase().getReference("/Notifications").child(objectId).push().getKey();
        return notificationId;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void NotifyOnclickListener(Context context, Notification curent, int position, String broadcastId) {
        MyCirclesViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(MyCirclesViewModel.class);
        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsParticularCircleLiveData(curent.getCircleId());
        liveData.observe((LifecycleOwner) context, dataSnapshot -> {
            Circle circle = dataSnapshot.getValue(Circle.class);
            if (circle != null) {
                liveData.removeObservers((LifecycleOwner) context);
                if(circle.getMembersList()==null)
                    Toast.makeText(context, "Not a member of this circle anymore", Toast.LENGTH_SHORT).show();
                else if (circle.getMembersList().containsKey(globalVariables.getCurrentUser().getUserId())) {
                    globalVariables.saveCurrentCircle(circle);
                    Intent intent = new Intent(context, CircleWall.class);
                    intent.putExtra("broadcastPos", position);
                    intent.putExtra("broadcastId", broadcastId);
                    context.startActivity(intent);
                    ((Activity) context).finishAfterTransition();
                }
                else {
                    Toast.makeText(context, "Not a member of this circle anymore", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "The Circle has been deleted by Creator", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

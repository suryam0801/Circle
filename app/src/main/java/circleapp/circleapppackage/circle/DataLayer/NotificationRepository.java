package circleapp.circleapppackage.circle.DataLayer;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsBL;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Notification;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;

public class NotificationRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void writeCommentNotifications(Context context, Notification notification, HashMap<String, Boolean> listenersList, String message, String title, List<Subscriber> listOfCirclePersonel) {
        Set<String> member;
        Set<String> tokenIds =new HashSet<>();
        if (listenersList != null) {
            listenersList.remove(globalVariables.getCurrentUser().getUserId());
            member = listenersList.keySet();
            if(member!=null){
                for (String i : member)
                {
                    for (Subscriber s : listOfCirclePersonel){
                        if(s!=null){
                            String tokenId = s.getToken_id();
                            if(!tokenIds.contains(tokenId)){
                                tokenIds.add(tokenId);
                                String state = "comment";
                                HelperMethodsBL.pushFCM(state, null,tokenId,notification,null, message,title, null,null,null, notification.getCircleId());
                                globalVariables.getFBDatabase().getReference("/Notifications").child(i).child(notification.getNotificationId()).setValue(notification);
                            }
                        }
                    }
                }
            }
        }

    }


    public void writeBroadcastNotifications(Context context, Notification notification, HashMap<String, Boolean> membersList, Broadcast broadcast, List<Subscriber> listOfCirclePersonel) {

        Set<String> tokenIds =new HashSet<>();
        Set<String> member;
        UserViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
        String apiurl = "https://circle-d8cc7.web.app/api/";
        if (membersList != null) {
            member = membersList.keySet();
            for (String i : member)

            {
                if(listOfCirclePersonel!=null)
                for(Subscriber s : listOfCirclePersonel){
                    if(s!=null){
                        String tokenId = s.getToken_id();
                        if(!tokenIds.contains(tokenId)){
                            tokenIds.add(tokenId);
                            String state = "broadcast";
                            if(broadcast!=null)
                                HelperMethodsBL.pushFCM(state, null, tokenId,notification,broadcast, null, null,null,null,null, notification.getCircleId());
                            globalVariables.getFBDatabase().getReference("/Notifications").child(i).child(notification.getNotificationId()).setValue(notification);
                        }
                    }
                }
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

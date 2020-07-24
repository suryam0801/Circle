package circleapp.circlepackage.circle.ViewModels.CircleWall;

import android.app.Activity;
import android.net.Uri;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.LocalObjectModels.Poll;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class CircleWallViewModel extends ViewModel {
    private MutableLiveData<Boolean> creationState;

    public MutableLiveData<Boolean> createBroadcast(String title, String description, Circle circle, User user, Activity activity){
        creationState = new MutableLiveData<>();
        String currentCircleId = circle.getId();
        String broadcastId = FirebaseWriteHelper.getBroadcastId(currentCircleId);
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();

        Broadcast normalBroadcast;
        normalBroadcast = new Broadcast(broadcastId, title, description, null,
                currentUserName, circle.getMembersList(), currentUserId, false, false, System.currentTimeMillis(), null,
                user.getProfileImageLink(), 0, 0,true);
        SendNotification.sendBCinfo(activity,normalBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(), title);

        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        SessionStorage.saveCircle(activity, circle);
        //updating broadcast in broadcast db
        FirebaseWriteHelper.writeBroadcast(activity, circle.getId(), normalBroadcast, newCount);

        creationState.setValue(true);
        return creationState;
    }
    public MutableLiveData<Boolean> createPhotoBroadcast(String title, Uri downloadLink, Circle circle, User user, boolean imageExists, Activity activity) {
        creationState = new MutableLiveData<>();
        String currentCircleId = circle.getId();
        String broadcastId = FirebaseWriteHelper.getBroadcastId(currentCircleId);
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();
        Broadcast photoBroadcast = new Broadcast();
        if (imageExists) {
            photoBroadcast = new Broadcast(broadcastId, title, null, downloadLink.toString(), currentUserName, circle.getMembersList(), currentUserId, false, true,
                    System.currentTimeMillis(), null, user.getProfileImageLink(), 0, 0,true);
        }


        SendNotification.sendBCinfo(activity, photoBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(),title);
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        SessionStorage.saveCircle(activity, circle);

        //updating broadcast in broadcast db
        FirebaseWriteHelper.writeBroadcast(activity, circle.getId(), photoBroadcast, newCount);
        creationState.setValue(true);
        return creationState;
    }

    public MutableLiveData<Boolean> createPollBroadcast(String pollQuestion, HashMap<String, Integer> options, boolean pollExists, boolean imageExists, Uri downloadLink, Circle circle, User user, Activity activity){
        creationState = new MutableLiveData<>();

        String currentCircleId = circle.getId();
        String broadcastId = FirebaseWriteHelper.getBroadcastId(currentCircleId);
        Broadcast pollBroadcast = new Broadcast();
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();
        if (pollExists) {

            Poll poll = new Poll(pollQuestion, options, null);
            if (imageExists) {
                pollBroadcast = new Broadcast(broadcastId, null, null, downloadLink.toString(), currentUserName, circle.getMembersList(), currentUserId, true, true,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0,true);
            } else
                pollBroadcast = new Broadcast(broadcastId, null, null, null, currentUserName, circle.getMembersList(), currentUserId, true, false,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0,true);
        }
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        SessionStorage.saveCircle(activity, circle);
        SendNotification.sendBCinfo(activity, pollBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(), pollQuestion);

        //updating broadcast in broadcast db
        FirebaseWriteHelper.writeBroadcast(activity, circle.getId(), pollBroadcast, newCount);
        creationState.setValue(true);
        return creationState;
    }
}

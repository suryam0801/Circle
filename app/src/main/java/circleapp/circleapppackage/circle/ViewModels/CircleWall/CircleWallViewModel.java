package circleapp.circleapppackage.circle.ViewModels.CircleWall;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circleapppackage.circle.DataLayer.BroadcastsRepository;
import circleapp.circleapppackage.circle.Helpers.SendNotification;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Poll;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class CircleWallViewModel extends ViewModel {
    private MutableLiveData<Boolean> creationState;
    private GlobalVariables globalVariables;
    private BroadcastsRepository broadcastsRepository = new BroadcastsRepository();

    public MutableLiveData<Boolean> createBroadcast(String title, String description, Circle circle, User user, Activity activity, List<Subscriber> listOfCirclePersonel){
        creationState = new MutableLiveData<>();
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsRepository.getBroadcastId(currentCircleId);
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();
        globalVariables  = new GlobalVariables();
        HashMap<String, String > circleMembersList = new HashMap<>();
        HashMap<String, Boolean> listenersList = new HashMap<>();
        if(circle.getMembersList()!=null){
            circleMembersList = circle.getMembersList();
            String key;
            for(Map.Entry<String, String > entry : circleMembersList.entrySet()){
                key = entry.getKey();
                listenersList.put(key,true);
            }
        }
        Broadcast normalBroadcast;
        normalBroadcast = new Broadcast(broadcastId, title, description, null,
                currentUserName, listenersList, currentUserId, false, false,false, System.currentTimeMillis(), null,
                user.getProfileImageLink(), System.currentTimeMillis(),true,1);
        Log.d("Push viewModel",user.getToken_id());
        SendNotification.sendBCinfo(activity,normalBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, listenersList, circle.getBackgroundImageLink(), title, listOfCirclePersonel);

        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
//        SessionStorage.saveCircle(activity, circle);
        globalVariables.saveCurrentCircle(circle);

        //updating broadcast in broadcast db
        broadcastsRepository.writeBroadcast(circle, normalBroadcast, newCount);

        creationState.setValue(true);
        return creationState;
    }
    public MutableLiveData<Boolean> createPhotoBroadcast(String title, Uri downloadLink, Circle circle, User user, boolean imageExists,boolean isFile, Activity activity, List<Subscriber> listOfCirclePersonel) {
        creationState = new MutableLiveData<>();
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsRepository.getBroadcastId(currentCircleId);
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();
        Broadcast photoBroadcast = new Broadcast();
        globalVariables  = new GlobalVariables();
        HashMap<String, String > circleMembersList = new HashMap<>();
        HashMap<String, Boolean> listenersList = new HashMap<>();
        if(circle.getMembersList()!=null){
            circleMembersList = circle.getMembersList();
            String key;
            for(Map.Entry<String, String > entry : circleMembersList.entrySet()){
                key = entry.getKey();
                listenersList.put(key,true);
            }
        }
        if (imageExists) {
            photoBroadcast = new Broadcast(broadcastId, title, null, downloadLink.toString(), currentUserName, listenersList, currentUserId, false, true,isFile,
                    System.currentTimeMillis(), null, user.getProfileImageLink(), System.currentTimeMillis(),true,1);
        }
        else
            photoBroadcast = new Broadcast(broadcastId, title, null, downloadLink.toString(), currentUserName, listenersList, currentUserId, false, false,isFile,
                    System.currentTimeMillis(), null, user.getProfileImageLink(), System.currentTimeMillis(),true,1);


        SendNotification.sendBCinfo(activity, photoBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, listenersList, circle.getBackgroundImageLink(),title, listOfCirclePersonel);
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
//        SessionStorage.saveCircle(activity, circle);
        globalVariables.saveCurrentCircle(circle);

        //updating broadcast in broadcast db
        broadcastsRepository.writeBroadcast(circle, photoBroadcast, newCount);
        creationState.setValue(true);
        return creationState;
    }

    public MutableLiveData<Boolean> createPollBroadcast(String pollQuestion, HashMap<String, Integer> options, boolean pollExists, boolean imageExists,boolean isFile, Uri downloadLink, Circle circle, User user, Activity activity, List<Subscriber> listOfCirclePersonel){
        creationState = new MutableLiveData<>();
        globalVariables  = new GlobalVariables();
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsRepository.getBroadcastId(currentCircleId);
        Broadcast pollBroadcast = new Broadcast();
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();
        HashMap<String, String > circleMembersList = new HashMap<>();
        HashMap<String, Boolean> listenersList = new HashMap<>();
        if(circle.getMembersList()!=null){
            circleMembersList = circle.getMembersList();
            String key;
            for(Map.Entry<String, String > entry : circleMembersList.entrySet()){
                key = entry.getKey();
                listenersList.put(key,true);
            }
        }
        if (pollExists) {

            Poll poll = new Poll(pollQuestion, options, null);
            if (imageExists) {
                pollBroadcast = new Broadcast(broadcastId, null, null, downloadLink.toString(), currentUserName, listenersList, currentUserId, true, true,false,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(),  System.currentTimeMillis(),true,1);
            }
            if(isFile)
                pollBroadcast = new Broadcast(broadcastId, null, null, null, currentUserName, listenersList, currentUserId, true, false,true,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(),  System.currentTimeMillis(),true,1);
        }
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
//        SessionStorage.saveCircle(activity, circle);
        globalVariables.saveCurrentCircle(circle);
        SendNotification.sendBCinfo(activity, pollBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, listenersList, circle.getBackgroundImageLink(), pollQuestion, listOfCirclePersonel);

        //updating broadcast in broadcast db
        broadcastsRepository.writeBroadcast(circle, pollBroadcast, newCount);
        creationState.setValue(true);
        return creationState;
    }
}
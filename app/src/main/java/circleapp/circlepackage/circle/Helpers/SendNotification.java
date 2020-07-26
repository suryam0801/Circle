package circleapp.circlepackage.circle.Helpers;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;

import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;

import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.Notification;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;


public class SendNotification {
    public static void sendCommentInfo(Context mContext, String userID, String broadcastId, String circleName, String circleId, String creatorName, HashMap<String, Boolean> listenersList, String circleIcon, String message, String title) {

        String notificationId = FirebaseWriteHelper.getNotificationId(broadcastId);
        message = message.substring(0, Math.min(message.length(), 60));
        if (message.length() >= 60)
            message = message + "...";
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        String getDate = getCurrentDateStamp();
        Notification notif = new Notification(circleName,userID,circleId,notificationId,creatorName,null,"comment_added",System.currentTimeMillis(),getDate,broadcastId,circleIcon,null,message);
        FirebaseWriteHelper.writeCommentNotifications(mContext,notif, listenersList,message,title);
    }

    public static void sendBCinfo(Context context, Broadcast broadcast, String userId, String broadcastId, String circleName, String circleId, String creatorName, HashMap<String, Boolean> membersList, String circleIcon, String message) {

        String notificationId = FirebaseWriteHelper.getNotificationId(broadcastId);
        message = message.substring(0, Math.min(message.length(), 60));
        if (message.length() >= 60)
            message = message + "...";
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        String getDate = getCurrentDateStamp();

        Notification notif = new Notification(circleName,userId,circleId,notificationId,creatorName,null,"broadcast_added",System.currentTimeMillis(),getDate,broadcastId,circleIcon,null,message);
        FirebaseWriteHelper.writeBroadcastNotifications(context,notif, membersList,broadcast);

    }


    public static void sendnotification(String state, String circleId, String circleName, String toUserId, String token_id, String name) {
//        This is the function to store the

        String notificationId = FirebaseWriteHelper.getNotificationId(toUserId);
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        String from = toUserId;
        String getDate = getCurrentDateStamp();
        Notification notif = new Notification(circleName,null,circleId,notificationId,from,toUserId,state,System.currentTimeMillis(),getDate,null,null,null,null);
        FirebaseWriteHelper.writeNormalNotifications(notif,token_id,name);
    }

    public static String getCurrentDateStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
    public static void sendApplication(String state, User user, Circle circle, Subscriber subscriber){
        HelperMethodsBL.pushFCM(state, state,null,null,null,null,null,subscriber.getName(),user.getToken_id(),circle.getName());

    }

}

package circleapp.circlepackage.circle.Helpers;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;

import circleapp.circlepackage.circle.ObjectModels.Broadcast;

import circleapp.circlepackage.circle.ObjectModels.Notification;


public class SendNotification {
    public static void sendCommentInfo(String userID, String broadcastId, String circleName, String circleId, String creatorName, HashMap<String, Boolean> listenersList, String circleIcon, String message) {

        String notificationId = FirebaseWriteHelper.getNotificationId(broadcastId);
        message = message.substring(0, Math.min(message.length(), 60));
        if (message.length() >= 60)
            message = message + "...";
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        String getDate = getCurrentDateStamp();
        Notification notif = new Notification(circleName,userID,circleId,notificationId,creatorName,null,"comment_added",System.currentTimeMillis(),getDate,broadcastId,circleIcon,null,message);
        FirebaseWriteHelper.writeCommentNotifications(notif, listenersList);
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


    public static void sendnotification(String state, String circleId, String circleName, String toUserId) {
//        This is the function to store the

        String notificationId = FirebaseWriteHelper.getNotificationId(toUserId);
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        String from = toUserId;
        String getDate = getCurrentDateStamp();
        Notification notif = new Notification(circleName,null,circleId,notificationId,from,toUserId,state,System.currentTimeMillis(),getDate,null,null,null,null);
        FirebaseWriteHelper.writeNormalNotifications(notif);
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


}

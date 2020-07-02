package circleapp.circlepackage.circle.Helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;

public class SendNotification {
    private static String TAG = SendNotification.class.getSimpleName();
    public static void sendCommentInfo(String userID, String broadcastId, String circleName,String circleId, String creatorName, HashMap<String, Boolean> listenersList,String circleIcon, String message){

        String notificationId = HelperMethods.uuidGet();
        message = message.substring(0, Math.min(message.length(), 60));
        if(message.length()>=60)
            message = message +"...";
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        Map<String, Object> applicationStatus = new HashMap<>();
        String from = userID;
        String getDate = getCurrentDateStamp();
        applicationStatus.put("state", "comment_added");
        applicationStatus.put("circleName", circleName);
        applicationStatus.put("circleId", circleId);
        applicationStatus.put("circleIcon", circleIcon);
        applicationStatus.put("broadcastId", broadcastId);
        applicationStatus.put("from", creatorName);
        applicationStatus.put("creatorId", userID);
        applicationStatus.put("notificationId", notificationId);
        applicationStatus.put("date", getDate);
        applicationStatus.put("timestamp", System.currentTimeMillis());
        applicationStatus.put("message", message);
        FirebaseWriteHelper.writeCommentNotifications(notificationId,userID,applicationStatus,listenersList);

    }

    public static void sendBCinfo(String userId, String broadcastId, String circleName,String circleId, String creatorName, HashMap<String, Boolean> membersList,String circleIcon, String message)
    {

        String notificationId = HelperMethods.uuidGet();
        message = message.substring(0, Math.min(message.length(), 60));
        if(message.length()>=60)
            message = message +"...";
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        Map<String, Object> applicationStatus = new HashMap<>();
        String from = userId;
        String getDate = getCurrentDateStamp();
        applicationStatus.put("state", "broadcast_added");
        applicationStatus.put("circleName", circleName);
        applicationStatus.put("circleId", circleId);
        applicationStatus.put("circleIcon", circleIcon);
        applicationStatus.put("broadcastId", broadcastId);
        applicationStatus.put("from", creatorName);
        applicationStatus.put("creatorId", userId);
        applicationStatus.put("notificationId", notificationId);
        applicationStatus.put("date", getDate);
        applicationStatus.put("timestamp", System.currentTimeMillis());
        applicationStatus.put("message",message);
        FirebaseWriteHelper.writeBroadcastNotifications(notificationId,userId,applicationStatus,membersList);
    }
    public static void sendnotification(String state, String circleId, String circleName, String toUserId) {
//        This is the function to store the

        String notificationId = HelperMethods.uuidGet();
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        Map<String, Object> applicationStatus = new HashMap<>();
        applicationStatus.put("state", state);
        applicationStatus.put("circleId", circleId);
        String from = toUserId;
        String getDate = getCurrentDateStamp();
        applicationStatus.put("from", from);
        applicationStatus.put("notify_to", toUserId);
        applicationStatus.put("circleName", circleName);
        applicationStatus.put("notificationId", notificationId);
        applicationStatus.put("date", getDate);
        applicationStatus.put("timestamp", System.currentTimeMillis());
        FirebaseWriteHelper.writeNormalNotifications(toUserId,notificationId,applicationStatus);
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

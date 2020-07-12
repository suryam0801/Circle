package circleapp.circlepackage.circle.Helpers;

import android.content.Context;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SendNotification {
    public static void sendCommentInfo(String userID, String broadcastId, String circleName, String circleId, String creatorName, HashMap<String, Boolean> listenersList, String circleIcon, String message) {

        String notificationId = FirebaseWriteHelper.getNotificationId(broadcastId);
        message = message.substring(0, Math.min(message.length(), 60));
        if (message.length() >= 60)
            message = message + "...";
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
        FirebaseWriteHelper.writeCommentNotifications(notificationId, userID, applicationStatus, listenersList);
    }

    public static void sendBCinfo(Context context, String userId, String broadcastId, String circleName, String circleId, String creatorName, HashMap<String, Boolean> membersList, String circleIcon, String message) {

        String notificationId = FirebaseWriteHelper.getNotificationId(broadcastId);
        message = message.substring(0, Math.min(message.length(), 60));
        if (message.length() >= 60)
            message = message + "...";
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
        applicationStatus.put("message", message);
        FirebaseWriteHelper.writeBroadcastNotifications(context,notificationId, userId, applicationStatus, membersList,circleName);

    }


    public static void sendnotification(String state, String circleId, String circleName, String toUserId) {
//        This is the function to store the

        String notificationId = FirebaseWriteHelper.getNotificationId(toUserId);
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
        FirebaseWriteHelper.writeNormalNotifications(toUserId, notificationId, applicationStatus);
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

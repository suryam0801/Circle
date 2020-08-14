package circleapp.circlepackage.circle.Helpers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ui.MainActivity;

public class FireBaseInstanceIDService extends FirebaseMessagingService {
    private static final String TAG =FireBaseInstanceIDService.class.getSimpleName();
    private GlobalVariables globalVariables = new GlobalVariables();

    Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
    private static final String channelId = String.valueOf(R.string.default_notification_channel_id);
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject data = new JSONObject(remoteMessage.getData());
                String data_title = data.getString("title");
                String data_body = data.getString("body");
                String data_data = data.getString("id");
                Log.d("NOTIFICATION ADAPTER: ", "NOTIF RECIEVED: "+data_title+"::"+data_body);
                if(data_title.contains("New Comment added in")){
                    if(globalVariables.getCurrentCircle()!=null){
                        if(data_data.equals(globalVariables.getCurrentCircle().getId()));
                        else
                            sendNotification(data_title,data_body, data_data);
                    }
                    else
                        sendNotification(data_title,data_body, data_data);
                }
                else
                    sendNotification(data_title,data_body, data_data);
                Log.d(TAG, "onMessageReceived: \n" +
                        "Extra Information: " + data_title+":::"+data_body);
                String messageTitle = remoteMessage.getNotification().getTitle();

                String messageBody = remoteMessage.getNotification().getBody();
//                sendNotification(messageTitle, messageBody);
                Log.d("NOTIFICATION ADAPTER: ", "NOTIF RECIEVED: "+messageTitle+"::"+messageBody);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        sendRegistrationToServer(token);

    }
    private void sendRegistrationToServer(String token) {
    }

    @SuppressLint("NewApi")
    private void sendNotification(String title, String messageBody, String dataPayload) {
        Log.d("infunc", "NOTIF RECIEVED: "+title+"::"+messageBody);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.notif_percussion);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        if(dataPayload!=null)
            notificationIntent.putExtra("circleId",dataPayload);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(title)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(notifSound);

        notificationBuilder.setContentIntent(contentIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//         Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            if(title.toLowerCase().contains("comment")){
                NotificationChannel channel1 = new NotificationChannel(channelId,
                        "Comment Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel1.setSound(notifSound,att);
//                channel1.setGroup("Comment Notifications");
                notificationManager.createNotificationChannel(channel1);
            }
            else if(title.toLowerCase().contains("post")){
                NotificationChannel channel2 = new NotificationChannel(channelId,
                        "New Post Notifications",
                        NotificationManager.IMPORTANCE_HIGH);
                channel2.setSound(notifSound,att);
//                channel2.setGroup("New Post Notifications");
                notificationManager.createNotificationChannel(channel2);
            }
            else if(title.toLowerCase().contains("applicant")||title.toLowerCase().contains("application")){
                NotificationChannel channel3 = new NotificationChannel(channelId,
                        "Circle applicants",
                        NotificationManager.IMPORTANCE_HIGH);
                channel3.setSound(notifSound,att);
//                channel3.setGroup("Circle applicants");
                notificationManager.createNotificationChannel(channel3);
            }
            else {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "General Notifications",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setSound(notifSound,att);
//                channel.setGroup("General Notifications");
                notificationManager.createNotificationChannel(channel);
            }
        }

//        NotificationChannelGroup notificationChannelGroup=
//                new  NotificationChannelGroup( GROUP_ID,
//                        "Notifs");
//        notificationManager.createNotificationChannelGroup(notificationChannelGroup);
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(HelperMethodsUI.uuidGet(), (int)System.currentTimeMillis(), notificationBuilder.build());
    }
}

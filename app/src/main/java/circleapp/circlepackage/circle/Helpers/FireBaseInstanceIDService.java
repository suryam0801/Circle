package circleapp.circlepackage.circle.Helpers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
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
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ui.ExploreTabbedActivity;

import static android.provider.Contacts.GroupMembership.GROUP_ID;
import static circleapp.circlepackage.circle.R.string.default_notification_channel_id;

public class FireBaseInstanceIDService extends FirebaseMessagingService {
    private static final String TAG =FireBaseInstanceIDService.class.getSimpleName();
    int notificationId = (int) System.currentTimeMillis();
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

//                sendNotification(data_title,data_body);
                Log.d(TAG, "onMessageReceived: \n" +
                        "Extra Information: " + data_title+":::"+data_body);
                Log.d("NOTIFICATION ADAPTER: ", "NOTIF RECIEVED: ");


                String messageTitle = remoteMessage.getNotification().getTitle();

                String messageBody = remoteMessage.getNotification().getBody();
                sendNotification(messageTitle, messageBody);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Uri notifSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.notif_percussion);

                Intent intent = new Intent(this, ExploreTabbedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this,getString(default_notification_channel_id))
                                .setContentTitle(messageTitle)
                                .setSmallIcon(R.drawable.circle_logo)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setContentText(messageBody+"Waste")
                                .setAutoCancel(false)
                                .setSound(notifSound);



                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



//                notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());
//                notificationManager.cancel(notificationId);
//                stopForeground(true);
                Log.d("NOTIFICATION ADAPTER: ", "NOTIF RECIEVED: "+messageTitle+"::"+messageBody+notificationId);
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
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }
    private void handleNow() {
    }

    private void sendRegistrationToServer(String token) {
    }

    @SuppressLint("NewApi")
    private void sendNotification(String title, String messageBody) {
        Log.d("infunc", "NOTIF RECIEVED: "+title+"::"+messageBody);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.notif_percussion);
        Intent notificationIntent = new Intent(this, ExploreTabbedActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(title)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentText(messageBody+"Actual")
                        .setStyle(new NotificationCompat.InboxStyle())
                        .setAutoCancel(false)
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
        notificationManager.notify(HelperMethodsUI.uuidGet(), Math.toIntExact(System.currentTimeMillis()), notificationBuilder.build());
    }
}

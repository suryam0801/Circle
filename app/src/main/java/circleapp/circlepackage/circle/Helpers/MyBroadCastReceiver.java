package circleapp.circlepackage.circle.Helpers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.work.impl.utils.ForceStopRunnable;

import java.util.Objects;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Login.GatherUserDetails;
import circleapp.circlepackage.circle.MainActivity;
import circleapp.circlepackage.circle.R;

@SuppressLint("RestrictedApi")
public class MyBroadCastReceiver extends BroadcastReceiver {
    private static final String TAG =MyBroadCastReceiver.class.getSimpleName() ;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive", "onReceive_called");
        Toast.makeText(context.getApplicationContext(), "Alarm Manager just ran", Toast.LENGTH_LONG).show();
        Log.d("MyBroadCastReceiver","Alarm Manager just ran");
        Log.d("onReceive_notifyied", "onReceive_called  ::");

        Intent mainIntent = new Intent(context, ExploreTabbedActivity.class);
        @SuppressWarnings("deprecation")
        Notification.Builder noti = new Notification.Builder(context)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 131314, mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle("We Miss You!")
                .setContentText("Please play our game again soon.")
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.circle_logo)
                .setTicker("We Miss You! Please come back and play our game again soon.")
                .setWhen(System.currentTimeMillis());
//                .getNotification();

        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(131315, noti.build());

        Log.v(TAG, "Notification sent");
        Toast.makeText(context.getApplicationContext(), "Notification just ran", Toast.LENGTH_LONG).show();
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder builder =
//                new NotificationCompat.Builder(context)
//                        .setContentTitle("Circle")
//                        .setLargeIcon(BitmapFactory.decodeResource(context.getApplicationContext().getResources(),
//                                R.mipmap.ic_launcher))
//                        .setSmallIcon(R.drawable.circle_logo)
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentText("Welcome to the Circle  You can find the people with same Interest in your Locality");
//
//
//        Intent notificationIntent = new Intent(context, ExploreTabbedActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(contentIntent);
//        // Add as notification
//        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(0, builder.build());
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Intent contentIntent = new Intent(context, ExploreTabbedActivity.class);
//        contentIntent.setAction(Intent.ACTION_MAIN);
//        contentIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        Notification notification = new NotificationCompat.Builder(context)
//                .setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, 0))
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setAutoCancel(true)
//                .setContentTitle("Test title")
//                .setContentText("Test text")
//                .setDefaults(NotificationCompat.DEFAULT_SOUND)
//                .build();
//
//        notificationManager.notify(123, notification);
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            Intent serviceIntent = new Intent(context, MyService.class);
//            context.startService(serviceIntent);
//        } else {
////            Intent serviceIntent = new Intent(context, MyService.class);
////            context.startService(serviceIntent);
//        }
    }
}

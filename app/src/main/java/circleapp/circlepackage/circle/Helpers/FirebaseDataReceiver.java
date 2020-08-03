package circleapp.circlepackage.circle.Helpers;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import circleapp.circlepackage.circle.R;

import static circleapp.circlepackage.circle.R.string.default_notification_channel_id;

public class FirebaseDataReceiver extends WakefulBroadcastReceiver {
    int notifid  = (int) System.currentTimeMillis();
    @Override
    public void onReceive(Context context, Intent intent) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context,context.getString(default_notification_channel_id))
                        .setContentTitle(intent.getExtras().getString("title"))
                        .setSmallIcon(R.drawable.circle_logo)
                        .setPriority(1)
                        .setContentText(intent.getExtras().getString("body")+"Madness Extra")
                        .setAutoCancel(false)
                        .setSound(defaultSoundUri);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//        notificationManager.notify(notifid+1 /* ID of notification */, notificationBuilder.build());
//        notificationManager.cancel(notifid-1);
        Log.d("FirebaseDataReceiver","Notification Recieved"+intent.getExtras().getString("title")+notifid);

    }
}

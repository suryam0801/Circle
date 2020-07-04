package circleapp.circlepackage.circle.Helpers;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

//import leakcanary.LeakCanary;


public class InAppNotificationHelper extends Application {
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        /*if(LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        LeakCanary.install(this);*/
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }
}

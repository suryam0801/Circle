package circleapp.circlepackage.circle.Helpers;

import android.content.Context;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import circleapp.circlepackage.circle.Explore.NotificationAdapter;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.NotifyUIObject;

public class FirebaseUtils {
    public static void FBUtils(NotifyUIObject notifyUIObject) {
        switch (notifyUIObject.getType())
        {
            case "notify":
                FbsingleValueEvent(notifyUIObject);
        }
    }

    private static void FbsingleValueEvent(NotifyUIObject notifyUIObject) {
        notifyUIObject.getNotifyDb().orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Notification notification = snapshot.getValue(Notification.class);
                        UiHelper.NotifyUIFragment(notifyUIObject.getContext(),notification,notifyUIObject.getPrevnotify(),notifyUIObject.getPreviousNotifs(),notifyUIObject.getThisWeekNotifs(),notifyUIObject.getAdapterPrevious(),notifyUIObject.getAdapterThisWeek(),notifyUIObject.getPreviousListView(),notifyUIObject.getThisWeekListView());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

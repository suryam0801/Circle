package circleapp.circlepackage.circle.Helpers;

import android.content.Context;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import circleapp.circlepackage.circle.Explore.NotificationAdapter;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.NotifyUIObject;

public class FirebaseUtils {
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public static void FBNotificationsRetrieve(NotifyUIObject notifyUIObject) {
        DatabaseReference notifyDb = database.getReference("Notifications").child(currentUser.getUid());
        notifyDb.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
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

    public static void FBCirclesRetrieval(){
        DatabaseReference circlesDB = database.getReference("Circles");
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
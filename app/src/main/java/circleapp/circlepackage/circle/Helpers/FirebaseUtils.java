package circleapp.circlepackage.circle.Helpers;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import circleapp.circlepackage.circle.Explore.NotificationAdapter;
import circleapp.circlepackage.circle.Explore.WorkbenchFragment;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.NotifyUIObject;
import circleapp.circlepackage.circle.ObjectModels.User;

public class FirebaseUtils {
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static DatabaseReference notifyDb,circlesDB;
    static FirebaseAuth currentUser = FirebaseAuth.getInstance();;
    public static void FbsingleValueEvent(NotifyUIObject notifyUIObject) {
        currentUser = FirebaseAuth.getInstance();
        notifyDb = database.getReference("Notifications").child(currentUser.getCurrentUser().getUid());
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

}
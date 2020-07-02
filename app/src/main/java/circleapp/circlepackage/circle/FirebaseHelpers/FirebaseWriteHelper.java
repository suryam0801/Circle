package circleapp.circlepackage.circle.FirebaseHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;

public class FirebaseWriteHelper {
    private static final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference CIRCLES_REF = database.getReference("/Circles");
    private static final DatabaseReference NOTIFS_REF = database.getReference("/Notifications");
    private static final DatabaseReference BROADCASTS_REF = database.getReference("/Broadcasts");
    private static final DatabaseReference CIRCLES_PERSONEL_REF = database.getReference("/CirclePersonel");
    private static final DatabaseReference USERS_REF = database.getReference("/USERS_REF").child(user.getUid());
    private static final DatabaseReference COMMENTS_REF = database.getReference("BroadcastComments");

    public static void deleteCircle(Context context, Circle circle, User user) {
        //reducing created circle count
        int currentCreatedCount = user.getCreatedCircles() - 1;
        user.setCreatedCircles(currentCreatedCount);
        SessionStorage.saveUser((Activity) context, user);

        CIRCLES_PERSONEL_REF.child(circle.getId()).removeValue();
        CIRCLES_REF.child(circle.getId()).removeValue();
        USERS_REF.child("createdCircles").setValue(currentCreatedCount);
        BROADCASTS_REF.child(circle.getId()).removeValue();
        COMMENTS_REF.child(circle.getId()).removeValue();
    }

    public static void exitCircle(Context context, Circle circle, User user) {
        //reducing active circle count
        int currentActiveCount = user.getActiveCircles() - 1;
        user.setActiveCircles(currentActiveCount);
        SessionStorage.saveUser((Activity) context, user);

        USERS_REF.child("activeCircles").setValue(currentActiveCount);
        CIRCLES_PERSONEL_REF.child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        CIRCLES_REF.child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();

    }

}

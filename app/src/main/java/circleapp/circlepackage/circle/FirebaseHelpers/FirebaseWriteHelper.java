package circleapp.circlepackage.circle.FirebaseHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;

import static circleapp.circlepackage.circle.MainActivity.TAG;

public class FirebaseWriteHelper {
    private static final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference CIRCLES_REF = database.getReference("/Circles");
    private static final DatabaseReference NOTIFS_REF = database.getReference("/Notifications");
    private static final DatabaseReference BROADCASTS_REF = database.getReference("/Broadcasts");
    private static final DatabaseReference CIRCLES_PERSONEL_REF = database.getReference("/CirclePersonel");
    private static final DatabaseReference USERS_REF = database.getReference("/Users").child(user.getUid());
    private static final DatabaseReference COMMENTS_REF = database.getReference("BroadcastComments");
    private static final DatabaseReference LOCATIONS_REF = database.getReference("Locations");
    private static final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private static final DatabaseReference USER_FEEDBACK_REF = database.getReference("UserFeedback");
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
    public static void writeBroadcast(Context context, String circleId, Broadcast broadcast, int newCount){
        CIRCLES_REF.child(circleId).child("noOfBroadcasts").setValue(newCount);
        BROADCASTS_REF.child(circleId).child(broadcast.getId()).setValue(broadcast);
    }
    public static StorageReference getStorageReference(String dbReference){
        return storageReference.child(dbReference);
    }
    public static void updateUserNewTimeStampComments(String userId, String broadcastId, long latestTimestamp){
        USERS_REF.child(userId).child("newTimeStampsComments").child(broadcastId).setValue(latestTimestamp);
    }
    public static void updateUserNewReadComments(String userId, String broadcastId, long numberOfComments){
        USERS_REF.child(userId).child("noOfReadDiscussions").child(broadcastId).setValue(numberOfComments);
    }
    public static void updateUserCount(String userId, String circleId, long noOfBroadcasts){
        USERS_REF.child(userId).child("notificationsAlert").child(circleId).setValue(noOfBroadcasts);
    }
    public static void updateUserProfilePic(UserProfileChangeRequest profileUpdates){
        user.updateProfile(profileUpdates);
    }

    public static void initializeNewCount(Circle c, User user) {
        if (user.getNotificationsAlert() != null && !user.getNotificationsAlert().containsKey(c.getId())) {
            HashMap<String, Integer> newNotifs = new HashMap<>(user.getNotificationsAlert());
            newNotifs.put(c.getId(), 0);
            user.setNotificationsAlert(newNotifs);
            updateUser(user);
        } else if (user.getNotificationsAlert() == null) {
            HashMap<String, Integer> newNotifs = new HashMap<>();
            newNotifs.put(c.getId(), 0);
            user.setNotificationsAlert(newNotifs);
            updateUser(user);
        }
    }

    public static void applyOrJoin(Circle circle, User user, Subscriber subscriber) {
        if (("review").equalsIgnoreCase(circle.getAcceptanceType())) {
            database.getReference().child("CirclePersonel").child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
            //adding userID to applicants list
            CIRCLES_REF.child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
            SendNotification.sendnotification("new_applicant", circle.getId(), circle.getName(), circle.getCreatorID());
        } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
            database.getReference().child("CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
            //adding userID to members list in circlesReference
            CIRCLES_REF.child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);

            int nowActive = user.getActiveCircles() + 1;
            user.setActiveCircles(nowActive);
            updateUser(user);
        }
    }

    public static void updateUser(User user) {
        USERS_REF.setValue(user);
    }

    public static void makeFeedbackEntry(Context context, Map<String, Object> map) {
        USER_FEEDBACK_REF.child(SessionStorage.getUser((Activity) context).getDistrict()).push().setValue(map);
    }
    public static void writeCommentNotifications(String notificationId, String userId, Map<String, Object> applicationStatus, HashMap<String, Boolean> listenersList){
        Set<String> member;
        if(listenersList!=null) {
            listenersList.remove(userId);
            member = listenersList.keySet();
            for (String i : member) {

                NOTIFS_REF.child(i).child(notificationId).setValue(applicationStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });

                db.collection("Users/" + i + "/CommentsNotification").add(applicationStatus).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });


            }
        }

    }
    public static void writeBroadcastNotifications(String notificationId, String userId, Map<String, Object> applicationStatus, HashMap<String, Boolean> membersList){
        Set<String> member;

        if(membersList!=null) {
            member = membersList.keySet();
            for (String i :member)
            {

                NOTIFS_REF.child(i).child(notificationId).setValue(applicationStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });

                db.collection("Users/" + i + "/BroadcastNotification").add(applicationStatus).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });


            }
        }
    }
    public static void writeNormalNotifications(String userId, String notificationId, Map<String, Object> applicationStatus){
        NOTIFS_REF.child(userId).child(notificationId).setValue(applicationStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

        db.collection("Users/" + userId + "/Notifications").add(applicationStatus).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }
}

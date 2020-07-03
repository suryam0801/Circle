package circleapp.circlepackage.circle.FirebaseHelpers;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.ReportAbuse;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;

public class FirebaseWriteHelper {
    private static final FirebaseAuth authenticationToken = FirebaseAuth.getInstance();
    private static final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
    private static final DatabaseReference CIRCLES_REF = database.getReference("/Circles");
    private static final DatabaseReference NOTIFS_REF = database.getReference("/Notifications");
    private static final DatabaseReference BROADCASTS_REF = database.getReference("/Broadcasts");
    private static final DatabaseReference CIRCLES_PERSONEL_REF = database.getReference("/CirclePersonel");
    private static final DatabaseReference USERS_REF = database.getReference("/Users").child(user.getUid());
    private static final DatabaseReference COMMENTS_REF = database.getReference("BroadcastComments");
    private static final DatabaseReference LOCATIONS_REF = database.getReference("Locations");
    private static final DatabaseReference REPORT_ABUSE_REF = database.getReference("ReportAbuse");
    private static final DatabaseReference STORAGE_REFERENCES = database.getReference("StorageReferences");
    private static final DatabaseReference USER_FEEDBACK_REF = database.getReference("UserFeedback");

    public static void deleteCircle(Context context, Circle circle, User user) {
        //reducing created circle count
        int currentCreatedCount = 0;
        if (user.getCreatedCircles() > 0)
            currentCreatedCount = user.getCreatedCircles() - 1;

        user.setCreatedCircles(currentCreatedCount);
        SessionStorage.saveUser((Activity) context, user);

        CIRCLES_PERSONEL_REF.child(circle.getId()).removeValue();
        CIRCLES_REF.child(circle.getId()).removeValue();
        USERS_REF.child("createdCircles").setValue(currentCreatedCount);
        BROADCASTS_REF.child(circle.getId()).removeValue();
        COMMENTS_REF.child(circle.getId()).removeValue();
        removeCircleImageReference(circle.getId(), circle.getBackgroundImageLink());
    }

    public static void exitCircle(Context context, Circle circle, User user) {
        //reducing active circle count
        int currentActiveCount = 0;
        if (user.getActiveCircles() > 0)
            currentActiveCount = user.getActiveCircles() - 1;

        user.setActiveCircles(currentActiveCount);
        SessionStorage.saveUser((Activity) context, user);

        USERS_REF.child("activeCircles").setValue(currentActiveCount);
        CIRCLES_PERSONEL_REF.child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        CIRCLES_REF.child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();
    }

    public static void deleteBroadcast(String circleId, Broadcast broadcast, int noOfBroadcasts) {
        if (broadcast.isImageExists()) {
            removeBroadcastImageReference(circleId,broadcast.getId(),broadcast.getAttachmentURI());
        }
        BROADCASTS_REF.child(circleId).child(broadcast.getId()).removeValue();
        COMMENTS_REF.child(circleId).child(broadcast.getId()).removeValue();
        CIRCLES_REF.child(circleId).child("noOfBroadcasts").setValue(noOfBroadcasts - 1);
    }

    public static void writeBroadcast(Context context, String circleId, Broadcast broadcast, int newCount) {
        CIRCLES_REF.child(circleId).child("noOfBroadcasts").setValue(newCount);
        BROADCASTS_REF.child(circleId).child(broadcast.getId()).setValue(broadcast);
        addBroadcastImageReference(circleId, broadcast.getId(), broadcast.getAttachmentURI());
    }

    public static StorageReference getStorageReference(String dbReference) {
        return mFirebaseStorage.getReference().child(dbReference);
    }

    public static void updateUserNewTimeStampComments(String userId, String broadcastId, long latestTimestamp) {
        USERS_REF.child(userId).child("newTimeStampsComments").child(broadcastId).setValue(latestTimestamp);
    }

    public static void updateUserNewReadComments(String userId, String broadcastId, long numberOfComments) {
        USERS_REF.child(userId).child("noOfReadDiscussions").child(broadcastId).setValue(numberOfComments);
    }

    public static void updateUserCount(String userId, String circleId, long noOfBroadcasts) {
        USERS_REF.child(userId).child("notificationsAlert").child(circleId).setValue(noOfBroadcasts);
    }

    public static void updateUserProfilePic(UserProfileChangeRequest profileUpdates) {
        user.updateProfile(profileUpdates);
    }

    public static void initializeNewCount(Context context, Circle c, User user) {
        if (user.getNotificationsAlert() != null && !user.getNotificationsAlert().containsKey(c.getId())) {
            HashMap<String, Integer> newNotifs = new HashMap<>(user.getNotificationsAlert());
            newNotifs.put(c.getId(), 0);
            user.setNotificationsAlert(newNotifs);
            updateUser(user, context);
        } else if (user.getNotificationsAlert() == null) {
            HashMap<String, Integer> newNotifs = new HashMap<>();
            newNotifs.put(c.getId(), 0);
            user.setNotificationsAlert(newNotifs);
            updateUser(user, context);
        }
    }

    public static void applyOrJoin(Context context, Circle circle, User user, Subscriber subscriber) {
        if (("review").equalsIgnoreCase(circle.getAcceptanceType())) {
            CIRCLES_PERSONEL_REF.child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
            //adding userID to applicants list
            CIRCLES_REF.child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
            SendNotification.sendnotification("new_applicant", circle.getId(), circle.getName(), circle.getCreatorID());
        } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
            CIRCLES_PERSONEL_REF.child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
            //adding userID to members list in circlesReference
            CIRCLES_REF.child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);

            int nowActive = user.getActiveCircles() + 1;
            user.setActiveCircles(nowActive);
            updateUser(user, context);
        }
    }

    public static void updateUser(User user, Context context) {
        USERS_REF.setValue(user);
        SessionStorage.saveUser((Activity) context, user);
    }

    public static void makeFeedbackEntry(Context context, Map<String, Object> map) {
        USER_FEEDBACK_REF.child(SessionStorage.getUser((Activity) context).getDistrict()).push().setValue(map);
    }

    public static void writeCommentNotifications(String notificationId, String userId, Map<String, Object> applicationStatus, HashMap<String, Boolean> listenersList) {
        Set<String> member;
        if (listenersList != null) {
            listenersList.remove(userId);
            member = listenersList.keySet();
            for (String i : member)
                NOTIFS_REF.child(i).child(notificationId).setValue(applicationStatus);

        }

    }

    public static void writeBroadcastNotifications(String notificationId, String userId, Map<String, Object> applicationStatus, HashMap<String, Boolean> membersList) {
        Set<String> member;

        if (membersList != null) {
            member = membersList.keySet();
            for (String i : member)
                NOTIFS_REF.child(i).child(notificationId).setValue(applicationStatus);

        }
    }

    public static void writeNormalNotifications(String userId, String notificationId, Map<String, Object> applicationStatus) {
        NOTIFS_REF.child(userId).child(notificationId).setValue(applicationStatus);
    }

    public static String getNotificationId(String broadcastId) {
        String notificationId = NOTIFS_REF.child(broadcastId).push().getKey();
        return notificationId;
    }

    public static String getBroadcastId(String circleId) {
        String broadcastId = BROADCASTS_REF.child(circleId).push().getKey();
        return broadcastId;
    }
    public static String getCircleId(){
        String circleId = CIRCLES_REF.push().getKey();
        return circleId;
    }
    public static String getUserId(){
        String userId = USERS_REF.push().getKey();
        return userId;
    }

    public static void addDistrict(String district) {
        LOCATIONS_REF.child(district).setValue(true);
    }

    public static void broadcastListenerList(int transaction, String userId, String circleId, String broadcastId) {
        if (transaction == 0)
            BROADCASTS_REF.child(circleId).child(broadcastId).child("listenersList").child(userId).setValue(true);
        else
            BROADCASTS_REF.child(circleId).child(broadcastId).child("listenersList").child(userId).removeValue();

    }

    public static String createPhotoBroadcast(String title, String photoUri, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference broadcastsDB;
        broadcastsDB = database.getReference("Broadcasts");
        String id = HelperMethods.uuidGet();
        Broadcast broadcast = new Broadcast(id, title, null, photoUri, creatorName, null, "AdminId", false, true, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments);
        broadcastsDB.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createPollBroadcast(String text, String creatorName, int offsetTimeStamp, HashMap<String, Integer> pollOptions, String downloadUri, int noOfComments, String circleId) {
        String id = HelperMethods.uuidGet();
        Broadcast broadcast;
        Poll poll = new Poll(text, pollOptions, null);
        if (downloadUri != null)
            broadcast = new Broadcast(id, null, null, downloadUri, creatorName, null, "AdminId", true, true, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments);
        else
            broadcast = new Broadcast(id, null, null, null, creatorName, null, "AdminId", true, false, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments);
        BROADCASTS_REF.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createMessageBroadcast(String title, String message, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {
        String id = HelperMethods.uuidGet();
        Broadcast broadcast = new Broadcast(id, title, message, null, creatorName, null, "AdminId", false, false, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments);
        BROADCASTS_REF.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createCircle(String name, String description, String acceptanceType, String creatorName, String district, int noOfBroadcasts, int noOfDiscussions, String category) {
        HashMap<String, Boolean> circleIntTags = new HashMap<>();
        circleIntTags.put("sample", true);
        String id = HelperMethods.uuidGet();
        Circle circle = new Circle(id, name, description, acceptanceType, "Everybody", "CreatorAdmin", creatorName, category, "default", null, null, district, null, System.currentTimeMillis(), noOfBroadcasts, noOfDiscussions);
        CIRCLES_REF.child(id).setValue(circle);
        return id;
    }

    public static void createComment(String name, String text, int offsetTimeStamp, String circleId, String broadcastId) {
        String id = HelperMethods.uuidGet();
        Comment comment = new Comment(name, text, id, null, System.currentTimeMillis() + offsetTimeStamp);
        COMMENTS_REF.child(circleId).child(broadcastId).child(id).setValue(comment);
    }

    public static void createReportAbuse(Context context, String circleID, String broadcastID, String commentID, String creatorID, String userID, String reportType) {
        String id = HelperMethods.uuidGet();
        ReportAbuse reportAbuse = new ReportAbuse(id, circleID, broadcastID, commentID, creatorID, userID, reportType);
        if (user.getUid() == creatorID) {
            Toast.makeText(context, "Stop Reporting your own Content", Toast.LENGTH_SHORT).show();
        } else {

            REPORT_ABUSE_REF.child(id).setValue(reportAbuse);
        }
    }
    public static void signOutAuth(){
        authenticationToken.signOut();
    }

    public static void deleteStorageReference(String reference){
        StorageReference temp = mFirebaseStorage.getReferenceFromUrl(reference);
        temp.delete();
    }
    public static void addBroadcastImageReference(String circleId, String broadcastId, String imageUrl){
        STORAGE_REFERENCES.child(circleId).child(broadcastId).setValue(imageUrl);
    }
    public static void addCircleImageReference(String circleId, String imageUrl){
        STORAGE_REFERENCES.child(circleId).child("CircleIcon").setValue(imageUrl);
    }
    public static void removeCircleImageReference(String circleId, String imageUrl){
        deleteStorageReference(imageUrl);
        /* ITERATE THROUGH BROADCASTS WITHIN CIRCLE ID
        STORAGE_REFERENCES.child(circleId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> broadcastImageList;
                if(dataSnapshot.exists()){
                    broadcastImageList = (Set<String>) ((HashMap<String, String>) dataSnapshot.getValue()).keySet();
                    for (String broadcastImage : broadcastImageList){
                        removeBroadcastImageReference(circleId, broadcastImage);
                    }
                } else {
                    broadcastImageList = Collections.<String>emptySet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        STORAGE_REFERENCES.child(circleId).removeValue();
    }
    public static void removeBroadcastImageReference(String circleId, String broadcastId, String imageUrl){
        deleteStorageReference(imageUrl);
        STORAGE_REFERENCES.child(circleId).child(broadcastId).removeValue();
    }


}

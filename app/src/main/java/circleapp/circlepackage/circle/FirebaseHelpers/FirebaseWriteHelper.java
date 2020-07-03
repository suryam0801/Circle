package circleapp.circlepackage.circle.FirebaseHelpers;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private static final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
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
            StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(broadcast.getAttachmentURI());
            photoRef.delete();
        }
        BROADCASTS_REF.child(circleId).child(broadcast.getId()).removeValue();
        COMMENTS_REF.child(circleId).child(broadcast.getId()).removeValue();
        CIRCLES_REF.child(circleId).child("noOfBroadcasts").setValue(noOfBroadcasts - 1);
    }

    public static void writeBroadcast(Context context, String circleId, Broadcast broadcast, int newCount) {
        CIRCLES_REF.child(circleId).child("noOfBroadcasts").setValue(newCount);
        BROADCASTS_REF.child(circleId).child(broadcast.getId()).setValue(broadcast);
    }

    public static StorageReference getStorageReference(String dbReference) {
        return storageReference.child(dbReference);
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
            updateUser(user, context);
        }
    }

    public static void updateUser(User user, Context context) {
        USERS_REF.setValue(user);
        SessionStorage.saveUser((Activity) context, user);
    }

    public static void updateBroadcast(Broadcast broadcast, Context context, String circleId) {
        BROADCASTS_REF.child(circleId).child(broadcast.getId()).setValue(broadcast);
        SessionStorage.saveBroadcast((Activity) context, broadcast);
    }

    public static void updateCircle(Circle circle, Context context) {
        CIRCLES_REF.child(circle.getId()).setValue(circle);
        SessionStorage.saveCircle((Activity) context, circle);
    }


    public static void makeFeedbackEntry(Context context, Map<String, Object> map) {
        USER_FEEDBACK_REF.child(SessionStorage.getUser((Activity) context).getDistrict()).push().setValue(map);
    }

    public static void makeNewComment(Map<String, Object> map, String circleId, String broadcastId) {
        COMMENTS_REF.child(circleId).child(broadcastId).push().setValue(map);
    }

    public static void createUserMadeCircle(Circle circle, Subscriber subscriber, String userId) {
        CIRCLES_REF.child(circle.getId()).setValue(circle);
        CIRCLES_PERSONEL_REF.child(circle.getId()).child("members").child(userId).setValue(subscriber);
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

    public static void acceptApplicant(String circleId, Subscriber selectedApplicant) {
        CIRCLES_PERSONEL_REF.child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        CIRCLES_REF.child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
        CIRCLES_PERSONEL_REF.child(circleId).child("members").child(selectedApplicant.getId()).setValue(selectedApplicant);
        CIRCLES_REF.child(circleId).child("membersList").child(selectedApplicant.getId()).setValue(true);
    }

    public static void rejectApplicant(String circleId, Subscriber selectedApplicant) {
        CIRCLES_PERSONEL_REF.child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        CIRCLES_REF.child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
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

    public static String createDefaultCircle(String name, String description, String acceptanceType, String creatorName, String district, int noOfBroadcasts, int noOfDiscussions, String category) {
        String id = HelperMethods.uuidGet();
        Circle circle = new Circle(id, name, description, acceptanceType, "Everybody", "CreatorAdmin", creatorName, category, "default", null, null, district, null, System.currentTimeMillis(), noOfBroadcasts, noOfDiscussions);
        CIRCLES_REF.child(id).setValue(circle);
        return id;
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


}

package circleapp.circlepackage.circle.FirebaseHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Notification;
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
    private static final DatabaseReference USERS_REF = database.getReference("/Users");
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
        USERS_REF.child(user.getUserId()).child("createdCircles").setValue(currentCreatedCount);
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

        USERS_REF.child(user.getUserId()).child("activeCircles").setValue(currentActiveCount);
        CIRCLES_PERSONEL_REF.child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        CIRCLES_REF.child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();
    }

    public static void deleteBroadcast(Context context, String circleId, Broadcast broadcast, int noOfBroadcasts, User user) {
        Log.d("wefkjn", "tempListening.toString()");
        if (broadcast.isImageExists())
            removeBroadcastImageReference(circleId, broadcast.getId(), broadcast.getAttachmentURI());

        BROADCASTS_REF.child(circleId).child(broadcast.getId()).removeValue();
        COMMENTS_REF.child(circleId).child(broadcast.getId()).removeValue();
        CIRCLES_REF.child(circleId).child("noOfBroadcasts").setValue(noOfBroadcasts-1);
        updateUserObjectWhenDeletingBroadcast(context, user, broadcast);
    }

    public static void updateUserObjectWhenDeletingBroadcast(Context context, User user, Broadcast broadcast) {
        //remove listening broadcast
        List<String> tempListening;
        if (user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId())) {
            tempListening = new ArrayList<>(user.getMutedBroadcasts());
            tempListening.remove(broadcast.getId());
            user.setMutedBroadcasts(tempListening);
            updateUser(user, context);
        }

        //remove no of read discussions
        HashMap<String, Integer> userNoReadComments;
        if (user.getNoOfReadDiscussions() != null && user.getNoOfReadDiscussions().containsKey(broadcast.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userNoReadComments = new HashMap<>(user.getNoOfReadDiscussions());
            userNoReadComments.remove(broadcast.getId());
            user.setNoOfReadDiscussions(userNoReadComments);
            Log.d("wefkjn", userNoReadComments.toString());
            updateUser(user, context);
        }

        //remove new timestamp comments
        HashMap<String, Long> userCommentTimestamps;
        if (user.getNewTimeStampsComments() != null && user.getNewTimeStampsComments().containsKey(broadcast.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userCommentTimestamps = new HashMap<>(user.getNewTimeStampsComments());
            userCommentTimestamps.remove(broadcast.getId());
            user.setNewTimeStampsComments(userCommentTimestamps);
            Log.d("wefkjn", userCommentTimestamps.toString());
            updateUser(user, context);
        }
    }

    public static void writeBroadcast(Context context, String circleId, Broadcast broadcast, int newCount, String userId) {
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

    public static void updateUserProfile(UserProfileChangeRequest profileUpdates) {
        user.updateProfile(profileUpdates);
    }

    public static FirebaseUser getUser() {
        return user;
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

    public static void acceptApplicant(String circleId, Subscriber selectedApplicant, Context context) {
        CIRCLES_PERSONEL_REF.child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        CIRCLES_REF.child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
        CIRCLES_PERSONEL_REF.child(circleId).child("members").child(selectedApplicant.getId()).setValue(selectedApplicant);
        CIRCLES_REF.child(circleId).child("membersList").child(selectedApplicant.getId()).setValue(true);
        FirebaseRetrievalViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) context).get(FirebaseRetrievalViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(selectedApplicant.getId());
        AtomicInteger noOfActiveCircles = new AtomicInteger();
        tempLiveData.observe((LifecycleOwner) context, dataSnapshot -> {
            User tempUser = dataSnapshot.getValue(User.class);
            noOfActiveCircles.set(tempUser.getActiveCircles());
        });
        USERS_REF.child(selectedApplicant.getId()).child("activeCircles").setValue(noOfActiveCircles.get() +1);
    }

    public static void rejectApplicant(String circleId, Subscriber selectedApplicant) {
        CIRCLES_PERSONEL_REF.child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        CIRCLES_REF.child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
    }

    public static void updateUser(User user, Context context) {
        SessionStorage.saveUser((Activity) context, user);
        USERS_REF.child(user.getUserId()).setValue(user);
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

    public static void makeNewComment(Comment comment, String circleId, String broadcastId) {
        COMMENTS_REF.child(circleId).child(broadcastId).push().setValue(comment);
    }
    public static String getCommentId(String circleId, String broadcastId){
        String commentId = COMMENTS_REF.child(circleId).child(broadcastId).push().getKey();
        return commentId;
    }

    public static void createUserMadeCircle(Circle circle, Subscriber subscriber, String userId) {
        CIRCLES_REF.child(circle.getId()).setValue(circle);
        CIRCLES_PERSONEL_REF.child(circle.getId()).child("members").child(userId).setValue(subscriber);
        if(!circle.getBackgroundImageLink().equals("default"))
        addCircleImageReference(circle.getId(),circle.getBackgroundImageLink());
    }

    public static void writeCommentNotifications(Notification notification, HashMap<String, Boolean> listenersList) {
        Set<String> member;
        if (listenersList != null) {
            listenersList.remove(notification.getCreatorId());
            member = listenersList.keySet();
            for (String i : member)
                NOTIFS_REF.child(i).child(notification.getNotificationId()).setValue(notification);

        }

    }

    public static void writeBroadcastNotifications(Notification notification, HashMap<String, Boolean> membersList) {
        Set<String> member;

        if (membersList != null) {
            member = membersList.keySet();
            for (String i : member)
                NOTIFS_REF.child(i).child(notification.getNotificationId()).setValue(notification);

        }
    }

    public static void writeNormalNotifications(Notification notification) {
        NOTIFS_REF.child(notification.getNotify_to()).child(notification.getNotificationId()).setValue(notification);
    }

    public static String getNotificationId(String objectId) {
        String notificationId = NOTIFS_REF.child(objectId).push().getKey();
        return notificationId;
    }

    public static String getBroadcastId(String circleId) {
        String broadcastId = BROADCASTS_REF.child(circleId).push().getKey();
        return broadcastId;
    }

    public static String getCircleId() {
        String circleId = CIRCLES_REF.push().getKey();
        return circleId;
    }

    public static String getUserId() {
        String userId = USERS_REF.child(user.getUid()).push().getKey();
        return userId;
    }

    public static void deleteFirebaseAuth() {
        user.delete();
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

    public static void signOutAuth() {
        authenticationToken.signOut();
    }

    public static FirebaseAuth getAuthToken() {
        return authenticationToken;
    }

    public static void deleteStorageReference(String reference) {
        StorageReference temp = mFirebaseStorage.getReferenceFromUrl(reference);
        temp.delete();
    }

    public static void addBroadcastImageReference(String circleId, String broadcastId, String imageUrl) {
        STORAGE_REFERENCES.child(circleId).child(broadcastId).setValue(imageUrl);
    }

    public static void addCircleImageReference(String circleId, String imageUrl) {
        STORAGE_REFERENCES.child(circleId).child("CircleIcon").setValue(imageUrl);
    }

    public static void NotifyOnclickListener(Context context, Notification curent, int position, String broadcastId) {
        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(FirebaseRetrievalViewModel.class);
        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsParticularCircleLiveData(curent.getCircleId());
        liveData.observe((LifecycleOwner) context, dataSnapshot -> {
            Circle circle = dataSnapshot.getValue(Circle.class);
            if (circle != null) {
                Log.d("Notification Fragment", "Circle list :: " + circle.toString());
                if (circle.getMembersList().containsKey(SessionStorage.getUser((Activity) context).getUserId())) {
                    SessionStorage.saveCircle((Activity) context, circle);
                    Intent intent = new Intent(context, CircleWall.class);
                    intent.putExtra("broadcastPos", position);
                    intent.putExtra("broadcastId", broadcastId);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                } else {
                    Toast.makeText(context, "Not a member of this circle anymore", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "The Circle has been deleted by Creator", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static void removeCircleImageReference(String circleId, String imageUrl) {
        if(!imageUrl.equals("default"))
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

    public static void removeBroadcastImageReference(String circleId, String broadcastId, String imageUrl) {
        deleteStorageReference(imageUrl);
        STORAGE_REFERENCES.child(circleId).child(broadcastId).removeValue();
    }

}

package circleapp.circlepackage.circle.FirebaseHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.Comment;
import circleapp.circlepackage.circle.data.ObjectModels.Notification;
import circleapp.circlepackage.circle.data.LocalObjectModels.Poll;
import circleapp.circlepackage.circle.data.ObjectModels.ReportAbuse;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circlepackage.circle.ui.MainActivity;

public class FirebaseWriteHelper {
    private static GlobalVariables globalVariables = new GlobalVariables();

    public static void deleteCircle(Circle circle, User user) {
        //reducing created circle count
        int currentCreatedCount = 0;
        if (user.getCreatedCircles() > 0)
            currentCreatedCount = user.getCreatedCircles() - 1;

        user.setCreatedCircles(currentCreatedCount);
        globalVariables.saveCurrentUser(user);

        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Users").child(user.getUserId()).child("createdCircles").setValue(currentCreatedCount);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circle.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circle.getId()).removeValue();
        removeCircleImageReference(circle.getId(), circle.getBackgroundImageLink());
    }

    public static void exitCircle(Circle circle, User user) {
        //reducing active circle count
        int currentActiveCount = 0;
        if (user.getActiveCircles() > 0)
            currentActiveCount = user.getActiveCircles() - 1;

        user.setActiveCircles(currentActiveCount);
        globalVariables.saveCurrentUser(user);

        globalVariables.getFBDatabase().getReference("/Users").child(user.getUserId()).child("activeCircles").setValue(currentActiveCount);
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();
    }

    public static void deleteBroadcast(String circleId, Broadcast broadcast, int noOfBroadcasts, User user) {
        Log.d("wefkjn", "tempListening.toString()");
        if (broadcast.isImageExists())
            removeBroadcastImageReference(circleId, broadcast.getId(), broadcast.getAttachmentURI());

        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcast.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcast.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("noOfBroadcasts").setValue(noOfBroadcasts-1);
        updateUserObjectWhenDeletingBroadcast(user, broadcast);
    }

    public static void updateUserObjectWhenDeletingBroadcast(User user, Broadcast broadcast) {
        //remove listening broadcast
        List<String> tempListening;
        if (user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId())) {
            tempListening = new ArrayList<>(user.getMutedBroadcasts());
            tempListening.remove(broadcast.getId());
            user.setMutedBroadcasts(tempListening);
            updateUser(user);
        }

        //remove no of read discussions
        HashMap<String, Integer> userNoReadComments;
        if (user.getNoOfReadDiscussions() != null && user.getNoOfReadDiscussions().containsKey(broadcast.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userNoReadComments = new HashMap<>(user.getNoOfReadDiscussions());
            userNoReadComments.remove(broadcast.getId());
            user.setNoOfReadDiscussions(userNoReadComments);
            Log.d("wefkjn", userNoReadComments.toString());
            updateUser(user);
        }

        //remove new timestamp comments
        HashMap<String, Long> userCommentTimestamps;
        if (user.getNewTimeStampsComments() != null && user.getNewTimeStampsComments().containsKey(broadcast.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userCommentTimestamps = new HashMap<>(user.getNewTimeStampsComments());
            userCommentTimestamps.remove(broadcast.getId());
            user.setNewTimeStampsComments(userCommentTimestamps);
            Log.d("wefkjn", userCommentTimestamps.toString());
            updateUser(user);
        }
    }

    public static void writeBroadcast(String circleId, Broadcast broadcast, int newCount) {
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("noOfBroadcasts").setValue(newCount);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcast.getId()).setValue(broadcast);
        addBroadcastImageReference(circleId, broadcast.getId(), broadcast.getAttachmentURI());
    }

    public static StorageReference getStorageReference(String dbReference) {
        return globalVariables.getFirebaseStorage().getReference().child(dbReference);
    }
    public static void setPersistenceEnabled(Context context, boolean toggle){
        SharedPreferences persistenceCheckPrefs = context.getSharedPreferences("PERSISTENCECHECK", Activity.MODE_PRIVATE);
        if (persistenceCheckPrefs.getBoolean(MainActivity.class.getCanonicalName(), true)) {
            persistenceCheckPrefs.edit().putBoolean(MainActivity.class.getCanonicalName(),false).apply();
            globalVariables.getFBDatabase().setPersistenceEnabled(toggle);
        }
    }

    public static void updateUserNewTimeStampComments(String userId, String broadcastId, long latestTimestamp) {
        globalVariables.getFBDatabase().getReference("/Users").child(userId).child("newTimeStampsComments").child(broadcastId).setValue(latestTimestamp);
    }

    public static void updateUserNewReadComments(String userId, String broadcastId, long numberOfComments) {
        globalVariables.getFBDatabase().getReference("/Users").child(userId).child("noOfReadDiscussions").child(broadcastId).setValue(numberOfComments);
    }

    public static void updateUserCount(String userId, String circleId, long noOfBroadcasts) {
        globalVariables.getFBDatabase().getReference("/Users").child(userId).child("notificationsAlert").child(circleId).setValue(noOfBroadcasts);
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
            globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
            //adding userID to applicants list
            globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
            SendNotification.sendApplication("new_applicant",user,circle,subscriber);
            SendNotification.sendnotification("new_applicant", circle.getId(), circle.getName(), circle.getCreatorID(), subscriber.getToken_id(), subscriber.getName());
        } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
            globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
            //adding userID to members list in circlesReference
            globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);

            int nowActive = user.getActiveCircles() + 1;
            user.setActiveCircles(nowActive);
            updateUser(user);
        }
    }

    public static void acceptApplicant(String circleId, Subscriber selectedApplicant, Context context) {
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("members").child(selectedApplicant.getId()).setValue(selectedApplicant);
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("membersList").child(selectedApplicant.getId()).setValue(true);
        UserViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(selectedApplicant.getId());
        AtomicInteger noOfActiveCircles = new AtomicInteger();
        tempLiveData.observe((LifecycleOwner) context, dataSnapshot -> {
            User tempUser = dataSnapshot.getValue(User.class);
            noOfActiveCircles.set(tempUser.getActiveCircles());
        });
        globalVariables.getFBDatabase().getReference("/Users").child(selectedApplicant.getId()).child("activeCircles").setValue(noOfActiveCircles.get() +1);
    }

    public static void rejectApplicant(String circleId, Subscriber selectedApplicant) {
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("applicants").child(selectedApplicant.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("applicantsList").child(selectedApplicant.getId()).removeValue();
    }

    public static void updateUser(User user) {
        Log.d("userTag",user.toString());
        globalVariables.saveCurrentUser(user);
        globalVariables.getFBDatabase().getReference("/Users").child(user.getUserId()).setValue(user);

    }

    public static void updateCirclePersonnel(User user, Circle circle, Subscriber subscriber){
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
        Log.d("123457","JobDone!!!!!!");
    }

    public static void updateBroadcast(Broadcast broadcast, String circleId) {
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcast.getId()).setValue(broadcast);
        globalVariables.saveCurrentBroadcast(broadcast);
    }

    public static void updateCircle(Circle circle) {
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).setValue(circle);
        globalVariables.saveCurrentCircle(circle);
    }


    public static void makeFeedbackEntry(Map<String, Object> map) {
        globalVariables.getFBDatabase().getReference("UserFeedback").child(globalVariables.getCurrentUser().getDistrict()).push().setValue(map);
    }

    public static void makeNewComment(Comment comment, String circleId, String broadcastId) {
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).push().setValue(comment);
    }
    public static String getCommentId(String circleId, String broadcastId){
        String commentId = globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).push().getKey();
        return commentId;
    }

    public static void createUserMadeCircle(Circle circle, Subscriber subscriber) {
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).setValue(circle);
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("members").child(subscriber.getId()).setValue(subscriber);
        if(!circle.getBackgroundImageLink().equals("default"))
        addCircleImageReference(circle.getId(),circle.getBackgroundImageLink());
    }

    public static void writeCommentNotifications(Context context, Notification notification, HashMap<String, Boolean> listenersList, String message, String title) {
        Set<String> member;
        UserViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
        if (listenersList != null) {
            listenersList.remove(notification.getCreatorId());
            member = listenersList.keySet();
            for (String i : member)
            {
                LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(i);
                liveData.observe((LifecycleOwner) context, dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        String tokenId = user.getToken_id();
                        String state = "comment";
                        HelperMethodsBL.pushFCM(state, null,tokenId,notification,null, message,title, null,null,null);
                        globalVariables.getFBDatabase().getReference("/Notifications").child(i).child(notification.getNotificationId()).setValue(notification);
                    } else {
                    }
                });
            }

        }

    }


    public static void writeBroadcastNotifications(Context context, Notification notification, HashMap<String, Boolean> membersList, Broadcast broadcast) {

        Set<String> member;
        UserViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
        String apiurl = "https://circle-d8cc7.web.app/api/";
        if (membersList != null) {
            member = membersList.keySet();
            for (String i : member)

            {
                LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(i);
                liveData.observe((LifecycleOwner) context, dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        String tokenId = user.getToken_id();
                        String state = "broadcast";
                        HelperMethodsBL.pushFCM(state, null, tokenId,notification,broadcast, null, null,null,null,null);
                        globalVariables.getFBDatabase().getReference("/Notifications").child(i).child(notification.getNotificationId()).setValue(notification);
                    } else {
                    }
                });
            }


        }
    }

    public static void writeNormalNotifications(Notification notification, String token_id, String name) {
        String state = "applicant";
        String application_state = notification.getState();
        HelperMethodsBL.pushFCM(state,application_state, token_id,notification,null, null, name,null, null,null);
        globalVariables.getFBDatabase().getReference("/Notifications").child(notification.getNotify_to()).child(notification.getNotificationId()).setValue(notification);
    }

    public static String getNotificationId(String objectId) {
        String notificationId = globalVariables.getFBDatabase().getReference("/Notifications").child(objectId).push().getKey();
        return notificationId;
    }

    public static String getBroadcastId(String circleId) {
        String broadcastId = globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).push().getKey();
        return broadcastId;
    }

    public static String getCircleId() {
        String circleId = globalVariables.getFBDatabase().getReference("/Circles").push().getKey();
        return circleId;
    }

    public static String getUserId() {
        String userId = globalVariables.getFBDatabase().getReference("/Users").child(globalVariables.getAuthenticationToken().getCurrentUser().getUid()).push().getKey();
        return userId;
    }

    public static void addDistrict(String district) {
        globalVariables.getFBDatabase().getReference("Locations").child(district).setValue(true);
    }

    public static void broadcastListenerList(int transaction, String userId, String circleId, String broadcastId) {
        if (transaction == 0)
            globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcastId).child("listenersList").child(userId).setValue(true);
        else
            globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcastId).child("listenersList").child(userId).removeValue();
    }

    public static String createPhotoBroadcast(String title, String photoUri, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {

        String id = HelperMethodsUI.uuidGet();
        Broadcast broadcast = new Broadcast(id, title, null, photoUri, creatorName, null, "AdminId", false, true, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments,true);
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createPollBroadcast(String text, String creatorName, int offsetTimeStamp, HashMap<String, Integer> pollOptions, String downloadUri, int noOfComments, String circleId) {
        String id = HelperMethodsUI.uuidGet();
        Broadcast broadcast;
        Poll poll = new Poll(text, pollOptions, null);
        if (downloadUri != null)
            broadcast = new Broadcast(id, null, null, downloadUri, creatorName, null, "AdminId", true, true, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments,true);
        else
            broadcast = new Broadcast(id, null, null, null, creatorName, null, "AdminId", true, false, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments,true);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createMessageBroadcast(String title, String message, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {
        String id = HelperMethodsUI.uuidGet();
        Broadcast broadcast = new Broadcast(id, title, message, null, creatorName, null, "AdminId", false, false, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments, true);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createDefaultCircle(String name, String description, String acceptanceType, String creatorName, String district, int noOfBroadcasts, int noOfDiscussions, String category) {
        String id = HelperMethodsUI.uuidGet();
        Circle circle = new Circle(id, name, description, acceptanceType, "Everybody", "CreatorAdmin", creatorName, category, "default", null, null, district, null, System.currentTimeMillis(), noOfBroadcasts, noOfDiscussions, true);
        globalVariables.getFBDatabase().getReference("/Circles").child(id).setValue(circle);
        return id;
    }

    public static void createReportAbuse(Context context, String circleID, String broadcastID, String commentID, String creatorID, String userID, String reportType) {
        String id = HelperMethodsUI.uuidGet();
        ReportAbuse reportAbuse = new ReportAbuse(id, circleID, broadcastID, commentID, creatorID, userID, reportType);
        if (globalVariables.getAuthenticationToken().getCurrentUser().getUid() == creatorID) {
            Toast.makeText(context, "Stop Reporting your own Content", Toast.LENGTH_SHORT).show();
        } else {

            globalVariables.getFBDatabase().getReference("ReportAbuse").child(id).setValue(reportAbuse);
        }
    }

    public static void signOutAuth() {
        globalVariables.getAuthenticationToken().signOut();
    }

    public static FirebaseAuth getAuthToken() {
        return globalVariables.getAuthenticationToken();
    }

    public static void deleteStorageReference(String reference) {
        StorageReference temp = globalVariables.getFirebaseStorage().getReferenceFromUrl(reference);
        temp.delete();
    }

    public static void addBroadcastImageReference(String circleId, String broadcastId, String imageUrl) {
        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).child(broadcastId).setValue(imageUrl);
    }

    public static void addCircleImageReference(String circleId, String imageUrl) {
        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).child("CircleIcon").setValue(imageUrl);
    }

    public static void NotifyOnclickListener(Context context, Notification curent, int position, String broadcastId) {
        MyCirclesViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(MyCirclesViewModel.class);
        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsParticularCircleLiveData(curent.getCircleId());
        liveData.observe((LifecycleOwner) context, dataSnapshot -> {
            Circle circle = dataSnapshot.getValue(Circle.class);
            if (circle != null) {
                Log.d("Notification Fragment", "Circle list :: " + circle.toString());
                if(circle.getMembersList()==null)
                    Toast.makeText(context, "Not a member of this circle anymore", Toast.LENGTH_SHORT).show();
                else if (circle.getMembersList().containsKey(globalVariables.getCurrentUser().getUserId())) {
                    globalVariables.saveCurrentCircle(circle);
                    Intent intent = new Intent(context, CircleWall.class);
                    intent.putExtra("broadcastPos", position);
                    intent.putExtra("broadcastId", broadcastId);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
                else {
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

        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> broadcastImageList;
                if(dataSnapshot.exists()){
                    broadcastImageList = (Set<String>) ((HashMap<String, String>) dataSnapshot.getValue());
                    for (String broadcastImage : broadcastImageList){
                        deleteStorageReference(broadcastImage);
                    }
                } else {
                    broadcastImageList = Collections.<String>emptySet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).removeValue();
    }

    public static void removeBroadcastImageReference(String circleId, String broadcastId, String imageUrl) {
        deleteStorageReference(imageUrl);
        globalVariables.getFBDatabase().getReference("StorageReferences").child(circleId).child(broadcastId).removeValue();
    }

}

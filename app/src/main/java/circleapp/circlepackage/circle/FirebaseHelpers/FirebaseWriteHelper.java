package circleapp.circlepackage.circle.FirebaseHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.Comment;
import circleapp.circlepackage.circle.data.ObjectModels.Notification;
import circleapp.circlepackage.circle.data.ObjectModels.Poll;
import circleapp.circlepackage.circle.data.ObjectModels.ReportAbuse;
import circleapp.circlepackage.circle.data.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.MyCirclesViewModel;
import circleapp.circlepackage.circle.ViewModels.UserViewModel;

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
            SendNotification.sendApplication("new_applicant",user,circle,subscriber);
            SendNotification.sendnotification("new_applicant", circle.getId(), circle.getName(), circle.getCreatorID(), subscriber.getToken_id(), subscriber.getName());
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
        UserViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);
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
                        SessionStorage.saveUser((Activity) context, user);
                        String state = "comment";
                        HelperMethods.pushFCM(state, null,tokenId,notification,null, message,title, null,null,null);
                        NOTIFS_REF.child(i).child(notification.getNotificationId()).setValue(notification);
                    } else {
                    }
                });
            }
//                NOTIFS_REF.child(i).child(notification.getNotificationId()).setValue(notification);

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
                        HelperMethods.pushFCM(state, null, tokenId,notification,broadcast, null, null,null,null,null);
                        NOTIFS_REF.child(i).child(notification.getNotificationId()).setValue(notification);
                    } else {
                    }
                });
            }


        }
    }

    public static void writeNormalNotifications(Notification notification, String token_id, String name) {
        String state = "applicant";
        String application_state = notification.getState();
        HelperMethods.pushFCM(state,application_state, token_id,notification,null, null, name,null, null,null);
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
        Broadcast broadcast = new Broadcast(id, title, null, photoUri, creatorName, null, "AdminId", false, true, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments,true);
        broadcastsDB.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createPollBroadcast(String text, String creatorName, int offsetTimeStamp, HashMap<String, Integer> pollOptions, String downloadUri, int noOfComments, String circleId) {
        String id = HelperMethods.uuidGet();
        Broadcast broadcast;
        Poll poll = new Poll(text, pollOptions, null);
        if (downloadUri != null)
            broadcast = new Broadcast(id, null, null, downloadUri, creatorName, null, "AdminId", true, true, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments,true);
        else
            broadcast = new Broadcast(id, null, null, null, creatorName, null, "AdminId", true, false, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments,true);
        BROADCASTS_REF.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createMessageBroadcast(String title, String message, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {
        String id = HelperMethods.uuidGet();
        Broadcast broadcast = new Broadcast(id, title, message, null, creatorName, null, "AdminId", false, false, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments, true);
        BROADCASTS_REF.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createDefaultCircle(String name, String description, String acceptanceType, String creatorName, String district, int noOfBroadcasts, int noOfDiscussions, String category) {
        String id = HelperMethods.uuidGet();
        Circle circle = new Circle(id, name, description, acceptanceType, "Everybody", "CreatorAdmin", creatorName, category, "default", null, null, district, null, System.currentTimeMillis(), noOfBroadcasts, noOfDiscussions, true);
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
        MyCirclesViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(MyCirclesViewModel.class);
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

        STORAGE_REFERENCES.child(circleId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        STORAGE_REFERENCES.child(circleId).removeValue();
    }

    public static void removeBroadcastImageReference(String circleId, String broadcastId, String imageUrl) {
        deleteStorageReference(imageUrl);
        STORAGE_REFERENCES.child(circleId).child(broadcastId).removeValue();
    }

public static void sendFCMPush(String tokenId, Context context) {
    Log.e("FCM", "Func called");
    String SERVER_KEY = String.valueOf(R.string.serverkey);
    String msg = "this is test message";
    String title = "my title";
    String token = tokenId;
    String FCM_URL= "https://fcm-xmpp.googleapis.com:5236";

    JSONObject obj = null;
    JSONObject objData = null;
    JSONObject dataobjData = null;

    try {
        obj = new JSONObject();
        objData = new JSONObject();

        objData.put("text", msg);
        objData.put("title", title);
//        objData.put("sound", "default");
//        objData.put("icon", "icon_name"); //   icon_name
//        objData.put("tag", token);
//        objData.put("priority", "high");

        dataobjData = new JSONObject();
        dataobjData.put("text", msg);
        dataobjData.put("title", title);

        obj.put("to", token);
        //obj.put("priority", "high");

        obj.put("notification", objData);
        obj.put("data", dataobjData);
        Log.e("return here>>", obj.toString());
    } catch (JSONException e) {
        e.printStackTrace();
    }

    JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, FCM_URL, obj,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("True", response + "");
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("False", error + "");
                }
            }) {
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> params = new HashMap<String, String>();
            params.put("Authorization", "key=" + SERVER_KEY);
            params.put("Content-Type",  "application/json; charset=utf-8");
            params.put("Connection","close");
            return params;
        }
    };
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    int socketTimeout = 1000 * 60;// 60 seconds
    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    jsObjRequest.setRetryPolicy(policy);
    requestQueue.add(jsObjRequest);
}
}

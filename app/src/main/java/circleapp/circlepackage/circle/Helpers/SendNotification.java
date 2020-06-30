package circleapp.circlepackage.circle.Helpers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SendNotification {
    private static String TAG = SendNotification.class.getSimpleName();
//    Circle circle = SessionStorage.getCircle(SendNotification.class.getCon);
    public static void sendCommentInfo(String userID, String broadcastId, String circleName,String circleId, String creatorName, HashMap<String, Boolean> listenersList,String circleIcon){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userNotify;
        FirebaseAuth currentUser =FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        userNotify = database.getReference("Notifications");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


        String notificationId = userNotify.child(broadcastId).push().getKey();
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        Map<String, Object> applicationStatus = new HashMap<>();
        String from = firebaseAuth.getCurrentUser().getUid();
        String getDate = getCurrentDateStamp();
        applicationStatus.put("state", "comment_added");
        applicationStatus.put("circleName", circleName);
        applicationStatus.put("circleId", circleId);
        applicationStatus.put("circleIcon", circleIcon);
        applicationStatus.put("broadcastId", broadcastId);
        applicationStatus.put("creatorName", creatorName);
        applicationStatus.put("creatorId", currentUser.getCurrentUser().getUid());
        applicationStatus.put("notificationId", notificationId);
        applicationStatus.put("date", getDate);
        applicationStatus.put("timestamp", System.currentTimeMillis());

        Set<String> member;
        listenersList.remove(userID);

        if(listenersList!=null) {
            member = listenersList.keySet();
            for (String i : member) {

                userNotify.child(i).child(notificationId).setValue(applicationStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Notification Sent Successfully !!!");
                    }
                });

                db.collection("Users/" + i + "/BroadcastNotification").add(applicationStatus).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Firestore//Broadcast::" + "Notification Sent Successfully !!!");
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Firestore//Broadcast::" + "Notification Sent Failed !!!" + e.toString());
                    }
                });


            }
        }

    }

    public static void sendBCinfo(String broadcastId, String circleName,String circleId, String creatorName, HashMap<String, Boolean> membersList,String circleIcon)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userNotify;
        FirebaseAuth currentUser =FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        userNotify = database.getReference("Notifications");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


        String notificationId = userNotify.child(broadcastId).push().getKey();
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        Map<String, Object> applicationStatus = new HashMap<>();
        String from = firebaseAuth.getCurrentUser().getUid();
        String getDate = getCurrentDateStamp();
        applicationStatus.put("state", "broadcast_added");
        applicationStatus.put("circleName", circleName);
        applicationStatus.put("circleId", circleId);
        applicationStatus.put("circleIcon", circleIcon);
        applicationStatus.put("broadcastId", broadcastId);
        applicationStatus.put("creatorName", creatorName);
        applicationStatus.put("creatorId", currentUser.getCurrentUser().getUid());
        applicationStatus.put("notificationId", notificationId);
        applicationStatus.put("date", getDate);
        applicationStatus.put("timestamp", System.currentTimeMillis());

        Set<String> member;

        if(membersList!=null) {
            member = membersList.keySet();
            for (String i :member)
            {

                userNotify.child(i).child(notificationId).setValue(applicationStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG,"Notification Sended Successfully !!!");
                }
            });

                db.collection("Users/" + i + "/BroadcastNotification").add(applicationStatus).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG,"Firestore//Broadcast::"+"Notification Sended Successfully !!!");
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Firestore//Broadcast::"+"Notification Sended Failed !!!"+e.toString());
                    }
                });


            }
        }



//        userNotify.child(toUserId).child(notificationId).setValue(applicationStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                Log.d(TAG,"Notification Sended Successfully !!!");
//            }
//        });
    }
    public static void sendnotification(String state, String circleId, String circleName, String toUserId) {
//        This is the function to store the

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userNotify;
        FirebaseAuth currentUser =FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        userNotify = database.getReference("Notifications");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


        String notificationId = userNotify.child(toUserId).push().getKey();
        //REFER NOTIFICATIONADAPTER FOR THE STATUS CODES!
        Map<String, Object> applicationStatus = new HashMap<>();
        applicationStatus.put("state", state);
        applicationStatus.put("circleId", circleId);
        String from = firebaseAuth.getCurrentUser().getUid();
        String getDate = getCurrentDateStamp();
        applicationStatus.put("from", from);
        applicationStatus.put("notify_to", toUserId);
        applicationStatus.put("circleName", circleName);
        applicationStatus.put("notificationId", notificationId);
        applicationStatus.put("date", getDate);
        applicationStatus.put("timestamp", System.currentTimeMillis());

        userNotify.child(toUserId).child(notificationId).setValue(applicationStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG,"Notification Sended Successfully !!!");
            }
        });

        db.collection("Users/" + toUserId + "/Notifications").add(applicationStatus).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG,"Firestore::"+"Notification Sended Successfully !!!");
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });


    }

    public static String getCurrentDateStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }


}

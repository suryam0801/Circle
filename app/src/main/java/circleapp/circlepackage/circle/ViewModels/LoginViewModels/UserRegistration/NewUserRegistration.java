package circleapp.circlepackage.circle.ViewModels.LoginViewModels.UserRegistration;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.LocationsViewModel;
import circleapp.circlepackage.circle.data.ObjectModels.User;

import static circleapp.circlepackage.circle.ui.MainActivity.TAG;

public class NewUserRegistration {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void userRegister(Activity activity, String uid, String Name, String district, String ward, String downloadUri, String avatar, String contact, boolean locationExists){
        if (!TextUtils.isEmpty(Name)) {
            //getting the current user id
            String userId = uid;

            //Merging the fname and lname to set the displayname to the user for easy access
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(Name)
                    .build();

            //update the user display name
            FirebaseWriteHelper.getAuthToken().getCurrentUser().updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(activity, "User Registered Successfully", Toast.LENGTH_LONG).show();
                            //Adding the user to collection
                            if (!locationExists){
                                FirebaseWriteHelper.addDistrict(district);
                                createInitialCircles(district);
                            }

                            addUser(userId,downloadUri,Name,contact,avatar,district,ward,activity);
                            Log.d(TAG, "User Registered success fully added");
                            Toast.makeText(activity, "User Registered Successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            //to signout the current firebase user
                            FirebaseWriteHelper.getAuthToken().signOut();
                            //delete the user details
                            FirebaseWriteHelper.getUser().delete();
                        }
                    });

        } else {
            Toast.makeText(activity, "Enter Valid details", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void addUser(String userId, String downloadUri, String Name, String contact, String avatar, String district, String ward, Activity activity){
        //TODO Cleanup this place with FBUtil
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DatabaseReference usersDB = database.getReference("Users");
        User user;
        // storing the tokenid for the notification purposes
        String token_id = FirebaseInstanceId.getInstance().getToken();

        //checking the dowloadUri to store the profile pic
        //if the downloadUri id null then 'default' value is stored
        if (downloadUri != null) {
            //creaeting the user object
            Log.d(TAG, "DownloadURI ::" + downloadUri);
            HashMap<String, Boolean> interestTag = new HashMap<>();
            interestTag.put("null", true);
            user = new User(Name, contact, downloadUri.toString(), userId, 0, 0, 0, token_id, ward,
                    district, null, null, null, null);
        } else if (!avatar.equals("")) {
            HashMap<String, Boolean> interestTag = new HashMap<>();
            interestTag.put("null", true);
            Log.d(TAG, "Avatar :: " + avatar);
            user = new User(Name, contact, avatar, userId, 0, 0, 0, token_id, ward, district,
                    null, null, null, null);
        } else {
            user = new User(Name, contact, "default", userId, 0, 0, 0,
                    token_id, ward, district, null, null, null, null);
        }
        //storing user as a json in file locally
        SessionStorage.saveUser(activity, user);
        //store user in realtime database. (testing possible options for fastest retrieval)
        usersDB.child(userId).setValue(user).addOnCompleteListener(task -> {
            db.collection("Users")
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
            sendIntentsToExploreTabbed(activity);
            activity.finishAfterTransition();
        });
    }

    private static void sendIntentsToExploreTabbed(Activity activity){
        Intent i = new Intent(activity, ExploreTabbedActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivity(i);
    }

    public static void createInitialCircles(String district){
        /*
        //admin circle
        String adminCircleId, adminNormalBroadcastId, adminPollBroadcastId;
        adminCircleId = HelperMethods.createCircle("Meet The Developers", "Get started by joining this circle to connect with the creators and get a crashcourse on how to use The Circle App.",
                "Automatic", "The Circle Team", "Admin World", 2, 0, "The Circle App");
        adminNormalBroadcastId = HelperMethods.createMessageBroadcast("Hi guys, Welcome to Circle", "Use this app to form circles " +
                "to find people around you that enjoy doing the same things as you. Organise events, make announcements and get " +
                "opinions. All on a single platform!", "Admin", 1, 0, adminCircleId);
        HashMap<String, Integer> adminPollOptions = new HashMap<>(); //creating poll options
        adminPollOptions.put("This app is amazing!", 0);
        adminPollOptions.put("I'd like to see some changes", 0);
        adminPollOptions.put("meh :D", 0);
        adminPollBroadcastId = HelperMethods.createPollBroadcast("Use polls like this to quickly get your friends’ opinion about something!", "Admin",
                2, adminPollOptions, null, 0, adminCircleId);
*/

        //quarantine circle
        String quarantineCircleId, quarantineNormalBroadcastId, quarantinePollBroadcastId;
        quarantineCircleId = FirebaseWriteHelper.createDefaultCircle("Quarantine Talks " + district, "Figure out how quarantine life is for the rest of " + district + " and ask any questions or help out your neighbors using this circle",
                "Automatic", "Vijay Ram", district, 2, 0, "Community Discussion");

        quarantineNormalBroadcastId = FirebaseWriteHelper.createMessageBroadcast("Welcome All! Stay Safe!","Hey guys lets use this app to connect with our neighborhood in these times of isolation. I hope we" +
                        " can help eachother stay safe and clarify any doubts in these uncertain times :)", "Mekkala Nair", 1,
                0,quarantineCircleId);

        HashMap<String, Integer> quarantinePollOptions = new HashMap<>(); //creating poll options
        quarantinePollOptions.put("Lets find out at 8 PM", 0);
        quarantinePollOptions.put("Never :(", 0);
        quarantinePollOptions.put("Soon? Please be soon!", 0);
        quarantinePollBroadcastId = FirebaseWriteHelper.createPollBroadcast("How much longer do you guys think our PM will extend lockdown?", "Jacob Abraham",
                2, quarantinePollOptions,"https://firebasestorage.googleapis.com/v0/b/circle-d8cc7.appspot.com/o/modi-us-2126610f-1481508682.jpg?alt=media&token=5ff4230c-945f-4918-9c21-bff5f90c75e9"
                , 0, quarantineCircleId);

        //students circle
        String studentsCircleId, studentsNormalBroadcastId, studentsPollBroadcastId;
        studentsCircleId = FirebaseWriteHelper.createDefaultCircle(district + " Students Hangout!", "Lets use this circle to unite all students in " + district + ". Voice your problems, " +
                        "questions, or anything you need support with. You will never walk alone!", "Automatic", "Srinithi",
                district, 0, 0, "Students & Clubs");

        studentsNormalBroadcastId = FirebaseWriteHelper.createMessageBroadcast("Let's show the unity and power of students!!!", "Welcome guys! Be respectful and have a good time. This circle will be our safe place from parents, college, school, and tests. " +
                "You have the support of all the students from " + district + " here!", "Srinithi", 1, 0, studentsCircleId);

        HashMap<String, Integer> pollOptionsStudentsCircle = new HashMap<>(); //creating poll options
        pollOptionsStudentsCircle.put("no! it will get cancelled!", 0);
        pollOptionsStudentsCircle.put("im preparing :(", 0);
        pollOptionsStudentsCircle.put("screw it! lets go with the flow", 0);

        studentsPollBroadcastId = FirebaseWriteHelper.createPollBroadcast("Do you guys think we will have exams?", "Vijai VJR", 1,
                pollOptionsStudentsCircle,"https://firebasestorage.googleapis.com/v0/b/circle-d8cc7.appspot.com/o/k9rd8iesn6ygrioen9cv.jpg?alt=media&token=220677ac-6e5f-473e-a28d-ae5c034e83e1",
                0, studentsCircleId);
    }


}

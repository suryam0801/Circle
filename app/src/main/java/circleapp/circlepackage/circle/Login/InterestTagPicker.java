package circleapp.circlepackage.circle.Login;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;

import circleapp.circlepackage.circle.Notification.SendNotification;

import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;

import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class InterestTagPicker extends AppCompatActivity {

    private String TAG = InterestTagPicker.class.getSimpleName();

    //Declare all UI elements for the InterestTagPicker Activity
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference broadcastsDB, circlesDB, commentsDB;
    private Button register, skip;
    private String fName, lName, userId, downloadUri, contact, ward, district;
    private List<String> locationTags = new ArrayList<>(), selectedInterestTags = new ArrayList<>();
    private List<String> dbInterestTags = new ArrayList<>();
    private List<String> autoCompleteItemsList = new ArrayList<>();
    private AutoCompleteTextView interestTagsEntry;
    private User user;
    private ChipGroup chipGroup;
    private Button interestTagAdd;
    private DatabaseReference tags, usersDB;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int noOfTagsChosen = 0;
    GatherUserDetails gatherUserDetails;
    ProgressDialog progressDialog;

    AnalyticsLogEvents analyticsLogEvents;


    private HashMap<String, Object> locIntTags = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_interest_tag_picker);

        //To set the Fullscreen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();

        noOfTagsChosen = 0;
        analyticsLogEvents = new AnalyticsLogEvents();
        register = findViewById(R.id.registerButton);
        skip = findViewById(R.id.skip_login_tag_picker);
        chipGroup = findViewById(R.id.interest_tag_chip_group);
        interestTagsEntry = findViewById(R.id.interest_tags_entry);
        interestTagAdd = findViewById(R.id.interest_tag_add_button);

        //Getting Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");

        gatherUserDetails = new GatherUserDetails();

        fName = getIntent().getStringExtra("fName");
        lName = getIntent().getStringExtra("lName");

        ward = getIntent().getStringExtra("ward");
        district = getIntent().getStringExtra("district");
        contact = getIntent().getStringExtra("contact");
        downloadUri = getIntent().getStringExtra("uri");
        progressDialog = new ProgressDialog(InterestTagPicker.this);
        progressDialog.setTitle("Registering User....");
        tags.child("locationInterestTags").child(district.trim()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    locIntTags = (HashMap<String, Object>) snapshot.getValue();

                    Log.d(TAG, "LOC INT TAGS: " + locIntTags.keySet().toString());
                    Log.d(TAG, "LOC INT TAGS: " + ward);


                    chipGroup.removeAllViews();
                    for (HashMap.Entry<String, Object> entry : locIntTags.entrySet()) {
                        Log.d(TAG, "ENTRY FOR LOC INT " + entry.toString());
                        List<String> tempInterests = new ArrayList<>(((HashMap<String, Boolean>) entry.getValue()).keySet());
                        for (String interest : tempInterests) {
                            if (!dbInterestTags.contains(interest)) { //avoid duplicate interests
                                if (entry.getKey().trim().equals(ward.trim()))
                                    dbInterestTags.add(0, interest);
                                else
                                    dbInterestTags.add(interest);

                                autoCompleteItemsList.add("#" + interest);
                            }
                        }

                        String[] arr = autoCompleteItemsList.toArray(new String[0]);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, arr);
                        interestTagsEntry.setAdapter(adapter);
                        //this for loop is used when the user wants to edit interest tag choices
                        for (String interest : selectedInterestTags) { //add selected interests as options even if they are not in the location-interest list
                            if (!dbInterestTags.contains(interest)) {
                                dbInterestTags.add(interest);
                                setTag(interest);
                            }
                        }
                    }
                    //set tags in ward order
                    for (String tag : dbInterestTags)
                        setTag(tag);
                } else {
                    //if location does not exist... write create manual circles
                    createInitialCirlces();
                    dbInterestTags.add("running");
                    dbInterestTags.add("writing");
                    dbInterestTags.add("cooking");
                    dbInterestTags.add("fitness");
                    dbInterestTags.add("music");
                    dbInterestTags.add("biking");

                    for (String interest : dbInterestTags) {
                        setTag(interest);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        register.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            //The function to register the Users with their appropriate details
            progressDialog.show();
            UserReg();
            Log.d(TAG,"Button Clicked");

//            gatherUserDetails.finishActivity(0);

        });

        skip.setOnClickListener(view -> {
            if (!selectedInterestTags.isEmpty()){
                analyticsLogEvents.logEvents(InterestTagPicker.this,"skipped_tags","chosen_tags","interest_tag_picker");
                selectedInterestTags.clear();
            }
            analyticsLogEvents.logEvents(InterestTagPicker.this,"skipped_tags_intenT","not_chosen_tags","interest_tag_picker");
            selectedInterestTags.add("null");
            UserReg();
        });

        //Touch listener to autoenter the # as prefix when user try to enter the new interest tag
        interestTagsEntry.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                String interestTag = interestTagsEntry.getText().toString().replace("#", "");
                if (!interestTag.isEmpty()) {
                    selectedInterestTags.add(interestTag);
                    if (!dbInterestTags.contains(interestTag)) {
                        dbInterestTags.add(interestTag);
                        setTag(interestTag);
                    } else {
                        chipGroup.removeViewAt(dbInterestTags.indexOf(interestTag));
                        setTag(interestTag);
                    }
                }
                handled = true;
            }
            return handled;
        });
        interestTagsEntry.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(interestTagsEntry, InputMethodManager.SHOW_IMPLICIT);
            interestTagsEntry.setText("#");
            interestTagsEntry.setSelection(interestTagsEntry.getText().length());
        });
        interestTagsEntry.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(interestTagsEntry, InputMethodManager.SHOW_IMPLICIT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        interestTagsEntry.setShowSoftInputOnFocus(true);
                    }
                    interestTagsEntry.setText("#");
                    interestTagsEntry.setSelection(interestTagsEntry.getText().length());
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                default:
                    break;
            }
            return true;

        });


        //the add button in edittext to add the new interest in the list
        interestTagAdd.setOnClickListener(view -> {
            String interestTag = interestTagsEntry.getText().toString().replace("#", "");
            if (!interestTag.isEmpty()) {
                if (!selectedInterestTags.contains(interestTag)) {
                    selectedInterestTags.add(interestTag);
                    if (!dbInterestTags.contains(interestTag)) {
                        dbInterestTags.add(interestTag);
                        setTag(interestTag);
                    } else {
                        chipGroup.removeViewAt(dbInterestTags.indexOf(interestTag));
                        setTag(interestTag);
                    }
                } else {
                    interestTagsEntry.setText("#");
                    interestTagsEntry.setSelection(interestTagsEntry.getText().length());
                }
            }
        });
    }

    private void createInitialCirlces() {
        broadcastsDB = database.getReference("Broadcasts");
        circlesDB = database.getReference("Circles");
        commentsDB = database.getReference("BroadcastComments");

        //admin circle
        HashMap<String, Boolean> circleIntTags = new HashMap<>();
        circleIntTags.put("sample", true);
        Circle adminCircle = new Circle("adminCircle", "Meet the developers of Circle",
                "Get started by joining this circle to connect with the creators and get a crashcourse on how to use The Circle App.",
                "automatic", "CreatorAdmin", "The Circle Team", circleIntTags,
                null, null, "test", null, System.currentTimeMillis(), 0);

        HashMap<String, Integer> pollOptions = new HashMap<>(); //creating poll options
        pollOptions.put("It's going to rain tomorrow", 0);
        pollOptions.put("No way, its dry as a dog biscuit", 0);
        Poll adminPoll = new Poll("Use polls like this to quickly get your friends’ opinion about something!", pollOptions, null);
        Broadcast commentBroadcast = new Broadcast("commentBroadcast", "You can have a discussion about your posts down in the " +
                "comments below. Click on Go to discussion to see the secret message. :)", null, "Jacob",
                "AdminId", false,
                (System.currentTimeMillis() - 1), null, "default");
        Broadcast pollBroadcast = new Broadcast("pollBroadcast", null, null, "Abrar", "AdminId", true,
                System.currentTimeMillis(), adminPoll, "default");
        Broadcast introBroadcast = new Broadcast("introBroadcast", "Welcome to Circle! Your friendly neighborhood app. Form circles " +
                "to find people around you that enjoy doing the same things as you. Organise events, make announcements and get " +
                "opinions - all on a single platform.", null, "Surya", "AdminId", false,
                (System.currentTimeMillis() + 1), null, "default");

        Comment comment = new Comment("Srinithi", "The answer to life is not 42. It's the bonds you build " +
                "around your circle.",
                "adminCommentId", null, System.currentTimeMillis());

        circlesDB.child("adminCircle").setValue(adminCircle);
        broadcastsDB.child("adminCircle").child("introBroadcast").setValue(introBroadcast);
        broadcastsDB.child("adminCircle").child("pollBroadcast").setValue(pollBroadcast);
        broadcastsDB.child("adminCircle").child("commentBroadcast").setValue(commentBroadcast);
        commentsDB.child("adminCircle").child("commentBroadcast").child("adminCommentId").setValue(comment);

        //running circle
        String runningCircleID = UUID.randomUUID().toString();
        String runningBroadcastID = UUID.randomUUID().toString();
        String runningBroadcasPollID = UUID.randomUUID().toString();
        String runningCommentID = UUID.randomUUID().toString();
        Circle runningCircle = new Circle(runningCircleID, "Morning Runner's " + district,
                "Hi guys, i would love to form a morning running group for anybody in " + district + ". Please join if you would like to be part of this friendly runner's circle",
                "automatic", "CreatorAdmin", "Vijay Ram", circleIntTags,
                null, null, district, ward, System.currentTimeMillis(), 0);
        HashMap<String, Integer> pollOptionsRunningCircle = new HashMap<>(); //creating poll options
        pollOptionsRunningCircle.put("Sure!", 0);
        pollOptionsRunningCircle.put("Thats too early :(", 0);
        Poll runningPoll = new Poll("Hey guys! Can we go running every friday early in the morning?", pollOptionsRunningCircle, null);
        Broadcast runnersBroadcastMessage = new Broadcast(runningBroadcastID, "Hi all! This is a group to find mates to go on daily runs with. Runners of all levels welcome!", null, "Vijay Ram", "AdminId", false,
                System.currentTimeMillis(), null, "default");
        Broadcast runnersBroadcastPoll = new Broadcast(runningBroadcasPollID, null, null, "Vijay Ram", "AdminId", true,
                System.currentTimeMillis(), runningPoll, "default");
        Comment runnerComment = new Comment("Madhu mitha", "Hey where do you guys go running?",
                runningCommentID, null, System.currentTimeMillis());
        circlesDB.child(runningCircleID).setValue(runningCircle);
        broadcastsDB.child(runningCircleID).child(runningBroadcastID).setValue(runnersBroadcastMessage);
        broadcastsDB.child(runningCircleID).child(runningBroadcasPollID).setValue(runnersBroadcastPoll);
        commentsDB.child(runningCircleID).child(runningBroadcastID).child(runningCommentID).setValue(runnerComment);

        //students circle
        String studentsCircleID = UUID.randomUUID().toString();
        String studentsBroadcastID = UUID.randomUUID().toString();
        String studentsBroadcastPollID = UUID.randomUUID().toString();
        String studentsCommentID = UUID.randomUUID().toString();
        String studentsCommentIDResponse = UUID.randomUUID().toString();
        Circle cookingCircle = new Circle(studentsCircleID, district + " Students Hangout!",
                "Lets use this circle to unite all students in " + district + ". Voice your problems, questions, or anything you need support with. You will never walk alone!",
                "automatic", "CreatorAdmin", "Malavika Kumar", circleIntTags,
                null, null, district, ward, System.currentTimeMillis(), 0);
        HashMap<String, Integer> pollOptionsStudentsCircle = new HashMap<>(); //creating poll options
        pollOptionsStudentsCircle.put("no! it will get cancelled!", 0);
        pollOptionsStudentsCircle.put("im preparing :(", 0);
        pollOptionsStudentsCircle.put("screw it! lets go with the flow", 0);
        Poll cookingPoll = new Poll("Are you guys still preparing for exams?", pollOptionsStudentsCircle, null);
        Broadcast studentBroadcast = new Broadcast(studentsBroadcastID, "Welcome guys! Be respectful and have a good time. This circle will be our safe place from parents, college, school, and tests. You have the support of all the students from " + district + " here!", null, "Mekkala Nair", "AdminId", false,
                System.currentTimeMillis(), null, "default");
        Broadcast studentBroadcastPoll = new Broadcast(studentsBroadcastPollID, null, null, "Mekkala Nair", "AdminId", true,
                System.currentTimeMillis(), cookingPoll, "default");
        Comment studentComment = new Comment("Arijit Samuel", "Can i post promotions for my college events here?",
                studentsCommentID, null, (System.currentTimeMillis() - (1800 * 1000)));
        Comment studentCommentResponse = new Comment("Mekkala Nair", "Yeah that's not a problem!",
                studentsCommentIDResponse, null, System.currentTimeMillis());
        circlesDB.child(studentsCircleID).setValue(cookingCircle);
        broadcastsDB.child(studentsCircleID).child(studentsBroadcastID).setValue(studentBroadcast);
        broadcastsDB.child(studentsCircleID).child(studentsBroadcastPollID).setValue(studentBroadcastPoll);
        commentsDB.child(studentsCircleID).child(studentsBroadcastID).child(studentsCommentID).setValue(studentComment);
        commentsDB.child(studentsCircleID).child(studentsBroadcastID).child(studentsCommentIDResponse).setValue(studentCommentResponse);

        //quarantine circle
        String quarantineCircleID = UUID.randomUUID().toString();
        String quarantineBroadcastID = UUID.randomUUID().toString();
        String quarantineBroadcastPollID = UUID.randomUUID().toString();
        String quarantineCommentID = UUID.randomUUID().toString();
        Circle quarantineCircle = new Circle(quarantineCircleID,  "Quarantine Talks " + district,
                "Figure out how quarantine life is for the rest of " + district + " and ask any questions or help out your neighbors using this circle",
                "automatic", "CreatorAdmin", "Surya Manivannan", circleIntTags,
                null, null, district, ward, System.currentTimeMillis(), 0);
        HashMap<String, Integer> pollOptionsQuarantineCircle = new HashMap<>(); //creating poll options
        pollOptionsQuarantineCircle.put("1 month", 0);
        pollOptionsQuarantineCircle.put("2 months", 0);
        pollOptionsQuarantineCircle.put("3 months", 0);
        pollOptionsQuarantineCircle.put("haven't even started", 0);
        Poll quarantinePoll = new Poll("How long have you been in quarantine?", pollOptionsQuarantineCircle, null);
        Broadcast quarantineBroadcast = new Broadcast(quarantineBroadcastID, "Hey guys lets use this app to connect with our neighborhood in these times of isolation. I hope we can help eachother stay safe and clarify any doubts in these uncertain times :)", null, "Mekkala Nair", "AdminId", false,
                (System.currentTimeMillis() - (1800 * 1000)), null, "default");
        Broadcast quarantineBroadcastPoll = new Broadcast(quarantineBroadcastPollID, null, null, "Mekkala Nair", "AdminId", true,
                System.currentTimeMillis(), quarantinePoll, "default");
        Comment quarantineComment = new Comment("Nithin M", "Where are you guys buying your essentials?",
                quarantineCommentID, null, (System.currentTimeMillis()));
        circlesDB.child(quarantineCircleID).setValue(quarantineCircle);
        broadcastsDB.child(quarantineCircleID).child(quarantineBroadcastID).setValue(quarantineBroadcast);
        broadcastsDB.child(quarantineCircleID).child(quarantineBroadcastPollID).setValue(quarantineBroadcastPoll);
        commentsDB.child(quarantineCircleID).child(quarantineBroadcastID).child(quarantineCommentID).setValue(quarantineComment);
    }

    // Function to add a chip to chipgroup
    private void setTag(final String name) {
        final Chip chip = new Chip(this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setRippleColor(ColorStateList.valueOf(Color.WHITE));
        chip.setPadding(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 3,
                        getResources().getDisplayMetrics()
                ),
                paddingDp, paddingDp, paddingDp);
        //chip name
        if (!name.contains("#"))
            chip.setText("#" + name);
        else
            chip.setText(name);

        if (selectedInterestTags.contains(name.replace("#", ""))) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
            chip.setTextColor(Color.BLACK);
        }
        //onclick listener to chip for select the chip from the chipgroup
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chip.getChipBackgroundColor().getDefaultColor() == -9655041) {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
                    chip.setTextColor(Color.BLACK);
                    selectedInterestTags.remove(chip.getText().toString().replace("#", ""));
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
                    chip.setTextColor(Color.WHITE);
                    selectedInterestTags.add(chip.getText().toString().replace("#", ""));

                }

                Log.d(TAG, "INTEREST TAG LIST: " + selectedInterestTags.toString());
            }

        });
        //Add a chip to the Chipgroup
        if (selectedInterestTags.contains(name))
            chipGroup.addView(chip, 0);
        else
            chipGroup.addView(chip);

        //set the # in edittext after adding the new location
        interestTagsEntry.setText("#");
        interestTagsEntry.setSelection(interestTagsEntry.getText().length());
    }

    public void UserReg() {
        Log.d(TAG,"User reg called");
        //Ensure the textboxes are not empty
        if (!TextUtils.isEmpty(fName) && !TextUtils.isEmpty(lName)) {
            //getting the current user id
            userId = firebaseAuth.getInstance().getCurrentUser().getUid();

            //Merging the fname and lname to set the displayname to the user for easy access
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fName + " " + lName)
                    .build();

            //update the user display name
            firebaseAuth.getCurrentUser().updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                noOfTagsChosen = selectedInterestTags.size();
                                Toast.makeText(InterestTagPicker.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                                analyticsLogEvents.logEvents(InterestTagPicker.this,"no_of_tags_chosen",noOfTagsChosen+"","interest_tag_picker");

                                //Adding the user to collection
                                addUser();
                                Log.d(TAG,"User Registered success fully added");
                                Toast.makeText(InterestTagPicker.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(InterestTagPicker.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                //to signout the current firebase user
                                firebaseAuth.signOut();
                                //delete the user details
                                firebaseAuth.getCurrentUser().delete();
                            }
                        }
                    });

        } else {
            Toast.makeText(InterestTagPicker.this, "Enter Valid details", Toast.LENGTH_LONG).show();
        }
    }

    //function that adds the user to the firestore
    private void addUser() {

        usersDB = database.getReference("Users");

        // storing the tokenid for the notification purposes
        String token_id = FirebaseInstanceId.getInstance().getToken();

        //set interest and location tags as hashmaps
        HashMap<String, Boolean> locationTagHashmap = new HashMap<>();
        for (String locationTemp : locationTags)
            locationTagHashmap.put(locationTemp, true);

        HashMap<String, Boolean> interestTagHashmap = new HashMap<>();
        for (String interestTemp : selectedInterestTags)
            interestTagHashmap.put(interestTemp, true);


        //checking the dowloadUri to store the profile pic
        //if the downloadUri id null then 'default' value is stored
        if (downloadUri != null) {
            //creaeting the user object
            user = new User(fName, lName, contact, downloadUri, interestTagHashmap, userId, 0, 0, 0, token_id, ward, district, null);
        } else {
            user = new User(fName, lName, contact, "default", interestTagHashmap, userId, 0, 0, 0, token_id, ward, district, null);
        }

        if (!selectedInterestTags.contains("null")) {
            for (String i : selectedInterestTags) {
                tags.child("interestTags").child(i).setValue(true);
                tags.child("locationInterestTags").child(district.trim()).child(ward.trim()).child(i).setValue(true);
            }
        }

        if (!dbInterestTags.contains("null")) {
            for (String i : dbInterestTags) {
                tags.child("interestTags").child(i).setValue(true);
                tags.child("locationInterestTags").child(district.trim()).child(ward.trim()).child(i).setValue(true);
            }
        }

        //storing user as a json in file locally
        String string = new Gson().toJson(user);
        SessionStorage.saveUser(InterestTagPicker.this, user);

        //store user in realtime database. (testing possible options for fastest retrieval)
        usersDB.child(userId).setValue(user).addOnCompleteListener(task -> {
            Log.d(TAG,"User data success fully added realtime db");

            Log.d(TAG,"User data success fully added");
            progressDialog.cancel();
            Intent i = new Intent(InterestTagPicker.this, ExploreTabbedActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY );
            startActivity(i);
            Log.d(TAG,"Intent lines are executed...");
            SendNotification.sendnotification("new_user","adminCircle","Meet the developers of Circle",firebaseAuth.getCurrentUser().getUid());
            sendnotify();
            gatherUserDetails.finishActivity(0);
            finish();
            db.collection("Users")
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
//                        progressDialog.cancel();

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed to create user", Toast.LENGTH_LONG).show();
                        }
                    });
            finish();
        });
    }

    private void sendnotify() {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Circle")
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                R.mipmap.ic_launcher))
                        .setSmallIcon(R.drawable.circle_logo)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentText("Welcome to the Circle " + firebaseAuth.getCurrentUser().getDisplayName() +
                                " You can find the people with same Interest in your Locality");

        Intent notificationIntent = new Intent(this, ExploreTabbedActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        chipGroup.removeAllViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}


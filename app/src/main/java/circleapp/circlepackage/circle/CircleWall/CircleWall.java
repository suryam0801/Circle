package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.PersonelDisplay.PersonelDisplay;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

public class CircleWall extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private FirebaseDatabase database;
    private DatabaseReference broadcastsDB,commentsDB, circlesPersonelDB, circlesDB, usersDB;
    private FirebaseAuth currentUser;

    private String TAG = CircleWall.class.getSimpleName();

    private LinearLayout emptyDisplay;

    private Circle circle;

    private List<String> pollAnswerOptionsList = new ArrayList<>();
    private boolean pollExists = false;

    private ImageButton exitOrDeleteButton, back, viewPersonelButton;
    private User user;

    //create broadcast popup ui elements
    private EditText setTitleET, setMessageET, setPollQuestionET, setPollOptionET;
    private LinearLayout pollCreateView, pollOptionsDisplay, broadcastDisplay;
    private TextView circleBannerName, broadcastHeader;
    private Button btnAddPollOption, btnUploadBroadcast, cancelButton;
    private Dialog createBroadcastPopup, confirmationDialog;
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton poll, newPost;
    String usersState;

    //elements for loading broadcasts, setting recycler view, and passing objects into adapter
    List<Broadcast> broadcastList = new ArrayList<>();
    AnalyticsLogEvents analyticsLogEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall);
        confirmationDialog = new Dialog(CircleWall.this);
        user = SessionStorage.getUser(CircleWall.this);
        circle = SessionStorage.getCircle(CircleWall.this);

        if (getIntent().getBooleanExtra("fromCircleWall", false) == true) {
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
        }

        database = FirebaseDatabase.getInstance();
        broadcastsDB = database.getReference("Broadcasts");
        circlesPersonelDB = database.getReference("CirclePersonel");
        circlesDB = database.getReference("Circles");
        usersDB = database.getReference("Users").child(user.getUserId());
        commentsDB = database.getReference("BroadcastComments");
        broadcastsDB.keepSynced(true);
        currentUser = FirebaseAuth.getInstance();
        analyticsLogEvents = new AnalyticsLogEvents();

        circleBannerName = findViewById(R.id.circleBannerName);
        exitOrDeleteButton = findViewById(R.id.share_with_friend_button);
        back = findViewById(R.id.bck_Circlewall);
        viewPersonelButton = findViewById(R.id.shareCircle);
        emptyDisplay = findViewById(R.id.circle_wall_empty_display);
        emptyDisplay.setVisibility(View.VISIBLE);
        poll = findViewById(R.id.poll_creation_FAB);
        newPost = findViewById(R.id.message_creation_FAB);
        floatingActionMenu = findViewById(R.id.menu);

        if (circle.getCreatorID().equals(user.getUserId()))
            exitOrDeleteButton.setBackground(getResources().getDrawable(R.drawable.ic_delete_forever_black_24dp));

        circleBannerName.setText(circle.getName());


        exitOrDeleteButton.setOnClickListener(view -> {
            if (circle.getCreatorID().equals(user.getUserId()))
                showDeleteDialog();
            else
                showExitDialog();
        });

        viewPersonelButton.setOnClickListener(view -> {
            Intent intent = new Intent(CircleWall.this, PersonelDisplay.class);
            intent.putExtra("userState", usersState);
            startActivity(intent);
            finish();
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
            finish();
        });

        poll.setOnClickListener(view -> {
            showCreateBroadcastDialog("poll");
            floatingActionMenu.close(true);

        });
        newPost.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(CircleWall.this, "add_message", "pressed_button", "circle_wall");
            showCreateBroadcastDialog("message");
            floatingActionMenu.close(true);
        });

        loadCircleBroadcasts();
    }

    private void loadCircleBroadcasts() {

        //initialize recylcerview
        RecyclerView recyclerView = findViewById(R.id.broadcastViewRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        final RecyclerView.Adapter adapter = new BroadcastListAdapter(CircleWall.this, broadcastList, circle);
        recyclerView.setAdapter(adapter);

        broadcastsDB.child(circle.getId()).orderByChild("timeStamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                broadcastList.add(0, broadcast); //to store timestamp values descendingly
                adapter.notifyItemInserted(0);
                recyclerView.setAdapter(adapter);

                emptyDisplay.setVisibility(View.GONE);
                initializeNewCommentsAlertTimestamp(broadcast);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                int position = HelperMethods.returnIndexOfBroadcast(broadcastList, broadcast);
                broadcastList.remove(position);
                adapter.notifyItemRemoved(position);

                broadcastList.add(position, broadcast);
                adapter.notifyItemInserted(position);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                int position = HelperMethods.returnIndexOfBroadcast(broadcastList, broadcast);
                broadcastList.remove(position);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void showExitDialog() {
        confirmationDialog.setContentView(R.layout.exit_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.remove_user_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.remove_user_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            exitCircle();
            confirmationDialog.dismiss();
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    public void showDeleteDialog() {

        confirmationDialog.setContentView(R.layout.delete_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.delete_circle_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.delete_circle_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            deleteCircle();
            confirmationDialog.dismiss();
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    private void deleteCircle() {
        circlesPersonelDB.child(circle.getId()).removeValue();
        circlesDB.child(circle.getId()).removeValue();
        //reducing created circle count
        int currentCreatedCount = user.getCreatedCircles() - 1;
        user.setCreatedCircles(currentCreatedCount);
        usersDB.child("createdCircles").setValue(currentCreatedCount);
        broadcastsDB.child(circle.getId()).removeValue();
        commentsDB.child(circle.getId()).removeValue();
        analyticsLogEvents.logEvents(CircleWall.this, "circle_delete", "delete_button", "circle_wall");
        startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
        finish();
    }

    private void exitCircle() {
        circlesPersonelDB.child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();
        //reducing active circle count
        int currentActiveCount = user.getActiveCircles() - 1;
        user.setActiveCircles(currentActiveCount);
        usersDB.child("activeCircles").setValue(currentActiveCount);
        analyticsLogEvents.logEvents(CircleWall.this, "circle_exit", "exit_button", "circle_wall");
        startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
        finish();
    }

    private void showCreateBroadcastDialog(String flag) {
        createBroadcastPopup = new Dialog(CircleWall.this);
        createBroadcastPopup.setContentView(R.layout.broadcast_create_popup_layout); //set dialog view
        createBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        broadcastHeader = createBroadcastPopup.findViewById(R.id.broadcast_header);
        setTitleET = createBroadcastPopup.findViewById(R.id.broadcastTitleEditText);
        setTitleET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        setMessageET = createBroadcastPopup.findViewById(R.id.broadcastDescriptionEditText);
        setMessageET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        setPollQuestionET = createBroadcastPopup.findViewById(R.id.poll_create_question_editText);
        setPollQuestionET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        setPollOptionET = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_editText);
        setPollOptionET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        pollOptionsDisplay = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_display);
        pollCreateView = createBroadcastPopup.findViewById(R.id.poll_create_layout);
        btnAddPollOption = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_add_btn);
        btnUploadBroadcast = createBroadcastPopup.findViewById(R.id.upload_broadcast_btn);
        broadcastDisplay = createBroadcastPopup.findViewById(R.id.create_broadcast_display);
        cancelButton = createBroadcastPopup.findViewById(R.id.create_broadcast_cancel_btn);

        cancelButton.setOnClickListener(view -> createBroadcastPopup.dismiss());

        //default will show message
        if (flag.equals("poll")) {
            pollCreateView.setVisibility(View.VISIBLE);
            broadcastHeader.setText("Create New Poll");
            broadcastDisplay.setVisibility(View.GONE);
        }
        else
            broadcastHeader.setText("Create New Broadcast");

        btnAddPollOption.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(CircleWall.this, "add_poll", "pressed_button", "circle_wall");

            String option = setPollOptionET.getText().toString();


            if ((option.contains(".") || option.contains("$") || option.contains("#") || option.contains("[") || option.contains("]")|| option.isEmpty())) {
                //checking for invalid characters
                Toast.makeText(getApplicationContext(), "Option cannot use special characters or be empty", Toast.LENGTH_SHORT).show();
            } else {
                if (!option.isEmpty() && !setPollQuestionET.getText().toString().isEmpty()) {

                    final TextView tv = generatePollOptionTV(option);

                    tv.setOnClickListener(view1 -> {
                        pollOptionsDisplay.removeView(tv);
                        pollAnswerOptionsList.remove(tv.getText());
                    });

                    pollAnswerOptionsList.add(option);
                    pollOptionsDisplay.addView(tv);
                    setPollOptionET.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUploadBroadcast.setOnClickListener(view -> {
            if (!pollAnswerOptionsList.isEmpty())
                pollExists = true;

            //only for message broadcast
            if(pollExists == false && setTitleET.getText().toString().isEmpty())
                Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();
            else
                createBroadcast();

        });

        createBroadcastPopup.show();
    }

    private void createBroadcast() {
        createBroadcastPopup.dismiss();

        String currentCircleId = circle.getId();
        String broadcastId = broadcastsDB.child(currentCircleId).push().getKey();
        String pollQuestion = setPollQuestionET.getText().toString();
        String currentUserName = currentUser.getCurrentUser().getDisplayName();
        String currentUserId = currentUser.getCurrentUser().getUid();

        SendNotification.sendBCinfo(broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList());

        //creating poll options hashmap
        HashMap<String, Integer> options = new HashMap<>();
        if (!pollAnswerOptionsList.isEmpty()) {
            for (String option : pollAnswerOptionsList)
                options.put(option, 0);
        }

        if (pollExists) {
            Poll poll = new Poll(pollQuestion, options, null);
            createAndUploadBroadcast(broadcastId, null, null, currentUserName, currentUserId, true, poll);
        } else {
            String title = null;
            String message = null;

            if (!setMessageET.getText().toString().isEmpty())
                message = setMessageET.getText().toString();
            if(!setTitleET.getText().toString().isEmpty())
                title = setTitleET.getText().toString();

            createAndUploadBroadcast(broadcastId, title,  message, currentUserName, currentUserId, false, null);
        }
    }

    public void createAndUploadBroadcast(String broadcastId, String title, String message, String userName, String userId, boolean localPollExists, Poll poll) {
        Broadcast broadcast;
        if(localPollExists) {
            broadcast = new Broadcast(broadcastId, title, message, null, userName, userId, true,
                    System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0);
        } else {
            broadcast = new Broadcast(broadcastId, title, message, null,
                    userName, userId, false, System.currentTimeMillis(), null,
                    user.getProfileImageLink(), 0, 0);
        }

        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        circlesDB.child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
        SessionStorage.saveCircle(CircleWall.this, circle);

        //updating broadcast in broadcast db
        broadcastsDB.child(circle.getId()).child(broadcastId).setValue(broadcast);
        pollExists = false;
        pollAnswerOptionsList.clear();
    }

    public TextView generatePollOptionTV(String option){
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 110);
        lparams.setMargins(0, 10, 20, 0);

        final TextView tv = new TextView(CircleWall.this);
        tv.setLayoutParams(lparams);
        tv.setText(option);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setBackground(getResources().getDrawable(R.drawable.poll_creation_item_option_background));
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_white_24dp, 0);
        tv.setPaddingRelative(40, 10, 40, 10);

        return tv;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CircleWall.this, ExploreTabbedActivity.class);
        startActivity(intent);
        finish();
    }

    //bottom sheet dialog onclick (only called when nagivating from create circle)
    @Override
    public void onButtonClicked(String text) {
        switch (text) {
            case "shareLink":
                HelperMethods.showShareCirclePopup(circle, CircleWall.this);
                break;
            case "copyLink":
                HelperMethods.copyLinkToClipBoard(circle, CircleWall.this);
                break;
        }
    }

    public void initializeNewCommentsAlertTimestamp(Broadcast b){
        HashMap<String, Long> commentTimeStampTemp;
        if(user.getNewTimeStampsComments() == null){
            //first time viewing any comments
            commentTimeStampTemp = new HashMap<>();
            commentTimeStampTemp.put(b.getId(), (long) 0);
            user.setNewTimeStampsComments(commentTimeStampTemp);

            SessionStorage.saveUser(CircleWall.this, user);
            usersDB.child("newTimeStampsComments").child(b.getId()).setValue(b.getLatestCommentTimestamp());
        } else if(user.getNewTimeStampsComments() != null && !user.getNewTimeStampsComments().containsKey(b.getId())){
            //if timestampcomments exists but does not contain value for that particular broadcast
            commentTimeStampTemp = new HashMap<>(user.getNewTimeStampsComments());
            commentTimeStampTemp.put(b.getId(), (long)  0);
            user.setNewTimeStampsComments(commentTimeStampTemp);

            SessionStorage.saveUser(CircleWall.this, user);
            usersDB.child("newTimeStampsComments").child(b.getId()).setValue(b.getLatestCommentTimestamp());
        }
    }

}
package circleapp.circlepackage.circle.Explore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.Notification.NotificationActivity;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.RecyclerItemClickListener;
import circleapp.circlepackage.circle.SessionStorage;

import static androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL;

public class Explore extends AppCompatActivity {

    private String TAG = Explore.class.getSimpleName();
    private List<Circle> exploreCircleList = new ArrayList<>();
    private List<Circle> workbenchCircleList = new ArrayList<>();
    private List<Circle> allCircles = new ArrayList<>();
    private FloatingActionButton btnAddCircle;
    private FirebaseDatabase database;
    private FirebaseAuth currentUser;
    private DatabaseReference circlesDB, usersDB;
    private ImageView profPic, notificationBell;
    private Dialog circleJoinDialog;
    private User user;
    private List<String> userTempinterestTagsList;
    private Uri intentUri;
    private boolean link_flag = false;
    int[] myImageList = new int[]{R.drawable.profile_image, R.drawable.profile_image_black_dude, R.drawable.profile_image_black_woman,
            R.drawable.profile_image_italian_dude, R.drawable.profile_image_lady_glasses};

    long startTimeCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intentUri = getIntent().getData();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        currentUser = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        circlesDB = database.getReference("Circles");
        usersDB = database.getReference().child("Users").child(currentUser.getCurrentUser().getUid());
        circlesDB.keepSynced(true); //synchronizes and stores local copy of data

        btnAddCircle = findViewById(R.id.add_circle_button);
        profPic = findViewById(R.id.explore_profilePicture);
        notificationBell = findViewById(R.id.main_activity_notifications_bell);
        circleJoinDialog = new Dialog(Explore.this);

        user = SessionStorage.getUser(Explore.this);
        SessionStorage.saveUser(Explore.this, user);
        Random r = new Random();
        int count = r.nextInt((4 - 0) + 1);
        Glide.with(getApplicationContext())
                .load(user.getProfileImageLink())
                .placeholder(ContextCompat.getDrawable(Explore.this, myImageList[count]))
                .into(profPic);

        //retrieve interest tags from user
        userTempinterestTagsList = new ArrayList<>(user.getInterestTags().keySet());

        startTimeCircle = System.currentTimeMillis();
        setCircleTabs();
        setWorkbenchTabs();

        //onClick listener for create project button
        btnAddCircle.setOnClickListener(view -> {
            startActivity(new Intent(Explore.this, CreateCircle.class));
            finish();
        });

        notificationBell.setOnClickListener(v -> {
            startActivity(new Intent(Explore.this, NotificationActivity.class));
            finish();
        });

        profPic.setOnClickListener(view -> {
            startActivity(new Intent(Explore.this, EditProfile.class));
            finish();
        });
    }

    private void setWorkbenchTabs() {
        //initialize  workbench recylcerview
        RecyclerView wbrecyclerView = findViewById(R.id.workRecyclerView);
        wbrecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager wblayoutManager = new LinearLayoutManager(getApplicationContext(), HORIZONTAL, false);
        wbrecyclerView.setLayoutManager(wblayoutManager);
        //initializing the WorkbenchDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual circles in the recycler view
        final RecyclerView.Adapter wbadapter = new WorkbenchDisplayAdapter(workbenchCircleList, Explore.this);
        wbrecyclerView.setAdapter(wbadapter);

        wbrecyclerView.addOnItemTouchListener(
                //RecyclerItemClickListener is a gestureDectector class which recognises the type of touch
                new RecyclerItemClickListener(this, wbrecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        SessionStorage.saveCircle(Explore.this, workbenchCircleList.get(position));
                        startActivity(new Intent(Explore.this, CircleWall.class));
                        finish();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        Toast.makeText(getApplicationContext(), "LONG PRESSED", Toast.LENGTH_SHORT).show();
                    }
                }));
        //single value listener for Circles Collection
        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call

        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                allCircles.add(circle);
                //checking if user is a member of the circle
                boolean existingMember = false;
                if (circle.getMembersList() != null) {
                    if (circle.getMembersList().keySet().contains(currentUser.getUid()))
                        existingMember = true;
                }

                //checking for duplicate
                boolean duplicate = false;
                for (Circle c : workbenchCircleList) {
                    if (c.getId().equals(circle.getId())) {
                        duplicate = true;
                    }
                }

                //setting the adapter initially
                //filter for only circles associated with creator id
                if ((circle.getCreatorID().equals(currentUser.getUid()) || existingMember == true) && duplicate == false) {
                    workbenchCircleList.add(circle);
                    //notify the adapter each time a new item needs to be added to the recycler view
                    wbadapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);

                if (circle.getCircleDistrict().equals(user.getDistrict())) {
                    int position = 0;
                    List<Circle> tempCircleList = new ArrayList<>(workbenchCircleList);
                    for (Circle c : tempCircleList) {
                        if (c.getId().equals(circle.getId())) {
                            workbenchCircleList.remove(position);
                            workbenchCircleList.add(position, circle);
                            wbadapter.notifyDataSetChanged();
                        }
                        ++position;
                    }
                }

                boolean existingMember = false;
                if (circle.getMembersList() != null) {
                    if (circle.getMembersList().keySet().contains(currentUser.getUid()))
                        existingMember = true;
                }

                //checking for duplicate
                boolean duplicate = false;
                for (Circle c : workbenchCircleList) {
                    if (c.getId().equals(circle.getId())) {
                        duplicate = true;
                    }
                }

                if (existingMember == true && duplicate == false) {
                    workbenchCircleList.add(circle);
                    wbadapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                if (circle.getCircleDistrict().equals(user.getDistrict())) {
                    int position = 0;
                    for (Circle c : workbenchCircleList) {
                        if (c.getId().equals(circle.getId())) {
                            workbenchCircleList.remove(position);
                            wbadapter.notifyDataSetChanged();
                            break;
                        }
                        ++position;
                    }
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setCircleTabs() {
        //initialize recylcerview
        RecyclerView exploreRecyclerView = findViewById(R.id.circlesRecyclerView);
        exploreRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        exploreRecyclerView.setLayoutManager(layoutManager);
        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        final RecyclerView.Adapter adapter = new CircleDisplayAdapter(Explore.this, exploreCircleList);
        exploreRecyclerView.setAdapter(adapter);

        exploreRecyclerView.addOnItemTouchListener(
                //RecyclerItemClickListener is a gestureDectector class which recognises the type of touch
                new RecyclerItemClickListener(Explore.this, exploreRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        displayJoinPopup(exploreCircleList.get(position));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        //showShareCirclePopup(exploreCircleList.get(position));
                    }
                })
        );

        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                allCircles.add(circle);

                //recieve request on opening
                if (intentUri != null) {
                    List<String> params = intentUri.getPathSegments();
                    String circleID = params.get(params.size() - 1);

                    if (circle.getId().equals(circleID)) {
                        link_flag = true;
                        displayJoinPopup(circle);
                    }
                }

                //admin circle
                if (circle.getCreatorID().equals("CreatorAdmin")) {
                    boolean contains = false;
                    for (Circle c : exploreCircleList) {
                        if (c.getId().equals(circle.getId()))
                            contains = true;
                    }
                    if (contains == false) {
                        if ((circle.getMembersList() != null && circle.getMembersList().keySet().contains(user.getUserId())) && circle.getCreatorName().equals("Admin")) {

                        } else {

                            if (circle.getCreatorName().equals("Admin") && contains == false) {
                                exploreCircleList.add(circle);
                                adapter.notifyDataSetChanged();
                            } else {
                                boolean membersExist = true;
                                if(circle.getMembersList()==null)
                                    membersExist = false;
                                else if (circle.getMembersList().containsKey(user.getUserId()))
                                    membersExist = true;

                                if(circle.getCircleDistrict().equals("test") || circle.getCircleDistrict().equals(user.getDistrict()) ){
                                    if(membersExist==false){
                                        exploreCircleList.add(circle);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }
                }


                if (circle.getCircleDistrict() != null && circle.getCircleDistrict().equals(user.getDistrict())) {
                    //retrieve location & interest tags from circle object since tags are stored as hashmaps
                    List<String> circleIteratorinterestTagsList = new ArrayList<>(circle.getInterestTags().keySet());

                    //checking if user is a member of the circle
                    boolean existingMember = false;
                    if (circle.getMembersList() != null) {
                        if (circle.getMembersList().keySet().contains(currentUser.getUid()))
                            existingMember = true;
                    }

                    if (!circle.getCreatorID().equals(currentUser.getUid()) && existingMember == false) {
                        //setting the adapter initially
                        //filter for only circles associated with matching user location and interests
                        for (String intIterator : userTempinterestTagsList) {
                            if (circleIteratorinterestTagsList.contains(intIterator)) {
                                //check if circle already exists
                                boolean circleExists = false;
                                for (Circle conditional : exploreCircleList) {
                                    if (conditional.getId().equals(circle.getId()))
                                        circleExists = true;
                                }

                                if (circleExists == false) {
                                    exploreCircleList.add(circle);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                if (circle.getCircleDistrict() != null && circle.getCircleDistrict().equals(user.getDistrict())) {
                    int position = 0;
                    List<Circle> tempCircleList = new ArrayList<>(exploreCircleList);
                    for (Circle c : tempCircleList) {
                        if (c.getId().equals(circle.getId())) {
                            if (circle.getMembersList() != null && circle.getMembersList().containsKey(user.getUserId())) {
                                exploreCircleList.remove(position);
                                adapter.notifyDataSetChanged();
                            } else {
                                exploreCircleList.remove(position);
                                exploreCircleList.add(position, circle);
                                adapter.notifyDataSetChanged();
                            }

                        }
                        ++position;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                if (circle.getCircleDistrict().equals(user.getDistrict())) {
                    int position = 0;
                    for (Circle c : exploreCircleList) {
                        if (c.getId().equals(circle.getId())) {
                            exploreCircleList.remove(position);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                        ++position;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayJoinPopup(final Circle circle) {
        circleJoinDialog.setContentView(R.layout.apply_popup_layout);

        final TextView circleName = circleJoinDialog.findViewById(R.id.join_popup_circle_name);
        final TextView creatorName = circleJoinDialog.findViewById(R.id.join_popup_circle_creator);
        final TextView circleDescription = circleJoinDialog.findViewById(R.id.join_popup_cicle_description);
        final Button cancelButton = circleJoinDialog.findViewById(R.id.join_popup_cancel_button);
        final Button acceptButton = circleJoinDialog.findViewById(R.id.join_popup_accept_button);

        circleName.setText(circle.getName());
        creatorName.setText(circle.getCreatorName());
        circleDescription.setText(circle.getDescription());
        circleDescription.setMaxLines(Integer.MAX_VALUE);

        if (("review").equalsIgnoreCase(circle.getAcceptanceType()))
            acceptButton.setText("Apply");

        //checking if user as already applied
        if (circle.getApplicantsList() != null) {
            if (circle.getApplicantsList().keySet().contains(currentUser.getCurrentUser().getUid())) {
                acceptButton.setClickable(false);
                acceptButton.setBackground(getResources().getDrawable(R.drawable.unpressable_button));
                acceptButton.setText("Pending Request");
                acceptButton.setTextColor(Color.parseColor("#D1D1D1"));
            }
        }

        //testing if popup is already in workbench
        if (link_flag == true) {
            for (Circle wbCircle : allCircles) {
                if (wbCircle.getMembersList() != null && wbCircle.getMembersList().keySet().contains((user.getUserId()))) {
                    acceptButton.setClickable(false);
                    acceptButton.setBackground(getResources().getDrawable(R.drawable.unpressable_button));
                    acceptButton.setText("Joined");
                    acceptButton.setTextColor(Color.parseColor("#D1D1D1"));
                }
            }
        }

        acceptButton.setOnClickListener(view -> {
            //creating a subscriber object to store. doesnt store private information such as tags and contact information.
            Subscriber subscriber = new Subscriber(user.getUserId(), user.getFirstName() + " " + user.getLastName(),
                    user.getProfileImageLink(), user.getToken_id(), System.currentTimeMillis());

            if (circle.getId().equals("adminCircle")) {
                SessionStorage.saveCircle(Explore.this, circle);
                link_flag = false;
                startActivity(new Intent(Explore.this, CircleWall.class));
                finish();
            } else {

                if (("review").equalsIgnoreCase(circle.getAcceptanceType())) {
                    database.getReference().child("CirclePersonel").child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
                    //adding userID to applicants list
                    circlesDB.child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
                } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
                    database.getReference().child("CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
                    //adding userID to members list in circlesReference
                    circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);
                    Log.d(TAG, "USER ACTIVE CIRCLES: " + user.getActiveCircles());
                    int nowActive = user.getActiveCircles() + 1;
                    usersDB.child("activeCircles").setValue((nowActive));
                }
                circleJoinDialog.dismiss();
            }

        });

        circleJoinDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                link_flag = false;
            }
        });

        cancelButton.setOnClickListener(view -> circleJoinDialog.dismiss());

        circleJoinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        circleJoinDialog.show();
    }

    private void showShareCirclePopup(Circle c) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Circle: Your friendly neighborhood app");
            String shareMessage = "\nLet me recommend you this application\n\n";
            //https://play.google.com/store/apps/details?id=
            Log.d(TAG, c.getId());
            shareMessage = "www.circleneighborhoodapp.com/" + c.getId();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception error) {

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        intentUri = getIntent().getData();
        //opening link joining
        if (intentUri != null) {
            List<String> params = intentUri.getPathSegments();
            String circleID = params.get(params.size() - 1);

            if (!allCircles.isEmpty()) {
                for (Circle c : allCircles) {
                    if (c.getId().equals(circleID)) {
                        link_flag = true;
                        displayJoinPopup(c);
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        link_flag = false;
        getIntent().setData(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        link_flag = false;
        getIntent().setData(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        finishAffinity();
        System.exit(0);
    }
}

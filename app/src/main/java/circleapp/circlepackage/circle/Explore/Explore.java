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
import com.google.firebase.analytics.FirebaseAnalytics;
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
    int[] myImageList = new int[]{R.drawable.person_blonde_head, R.drawable.person_job, R.drawable.person_singing,
            R.drawable.person_teacher, R.drawable.person_woman_dancing};

    long startTimeCircle;
    private FirebaseAnalytics firebaseAnalytics;

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
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setCurrentScreen(Explore.this, "Viewing explore tab", null);

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
        final RecyclerView.Adapter adapter = new CircleDisplayAdapter(Explore.this, exploreCircleList, user);
        exploreRecyclerView.setAdapter(adapter);

        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                allCircles.add(circle);

                List<String> circleIteratorinterestTagsList = new ArrayList<>(circle.getInterestTags().keySet());

                //recieve request on opening
                if (intentUri != null) {
                    List<String> params = intentUri.getPathSegments();
                    String circleID = params.get(params.size() - 1);

                    if (circle.getId().equals(circleID)) {
                        link_flag = true;
                    }
                }

                //checking if circle already exists
                boolean contains = false;
                for (Circle c : exploreCircleList) {
                    if (c.getId().equals(circle.getId()))
                        contains = true;
                }

                //checking if user is a member of the circle
                boolean existingMember = false;
                if (circle.getMembersList() != null) {
                    if (circle.getMembersList().keySet().contains(currentUser.getUid()))
                        existingMember = true;
                }

                if(contains == false) {
                    if (circle.getCreatorName().equals("Admin")) { //add default admin entry tag
                        exploreCircleList.add(0,circle);
                        adapter.notifyDataSetChanged();
                    } else if(circle.getCircleDistrict().equals(user.getDistrict())){

                        //add both cooking and running tags
                        if(circle.getInterestTags().keySet().contains("sample")){
                            exploreCircleList.add(circle);
                            adapter.notifyDataSetChanged();
                        }

                        //add all relevant tags in that area
                        if (!circle.getCreatorID().equals(currentUser.getUid()) && existingMember == false) {
                            //filter for only circles associated with matching user location and interests
                            for (String intIterator : userTempinterestTagsList) {
                                if (circleIteratorinterestTagsList.contains(intIterator)) {
                                    //check if circle already exists
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

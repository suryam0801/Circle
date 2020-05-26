package circleapp.circlepackage.circle.Explore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.Login.PhoneLogin;
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
    private FloatingActionButton btnAddCircle;
    private FirebaseDatabase database;
    private FirebaseAuth currentUser;
    private DatabaseReference circlesDB, usersDB;
    private ImageView profPic;
    private Dialog circleJoinDialog;
    User user;
    List<String> userTemplocationTagsList;
    List<String> userTempinterestTagsList;

    long startTimeCircle, startTimeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        btnAddCircle = findViewById(R.id.add_circle_button);
        profPic = findViewById(R.id.explore_profilePicture);
        circleJoinDialog = new Dialog(Explore.this);

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance();

        circlesDB = database.getReference("Circles");
        circlesDB.keepSynced(true); //synchronizes and stores local copy of data
        usersDB = database.getReference("Users").child(currentUser.getCurrentUser().getUid());
        usersDB.keepSynced(true);

        startTimeUser = System.currentTimeMillis();
        usersDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "TIME TAKEN USERS: " + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - startTimeUser));
                user = dataSnapshot.getValue(User.class);
                SessionStorage.saveUser(Explore.this, user);
                Glide.with(Explore.this)
                        .load(user.getProfileImageLink())
                        .placeholder(ContextCompat.getDrawable(Explore.this, R.drawable.profile_image))
                        .into(profPic);

                //retrieve location & interest tags from users
                userTemplocationTagsList = new ArrayList<>(user.getLocationTags().keySet());
                userTempinterestTagsList = new ArrayList<>(user.getInterestTags().keySet());

                startTimeCircle = System.currentTimeMillis();
                setCircleTabs();
                setWorkbenchTabs();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                currentUser.signOut();
                startActivity(new Intent(Explore.this, PhoneLogin.class));
                finish();
            }
        });


        //onClick listener for create project button
        btnAddCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Explore.this, CreateCircle.class));
            }
        });

        profPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Explore.this, EditProfile.class));
            }
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

                //checking if user is a member of the circle
                boolean existingMember = false;
                if (circle.getMembersList() != null) {
                    if (circle.getMembersList().keySet().contains(currentUser.getUid()))
                        existingMember = true;
                }

                //setting the adapter initially
                //filter for only circles associated with creator id
                if (circle.getCreatorID().equals(currentUser.getUid()) || existingMember == true) {
                    workbenchCircleList.add(circle);
                    //notify the adapter each time a new item needs to be added to the recycler view
                    wbadapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);
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

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
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
                        Toast.makeText(getApplicationContext(), "LONG PRESSED", Toast.LENGTH_SHORT).show();
                    }
                })
        );

        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);

                //retrieve location & interest tags from circle object since tags are stored as hashmaps
                List<String> circleIteratorlocationTagsList = new ArrayList<>(circle.getLocationTags().keySet());
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
                    for (String locIterator : userTemplocationTagsList) {
                        if (circleIteratorlocationTagsList.contains(locIterator)) {
                            for (String intIterator : userTempinterestTagsList) {
                                if (circleIteratorinterestTagsList.contains(intIterator)) {
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
                int position = 0;
                List<Circle> tempCircleList = new ArrayList<>(exploreCircleList);
                for (Circle c : tempCircleList) {
                    if (c.getId().equals(circle.getId())) {
                        exploreCircleList.remove(position);
                        exploreCircleList.add(position, circle);
                        adapter.notifyDataSetChanged();
                    }
                    ++position;
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
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
            if (circle.getApplicantsList().keySet().contains(currentUser.getCurrentUser().getUid())){
                acceptButton.setClickable(false);
                acceptButton.setBackground(getResources().getDrawable(R.drawable.unpressable_button));
                acceptButton.setText("Pending Request");
                acceptButton.setTextColor(Color.parseColor("#D1D1D1"));
            }
        }

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creating a subscriber object to store. doesnt store private information such as tags and contact information.
                Subscriber subscriber = new Subscriber(user.getUserId(), user.getFirstName() + " " + user.getLastName(),
                        user.getProfileImageLink(), user.getToken_id(), System.currentTimeMillis());

                if (("review").equalsIgnoreCase(circle.getAcceptanceType())) {
                    database.getReference().child("CirclePersonel").child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
                    //adding userID to applicants list
                    circlesDB.child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
                } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
                    database.getReference().child("CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
                    //adding userID to members list in circlesReference
                    circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);
                    int nowActive = user.getActiveCircles() + 1;
                    usersDB.child("activeCircles").setValue((nowActive));
                }

                circleJoinDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circleJoinDialog.dismiss();
            }
        });

        circleJoinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        circleJoinDialog.show();
    }

    private void suggestInterests() {
        circlesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

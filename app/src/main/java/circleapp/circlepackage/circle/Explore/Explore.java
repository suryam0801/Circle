package circleapp.circlepackage.circle.Explore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.ObjectModels.Circle;
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
    private DatabaseReference circles;
    private ImageView profPic;
    User user;

    long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        btnAddCircle = findViewById(R.id.add_circle_button);
        profPic = findViewById(R.id.explore_profilePicture);

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance();

        circles = database.getReference("Circles");
        circles.keepSynced(true); //synchronizes and stores local copy of data

        final SharedPreferences sharedPreferences = getSharedPreferences("LocalUserPermaStore", MODE_PRIVATE);
        String userJSON = sharedPreferences.getString("myUserDetails", "default");
        Log.d(TAG, "!!!!! " + FirebaseAuth.getInstance().getCurrentUser().getUid());
        user = new Gson().fromJson(userJSON, User.class);
        if (user == null || currentUser.getCurrentUser().getUid() == null) {
            currentUser.signOut();
            startActivity(new Intent(Explore.this, PhoneLogin.class));
            finish();
        }

        Glide.with(Explore.this)
                .load(user.getProfileImageLink())
                .placeholder(ContextCompat.getDrawable(Explore.this, R.drawable.profile_image))
                .into(profPic);

        SessionStorage.saveUser(Explore.this, user);

        startTime = System.currentTimeMillis();
        setCircleTabs();
        setWorkbenchTabs();

        //onClick listener for create project button
        btnAddCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Explore.this, CreateCircle.class));
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
                        Circle selectedWBCircle = workbenchCircleList.get(position);
                        SessionStorage.saveCircle(Explore.this, workbenchCircleList.get(position));
                        Log.d(TAG, "onItemClick: " + selectedWBCircle);
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
        circles.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //filter through each Circle in the Circles database
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //casts the datasnapshot to Circle Object
                    Circle circle = postSnapshot.getValue(Circle.class);
                    Log.d(TAG, circle.toString());
                    //*FROM HERE*
                    //without cloning the arraylist, concurrency execption will be thrown since system is editing and reading myCircleList at the same time
                    int position = 0;
                    List<Circle> wbtempList = new ArrayList<>(workbenchCircleList);
                    //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                    for (Circle c : wbtempList) {
                        if (c.getId().equals(circle.getId())) {
                            workbenchCircleList.remove(position);
                            wbadapter.notifyDataSetChanged();
                        }
                        position++;
                    }
                    //*TO HERE* only for changing values for updated or modified children in database

                    //setting the adapter initially
                    //filter for only circles associated with creator id
                    if (circle.getCreatorID().equals(currentUser.getUid())) {
                        workbenchCircleList.add(circle);
                        //notify the adapter each time a new item needs to be added to the recycler view
                        wbadapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setCircleTabs() {
        //initialize recylcerview
        RecyclerView recyclerView = findViewById(R.id.circlesRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        final RecyclerView.Adapter adapter = new CircleDisplayAdapter(Explore.this, exploreCircleList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                //RecyclerItemClickListener is a gestureDectector class which recognises the type of touch
                new RecyclerItemClickListener(Explore.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        SessionStorage.saveCircle(Explore.this, exploreCircleList.get(position));
                        startActivity(new Intent(Explore.this, CircleWall.class));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        Toast.makeText(getApplicationContext(), "LONG PRESSED", Toast.LENGTH_SHORT).show();
                    }
                })
        );
        //single value listener for Circles Collection
        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circles.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //filter through each Circle in the Circles database
                Log.d(TAG, "TIME IN MILLISECONDS: " + TimeUnit.MILLISECONDS.toSeconds(startTime - System.currentTimeMillis()));
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //casts the datasnapshot to Circle Object
                    Circle circle = postSnapshot.getValue(Circle.class);

                    //*FROM HERE*
                    //without cloning the arraylist, concurrency execption will be thrown since system is editing and reading circlesList at the same time
                    int position = 0;
                    List<Circle> tempList = new ArrayList<>(exploreCircleList);
                    //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                    for (Circle c : tempList) {
                        if (c.getId().equals(circle.getId())) {
                            exploreCircleList.remove(position);
                            adapter.notifyDataSetChanged();
                            --position;
                        }
                        ++position;
                    }
                    //*TO HERE* only for changing values for updated or modified children in database

                    //setting the adapter initially
                    //filter for only circles associated with matching user location and interests
                    for (String locIterator : user.getLocationTags()) {
                        if (circle.getLocationTags().contains("#" + locIterator)) {
                            for (String intIterator : user.getInterestTags()) {
                                if (circle.getInterestTags().contains("#" + intIterator)) {
                                    exploreCircleList.add(circle);
                                    //notify the adapter each time a new item needs to be added to the recycler view
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void suggestInterests() {
        circles.addValueEventListener(new ValueEventListener() {
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

package circleapp.circlepackage.circle.MainDisplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.RecyclerItemClickListener;
import circleapp.circlepackage.circle.SessionStorage;

public class Explore extends AppCompatActivity {

    private String TAG = Explore.class.getSimpleName();
    private List<Circle> circleList = new ArrayList<>();
    private FloatingActionButton btnAddCircle;
    private FirebaseDatabase database;
    private DatabaseReference circles;
    private ImageView profPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);


        btnAddCircle = findViewById(R.id.add_circle_button);
        profPic = findViewById(R.id.explore_profilePicture);

        database = FirebaseDatabase.getInstance();
        //persistence automatically handles offline behavior
        database.setPersistenceEnabled(true);

        circles = database.getReference("Circles");
        //synchronizes and stores local copy of data
        circles.keepSynced(true);

        //initialize recylcerview
        RecyclerView recyclerView = findViewById(R.id.circlesRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        final RecyclerView.Adapter adapter = new CircleDisplayAdapter(Explore.this, circleList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                //RecyclerItemClickListener is a gestureDectector class which recognises the type of touch
                new RecyclerItemClickListener(Explore.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        SessionStorage.saveCircle(Explore.this, circleList.get(position));
                        startActivity(new Intent(Explore.this, CircleWall.class));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        Toast.makeText(getApplicationContext(), "LONG PRESSED", Toast.LENGTH_SHORT).show();
                    }
                })
        );

        //Creating a testUser
        final List<String> locationTags = new ArrayList<>();
        locationTags.add("psg");
        final List<String> interestTags = new ArrayList<>();
        interestTags.add("basketball");
        final User user = new User("Surya", "Manivannan", "+17530043008", "default",
                locationTags, interestTags, "UUID", 0, 0, 0, "TOKEN_ID");

        //load user profile picture
        Glide.with(Explore.this)
                .load("")
                .placeholder(ContextCompat.getDrawable(Explore.this, R.drawable.profile_image))
                .into(profPic);


        //single value listener for Circles Collection
        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circles.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //filter through each Circle in the Circles database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //casts the datasnapshot to Circle Object
                    Circle circle = postSnapshot.getValue(Circle.class);


                    //*FROM HERE*
                    //without cloning the arraylist, concurrency execption will be thrown since system is editing and reading circlesList at the same time
                    int position = 0;
                    List<Circle> tempList = new ArrayList<>(circleList);
                    //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                    for (Circle c : tempList) {
                        if (c.getId().equals(circle.getId())) {
                            circleList.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                        position++;
                    }
                    //*TO HERE* only for changing values for updated or modified children in database

                    //setting the adapter initially
                    //filter for only circles associated with matching user location and interests
                    for (String locIterator : user.getLocationTags()) {
                        if (circle.getLocationTags().contains(locIterator)) {
                            for (String intIterator : user.getInterestTags()) {
                                if (circle.getInterestTags().contains(intIterator)) {
                                    circleList.add(circle);
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

        //onClick listener for create project button
        btnAddCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Explore.this, CreateCircle.class));
            }
        });
    }
}

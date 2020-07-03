package circleapp.circlepackage.circle.PersonelDisplay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.Person;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;

public class PersonelDisplay extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference circlesPersonelDB;
    private List<Subscriber> applicantsList;
    private ImageButton back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personel_display);
        Circle circle = SessionStorage.getCircle(this);

        database = FirebaseDatabase.getInstance();
        circlesPersonelDB = database.getReference("CirclePersonel").child(circle.getId());//circle.getId()

        back = findViewById(R.id.bck_applicants_display);
        RecyclerView recyclerView = findViewById(R.id.allApplicants_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        applicantsList = new ArrayList<>(); //initialize membersList

        back.setOnClickListener(view -> {
            startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
            finish();
        });


        final RecyclerView.Adapter adapter = new ApplicantListAdapter(this, applicantsList, circle);
        if (SessionStorage.getUser((PersonelDisplay.this)).getUserId().equalsIgnoreCase(circle.getCreatorID()))
            recyclerView.setAdapter(adapter);

        circlesPersonelDB.child("applicants").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                recyclerView.setAdapter(adapter);
                Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                applicantsList.add(subscriber);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                recyclerView.setAdapter(adapter);
                Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                int position = 0;
                List<Subscriber> tempList = new ArrayList<>(applicantsList);
                //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                for (Subscriber sub : tempList) {
                    if (sub.getId().equals(subscriber.getId())) {
                        applicantsList.remove(position);
                        adapter.notifyItemRemoved(position);
                        break;
                    }
                    position = position + 1;
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
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
        finish();
    }
}
